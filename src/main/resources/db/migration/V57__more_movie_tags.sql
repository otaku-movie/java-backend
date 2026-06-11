-- ============================================================
-- V57: 追加更多电影标签（类型）译名
--
-- V56 只预填了 TMDb 标准 19 个类型，数量偏少。这里补充一批日本影讯/影院常见的
-- 类型 / 题材标签，带中英译名。仍走 crawl schema，与 V55/V56 同口径。
--
-- 幂等：仅当 name 不存在（未软删）时 INSERT，避免与 V56 或爬虫已建标签重复。
-- 爬虫导入类型时按 name get-or-create，会复用这些已带译名的行。
-- ============================================================

SET timezone = 'Asia/Tokyo';

INSERT INTO crawl.movie_tag (name, name_zh, name_en, create_time, update_time, deleted)
SELECT v.ja, v.zh, v.en, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
  ('サスペンス', '悬念', 'Suspense'),
  ('ミュージカル', '音乐剧', 'Musical'),
  ('スポーツ', '体育', 'Sports'),
  ('青春', '青春', 'Coming of Age'),
  ('時代劇', '时代剧', 'Period Drama'),
  ('特撮', '特摄', 'Tokusatsu'),
  ('パニック', '灾难', 'Disaster'),
  ('スパイ', '间谍', 'Spy'),
  ('伝記', '传记', 'Biography'),
  ('政治', '政治', 'Political'),
  ('医療', '医疗', 'Medical'),
  ('グルメ', '美食', 'Gourmet'),
  ('学園', '校园', 'School'),
  ('アイドル', '偶像', 'Idol'),
  ('ホームドラマ', '家庭伦理', 'Family Drama'),
  ('怪獣', '怪兽', 'Kaiju'),
  ('ギャグ', '搞笑', 'Gag'),
  ('ヒューマンドラマ', '人文剧情', 'Human Drama'),
  ('ラブストーリー', '爱情故事', 'Love Story'),
  ('オカルト', '灵异', 'Occult')
) AS v(ja, zh, en)
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.movie_tag mt WHERE mt.name = v.ja AND mt.deleted = 0
);
