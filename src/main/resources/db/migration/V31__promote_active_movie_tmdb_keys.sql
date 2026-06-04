-- 合并 movie 后，确保有效行持有 tmdb-* movie_key。
-- 若 deleted 的旧重复行占着 tmdb key，会导致后续 importer ON CONFLICT 命中 deleted 行。

WITH pairs AS (
  SELECT
    active.id AS active_id,
    deleted_dup.id AS deleted_id,
    'tmdb-' || active.tmdb_id::text AS tmdb_key
  FROM crawl.movie active
  JOIN crawl.movie deleted_dup
    ON deleted_dup.name = active.name
   AND deleted_dup.deleted = 1
   AND deleted_dup.movie_key = 'tmdb-' || active.tmdb_id::text
  WHERE active.deleted = 0
    AND active.tmdb_id IS NOT NULL
    AND active.movie_key IS DISTINCT FROM 'tmdb-' || active.tmdb_id::text
)
UPDATE crawl.movie m
   SET movie_key = m.movie_key || ':merged-' || m.id::text,
       update_time = CURRENT_TIMESTAMP
  FROM pairs p
 WHERE m.id = p.deleted_id;

WITH pairs AS (
  SELECT
    active.id AS active_id,
    'tmdb-' || active.tmdb_id::text AS tmdb_key
  FROM crawl.movie active
  WHERE active.deleted = 0
    AND active.tmdb_id IS NOT NULL
    AND active.movie_key IS DISTINCT FROM 'tmdb-' || active.tmdb_id::text
    AND NOT EXISTS (
      SELECT 1
      FROM crawl.movie other
      WHERE other.movie_key = 'tmdb-' || active.tmdb_id::text
    )
)
UPDATE crawl.movie m
   SET movie_key = p.tmdb_key,
       update_time = CURRENT_TIMESTAMP
  FROM pairs p
 WHERE m.id = p.active_id;
