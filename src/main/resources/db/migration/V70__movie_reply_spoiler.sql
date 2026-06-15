-- 评论回复增加「剧透」标记：用户回复时可声明含剧透，C 端默认对剧透回复做模糊遮罩，
-- 点击「查看」后才展示明文。0=普通（默认），1=含剧透。
--
-- movie_reply 同时存在于 public 与 crawl 两个 schema（见 V3 镜像），两边都补列保持一致。
-- 全部使用 IF NOT EXISTS，旧库重复执行为 no-op。

ALTER TABLE IF EXISTS public.movie_reply
  ADD COLUMN IF NOT EXISTS spoiler SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS crawl.movie_reply
  ADD COLUMN IF NOT EXISTS spoiler SMALLINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN public.movie_reply.spoiler IS '是否含剧透：0=否 1=是';
COMMENT ON COLUMN crawl.movie_reply.spoiler IS '是否含剧透：0=否 1=是';
