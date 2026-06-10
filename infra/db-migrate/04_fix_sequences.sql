-- Advance every crawl sequence past the current max value of the columns that
-- use it. Needed after loading seed/migrated data with explicit ids (a
-- data-only dump does not always emit setval, and shared sequences must be set
-- to the GLOBAL max across all tables that share them, e.g. public.auto_increment).
--
-- Idempotent: safe to run repeatedly; only ever moves a sequence forward.

\set ON_ERROR_STOP on

DO $$
DECLARE
  rec      record;
  curmax   bigint;
  seq_max  jsonb := '{}'::jsonb;
  s        text;
  v        bigint;
BEGIN
  -- Walk every crawl column whose DEFAULT is nextval(<seq>), parsing the
  -- sequence directly from the default expression. This (unlike
  -- pg_get_serial_sequence) also covers shared sequences such as
  -- public.auto_increment and any sequence not formally OWNED BY the column.
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
      RAISE NOTICE 'setval % -> %', s, v;
    END IF;
  END LOOP;
END $$;
