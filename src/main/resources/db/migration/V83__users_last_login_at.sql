-- users 表补齐最近登录时间。
--
-- 背景：User 实体与登录流程会更新 last_login_at，但 V1 基线 users 表无此列
-- （last_login_at 仅存在于 user_oauth_binding）。登录 touchLoginActivity 写库时报
-- 「列 last_login_at は存在しません」。
--
-- 影响范围：public 与 crawl 两个 schema 下的 users 表。

SET timezone = 'Asia/Tokyo';

ALTER TABLE public.users
  ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

COMMENT ON COLUMN public.users.last_login_at IS '最近一次登录时间；每次登录更新';

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'crawl') THEN
    ALTER TABLE crawl.users
      ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
    COMMENT ON COLUMN crawl.users.last_login_at IS '最近一次登录时间；每次登录更新';
  END IF;
END $$;
