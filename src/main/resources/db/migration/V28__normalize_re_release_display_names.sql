-- 重映展示名回填为干净片名；版本/重映类型放在 version_info。
-- 如果多条记录回填后会变成同一 movie_id + movie.name，则先合并引用并逻辑删除重复行。

CREATE OR REPLACE FUNCTION normalize_re_release_display_names_for_schema(p_schema text)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
  DROP TABLE IF EXISTS tmp_re_release_name_dup_map;

  EXECUTE format($SQL$
    CREATE TEMP TABLE tmp_re_release_name_dup_map ON COMMIT DROP AS
    WITH keyed AS (
      SELECT
        rr.id,
        rr.movie_id,
        NULLIF(m.name, '') AS target_name
      FROM %I.re_release rr
      JOIN %I.movie m ON m.id = rr.movie_id
      WHERE rr.deleted = 0
        AND m.deleted = 0
        AND NULLIF(m.name, '') IS NOT NULL
    ),
    groups AS (
      SELECT movie_id, target_name, MIN(id) AS keep_id
      FROM keyed
      GROUP BY movie_id, target_name
      HAVING COUNT(*) > 1
    )
    SELECT k.id AS old_id, g.keep_id
    FROM keyed k
    JOIN groups g
      ON g.movie_id = k.movie_id
     AND g.target_name = k.target_name
    WHERE k.id <> g.keep_id
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    WITH grouped_rows AS (
      SELECT COALESCE(dm.keep_id, rr.id) AS keep_id, rr.*
      FROM %I.re_release rr
      LEFT JOIN tmp_re_release_name_dup_map dm ON dm.old_id = rr.id
      WHERE rr.deleted = 0
        AND COALESCE(dm.keep_id, rr.id) IN (
          SELECT keep_id FROM tmp_re_release_name_dup_map
        )
    ),
    merged_versions AS (
      SELECT
        gr.keep_id,
        string_agg(DISTINCT trim(v.label), ' / ' ORDER BY trim(v.label)) AS version_info
      FROM grouped_rows gr
      CROSS JOIN LATERAL regexp_split_to_table(COALESCE(gr.version_info, ''), '\s*/\s*') AS v(label)
      WHERE trim(v.label) <> ''
      GROUP BY gr.keep_id
    ),
    merged_values AS (
      SELECT
        keep_id,
        MIN(start_date) AS start_date,
        MIN(end_date) FILTER (WHERE end_date IS NOT NULL) AS end_date,
        MIN(poster_override) FILTER (WHERE COALESCE(poster_override, '') <> '') AS poster_override,
        MIN(time_override) FILTER (WHERE time_override IS NOT NULL) AS time_override
      FROM grouped_rows
      GROUP BY keep_id
    )
    UPDATE %I.re_release keep
       SET start_date = mv.start_date,
           end_date = COALESCE(keep.end_date, mv.end_date),
           version_info = COALESCE(NULLIF(mver.version_info, ''), keep.version_info),
           poster_override = COALESCE(keep.poster_override, mv.poster_override),
           time_override = COALESCE(keep.time_override, mv.time_override),
           update_time = CURRENT_TIMESTAMP
      FROM merged_values mv
      LEFT JOIN merged_versions mver ON mver.keep_id = mv.keep_id
     WHERE keep.id = mv.keep_id
  $SQL$, p_schema, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.movie_show_time mst
       SET re_release_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_name_dup_map dm
     WHERE mst.re_release_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.benefit b
       SET re_release_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_name_dup_map dm
     WHERE b.re_release_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.re_release rr
       SET deleted = 1,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_name_dup_map dm
     WHERE rr.id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.re_release rr
       SET display_name_override = NULLIF(m.name, ''),
           update_time = CURRENT_TIMESTAMP
      FROM %I.movie m
     WHERE rr.movie_id = m.id
       AND rr.deleted = 0
       AND m.deleted = 0
       AND NULLIF(m.name, '') IS NOT NULL
       AND COALESCE(NULLIF(rr.display_name_override, ''), '') <> NULLIF(m.name, '')
  $SQL$, p_schema, p_schema);
END $$;

SELECT normalize_re_release_display_names_for_schema('public');
SELECT normalize_re_release_display_names_for_schema('crawl');

DROP FUNCTION IF EXISTS normalize_re_release_display_names_for_schema(text);
