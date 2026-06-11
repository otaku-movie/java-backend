-- ============================================================
-- V58: 地区（areas）表增加「中文名 / 英文名」译名列
--
-- areas 是日本三级行政区字典（地方 8 → 都道府県 47 → 市区町村 272），原本只存日文 name。
-- C 端地区筛选需要「日文 + 翻译语言」并列显示（当前语言为日语时只显示日文）。这里给
-- areas 加 name_zh / name_en，并预填地方、都道府県两级译名；市区町村暂不预填，译名为空时
-- C 端只显示日文（回退）。
--
-- 接口仍返回原始日文 name（自动定位匹配依赖它），译名作为独立字段 name_zh/name_en 返回，
-- 由 App 端按当前语言并列拼接。业务 datasource 使用 crawl schema，这里直接对 crawl.* 加列。
-- ============================================================

SET timezone = 'Asia/Tokyo';

ALTER TABLE crawl.areas
    ADD COLUMN IF NOT EXISTS name_zh VARCHAR(50),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(50);

COMMENT ON COLUMN crawl.areas.name_zh IS '地区中文名；为空时 C 端只显示日文 name';
COMMENT ON COLUMN crawl.areas.name_en IS '地区英文名；为空时 C 端只显示日文 name';

-- ------------------------------------------------------------
-- 地方（8 个）译名
-- ------------------------------------------------------------
UPDATE crawl.areas AS t
SET name_zh = v.zh, name_en = v.en
FROM (VALUES
  ('北海道地方', '北海道地区', 'Hokkaido'),
  ('東北地方', '东北地区', 'Tohoku'),
  ('関東地方', '关东地区', 'Kanto'),
  ('中部地方', '中部地区', 'Chubu'),
  ('近畿地方', '近畿地区', 'Kinki'),
  ('中国地方', '中国地区', 'Chugoku'),
  ('四国地方', '四国地区', 'Shikoku'),
  ('九州・沖縄地方', '九州·冲绳地区', 'Kyushu-Okinawa')
) AS v(ja, zh, en)
WHERE t.name = v.ja
  AND (t.name_zh IS NULL OR t.name_zh = '');

-- ------------------------------------------------------------
-- 都道府県（47 个）译名
-- ------------------------------------------------------------
UPDATE crawl.areas AS t
SET name_zh = v.zh, name_en = v.en
FROM (VALUES
  ('北海道', '北海道', 'Hokkaido'),
  ('青森県', '青森县', 'Aomori'),
  ('岩手県', '岩手县', 'Iwate'),
  ('宮城県', '宫城县', 'Miyagi'),
  ('秋田県', '秋田县', 'Akita'),
  ('山形県', '山形县', 'Yamagata'),
  ('福島県', '福岛县', 'Fukushima'),
  ('茨城県', '茨城县', 'Ibaraki'),
  ('栃木県', '枥木县', 'Tochigi'),
  ('群馬県', '群马县', 'Gunma'),
  ('埼玉県', '埼玉县', 'Saitama'),
  ('千葉県', '千叶县', 'Chiba'),
  ('東京都', '东京都', 'Tokyo'),
  ('神奈川県', '神奈川县', 'Kanagawa'),
  ('新潟県', '新潟县', 'Niigata'),
  ('富山県', '富山县', 'Toyama'),
  ('石川県', '石川县', 'Ishikawa'),
  ('福井県', '福井县', 'Fukui'),
  ('山梨県', '山梨县', 'Yamanashi'),
  ('長野県', '长野县', 'Nagano'),
  ('岐阜県', '岐阜县', 'Gifu'),
  ('静岡県', '静冈县', 'Shizuoka'),
  ('愛知県', '爱知县', 'Aichi'),
  ('三重県', '三重县', 'Mie'),
  ('滋賀県', '滋贺县', 'Shiga'),
  ('京都府', '京都府', 'Kyoto'),
  ('大阪府', '大阪府', 'Osaka'),
  ('兵庫県', '兵库县', 'Hyogo'),
  ('奈良県', '奈良县', 'Nara'),
  ('和歌山県', '和歌山县', 'Wakayama'),
  ('鳥取県', '鸟取县', 'Tottori'),
  ('島根県', '岛根县', 'Shimane'),
  ('岡山県', '冈山县', 'Okayama'),
  ('広島県', '广岛县', 'Hiroshima'),
  ('山口県', '山口县', 'Yamaguchi'),
  ('徳島県', '德岛县', 'Tokushima'),
  ('香川県', '香川县', 'Kagawa'),
  ('愛媛県', '爱媛县', 'Ehime'),
  ('高知県', '高知县', 'Kochi'),
  ('福岡県', '福冈县', 'Fukuoka'),
  ('佐賀県', '佐贺县', 'Saga'),
  ('長崎県', '长崎县', 'Nagasaki'),
  ('熊本県', '熊本县', 'Kumamoto'),
  ('大分県', '大分县', 'Oita'),
  ('宮崎県', '宫崎县', 'Miyazaki'),
  ('鹿児島県', '鹿儿岛县', 'Kagoshima'),
  ('沖縄県', '冲绳县', 'Okinawa')
) AS v(ja, zh, en)
WHERE t.name = v.ja
  AND (t.name_zh IS NULL OR t.name_zh = '');
