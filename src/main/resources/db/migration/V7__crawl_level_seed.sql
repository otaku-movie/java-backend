-- ============================================================
-- V7: seed crawl.level with Japan movie certification levels
--
-- The crawler writes crawl.movie.level_id from TMDb JP certifications.
-- Keep these seed rows idempotent so the migration is safe on databases
-- where crawl.level has already been copied or manually populated.
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

INSERT INTO crawl.level (id, name, description, create_time, update_time, deleted)
SELECT v.id, v.name, v.description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (
  VALUES
    (1, 'G',     '小さな子供から大人まで安心して観ることができる内容です。家庭での視聴に最適です。'),
    (2, 'PG-12', '軽い暴力シーンや軽度の感情的な対立を含む可能性がありますが、大部分の子供には適しています。'),
    (3, 'R-15',  '暴力シーンや粗野な言葉、複雑な感情問題などが含まれる可能性があります。15歳以上の視聴者向けです。'),
    (4, 'R-18',  '成人向けの内容が含まれる映画で、極端な暴力や性的なシーンなどが含まれます。')
) AS v(id, name, description)
WHERE NOT EXISTS (
  SELECT 1
  FROM crawl.level l
  WHERE l.id = v.id OR (l.name = v.name AND l.deleted = 0)
);
