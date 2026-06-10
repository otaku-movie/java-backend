-- Verify minimal cinema migration for prod_movie.
--
-- This script is useful both before and after 02_migrate_cinema_from_staging.sql:
--   - Before: staging source quality checks should return zero issues.
--   - After: prod duplicate/reference checks should return zero issues.

\set ON_ERROR_STOP on

SELECT 'staging row counts' AS section,
       (SELECT COUNT(*) FROM staging.brand) AS brands,
       (SELECT COUNT(*) FROM staging.cinema) AS cinemas,
       (SELECT COUNT(*) FROM staging.theater_hall) AS halls,
       (SELECT COUNT(*) FROM staging.cinema_spec_spec) AS cinema_specs;

-- Source quality: every query below should return 0 rows.
SELECT 'staging duplicated brand.name' AS issue, name, COUNT(*) AS count
FROM staging.brand
WHERE name IS NOT NULL
GROUP BY name
HAVING COUNT(*) > 1;

SELECT 'staging cinema null key' AS issue, id::text AS key, name
FROM staging.cinema
WHERE COALESCE(deleted, 0) = 0
  AND cinema_key IS NULL;

SELECT 'staging duplicated cinema_key' AS issue, cinema_key AS key, COUNT(*)::text AS count
FROM staging.cinema
WHERE COALESCE(deleted, 0) = 0
  AND cinema_key IS NOT NULL
GROUP BY cinema_key
HAVING COUNT(*) > 1;

SELECT 'staging hall null crawl_name' AS issue, id::text AS key, name
FROM staging.theater_hall
WHERE COALESCE(deleted, 0) = 0
  AND crawl_name IS NULL;

SELECT 'staging duplicated hall' AS issue,
       (cinema_id::text || '/' || crawl_name) AS key,
       COUNT(*)::text AS count
FROM staging.theater_hall
WHERE COALESCE(deleted, 0) = 0
  AND crawl_name IS NOT NULL
GROUP BY cinema_id, crawl_name
HAVING COUNT(*) > 1;

SELECT 'staging duplicated cinema_spec_spec' AS issue,
       (cinema_id::text || '/' || spec_id::text) AS key,
       COUNT(*)::text AS count
FROM staging.cinema_spec_spec
WHERE COALESCE(deleted, 0) = 0
GROUP BY cinema_id, spec_id
HAVING COUNT(*) > 1;

-- Prod duplicate checks: every query below should return 0 rows.
SELECT 'prod duplicated brand.name' AS issue, name, COUNT(*) AS count
FROM crawl.brand
WHERE name IS NOT NULL
GROUP BY name
HAVING COUNT(*) > 1;

SELECT 'prod duplicated cinema_key' AS issue, cinema_key AS key, COUNT(*)::text AS count
FROM crawl.cinema
WHERE COALESCE(deleted, 0) = 0
  AND cinema_key IS NOT NULL
GROUP BY cinema_key
HAVING COUNT(*) > 1;

SELECT 'prod duplicated hall' AS issue,
       (cinema_id::text || '/' || crawl_name) AS key,
       COUNT(*)::text AS count
FROM crawl.theater_hall
WHERE COALESCE(deleted, 0) = 0
  AND crawl_name IS NOT NULL
GROUP BY cinema_id, crawl_name
HAVING COUNT(*) > 1;

SELECT 'prod duplicated cinema_spec_spec' AS issue,
       (cinema_id::text || '/' || spec_id::text) AS key,
       COUNT(*)::text AS count
FROM crawl.cinema_spec_spec
WHERE COALESCE(deleted, 0) = 0
GROUP BY cinema_id, spec_id
HAVING COUNT(*) > 1;

-- Reference checks: every query below should return 0 rows.
SELECT 'prod cinema missing brand' AS issue, c.id::text AS key, c.name
FROM crawl.cinema c
WHERE COALESCE(c.deleted, 0) = 0
  AND c.brand_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM crawl.brand b WHERE b.id = c.brand_id);

SELECT 'prod cinema missing prefecture area' AS issue, c.id::text AS key, c.name
FROM crawl.cinema c
WHERE COALESCE(c.deleted, 0) = 0
  AND c.prefecture_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM crawl.areas a WHERE a.id = c.prefecture_id);

SELECT 'prod cinema missing city area' AS issue, c.id::text AS key, c.name
FROM crawl.cinema c
WHERE COALESCE(c.deleted, 0) = 0
  AND c.city_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM crawl.areas a WHERE a.id = c.city_id);

SELECT 'prod hall missing cinema' AS issue, h.id::text AS key, h.name
FROM crawl.theater_hall h
WHERE COALESCE(h.deleted, 0) = 0
  AND NOT EXISTS (SELECT 1 FROM crawl.cinema c WHERE c.id = h.cinema_id);

SELECT 'prod cinema_spec_spec missing cinema' AS issue,
       (css.cinema_id::text || '/' || css.spec_id::text) AS key,
       NULL::text AS name
FROM crawl.cinema_spec_spec css
WHERE COALESCE(css.deleted, 0) = 0
  AND NOT EXISTS (SELECT 1 FROM crawl.cinema c WHERE c.id = css.cinema_id);

SELECT 'prod cinema_spec_spec missing spec' AS issue,
       (css.cinema_id::text || '/' || css.spec_id::text) AS key,
       NULL::text AS name
FROM crawl.cinema_spec_spec css
WHERE COALESCE(css.deleted, 0) = 0
  AND NOT EXISTS (SELECT 1 FROM crawl.cinema_spec s WHERE s.id = css.spec_id);

SELECT 'prod row counts' AS section,
       (SELECT COUNT(*) FROM crawl.brand WHERE COALESCE(deleted, 0) = 0) AS brands,
       (SELECT COUNT(*) FROM crawl.cinema WHERE COALESCE(deleted, 0) = 0) AS cinemas,
       (SELECT COUNT(*) FROM crawl.theater_hall WHERE COALESCE(deleted, 0) = 0) AS halls,
       (SELECT COUNT(*) FROM crawl.cinema_spec_spec WHERE COALESCE(deleted, 0) = 0) AS cinema_specs;
