-- 放映类型字典（2D、3D）
-- 票价 = 票种基础价(movie_ticket_type) + 3D加价(若有) + 规格加价(cinema_spec_spec.plus_price)

INSERT INTO dict (name, code)
SELECT '放映类型', 'dimensionType'
WHERE NOT EXISTS (SELECT 1 FROM dict WHERE code = 'dimensionType');

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '2D', 1 FROM dict d WHERE d.code = 'dimensionType'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 1);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '3D', 2 FROM dict d WHERE d.code = 'dimensionType'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 2);
