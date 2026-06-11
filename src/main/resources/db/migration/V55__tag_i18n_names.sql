-- ============================================================
-- V55: 标签表增加「中文名 / 英文名」译名列
--
-- movie_show_time_tag（应援上映/舞台挨拶…）与 movie_tag（TMDb 类型名）原本只存日文
-- name，App 切换语言时无法本地化。这里给两张表各加 name_zh / name_en，由后端按当前
-- Accept-Language 选用：接口对外仍只返回单个 name 字段（已填好对应语言），译名为空时
-- 回退日文原名。
--
-- 本脚本只做 DDL（加列）。译名数据的预填见 V56（DML 与 DDL 分离，避免「改已应用脚本」
-- 导致 Flyway 跳过/校验失败）。这两类标签会随爬虫自动增长，故译名放在 DB（可后台维护）。
-- 业务 datasource 使用 crawl schema，这里直接对 crawl.* 加列（与 V51 同口径）。
-- ============================================================

SET timezone = 'Asia/Tokyo';

ALTER TABLE crawl.movie_show_time_tag
    ADD COLUMN IF NOT EXISTS name_zh VARCHAR(255),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(255);

COMMENT ON COLUMN crawl.movie_show_time_tag.name_zh IS '上映场次标签中文名；为空时 C 端回退日文 name';
COMMENT ON COLUMN crawl.movie_show_time_tag.name_en IS '上映场次标签英文名；为空时 C 端回退日文 name';

ALTER TABLE crawl.movie_tag
    ADD COLUMN IF NOT EXISTS name_zh VARCHAR(255),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(255);

COMMENT ON COLUMN crawl.movie_tag.name_zh IS '电影标签（类型）中文名；为空时 C 端回退日文 name';
COMMENT ON COLUMN crawl.movie_tag.name_en IS '电影标签（类型）英文名；为空时 C 端回退日文 name';
