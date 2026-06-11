-- ============================================================
-- V62: 修正第三级地区（市区町村）英文罗马字
--
-- V59/V60 的英文名主要由自动罗马字生成，少数地名有固有读法或长音写法，
-- 自动转换会误读。这里按 id + 日文 name 精确修正这些确定错误的英文名；
-- 只更新 name_en，不改中文译名。
-- ============================================================

SET timezone = 'Asia/Tokyo';

UPDATE crawl.areas AS t
SET name_en = v.en
FROM (VALUES
  (94,  '豊島区',   'Toshima Ward'),
  (128, '昭島市',   'Akishima City'),
  (145, '新座市',   'Niiza City'),
  (176, '新潟市',   'Niigata City'),
  (182, '飯田市',   'Iida City'),
  (219, '箕面市',   'Minoh City'),
  (254, '宇多津町', 'Utazu Town'),
  (258, '新居浜市', 'Niihama City'),
  (263, '中間市',   'Nakama City'),
  (266, '飯塚市',   'Iizuka City'),
  (282, '都城市',   'Miyakonojo City'),
  (285, '北谷町',   'Chatan Town'),
  (286, '南風原町', 'Haebaru Town'),
  (312, '豊田市',   'Toyota City')
) AS v(id, ja, en)
WHERE t.id = v.id
  AND t.name = v.ja;
