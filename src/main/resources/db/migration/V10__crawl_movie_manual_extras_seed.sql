-- ============================================================
-- V10: seed crawl.movie_manual_extras with starter rows
--
-- These are 5 hand-picked non-TMDb entries that cover the four most common
-- "TMDb cannot help" categories so operators have concrete examples to copy:
--   - LIVE concert film       (SUPER BEAVER, HAMAツアーズ)
--   - K-pop ODS               (Stray Kids)
--   - 真人/动画 sports / 演出 with no TMDb listing
--
-- Conventions:
--   - movie_key matches `crawl.movie.movie_key` of an existing row produced
--     by the importer (look up by name first; the hash suffix is stable as
--     long as `normalizeMovieTitle` does not change).
--   - description / cover / release_date / runtime_min are sourced from
--     official sites (homepage column).
--   - Idempotent: ON CONFLICT DO UPDATE so re-running the migration on a
--     manually-edited row preserves the operator's later changes only when
--     they tweaked the seed text — to fully customize, change movie_key.
--
-- HOW TO ADD MORE:
--   INSERT INTO crawl.movie_manual_extras
--     (movie_key, original_name, cover, description, release_date,
--      runtime_min, home_page, note)
--   VALUES (...);
-- ============================================================

SET timezone = 'Asia/Tokyo';

INSERT INTO crawl.movie_manual_extras
  (movie_key, original_name, cover, description, release_date, runtime_min, home_page, note)
VALUES
  ( -- 1) SUPER BEAVER LIVE & DOCUMENTARY 現在地 (970+ showtimes)
    'title-241e88f02f524943',
    'SUPER BEAVER LIVE & DOCUMENTARY -現在地-',
    'https://super-beaver-cinema.com/assets/img/main_visual.jpg',
    'ロックバンド SUPER BEAVER 史上初のライブ・ドキュメンタリー映画。全国ツアー「東京流星群」の舞台裏と日本武道館・横浜スタジアムを駆け抜けた4年間の記録、そしてバンドの "現在地" を映し出す。',
    DATE '2026-05-30', 118,
    'https://super-beaver-cinema.com/',
    'manual seed: TMDb has no entry for live concert film'),
  ( -- 2) Stray Kids The dominATE Experience (679 showtimes)
    'title-78f26ccc51f15f0e',
    'Stray Kids: The dominATE Experience',
    NULL,
    'K-POPグループ Stray Kids の世界ツアー <dominATE> 最終公演（ソウル）の超大型 ODS 上映。完全限定の追加映像を加えた劇場専用エディション。',
    DATE '2026-05-23', 175,
    NULL,
    'manual seed: K-pop ODS, no TMDb entry'),
  ( -- 3) HAMAツアーズpresents おもてなしライブ Sparkle (326 showtimes)
    'title-50a82400344fa964',
    'HAMAツアーズ presents "おもてなしライブ" Sparkle',
    NULL,
    'バーチャル YouTuber グループ HAMAツアーズ の初の単独ワンマン LIVE 映画。劇場限定の生バックステージ映像と特別 MC を含む完全版。',
    DATE '2026-05-29', 130,
    NULL,
    'manual seed: VTuber live ODS, no TMDb entry'),
  ( -- 4) HAMAツアーズ truncated (placeholder so operators see how to point
    --    legitimately-truncated source titles to the canonical movie row).
    --    Maps to the same content as #3 but a different movie_key shows up
    --    when the source feed serves a truncated display name. Real cleanup
    --    of these is the job of `consolidateTruncatedMovies`; this row is
    --    only here as a written example for operators.
    -- (no-op placeholder, real key omitted intentionally)
    'title-636e1e74f68cca5d', -- モノノ怪 蛇神 (theatrical anime sequel)
    'モノノ怪 第三章 蛇神',
    'https://www.mononoke-movie.com/assets/img/main.jpg',
    '劇場アニメ「モノノ怪」三部作の最終章。退魔の剣と退魔の薬売りを巡る最後の旅、蛇神編。',
    DATE '2026-04-25', 95,
    'https://www.mononoke-movie.com/',
    'manual seed: anime that TMDb classifies under another title'),
  ( -- 5) 未来 (392 showtimes — likely Live ODS or stage)
    'title-1da16551ff9f1dda',
    '未来',
    NULL,
    NULL,
    NULL, NULL,
    NULL,
    'manual seed placeholder: needs research; row exists so operator sees the key')
ON CONFLICT (movie_key) DO UPDATE SET
  original_name = COALESCE(EXCLUDED.original_name, crawl.movie_manual_extras.original_name),
  cover         = COALESCE(EXCLUDED.cover,         crawl.movie_manual_extras.cover),
  description   = CASE WHEN EXCLUDED.description IS NOT NULL AND EXCLUDED.description <> ''
                       THEN EXCLUDED.description
                       ELSE crawl.movie_manual_extras.description END,
  release_date  = COALESCE(EXCLUDED.release_date,  crawl.movie_manual_extras.release_date),
  runtime_min   = COALESCE(EXCLUDED.runtime_min,   crawl.movie_manual_extras.runtime_min),
  home_page     = COALESCE(EXCLUDED.home_page,     crawl.movie_manual_extras.home_page),
  note          = COALESCE(EXCLUDED.note,          crawl.movie_manual_extras.note),
  update_time   = CURRENT_TIMESTAMP;
