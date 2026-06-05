-- V33: movie_ticket_type 增量 upsert 支持 —— 新增 content_key + 部分唯一索引。
--
-- 背景：import:prices 旧逻辑对每个影院「DELETE FROM movie_ticket_type WHERE cinema_id=?
-- 再 INSERT」，每次跑都重排 id，会让 select_seat.movie_ticket_type_id / 订单引用漂移。
-- 改为按 content_key（影院+票种名+排期维度的确定性哈希）upsert，并把本轮未出现的旧票种
-- 软删（deleted=1，保留 id），从而保持 movie_ticket_type.id 稳定。
--
-- 幂等：列与索引都用 IF NOT EXISTS；crawl schema 仅在存在时处理。

-- public 业务表
ALTER TABLE public.movie_ticket_type ADD COLUMN IF NOT EXISTS content_key VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS uq_movie_ticket_type_cinema_content
  ON public.movie_ticket_type (cinema_id, content_key)
  WHERE deleted = 0 AND content_key IS NOT NULL;

-- crawl 镜像表（V3 用 LIKE public ... INCLUDING ALL 建表，但建表后 public 加列不会自动同步，
-- 这里单独补；import:prices 实际写的就是 crawl schema）。
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'crawl' AND table_name = 'movie_ticket_type'
  ) THEN
    ALTER TABLE crawl.movie_ticket_type ADD COLUMN IF NOT EXISTS content_key VARCHAR(255);
    CREATE UNIQUE INDEX IF NOT EXISTS uq_crawl_movie_ticket_type_cinema_content
      ON crawl.movie_ticket_type (cinema_id, content_key)
      WHERE deleted = 0 AND content_key IS NOT NULL;
  END IF;
END $$;
