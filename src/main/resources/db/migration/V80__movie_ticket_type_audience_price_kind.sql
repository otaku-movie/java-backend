-- V79: movie_ticket_type 票种语义字段 —— 人群大类 / 票价性质 / 服务日代码 / 会员限定
--
-- 供 price-preview、选票种展示使用；由 import:prices 从爬虫 category / service_day.code 写入。

ALTER TABLE public.movie_ticket_type
  ADD COLUMN IF NOT EXISTS audience_category VARCHAR(32),
  ADD COLUMN IF NOT EXISTS price_kind VARCHAR(16),
  ADD COLUMN IF NOT EXISTS service_day_code VARCHAR(32),
  ADD COLUMN IF NOT EXISTS member_required BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN public.movie_ticket_type.audience_category IS '人群票档大类：adult/member/college/…（爬虫 PriceCategory）';
COMMENT ON COLUMN public.movie_ticket_type.price_kind IS '票价性质：base=常设 promo=服务日/折扣';
COMMENT ON COLUMN public.movie_ticket_type.service_day_code IS '服务日代码：wednesday/monday_member/…（爬虫 ServiceDayCode）';
COMMENT ON COLUMN public.movie_ticket_type.member_required IS '是否会员限定（展示/预览用，不代替验资）';

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'crawl' AND table_name = 'movie_ticket_type'
  ) THEN
    ALTER TABLE crawl.movie_ticket_type
      ADD COLUMN IF NOT EXISTS audience_category VARCHAR(32),
      ADD COLUMN IF NOT EXISTS price_kind VARCHAR(16),
      ADD COLUMN IF NOT EXISTS service_day_code VARCHAR(32),
      ADD COLUMN IF NOT EXISTS member_required BOOLEAN NOT NULL DEFAULT FALSE;
  END IF;
END $$;
