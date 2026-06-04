-- ============================================================
-- V19: 给 public.movie_show_time 补 reservation_params_json 列
--
-- 背景：
--   `crawl.movie_show_time` 在 V6 已添加 reservation_params_json，
--   存放各影院官网的购票深链 + 透传参数；app 端「购票」按钮
--   现在改为直接外跳到影院官网，前端要读到该列。
--
--   dev 连接用 currentSchema=crawl，生产连接用 currentSchema=public。
--   为了让同一份 mapper SQL 在两套环境都能跑，public schema 也得有该列。
--   该列在 public 环境下不会被填充（业务侧没有写入逻辑），但保持结构对齐，
--   避免 SQL 直接 NULL 比较时报「列不存在」。
-- ============================================================

ALTER TABLE public.movie_show_time
  ADD COLUMN IF NOT EXISTS reservation_params_json JSONB;

COMMENT ON COLUMN public.movie_show_time.reservation_params_json
  IS '影院官方购票链接与外跳参数（mirror 自 crawl.movie_show_time，结构相同）';
