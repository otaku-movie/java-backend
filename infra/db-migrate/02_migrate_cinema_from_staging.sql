-- Migrate minimal cinema data from staging.* into crawl.* for prod_movie.
--
-- Strategy A:
--   - Do NOT preserve old ids.
--   - Let prod_movie identity sequences allocate new ids.
--   - Align parent/child rows by business keys:
--       brand.name
--       cinema.cinema_key
--       theater_hall (new cinema_id, crawl_name)
--       cinema_spec_spec (new cinema_id, spec_id)
--
-- Preconditions:
--   - Flyway has already created crawl.* tables in prod_movie.
--   - 01_prepare_cinema_staging.sql has been run.
--   - Old crawl.brand/cinema/theater_hall/cinema_spec_spec rows have been
--     imported into staging.brand/cinema/theater_hall/cinema_spec_spec.

\set ON_ERROR_STOP on

BEGIN;

-- Basic source quality gates. These prevent ambiguous mapping and duplicate upsert rows.
DO $$
DECLARE
  problem_count integer;
BEGIN
  SELECT COUNT(*) INTO problem_count
  FROM (
    SELECT name
    FROM staging.brand
    WHERE name IS NOT NULL
    GROUP BY name
    HAVING COUNT(*) > 1
  ) d;
  IF problem_count > 0 THEN
    RAISE EXCEPTION 'staging.brand has % duplicated name values', problem_count;
  END IF;

  SELECT COUNT(*) INTO problem_count
  FROM staging.cinema
  WHERE COALESCE(deleted, 0) = 0
    AND cinema_key IS NULL;
  IF problem_count > 0 THEN
    RAISE EXCEPTION 'staging.cinema has % active rows with null cinema_key', problem_count;
  END IF;

  SELECT COUNT(*) INTO problem_count
  FROM (
    SELECT cinema_key
    FROM staging.cinema
    WHERE COALESCE(deleted, 0) = 0
      AND cinema_key IS NOT NULL
    GROUP BY cinema_key
    HAVING COUNT(*) > 1
  ) d;
  IF problem_count > 0 THEN
    RAISE EXCEPTION 'staging.cinema has % duplicated active cinema_key values', problem_count;
  END IF;

  SELECT COUNT(*) INTO problem_count
  FROM staging.theater_hall
  WHERE COALESCE(deleted, 0) = 0
    AND crawl_name IS NULL;
  IF problem_count > 0 THEN
    RAISE EXCEPTION 'staging.theater_hall has % active rows with null crawl_name', problem_count;
  END IF;

  SELECT COUNT(*) INTO problem_count
  FROM (
    SELECT cinema_id, crawl_name
    FROM staging.theater_hall
    WHERE COALESCE(deleted, 0) = 0
      AND crawl_name IS NOT NULL
    GROUP BY cinema_id, crawl_name
    HAVING COUNT(*) > 1
  ) d;
  IF problem_count > 0 THEN
    RAISE EXCEPTION 'staging.theater_hall has % duplicated active (cinema_id, crawl_name) pairs', problem_count;
  END IF;

  SELECT COUNT(*) INTO problem_count
  FROM (
    SELECT cinema_id, spec_id
    FROM staging.cinema_spec_spec
    WHERE COALESCE(deleted, 0) = 0
    GROUP BY cinema_id, spec_id
    HAVING COUNT(*) > 1
  ) d;
  IF problem_count > 0 THEN
    RAISE EXCEPTION 'staging.cinema_spec_spec has % duplicated active (cinema_id, spec_id) pairs', problem_count;
  END IF;
END $$;

-- 0) areas: standard administrative dictionary. Migrate preserving old ids so
-- cinema.area_id/prefecture_id/region_id can keep their original values. Safe to
-- re-run: ON CONFLICT (id) DO NOTHING. After insert, bump the identity sequence
-- past max(id) so future auto-id inserts won't collide.
DO $$
DECLARE
  insert_cols text;
  select_exprs text;
