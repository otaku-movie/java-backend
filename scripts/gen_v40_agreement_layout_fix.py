#!/usr/bin/env python3
"""Generate V40 migration to update already-published 1.1.0 agreement layout.

V39 may have already been applied in local/dev DBs. Editing V39 after that does
not change existing rows, so V40 updates the 1.1.0 records in place without
bumping the agreement version again.
"""
import os
import re


ROOT = os.path.normpath(os.path.join(os.path.dirname(__file__), ".."))
MIGRATION_DIR = os.path.join(ROOT, "src", "main", "resources", "db", "migration")
SOURCE = os.path.join(MIGRATION_DIR, "V39__agreement_firebase_v1_1_0.sql")
OUT = os.path.join(MIGRATION_DIR, "V40__agreement_v1_1_0_layout_fix.sql")


BLOCK_RE = re.compile(
    r"-- ---- public: (?P<code>[A-Z_]+) / (?P<lang>[a-z]+) / 1\.1\.0 .*?"
    r"\$body\$\n(?P<content>.*?)\$body\$::text AS content",
    re.S,
)


def update_sql(schema: str, code: str, lang: str, content: str) -> str:
    return f"""\
-- ---- {schema}: {code} / {lang} -------------------------------------------
UPDATE {schema}.agreement
   SET content = $body$
{content}$body$,
       update_time = CURRENT_TIMESTAMP
 WHERE code = '{code}'
   AND language = '{lang}'
   AND version = '1.1.0'
   AND deleted = 0;
"""


def main() -> None:
    with open(SOURCE, "r", encoding="utf-8") as f:
        source = f.read()

    blocks = list(BLOCK_RE.finditer(source))
    if len(blocks) != 9:
        raise RuntimeError(f"Expected 9 public agreement blocks, got {len(blocks)}")

    parts = [
        """\
-- ============================================================
-- V40: fix agreement 1.1.0 document layout
--
-- 背景：
--   * V39 在部分环境已执行，直接修改 V39 不会更新 DB 中已有内容；
--   * 本迁移只修正文档排版，不改变 version，避免用户因纯排版修复再次同意；
--   * 修复点：
--     - 将“1.1.0 更新内容”移动到标题 + 版本信息下方；
--     - 版本 / 生效日期 / 最后更新各占一行。
-- ============================================================

SET timezone = 'Asia/Tokyo';

"""
    ]

    for schema in ("public", "crawl"):
        for match in blocks:
            parts.append(
                update_sql(
                    schema,
                    match.group("code"),
                    match.group("lang"),
                    match.group("content"),
                )
            )

    with open(OUT, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(parts))
    print(f"Written {OUT} ({os.path.getsize(OUT)} bytes)")


if __name__ == "__main__":
    main()
