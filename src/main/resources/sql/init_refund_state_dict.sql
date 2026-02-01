-- 退款状态字典及字典项
-- 新安装使用 init_schema.sql 后执行，或已有库直接执行
-- 注意：dict_item 表需有 name、code 列（若为 label、value 请调整插入列名）

INSERT INTO dict (name, code) 
SELECT '退款状态', 'refund_state' 
WHERE NOT EXISTS (SELECT 1 FROM dict WHERE code = 'refund_state');

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '无退款', 1 FROM dict d WHERE d.code = 'refund_state'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 1);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '退款中', 2 FROM dict d WHERE d.code = 'refund_state'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 2);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '已退款', 3 FROM dict d WHERE d.code = 'refund_state'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 3);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '退款失败', 4 FROM dict d WHERE d.code = 'refund_state'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 4);

-- 退款申请状态字典（用于退款申请、处理流程）
INSERT INTO dict (name, code) 
SELECT '退款申请状态', 'refund_apply_status' 
WHERE NOT EXISTS (SELECT 1 FROM dict WHERE code = 'refund_apply_status');

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '无申请', 1 FROM dict d WHERE d.code = 'refund_apply_status'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 1);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '已申请', 2 FROM dict d WHERE d.code = 'refund_apply_status'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 2);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '处理中', 3 FROM dict d WHERE d.code = 'refund_apply_status'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 3);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '已同意', 4 FROM dict d WHERE d.code = 'refund_apply_status'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 4);

INSERT INTO dict_item (dict_id, name, code)
SELECT d.id, '已拒绝', 5 FROM dict d WHERE d.code = 'refund_apply_status'
AND NOT EXISTS (SELECT 1 FROM dict_item di WHERE di.dict_id = d.id AND di.code = 5);
