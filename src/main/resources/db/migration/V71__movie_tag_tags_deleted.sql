-- 电影标签关联表补齐逻辑删除字段。
--
-- 背景：
--   MovieTagTags 实体和基线建表都包含 deleted 字段，但部分已有数据库中的
--   movie_tag_tags 可能来自较早结构，缺少该列，导致电影合并页统计标签引用时
--   查询 x.deleted 报错。
--
-- 影响范围：
--   movie_tag_tags 同时存在于 public 与 crawl 两个 schema（见 V3 镜像），
--   两边都补列保持结构一致。使用 IF NOT EXISTS，已存在该列的环境为 no-op。

ALTER TABLE IF EXISTS public.movie_tag_tags
  ADD COLUMN IF NOT EXISTS deleted INTEGER DEFAULT 0;

ALTER TABLE IF EXISTS crawl.movie_tag_tags
  ADD COLUMN IF NOT EXISTS deleted INTEGER DEFAULT 0;

COMMENT ON COLUMN public.movie_tag_tags.deleted IS '逻辑删除标记：0=未删除 1=已删除';
COMMENT ON COLUMN crawl.movie_tag_tags.deleted IS '逻辑删除标记：0=未删除 1=已删除';
