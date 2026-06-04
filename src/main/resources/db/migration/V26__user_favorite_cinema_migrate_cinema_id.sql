-- 若曾执行过旧版 V25（cinema_key 列），迁移到 cinema_id。新装库 V25 已是 cinema_id，本脚本为 no-op。

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'crawl' AND table_name = 'user_favorite_cinema' AND column_name = 'cinema_key'
  ) THEN
    ALTER TABLE crawl.user_favorite_cinema ADD COLUMN IF NOT EXISTS cinema_id INTEGER;

    UPDATE crawl.user_favorite_cinema ufc
       SET cinema_id = c.id
      FROM crawl.cinema c
     WHERE ufc.cinema_key = c.cinema_key
       AND ufc.cinema_id IS NULL
       AND ufc.deleted = 0;

    DELETE FROM crawl.user_favorite_cinema WHERE cinema_id IS NULL;

    DROP INDEX IF EXISTS uk_crawl_user_favorite_cinema_user_key;
    ALTER TABLE crawl.user_favorite_cinema DROP COLUMN IF EXISTS cinema_key;
    ALTER TABLE crawl.user_favorite_cinema ALTER COLUMN cinema_id SET NOT NULL;

    IF NOT EXISTS (
      SELECT 1 FROM pg_constraint WHERE conname = 'crawl_user_favorite_cinema_cinema_id_fkey'
    ) THEN
      ALTER TABLE crawl.user_favorite_cinema
        ADD CONSTRAINT crawl_user_favorite_cinema_cinema_id_fkey
        FOREIGN KEY (cinema_id) REFERENCES crawl.cinema(id) ON DELETE CASCADE;
    END IF;

    CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_user_favorite_cinema_user_cinema
      ON crawl.user_favorite_cinema (user_id, cinema_id) WHERE deleted = 0;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'user_favorite_cinema' AND column_name = 'cinema_key'
  ) THEN
    ALTER TABLE public.user_favorite_cinema ADD COLUMN IF NOT EXISTS cinema_id INTEGER;

    UPDATE public.user_favorite_cinema ufc
       SET cinema_id = c.id
      FROM public.cinema c
     WHERE ufc.cinema_key = c.cinema_key
       AND ufc.cinema_id IS NULL
       AND ufc.deleted = 0;

    DELETE FROM public.user_favorite_cinema WHERE cinema_id IS NULL;

    DROP INDEX IF EXISTS uk_user_favorite_cinema_user_key;
    ALTER TABLE public.user_favorite_cinema DROP COLUMN IF EXISTS cinema_key;
    ALTER TABLE public.user_favorite_cinema ALTER COLUMN cinema_id SET NOT NULL;

    IF NOT EXISTS (
      SELECT 1 FROM pg_constraint WHERE conname = 'user_favorite_cinema_cinema_id_fkey'
    ) THEN
      ALTER TABLE public.user_favorite_cinema
        ADD CONSTRAINT user_favorite_cinema_cinema_id_fkey
        FOREIGN KEY (cinema_id) REFERENCES public.cinema(id) ON DELETE CASCADE;
    END IF;

    CREATE UNIQUE INDEX IF NOT EXISTS uk_user_favorite_cinema_user_cinema
      ON public.user_favorite_cinema (user_id, cinema_id) WHERE deleted = 0;
  END IF;
END $$;
