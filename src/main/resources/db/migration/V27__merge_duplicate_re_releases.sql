-- 合并重复重映：同一 movie_id + 展示名只保留一条 re_release；
-- 没有展示名的旧数据则按 movie_id + start_date 兜底。
--
-- importer 过去按 (movie_id, start_date, version_info) 去重，导致同一片名
-- 因上映日/版本说明细微差异拆成多条（例如同一个 4K デジタルリマスター）。

CREATE OR REPLACE FUNCTION merge_re_release_duplicates_for_schema(p_schema text)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
  DROP TABLE IF EXISTS tmp_re_release_dup_map;

  EXECUTE format($SQL$
    CREATE TEMP TABLE tmp_re_release_dup_map ON COMMIT DROP AS
    WITH keyed AS (
      SELECT
        id,
        movie_id,
        CASE
          WHEN COALESCE(display_name_override, '') <> '' THEN 'title:' || display_name_override
          ELSE 'date:' || start_date::text
        END AS merge_key
      FROM %I.re_release
      WHERE deleted = 0
    ),
    groups AS (
      SELECT movie_id, merge_key, MIN(id) AS keep_id
      FROM keyed
      GROUP BY movie_id, merge_key
      HAVING COUNT(*) > 1
    )
    SELECT k.id AS old_id, g.keep_id
    FROM keyed k
    JOIN groups g
      ON g.movie_id = k.movie_id
     AND g.merge_key = k.merge_key
    WHERE k.id <> g.keep_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    WITH grouped_rows AS (
      SELECT
        COALESCE(dm.keep_id, r.id) AS keep_id,
        r.*
      FROM %I.re_release r
      LEFT JOIN tmp_re_release_dup_map dm ON dm.old_id = r.id
      WHERE r.deleted = 0
        AND COALESCE(dm.keep_id, r.id) IN (
          SELECT keep_id FROM tmp_re_release_dup_map
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
        MIN(display_name_override) FILTER (WHERE COALESCE(display_name_override, '') <> '') AS display_name_override,
        MIN(poster_override) FILTER (WHERE COALESCE(poster_override, '') <> '') AS poster_override,
        MIN(time_override) FILTER (WHERE time_override IS NOT NULL) AS time_override
      FROM grouped_rows
      GROUP BY keep_id
    )
    UPDATE %I.re_release keep
       SET start_date = mv.start_date,
           end_date = COALESCE(keep.end_date, mv.end_date),
           version_info = COALESCE(NULLIF(mver.version_info, ''), keep.version_info),
           display_name_override = COALESCE(keep.display_name_override, mv.display_name_override),
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
      FROM tmp_re_release_dup_map dm
     WHERE mst.re_release_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.benefit b
       SET re_release_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_dup_map dm
     WHERE b.re_release_id = dm.old_id
  $SQL$, p_schema);

  EXECUTE format($SQL$
    UPDATE %I.re_release r
       SET deleted = 1,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_re_release_dup_map dm
     WHERE r.id = dm.old_id
  $SQL$, p_schema);
END $$;

SELECT merge_re_release_duplicates_for_schema('public');
SELECT merge_re_release_duplicates_for_schema('crawl');

DROP FUNCTION IF EXISTS merge_re_release_duplicates_for_schema(text);

CREATE UNIQUE INDEX IF NOT EXISTS uk_re_release_movie_display_name
  ON public.re_release (movie_id, display_name_override)
  WHERE deleted = 0 AND COALESCE(display_name_override, '') <> '';

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_re_release_movie_display_name
  ON crawl.re_release (movie_id, display_name_override)
  WHERE deleted = 0 AND COALESCE(display_name_override, '') <> '';