BEGIN
  SELECT
    string_agg(format('%I', s.column_name), ', ' ORDER BY s.ordinal_position),
    string_agg(format('s.%I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO insert_cols, select_exprs
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'areas'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'areas';

  EXECUTE format(
    'INSERT INTO crawl.areas (%s)
     SELECT %s
     FROM staging.areas s
     ON CONFLICT (id) DO NOTHING',
    insert_cols,
    select_exprs
  );
END $$;

SELECT setval(
  pg_get_serial_sequence('crawl.areas', 'id'),
  GREATEST((SELECT COALESCE(MAX(id), 1) FROM crawl.areas), 1),
  true
);

-- 1) brand: upsert by name. There is no unique index on brand.name, so avoid
-- duplicates with NOT EXISTS and update matching names explicitly.
DO $$
DECLARE
  insert_cols text;
  select_exprs text;
  update_set text;
BEGIN
  SELECT
    string_agg(format('%I', s.column_name), ', ' ORDER BY s.ordinal_position),
    string_agg(format('s.%I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO insert_cols, select_exprs
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'brand'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'brand'
    AND s.column_name <> 'id';

  EXECUTE format(
    'INSERT INTO crawl.brand (%s)
     SELECT %s
     FROM staging.brand s
     WHERE s.name IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM crawl.brand c WHERE c.name = s.name)',
    insert_cols,
    select_exprs
  );

  SELECT string_agg(format('%1$I = s.%1$I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO update_set
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'brand'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'brand'
    AND s.column_name NOT IN ('id', 'name', 'create_time');

  IF update_set IS NOT NULL THEN
    EXECUTE format(
      'UPDATE crawl.brand c
       SET %s
       FROM staging.brand s
       WHERE c.name = s.name',
      update_set
    );
  END IF;
END $$;

CREATE TEMP TABLE brand_map AS
SELECT s.id AS old_id, c.id AS new_id
FROM staging.brand s
JOIN crawl.brand c ON c.name = s.name;

-- 2) cinema: upsert by cinema_key. All common columns are copied except id and
-- brand_id; brand_id is remapped through brand_map.
DO $$
DECLARE
  base_insert_cols text;
  base_select_exprs text;
  update_set text;
BEGIN
  SELECT
    string_agg(format('%I', s.column_name), ', ' ORDER BY s.ordinal_position),
    string_agg(format('s.%I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO base_insert_cols, base_select_exprs
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'cinema'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'cinema'
    AND s.column_name NOT IN ('id', 'brand_id');

  SELECT string_agg(format('%1$I = EXCLUDED.%1$I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO update_set
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'cinema'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'cinema'
    AND s.column_name NOT IN ('id', 'cinema_key', 'create_time');

  EXECUTE format(
    'INSERT INTO crawl.cinema (%s, brand_id)
     SELECT %s, bm.new_id
     FROM staging.cinema s
     LEFT JOIN brand_map bm ON bm.old_id = s.brand_id
     WHERE COALESCE(s.deleted, 0) = 0
       AND s.cinema_key IS NOT NULL
     ON CONFLICT (cinema_key) WHERE cinema_key IS NOT NULL
     DO UPDATE SET %s',
    base_insert_cols,
    base_select_exprs,
    update_set
  );
END $$;

CREATE TEMP TABLE cinema_map AS
SELECT s.id AS old_id, c.id AS new_id
FROM staging.cinema s
JOIN crawl.cinema c ON c.cinema_key = s.cinema_key
WHERE s.cinema_key IS NOT NULL;

-- 3) theater_hall: remap cinema_id through cinema_map, upsert by
-- (new cinema_id, crawl_name). Common columns are copied dynamically.
DO $$
DECLARE
  base_insert_cols text;
  base_select_exprs text;
  update_set text;
BEGIN
  SELECT
    string_agg(format('%I', s.column_name), ', ' ORDER BY s.ordinal_position),
    string_agg(format('s.%I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO base_insert_cols, base_select_exprs
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'theater_hall'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'theater_hall'
    AND s.column_name NOT IN ('id', 'cinema_id');

  SELECT string_agg(format('%1$I = EXCLUDED.%1$I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO update_set
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'theater_hall'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'theater_hall'
    AND s.column_name NOT IN ('id', 'cinema_id', 'crawl_name', 'create_time');

  EXECUTE format(
    'INSERT INTO crawl.theater_hall (%s, cinema_id)
     SELECT %s, cm.new_id
     FROM staging.theater_hall s
     JOIN cinema_map cm ON cm.old_id = s.cinema_id
     WHERE COALESCE(s.deleted, 0) = 0
       AND s.crawl_name IS NOT NULL
     ON CONFLICT (cinema_id, crawl_name) WHERE deleted = 0 AND crawl_name IS NOT NULL
     DO UPDATE SET %s',
    base_insert_cols,
    base_select_exprs,
    update_set
  );
END $$;

-- 4) cinema_spec_spec: remap cinema_id and keep spec_id (standard dictionary id).
DO $$
DECLARE
  base_insert_cols text;
  base_select_exprs text;
BEGIN
  SELECT
    string_agg(format('%I', s.column_name), ', ' ORDER BY s.ordinal_position),
    string_agg(format('s.%I', s.column_name), ', ' ORDER BY s.ordinal_position)
  INTO base_insert_cols, base_select_exprs
  FROM information_schema.columns s
  JOIN information_schema.columns t
    ON t.table_schema = 'crawl'
   AND t.table_name = 'cinema_spec_spec'
   AND t.column_name = s.column_name
  WHERE s.table_schema = 'staging'
    AND s.table_name = 'cinema_spec_spec'
    AND s.column_name <> 'cinema_id';

  EXECUTE format(
    'INSERT INTO crawl.cinema_spec_spec (%s, cinema_id)
     SELECT %s, cm.new_id
     FROM staging.cinema_spec_spec s
     JOIN cinema_map cm ON cm.old_id = s.cinema_id
     WHERE COALESCE(s.deleted, 0) = 0
     ON CONFLICT (cinema_id, spec_id) WHERE deleted = 0 DO NOTHING',
    base_insert_cols,
    base_select_exprs
  );
END $$;

SELECT
  (SELECT COUNT(*) FROM crawl.areas) AS migrated_areas,
  (SELECT COUNT(*) FROM brand_map) AS mapped_brands,
  (SELECT COUNT(*) FROM cinema_map) AS mapped_cinemas,
  (SELECT COUNT(*) FROM crawl.theater_hall h JOIN cinema_map cm ON cm.new_id = h.cinema_id WHERE h.deleted = 0) AS migrated_halls,
  (SELECT COUNT(*) FROM crawl.cinema_spec_spec css JOIN cinema_map cm ON cm.new_id = css.cinema_id WHERE css.deleted = 0) AS migrated_cinema_specs;

COMMIT;
