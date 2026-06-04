-- ============================================================
-- V13: crawl.movie 加 kind 列，区分电影 vs 非电影（演唱会 / 体育 /
--      舞台 / Live FILM 等 ODS 内容）。
--
-- 背景：
--   - 影院售票里除了电影本身，也常上架 ODS（Other Digital Source）：
--     LIVE 演唱会、World Tour、FIFA 转播、宝塚 / 歌舞伎 中継、Live Film
--     等。它们走的是同一套 movie_show_time 流水，所以历史上全部塞进
--     crawl.movie 一张表。
--   - 业务侧只想在"电影列表"里看到真正的剧场公映片，需要一个稳定的
--     过滤位。importer 改完之后会按"标题含 LIVE/コンサート/WORLD TOUR
--     /舞台/歌舞伎/宝塚 等关键词 + tmdb_id IS NULL（TMDb 不收 ODS）"
--     的双条件把 crawl.movie.kind 标成 'ods'。
--
-- 字段定义：
--   - kind VARCHAR(16) NOT NULL DEFAULT 'movie'
--       'movie' — 普通电影（默认）
--       'ods'   — 演唱会 / 体育 / 舞台 / Live Film 等非电影类放映
--
-- 兼容性：
--   - 列默认值 'movie' + NOT NULL，已存在行会自动回填为 'movie'，
--     后续 importer 重跑时再根据规则把 ODS 行覆盖成 'ods'。
--   - public.movie 不动；这是 crawler 专属语义，业务表如果以后要同样
--     字段，再单独追加迁移。
--   - 加部分索引方便按 kind 过滤（'movie' 是热路径，建一个 deleted=0
--     的过滤索引性价比最高）。
-- ============================================================

ALTER TABLE crawl.movie
  ADD COLUMN IF NOT EXISTS kind VARCHAR(16) NOT NULL DEFAULT 'movie';

CREATE INDEX IF NOT EXISTS idx_crawl_movie_kind
  ON crawl.movie (kind) WHERE deleted = 0;
