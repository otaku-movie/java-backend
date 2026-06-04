-- ============================================================
-- V9: crawl.movie_manual_extras
--
-- For movies that have no matching TMDb entry (concerts / live ODS /
-- 宝塚 / sports public viewings / niche stage / rerun screenings ...),
-- TMDb master never fills cover / description / release_date / runtime
-- and importer fallback can only derive `time` from showtime durations.
--
-- This table is the **single human-maintained surface** for filling those
-- gaps. The importer runs a final UPDATE that COALESCEs values from this
-- table into `crawl.movie` so the manual override survives every reimport
-- (no need to repeatedly hand-patch the DB).
--
-- Operator workflow:
--   1) Look at  SELECT id, name FROM crawl.movie WHERE deleted = 0 AND tmdb_id IS NULL;
--   2) Pick a movie_key from that row (`crawl.movie.movie_key`).
--   3) INSERT a row into this table with the fields you want to set.
--   4) Re-run `npm run import:data` (or just the backfill helper) — values
--      land into `crawl.movie` automatically. Empty / NULL fields here are
--      ignored, so partial overrides are fine.
--
-- The table lives in the `crawl` schema only — public.movie has its own
-- editorial workflow and should not be coupled to the crawler pipeline.
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

CREATE TABLE IF NOT EXISTS crawl.movie_manual_extras (
  movie_key     VARCHAR(160) PRIMARY KEY,
  -- Display name override; importer never overwrites a non-empty
  -- crawl.movie.name from this column unless the existing name is empty.
  name          VARCHAR(255),
  original_name VARCHAR(255),
  cover         VARCHAR(255),
  description   TEXT,
  release_date  DATE,
  -- runtime in minutes, 30..600 sanity range
  runtime_min   SMALLINT CHECK (runtime_min IS NULL OR (runtime_min BETWEEN 30 AND 600)),
  movie_rate    REAL     CHECK (movie_rate IS NULL OR (movie_rate >= 0 AND movie_rate <= 10)),
  -- crawl.level.id (e.g. 1=G, 2=PG-12, 3=R-15, 4=R-18) — must match V7 seeds
  level_id      INTEGER,
  home_page     VARCHAR(255),
  start_date    VARCHAR(255),
  end_date      VARCHAR(255),
  -- Free-form note (why this entry exists, source URL, who maintains it).
  note          TEXT,
  create_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE crawl.movie_manual_extras IS
  'Hand-maintained overrides for crawl.movie fields when TMDb lacks the entry (Live / Stage / Sports / non-movie ODS).';
