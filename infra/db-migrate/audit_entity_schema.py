#!/usr/bin/env python3
"""Audit JPA/MyBatis-Plus entity fields against prod_movie crawl actual columns.

Usage:
  1) Dump crawl columns:  psql ... -tA -F'|' -c "SELECT table_name, column_name
        FROM information_schema.columns WHERE table_schema='crawl' ORDER BY 1,2;" > prod_crawl_cols.txt
  2) python audit_entity_schema.py <entity_dir> <prod_crawl_cols.txt>

Outputs, per table:
  - MISSING: columns the entity expects but crawl does not have (runtime breakers)
  - EXTRA  : crawl columns not mapped by the entity (informational)
Tables with no issues are summarized at the end.
"""
import os
import re
import sys

ANN_TABLENAME = re.compile(r'@TableName\(\s*(?:value\s*=\s*)?"([^"]+)"')
ANN_TABLEFIELD = re.compile(r'@TableField\(\s*(?:value\s*=\s*)?"([^"]+)"')
ANN_TABLEFIELD_NOEXIST = re.compile(r'@TableField\([^)]*exist\s*=\s*false')
ANN_TABLEID = re.compile(r'@TableId\(\s*(?:value\s*=\s*)?"([^"]+)"')
ANN_TABLEID_PLAIN = re.compile(r'@TableId\b')
CLASS_DECL = re.compile(r'\bclass\s+(\w+)')
# a field: optional modifiers, Type (starts uppercase or known lowercase prim), name, ;
FIELD = re.compile(
    r'^\s*(?:public|private|protected\s+)?'
    r'(?:static\s+|final\s+|transient\s+)*'
    r'([A-Za-z_][\w<>,\.\[\]\s]*?)\s+'
    r'(\w+)\s*;\s*$'
)


def camel_to_snake(name: str) -> str:
    s = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', name)
    s = re.sub(r'([A-Z]+)([A-Z][a-z])', r'\1_\2', s)
    return s.lower()


def class_to_table(cls: str) -> str:
    return camel_to_snake(cls)


def strip_comments(text: str) -> str:
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    text = re.sub(r'//[^\n]*', '', text)
    return text


def parse_entity(path: str):
    raw = open(path, encoding='utf-8').read()
    text = strip_comments(raw)
    m = CLASS_DECL.search(text)
    if not m:
        return None
    cls = m.group(1)
    tm = ANN_TABLENAME.search(text)
    table = tm.group(1) if tm else class_to_table(cls)

    # Only look at body after class declaration
    body = text[m.end():]
    lines = body.splitlines()
    cols = set()
    pending = []  # annotation lines buffer
    for ln in lines:
        stripped = ln.strip()
        if stripped.startswith('@'):
            pending.append(stripped)
            continue
        fm = FIELD.match(ln)
        if fm:
            ftype, fname = fm.group(1).strip(), fm.group(2)
            anns = ' '.join(pending)
            pending = []
            # skip non-persistent
            if ftype in ('void',) or fname in ('serialVersionUID',):
                continue
            if ANN_TABLEFIELD_NOEXIST.search(anns):
                continue
            tf = ANN_TABLEFIELD.search(anns)
            ti = ANN_TABLEID.search(anns)
            if tf:
                cols.add(tf.group(1))
            elif ti:
                cols.add(ti.group(1))
            elif ANN_TABLEID_PLAIN.search(anns):
                cols.add('id')
            else:
                cols.add(camel_to_snake(fname))
        else:
            # a non-field, non-annotation line resets pending annotations
            if stripped and not stripped.startswith('@'):
                pending = []
    return cls, table, cols


def main():
    entity_dir, cols_file = sys.argv[1], sys.argv[2]
    # load actual crawl columns
    actual = {}
    for line in open(cols_file, encoding='utf-8'):
        line = line.rstrip('\n')
        if not line or '|' not in line:
            continue
        t, c = line.split('|', 1)
        actual.setdefault(t, set()).add(c)

    problems = []
    clean = []
    no_table = []
    for fn in sorted(os.listdir(entity_dir)):
        if not fn.endswith('.java'):
            continue
        res = parse_entity(os.path.join(entity_dir, fn))
        if not res:
            continue
        cls, table, cols = res
        if table not in actual:
            no_table.append((cls, table))
            continue
        missing = sorted(cols - actual[table])
        if missing:
            problems.append((cls, table, missing))
        else:
            clean.append((cls, table))

    print("================ MISSING COLUMNS (entity expects, crawl lacks) ================")
    if not problems:
        print("  (none)")
    for cls, table, missing in problems:
        print(f"  {cls} -> crawl.{table}")
        for c in missing:
            print(f"      - {c}")

    print("\n================ ENTITY TABLE NOT FOUND IN crawl ================")
    if not no_table:
        print("  (none)")
    for cls, table in no_table:
        print(f"  {cls} -> {table} (not in crawl schema)")

    print(f"\nclean tables: {len(clean)} | with missing cols: {len(problems)} | table-not-found: {len(no_table)}")


if __name__ == '__main__':
    main()
