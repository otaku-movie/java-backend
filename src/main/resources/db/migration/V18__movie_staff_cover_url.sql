-- ============================================================
-- V18: add `cover_url` to movie / staff in both schemas.
--
-- Background:
--   `movie.cover` and `staff.cover` historically point at third-party
--   URLs (影院官网 / TMDb / 单片官网). 252/276 covers come from JP
--   theater CDNs with Referer hot-link guards and JP-only latency.
--
-- Strategy:
--   We mirror those images to Cloudflare R2 as WebP (script
--   `npm run mirror:images`) and store the public R2 URL in a new
--   `cover_url` column. The original `cover` (raw third-party URL)
--   stays in place as fallback / audit trail.
--
-- API contract:
--   Front-end reads `cover_url ?? cover` so we get a graceful
--   fallback while mirroring catches up.
-- ============================================================

ALTER TABLE public.movie
  ADD COLUMN IF NOT EXISTS cover_url VARCHAR(512);

ALTER TABLE crawl.movie
  ADD COLUMN IF NOT EXISTS cover_url VARCHAR(512);

ALTER TABLE public.staff
  ADD COLUMN IF NOT EXISTS cover_url VARCHAR(512);

ALTER TABLE crawl.staff
  ADD COLUMN IF NOT EXISTS cover_url VARCHAR(512);

COMMENT ON COLUMN crawl.movie.cover_url IS
  'Cloudflare R2 mirror of `cover` (WebP). NULL = not mirrored yet, front-end falls back to `cover`.';
COMMENT ON COLUMN crawl.staff.cover_url IS
  'Cloudflare R2 mirror of `cover` (WebP). NULL = not mirrored yet.';
