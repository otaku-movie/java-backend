-- ============================================================
-- V44: 给 crawl.movie_rate 补 (user_id, movie_id) 唯一索引。
--
-- 背景：
--   V38 用的是不带 schema 前缀的 `CREATE UNIQUE INDEX ... ON movie_rate`，
--   依赖执行时的 currentSchema。历史 dev 环境 V38 在 currentSchema=crawl
--   下执行，索引落到了 crawl.movie_rate；而全新库统一以 public 跑 Flyway
--   时，V38 会落到 public.movie_rate，导致 crawl.movie_rate 缺这个唯一键。
--
--   按“禁止修改已部署 V 脚本”的约定，这里不动 V38，新增 V44 显式给
--   crawl.movie_rate 补索引，保证任意初始化路径下 crawl 都有该约束。
--
-- 幂等性：
--   IF NOT EXISTS —— 对已存在该索引的 dev 环境为 no-op。
-- ============================================================

CREATE UNIQUE INDEX IF NOT EXISTS uk_movie_rate_user_movie
    ON crawl.movie_rate (user_id, movie_id);
