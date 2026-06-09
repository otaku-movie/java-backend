-- Preserve admin edits from crawler re-imports.
-- `manual_locked_fields` is a field-level lock list for crawler-owned content rows.
-- `credits_locked` protects movie credits/characters from crawler append-only imports.

SET timezone = 'Asia/Tokyo';

ALTER TABLE crawl.cinema
  ADD COLUMN IF NOT EXISTS manual_locked_fields TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[];

ALTER TABLE crawl.theater_hall
  ADD COLUMN IF NOT EXISTS manual_locked_fields TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[];

ALTER TABLE crawl.movie
  ADD COLUMN IF NOT EXISTS credits_locked BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN crawl.cinema.manual_locked_fields IS
  'Column names manually edited in admin; crawler import must preserve these fields.';

COMMENT ON COLUMN crawl.theater_hall.manual_locked_fields IS
  'Column names manually edited in admin; crawler import must preserve these fields where applicable.';

COMMENT ON COLUMN crawl.movie.credits_locked IS
  'True when admin-managed movie credits/characters should not be appended by crawler imports.';
