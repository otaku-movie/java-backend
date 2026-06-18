-- V80: 用户注册来源（h5 / ios / android …），便于区分 App 与 H5 等渠道。

ALTER TABLE public.users
  ADD COLUMN IF NOT EXISTS register_source VARCHAR(16);

COMMENT ON COLUMN public.users.register_source IS '注册来源：h5/ios/android/unknown；OAuth 首次建号时同样写入';

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'crawl' AND table_name = 'users'
  ) THEN
    ALTER TABLE crawl.users
      ADD COLUMN IF NOT EXISTS register_source VARCHAR(16);
  END IF;
END $$;
