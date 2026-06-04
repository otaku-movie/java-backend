-- 合并同名 movie 行。
-- 一些无 TMDb 的重映/活动标题过去会因为 raw display_title 不同生成不同 movie_key，
-- 但清洗后的 movie.name 完全相同；这些属于同一部电影，保留最小 id，其它行逻辑删除。

CREATE OR REPLACE FUNCTION merge_duplicate_movies_by_name_for_schema(p_schema text)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
  DROP TABLE IF EXISTS tmp_movie_dup_map;
  DROP TABLE IF EXISTS tmp_re_release_movie_dup_map;

  EXECUTE format($SQL$
    CREATE TEMP TABLE tmp_movie_dup_map ON COMMIT DROP AS
    WITH groups AS (
      SELECT
        name,
        (array_agg(id ORDER BY (tmdb_id IS NULL), id))[1] AS keep_id
      FROM %I.movie
      WHERE deleted = 0
        AND COALESCE(name, '') <> ''
      GROUP BY name
      HAVING COUNT(*) > 1
    )
    SELECT m.id AS old_id, g.keep_id
    FROM %I.movie m
    JOIN groups g ON g.name = m.name
    WHERE m.deleted = 0
      AND m.id <> g.keep_id
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie keep
       SET cover = COALESCE(keep.cover, old_vals.cover),
           description = COALESCE(NULLIF(keep.description, ''), old_vals.description, ''),
           time = COALESCE(keep.time, old_vals.time),
           release_date = COALESCE(keep.release_date, old_vals.release_date),
           movie_rate = COALESCE(keep.movie_rate, old_vals.movie_rate),
           status = COALESCE(keep.status, old_vals.status),
           original_name = COALESCE(keep.original_name, old_vals.original_name),
           home_page = COALESCE(keep.home_page, old_vals.home_page),
           level_id = COALESCE(keep.level_id, old_vals.level_id),
           start_date = COALESCE(keep.start_date, old_vals.start_date),
           end_date = COALESCE(keep.end_date, old_vals.end_date),
           tmdb_id = COALESCE(keep.tmdb_id, old_vals.tmdb_id),
           imdb_id = COALESCE(keep.imdb_id, old_vals.imdb_id),
           kind = CASE WHEN old_vals.kind = 'ods' THEN 'ods' ELSE keep.kind END,
           update_time = CURRENT_TIMESTAMP
      FROM (
        SELECT
          dm.keep_id,
          MIN(m.cover) FILTER (WHERE COALESCE(m.cover, '') <> '') AS cover,
          MIN(m.description) FILTER (WHERE COALESCE(m.description, '') <> '') AS description,
          MIN(m.time) FILTER (WHERE m.time IS NOT NULL) AS time,
          MIN(m.release_date) FILTER (WHERE m.release_date IS NOT NULL) AS release_date,
          MIN(m.movie_rate) FILTER (WHERE m.movie_rate IS NOT NULL) AS movie_rate,
          MIN(m.status) FILTER (WHERE m.status IS NOT NULL) AS status,
          MIN(m.original_name) FILTER (WHERE COALESCE(m.original_name, '') <> '') AS original_name,
          MIN(m.home_page) FILTER (WHERE COALESCE(m.home_page, '') <> '') AS home_page,
          MIN(m.level_id) FILTER (WHERE m.level_id IS NOT NULL) AS level_id,
          MIN(m.start_date) FILTER (WHERE COALESCE(m.start_date, '') <> '') AS start_date,
          MIN(m.end_date) FILTER (WHERE COALESCE(m.end_date, '') <> '') AS end_date,
          MIN(m.tmdb_id) FILTER (WHERE m.tmdb_id IS NOT NULL) AS tmdb_id,
          MIN(m.imdb_id) FILTER (WHERE COALESCE(m.imdb_id, '') <> '') AS imdb_id,
          MAX(m.kind) FILTER (WHERE COALESCE(m.kind, '') <> '') AS kind
        FROM tmp_movie_dup_map dm
        JOIN %I.movie m ON m.id = dm.old_id
        GROUP BY dm.keep_id
      ) old_vals
     WHERE keep.id = old_vals.keep_id
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    CREATE TEMP TABLE tmp_re_release_movie_dup_map ON COMMIT DROP AS
    SELECT old_rr.id AS old_id, MIN(keep_rr.id) AS keep_id
    FROM %I.re_release old_rr
    JOIN tmp_movie_dup_map dm ON dm.old_id = old_rr.movie_id
    JOIN %I.re_release keep_rr
      ON keep_rr.movie_id = dm.keep_id
     AND keep_rr.deleted = 0
     AND (
       (COALESCE(old_rr.display_name_override, '') <> ''
        AND old_rr.display_name_override = keep_rr.display_name_override)
       OR (COALESCE(old_rr.display_name_override, '') = ''
           AND old_rr.start_date = keep_rr.start_date)
     )
    WHERE old_rr.deleted = 0
    GROUP BY old_rr.id
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_show_time mst
       SET re_release_id = rr.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_movie_dup_map rr
     WHERE mst.re_release_id = rr.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.benefit b
       SET re_release_id = rr.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_movie_dup_map rr
     WHERE b.re_release_id = rr.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.re_release rr
       SET deleted = 1,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_movie_dup_map dup
     WHERE rr.id = dup.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.re_release rr
       SET movie_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_dup_map dm
     WHERE rr.movie_id = dm.old_id
       AND rr.deleted = 0
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_show_time mst
       SET movie_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_dup_map dm
     WHERE mst.movie_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_version mv
       SET movie_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_dup_map dm
     WHERE mv.movie_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_comment mc
       SET movie_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_dup_map dm
     WHERE mc.movie_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_rate mr
       SET movie_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_dup_map dm
     WHERE mr.movie_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    DELETE FROM %I.movie_tag_tags old_link
    USING tmp_movie_dup_map dm
    WHERE old_link.movie_id = dm.old_id
      AND EXISTS (
        SELECT 1 FROM %I.movie_tag_tags keep_link
        WHERE keep_link.movie_id = dm.keep_id
          AND keep_link.movie_tag_id = old_link.movie_tag_id
      )
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_tag_tags link
       SET movie_id = dm.keep_id
      FROM tmp_movie_dup_map dm
     WHERE link.movie_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    DELETE FROM %I.movie_character old_link
    USING tmp_movie_dup_map dm
    WHERE old_link.movie_id = dm.old_id
      AND EXISTS (
        SELECT 1 FROM %I.movie_character keep_link
        WHERE keep_link.movie_id = dm.keep_id
          AND keep_link.character_id = old_link.character_id
      )
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_character link
       SET movie_id = dm.keep_id
      FROM tmp_movie_dup_map dm
     WHERE link.movie_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    DELETE FROM %I.movie_staff old_link
    USING tmp_movie_dup_map dm
    WHERE old_link.movie_id = dm.old_id
      AND EXISTS (
        SELECT 1 FROM %I.movie_staff keep_link
        WHERE keep_link.movie_id = dm.keep_id
          AND keep_link.staff_id = old_link.staff_id
          AND keep_link.position_id = old_link.position_id
      )
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_staff link
       SET movie_id = dm.keep_id
      FROM tmp_movie_dup_map dm
     WHERE link.movie_id = dm.old_id
  $SQL$, p_schema);

  IF to_regclass(p_schema || '.benefit') IS NOT NULL THEN
    EXECUTE format($SQL$
      UPDATE %I.benefit b
         SET movie_id = dm.keep_id,
             update_time = CURRENT_TIMESTAMP
        FROM tmp_movie_dup_map dm
       WHERE b.movie_id = dm.old_id
    $SQL$, p_schema);
  END IF;

  IF to_regclass(p_schema || '.presale') IS NOT NULL THEN
    EXECUTE format($SQL$
      UPDATE %I.presale p
         SET deleted = 1,
             update_time = CURRENT_TIMESTAMP
        FROM tmp_movie_dup_map dm
       WHERE p.movie_id = dm.old_id
         AND p.deleted = 0
         AND EXISTS (
           SELECT 1 FROM %I.presale keep_p
           WHERE keep_p.movie_id = dm.keep_id
             AND keep_p.deleted = 0
         )
    $SQL$, p_schema, p_schema);

    EXECUTE format($SQL$
      UPDATE %I.presale p
         SET movie_id = dm.keep_id,
             update_time = CURRENT_TIMESTAMP
        FROM tmp_movie_dup_map dm
       WHERE p.movie_id = dm.old_id
         AND p.deleted = 0
    $SQL$, p_schema);
  END IF;

  EXECUTE format($SQL$
    UPDATE %I.movie old_movie
       SET deleted = 1,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_dup_map dm
     WHERE old_movie.id = dm.old_id
  $SQL$, p_schema);
END $$;

SELECT merge_duplicate_movies_by_name_for_schema('crawl');

DROP FUNCTION IF EXISTS merge_duplicate_movies_by_name_for_schema(text);
