-- V47: 给字典项补齐「字符串枚举值」(dict_item.description)。
--
-- 背景：dict_item.code 只是数字，前后端若仅靠数字交换语义很脆弱。除
-- crawlTicketStatus（由 crawler importer seed 时已写 description=on_sale 等）外，
-- 其余字典项的 description 一直为空。这里按 (dict.code, dict_item.code) 把后端
-- enumerate 包里各枚举常量的「字符串名」回填进去，使字典真正支持字符串枚举。
--
-- 取值口径：与 com.example.backend.enumerate.* 各枚举的常量名 **逐字一致**，
-- 例如 ShowTimeState{no_started=1,screening=2,ended=3} → cinemaPlayState。
-- 幂等：只在 description 为空时写，重复执行不覆盖人工修订过的值。
--
-- 不涉及 code/数字的任何改动，仅补 description 字符串。

UPDATE crawl.dict_item di
SET description = m.enum_str
FROM (
  VALUES
    -- dimensionType（放映类型；无独立 enum，沿用 2d/3d 习惯值）
    ('dimensionType', '1', '2d'),
    ('dimensionType', '2', '3d'),
    -- dubbingVersion ← DubbingVersionEnum{original=1,dubbed=2}
    ('dubbingVersion', '1', 'original'),
    ('dubbingVersion', '2', 'dubbed'),
    -- cinemaPlayState ← ShowTimeState{no_started=1,screening=2,ended=3}
    ('cinemaPlayState', '1', 'no_started'),
    ('cinemaPlayState', '2', 'screening'),
    ('cinemaPlayState', '3', 'ended'),
    -- presaleMubitikeType ← PresaleMubitikeType{online=1,physical=2}
    ('presaleMubitikeType', '1', 'online'),
    ('presaleMubitikeType', '2', 'physical'),
    -- benefitStockStatus ← BenefitStockStatus{in_stock=1,...,unknown=6}
    ('benefitStockStatus', '1', 'in_stock'),
    ('benefitStockStatus', '2', 'low_stock'),
    ('benefitStockStatus', '3', 'out_of_stock'),
    ('benefitStockStatus', '4', 'reservation_only'),
    ('benefitStockStatus', '5', 'ended'),
    ('benefitStockStatus', '6', 'unknown'),
    -- refundApplyStatus ← RefundApplyStatus{applying=1,...,cancelled=5}
    ('refundApplyStatus', '1', 'applying'),
    ('refundApplyStatus', '2', 'approved'),
    ('refundApplyStatus', '3', 'rejected'),
    ('refundApplyStatus', '4', 'completed'),
    ('refundApplyStatus', '5', 'cancelled'),
    -- ruleTypePriority ← RuleTypePriority{member_day=1,...,default_rule=6}
    ('ruleTypePriority', '1', 'member_day'),
    ('ruleTypePriority', '2', 'weekday'),
    ('ruleTypePriority', '3', 'holiday'),
    ('ruleTypePriority', '4', 'specific_date'),
    ('ruleTypePriority', '5', 'time_range'),
    ('ruleTypePriority', '6', 'default_rule'),
    -- ticketTypeScheduleType ← TicketTypeScheduleType{always=1,...,time_range=4}
    ('ticketTypeScheduleType', '1', 'always'),
    ('ticketTypeScheduleType', '2', 'recurring'),
    ('ticketTypeScheduleType', '3', 'specific_date'),
    ('ticketTypeScheduleType', '4', 'time_range'),
    -- releaseStatus ← MovieReleaseState{not_started=1,showing=2,ended=3}
    ('releaseStatus', '1', 'not_started'),
    ('releaseStatus', '2', 'showing'),
    ('releaseStatus', '3', 'ended'),
    -- refundState ← RefundState{no_refund=1,...,partial_refund=5}
    ('refundState', '1', 'no_refund'),
    ('refundState', '2', 'refunding'),
    ('refundState', '3', 'refunded'),
    ('refundState', '4', 'refund_fail'),
    ('refundState', '5', 'partial_refund'),
    -- payState ← PayState{no_pay=1,paying=2,payed=3,pay_fail=4,refund=5}
    ('payState', '1', 'no_pay'),
    ('payState', '2', 'paying'),
    ('payState', '3', 'payed'),
    ('payState', '4', 'pay_fail'),
    ('payState', '5', 'refund'),
    -- pricingMode ← PricingMode{fixed=1,rule=2}
    ('pricingMode', '1', 'fixed'),
    ('pricingMode', '2', 'rule'),
    -- presaleDiscountMode ← PresaleDiscountModeEnum{rate=1,fixed=2}
    ('presaleDiscountMode', '1', 'rate'),
    ('presaleDiscountMode', '2', 'fixed'),
    -- seatState ← SeatState{optional=1,sold=2,locked=3}
    ('seatState', '1', 'optional'),
    ('seatState', '2', 'sold'),
    ('seatState', '3', 'locked'),
    -- benefitPhaseStatus ← benefitPhaseStatus{not_started=1,in_progress=2,ended=3}
    ('benefitPhaseStatus', '1', 'not_started'),
    ('benefitPhaseStatus', '2', 'in_progress'),
    ('benefitPhaseStatus', '3', 'ended'),
    -- orderState ← OrderState{no_pay=1,payed=2,pay_fail=3,cancel=4,completed=5}
    ('orderState', '1', 'no_pay'),
    ('orderState', '2', 'payed'),
    ('orderState', '3', 'pay_fail'),
    ('orderState', '4', 'cancel'),
    ('orderState', '5', 'completed'),
    -- helloMovie ← Hello{unknown,hello_movie}（按 name 对齐：未知/你好电影）
    ('helloMovie', '1', 'unknown'),
    ('helloMovie', '2', 'hello_movie')
) AS m(dict_code, item_code, enum_str)
JOIN crawl.dict d ON d.code = m.dict_code
WHERE di.dict_id = d.id
  AND di.code = m.item_code::int
  AND (di.description IS NULL OR di.description = '');
