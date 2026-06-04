-- ============================================================
-- V16: seed `dimensionType` / `dubbingVersion` dicts in
--      public.dict (and mirror into crawl.dict) so the
--      `movie_show_time.dimension_type` column values (1=2D, 2=3D)
--      and the future dub-flag both resolve through dict_item.code.
--
-- Most production databases already have these rows in public; the
-- migration is idempotent on dict.code and dict_item.(dict_id, code).
-- crawl.dict is rewritten by `import:data` (TRUNCATE) so the seed
-- there is just to make a fresh DB usable before the first import.
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

-- ---- helper: seed a dict + items into a target schema --------------------
-- We can't easily parameterize schema names in DML, so the block is
-- duplicated for public and crawl. The payload is identical.

-- ---- public.dict --------------------------------------------------------
INSERT INTO public.dict (name, code)
SELECT v.name, v.code
FROM (VALUES ('放映类型', 'dimensionType'), ('バージョン', 'dubbingVersion')) AS v(name, code)
WHERE NOT EXISTS (
  SELECT 1 FROM public.dict d WHERE d.code = v.code
);

INSERT INTO public.dict_item (dict_id, name, description, code)
SELECT d.id, v.name, NULL, v.code
FROM public.dict d
JOIN (VALUES
        ('dimensionType', '2D',           1),
        ('dimensionType', '3D',           2),
        ('dubbingVersion','オリジナル版', 1),
        ('dubbingVersion','吹き替え版',   2)
     ) AS v(dict_code, name, code) ON v.dict_code = d.code
WHERE NOT EXISTS (
  SELECT 1 FROM public.dict_item di
   WHERE di.dict_id = d.id AND di.code = v.code
);

-- ---- crawl.dict ---------------------------------------------------------
INSERT INTO crawl.dict (name, code)
SELECT v.name, v.code
FROM (VALUES ('放映类型', 'dimensionType'), ('バージョン', 'dubbingVersion')) AS v(name, code)
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.dict d WHERE d.code = v.code
);

INSERT INTO crawl.dict_item (dict_id, name, description, code)
SELECT d.id, v.name, NULL, v.code
FROM crawl.dict d
JOIN (VALUES
        ('dimensionType', '2D',           1),
        ('dimensionType', '3D',           2),
        ('dubbingVersion','オリジナル版', 1),
        ('dubbingVersion','吹き替え版',   2)
     ) AS v(dict_code, name, code) ON v.dict_code = d.code
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.dict_item di
   WHERE di.dict_id = d.id AND di.code = v.code
);
