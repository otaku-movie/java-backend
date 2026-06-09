-- Keep crawler hall identity separate from admin-facing hall display name.
-- This lets admin rename theater_hall.name without causing the next crawler
-- import to insert a duplicate row under the source-side screen name.

SET timezone = 'Asia/Tokyo';

ALTER TABLE crawl.theater_hall
  ADD COLUMN IF NOT EXISTS crawl_name VARCHAR(255);

UPDATE crawl.theater_hall
SET crawl_name = name
WHERE crawl_name IS NULL
  AND name IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_theater_hall_cinema_crawl_name
  ON crawl.theater_hall (cinema_id, crawl_name)
  WHERE deleted = 0 AND crawl_name IS NOT NULL;

COMMENT ON COLUMN crawl.theater_hall.crawl_name IS
  'Stable source-side hall name used by crawler matching; display name may be edited in admin.';
