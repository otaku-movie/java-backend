-- 电影角色关联表补齐逻辑删除字段。
--
-- 背景：
--   MovieCharacter 实体声明了 @TableLogic deleted，但 V1 基线中的
--   movie_character 建表语句缺少该列。电影合并页统计角色引用时会查询
--   movie_character.deleted，导致已有库报「列 x.deleted 不存在」。
--
-- 影响范围：
--   movie_character 同时存在于 public 与 crawl 两个 schema（见 V3 镜像），
--   两边都补列保持结构一致。使用 IF NOT EXISTS，已存在该列的环境为 no-op。

ALTER TABLE IF EXISTS public.movie_character
  ADD COLUMN IF NOT EXISTS deleted INTEGER DEFAULT 0;

ALTER TABLE IF EXISTS crawl.movie_character
  ADD COLUMN IF NOT EXISTS deleted INTEGER DEFAULT 0;

COMMENT ON COLUMN public.movie_character.deleted IS '逻辑删除标记：0=未删除 1=已删除';
COMMENT ON COLUMN crawl.movie_character.deleted IS '逻辑删除标记：0=未删除 1=已删除';
