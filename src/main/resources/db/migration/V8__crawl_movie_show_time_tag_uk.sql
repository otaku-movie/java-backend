-- ============================================================
-- V8: ensure crawl.movie_show_time_tag has a unique key on `name`
--
-- The crawler importer maps decoration tokens stripped from
-- `movie.display_title` (字幕 / 吹替 / 舞台挨拶 / 応援上映 /
--  デジタルリマスター版 / 午前十時の映画祭NN / PG12 ...) into
-- `crawl.movie_show_time_tag` rows and references them from
-- `crawl.movie_show_time.show_time_tag_id INTEGER[]`.
--
-- A partial unique index lets the importer use `ON CONFLICT (name)
-- WHERE deleted = 0 DO NOTHING ... RETURNING id` for idempotent
-- upserts and prevents duplicate tag rows from accumulating across
-- repeated import runs.
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_movie_show_time_tag_name
  ON crawl.movie_show_time_tag (name) WHERE deleted = 0;
