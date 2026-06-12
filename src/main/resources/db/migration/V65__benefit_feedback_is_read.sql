-- 用户特典反馈增加「已读/未读」标记，用于后台运营按特典/影院汇总未读条数并提示新反馈。
-- 0=未读（默认），1=已读。运营在「影院库存 - 用户反馈」/「影院维度 - 用户反馈」查看后置为已读。
--
-- benefit_user_feedback 同时存在于 public 与 crawl 两个 schema（见 V5/V6），两边都补列，保持镜像一致。
-- 全部使用 IF NOT EXISTS，旧库重复执行为 no-op。

ALTER TABLE IF EXISTS public.benefit_user_feedback
  ADD COLUMN IF NOT EXISTS is_read SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS crawl.benefit_user_feedback
  ADD COLUMN IF NOT EXISTS is_read SMALLINT NOT NULL DEFAULT 0;

-- 按「特典/影院 + 未读」过滤计数的部分索引
CREATE INDEX IF NOT EXISTS idx_benefit_feedback_unread
  ON public.benefit_user_feedback (benefit_id, cinema_id) WHERE deleted = 0 AND is_read = 0;
CREATE INDEX IF NOT EXISTS idx_benefit_feedback_unread
  ON crawl.benefit_user_feedback (benefit_id, cinema_id) WHERE deleted = 0 AND is_read = 0;

COMMENT ON COLUMN public.benefit_user_feedback.is_read IS '后台是否已读：0=未读 1=已读';
COMMENT ON COLUMN crawl.benefit_user_feedback.is_read IS '后台是否已读：0=未读 1=已读';
