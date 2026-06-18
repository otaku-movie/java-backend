-- 最近一次登录来源（h5 / ios / android），与 register_source 取值一致。
ALTER TABLE public.users
  ADD COLUMN IF NOT EXISTS last_login_source VARCHAR(16);

COMMENT ON COLUMN public.users.last_login_source IS '最近一次登录来源：h5/ios/android/unknown；每次登录更新';

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'crawl') THEN
    ALTER TABLE crawl.users
      ADD COLUMN IF NOT EXISTS last_login_source VARCHAR(16);
  END IF;
END $$;
