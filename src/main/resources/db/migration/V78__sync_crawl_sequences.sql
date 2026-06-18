-- 电影合并 / 手工迁移 / 显式 id 写入后，public 侧序列常落后于 crawl 表 MAX(id)，
-- 导致 import 插入 movie_version、movie_show_time_tag 等表时触发 pkey 冲突。
-- 逻辑与 cinema-crawler/infra/db-migrate/04_fix_sequences.sql 一致，可重复执行。

DO $$
DECLARE
  rec      record;
  curmax   bigint;
  seq_max  jsonb := '{}'::jsonb;
  s        text;
  v        bigint;
BEGIN
  FOR rec IN
    SELECT c.relname AS tbl,
           a.attname AS col,
           regexp_replace(
             pg_get_expr(ad.adbin, ad.adrelid),
             '^nextval\(''([^'']+)''.*$', '\1'
           ) AS seq
    FROM pg_attrdef ad
    JOIN pg_attribute a ON a.attrelid = ad.adrelid AND a.attnum = ad.adnum
    JOIN pg_class c     ON c.oid = ad.adrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'crawl'
      AND pg_get_expr(ad.adbin, ad.adrelid) LIKE 'nextval(%'
  LOOP
    EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM crawl.%I', rec.col, rec.tbl)
      INTO curmax;
    v := COALESCE((seq_max ->> rec.seq)::bigint, 0);
    IF curmax > v THEN
      seq_max := jsonb_set(seq_max, ARRAY[rec.seq], to_jsonb(curmax));
    END IF;
  END LOOP;

  FOR s, v IN SELECT key, value::text::bigint FROM jsonb_each_text(seq_max) LOOP
    IF v > 0 THEN
      EXECUTE format('SELECT setval(%L, %s, true)', s, v);
    END IF;
  END LOOP;
END $$;
