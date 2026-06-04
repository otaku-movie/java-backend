-- ============================================================
-- V15: seed public.language / crawl.language with common
--      cinema-source languages so movie_show_time.subtitle_id
--      (and crawl.movie.original_language in the future) can
--      reference real rows.
--
-- public.language already exists with (id, code, name, ...);
-- existing rows: en/English, ja/日本語, zh-CN/简体中文.
-- The list below covers languages we routinely see in Japan
-- theatrical catalogs (Hollywood/EU/KR/CN/TW/TH/IN imports).
-- All inserts are idempotent on `code`.
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

-- ---- public.language ------------------------------------------------------
-- Keep only the languages we actually see in Japan theatrical catalogs:
-- en / ja / zh-CN / zh-TW / ko cover ~98% of imports; fr/de/es/it round out
-- the regular EU art-house slate; `und` is for multi-language / unknown.
INSERT INTO public.language (code, name, create_time, update_time, deleted)
SELECT v.code, v.name, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (
  VALUES
    ('en',    'English'),
    ('ja',    '日本語'),
    ('zh-CN', '简体中文'),
    ('zh-TW', '繁體中文'),
    ('ko',    '한국어'),
    ('fr',    'Français'),
    ('de',    'Deutsch'),
    ('es',    'Español'),
    ('it',    'Italiano'),
    ('und',   '多言語')
) AS v(code, name)
WHERE NOT EXISTS (
  SELECT 1 FROM public.language l
   WHERE l.code = v.code AND l.deleted = 0
);

-- ---- crawl.language -------------------------------------------------------
-- Mirror the public seed; crawler queries language via `crawl, public`
-- search_path, but writing both keeps tools that point at the crawl schema
-- consistent.
INSERT INTO crawl.language (id, code, name, create_time, update_time, deleted)
SELECT l.id, l.code, l.name, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM public.language l
WHERE l.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM crawl.language cl
     WHERE cl.code = l.code AND cl.deleted = 0
  );
