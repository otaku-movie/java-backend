-- ============================================================
-- V17: add `dub_version` to movie_show_time in both schemas.
--
-- Stores `dict[dubbingVersion].code`:
--   1 → オリジナル版 (raw.dub = false)
--   2 → 吹き替え版   (raw.dub = true)
--   NULL → source did not expose the flag
--
-- Mirrors the `dimension_type` pattern (integer column, no FK,
-- value matches dict_item.code so the dict label is a join).
-- ============================================================

ALTER TABLE public.movie_show_time
  ADD COLUMN IF NOT EXISTS dub_version INTEGER;

ALTER TABLE crawl.movie_show_time
  ADD COLUMN IF NOT EXISTS dub_version INTEGER;

COMMENT ON COLUMN crawl.movie_show_time.dub_version IS
  'dict[dubbingVersion].code: 1=オリジナル版, 2=吹き替え版, NULL=unknown';
COMMENT ON COLUMN public.movie_show_time.dub_version IS
  'dict[dubbingVersion].code: 1=オリジナル版, 2=吹き替え版, NULL=unknown';
