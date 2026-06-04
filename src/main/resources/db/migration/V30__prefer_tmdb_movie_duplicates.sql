-- V29 合并同名 movie 时应优先保留 TMDb canonical 行。
-- 若环境中曾按旧逻辑保留了 title-key 行、删除了同名 tmdb 行，本迁移把引用迁回 tmdb 行。

CREATE OR REPLACE FUNCTION prefer_tmdb_movie_duplicates_for_crawl()
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
  DROP TABLE IF EXISTS tmp_movie_tmdb_promote_map;

  CREATE TEMP TABLE tmp_movie_tmdb_promote_map ON COMMIT DROP AS
  SELECT active.id AS old_id, MIN(deleted_tmdb.id) AS keep_id
  FROM crawl.movie active
  JOIN crawl.movie deleted_tmdb
    ON deleted_tmdb.name = active.name
   AND deleted_tmdb.deleted = 1
   AND deleted_tmdb.tmdb_id IS NOT NULL
  WHERE active.deleted = 0
    AND active.tmdb_id IS NULL
    AND COALESCE(active.name, '') <> ''
  GROUP BY active.id;

  UPDATE crawl.movie keep
     SET deleted = 0,
         cover = COALESCE(keep.cover, old_movie.cover),
         description = COALESCE(NULLIF(keep.description, ''), old_movie.description, ''),
         time = COALESCE(keep.time, old_movie.time),
         release_date = COALESCE(keep.release_date, old_movie.release_date),
         movie_rate = COALESCE(keep.movie_rate, old_movie.movie_rate),
         status = COALESCE(keep.status, old_movie.status),
         original_name = COALESCE(keep.original_name, old_movie.original_name),
         home_page = COALESCE(keep.home_page, old_movie.home_page),
         level_id = COALESCE(keep.level_id, old_movie.level_id),
         start_date = COALESCE(keep.start_date, old_movie.start_date),
         end_date = COALESCE(keep.end_date, old_movie.end_date),
         imdb_id = COALESCE(keep.imdb_id, old_movie.imdb_id),
         kind = CASE WHEN old_movie.kind = 'ods' THEN 'ods' ELSE keep.kind END,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
    JOIN crawl.movie old_movie ON old_movie.id = dm.old_id
   WHERE keep.id = dm.keep_id;

  UPDATE crawl.movie_show_time mst
     SET movie_id = dm.keep_id,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
   WHERE mst.movie_id = dm.old_id;

  UPDATE crawl.movie_version mv
     SET movie_id = dm.keep_id,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
   WHERE mv.movie_id = dm.old_id;

  UPDATE crawl.movie_comment mc
     SET movie_id = dm.keep_id,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
   WHERE mc.movie_id = dm.old_id;

  UPDATE crawl.movie_rate mr
     SET movie_id = dm.keep_id,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
   WHERE mr.movie_id = dm.old_id;

  UPDATE crawl.re_release rr
     SET movie_id = dm.keep_id,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
   WHERE rr.movie_id = dm.old_id
     AND rr.deleted = 0;

  UPDATE crawl.benefit b
     SET movie_id = dm.keep_id,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
   WHERE b.movie_id = dm.old_id;

  DELETE FROM crawl.movie_tag_tags old_link
  USING tmp_movie_tmdb_promote_map dm
  WHERE old_link.movie_id = dm.old_id
    AND EXISTS (
      SELECT 1 FROM crawl.movie_tag_tags keep_link
      WHERE keep_link.movie_id = dm.keep_id
        AND keep_link.movie_tag_id = old_link.movie_tag_id
    );

  UPDATE crawl.movie_tag_tags link
     SET movie_id = dm.keep_id
    FROM tmp_movie_tmdb_promote_map dm
   WHERE link.movie_id = dm.old_id;

  DELETE FROM crawl.movie_character old_link
  USING tmp_movie_tmdb_promote_map dm
  WHERE old_link.movie_id = dm.old_id
    AND EXISTS (
      SELECT 1 FROM crawl.movie_character keep_link
      WHERE keep_link.movie_id = dm.keep_id
        AND keep_link.character_id = old_link.character_id
    );

  UPDATE crawl.movie_character link
     SET movie_id = dm.keep_id
    FROM tmp_movie_tmdb_promote_map dm
   WHERE link.movie_id = dm.old_id;

  DELETE FROM crawl.movie_staff old_link
  USING tmp_movie_tmdb_promote_map dm
  WHERE old_link.movie_id = dm.old_id
    AND EXISTS (
      SELECT 1 FROM crawl.movie_staff keep_link
      WHERE keep_link.movie_id = dm.keep_id
        AND keep_link.staff_id = old_link.staff_id
        AND keep_link.position_id = old_link.position_id
    );

  UPDATE crawl.movie_staff link
     SET movie_id = dm.keep_id
    FROM tmp_movie_tmdb_promote_map dm
   WHERE link.movie_id = dm.old_id;

  IF to_regclass('crawl.presale') IS NOT NULL THEN
    UPDATE crawl.presale p
       SET deleted = 1,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_tmdb_promote_map dm
     WHERE p.movie_id = dm.old_id
       AND p.deleted = 0
       AND EXISTS (
         SELECT 1 FROM crawl.presale keep_p
         WHERE keep_p.movie_id = dm.keep_id
           AND keep_p.deleted = 0
       );

    UPDATE crawl.presale p
       SET movie_id = dm.keep_id,
           update_time = CURRENT_TIMESTAMP
      FROM tmp_movie_tmdb_promote_map dm
     WHERE p.movie_id = dm.old_id
       AND p.deleted = 0;
  END IF;

  UPDATE crawl.movie old_movie
     SET deleted = 1,
         update_time = CURRENT_TIMESTAMP
    FROM tmp_movie_tmdb_promote_map dm
   WHERE old_movie.id = dm.old_id;
END $$;

SELECT prefer_tmdb_movie_duplicates_for_crawl();

DROP FUNCTION IF EXISTS prefer_tmdb_movie_duplicates_for_crawl();
