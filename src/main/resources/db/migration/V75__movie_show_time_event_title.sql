-- 场次「特殊场次名」字段。
--
-- 背景：
--   影院常把活动场标题写成 `『正片名』"宣传语"卒業記念舞台挨拶【LIVE ZOUND…】＜特別興行＞`，
--   正片名只在 `『』` 内（爬虫已据此规范化 movie.name）。引号外的活动名 / 宣传语
--   过去被整体丢弃；现把它作为「特殊场次名」单独记录到 movie_show_time.event_title，
--   供前端在该场次旁展示（普通场次为 NULL）。
--
-- 影响范围：
--   movie_show_time 同时存在于 public 与 crawl 两个 schema（见 V3 镜像），两边都补列
--   保持结构一致。使用 IF NOT EXISTS，已存在该列的环境为 no-op。

ALTER TABLE IF EXISTS public.movie_show_time
  ADD COLUMN IF NOT EXISTS event_title VARCHAR(255);

ALTER TABLE IF EXISTS crawl.movie_show_time
  ADD COLUMN IF NOT EXISTS event_title VARCHAR(255);

COMMENT ON COLUMN public.movie_show_time.event_title IS '特殊场次名/活动名（来自标题装饰，普通场次为空）';
COMMENT ON COLUMN crawl.movie_show_time.event_title IS '特殊场次名/活动名（来自标题装饰，普通场次为空）';
