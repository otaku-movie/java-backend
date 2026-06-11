-- ============================================================
-- V61: 修正第三级地区（市区町村）中文译名
--
-- V60 的自动简体化遗漏了几个日文新字体（並/児/覇/雲）与含假名（の/ヶ）的写法，
-- 导致少数中文译名不正确。这里按 id + 日文 name 精确修正；顺带修正其中两行明显
-- 错误的英文罗马字（鹿児島市 / 鶴ヶ島市）。允许覆盖既有译名。
-- ============================================================

SET timezone = 'Asia/Tokyo';

UPDATE crawl.areas AS t
SET name_zh = v.zh, name_en = v.en
FROM (VALUES
  (93,  '杉並区',     '杉并区',     'Suginami Ward'),
  (283, '鹿児島市',   '鹿儿岛市',   'Kagoshima City'),
  (284, '那覇市',     '那霸市',     'Naha City'),
  (289, '日の出町',   '日之出町',   'Hinode Town'),
  (290, '茅ヶ崎市',   '茅崎市',     'Chigasaki City'),
  (293, '鶴ヶ島市',   '鹤岛市',     'Tsurugashima City'),
  (338, '出雲市',     '出云市',     'Izumo City')
) AS v(id, ja, zh, en)
WHERE t.id = v.id
  AND t.name = v.ja;
