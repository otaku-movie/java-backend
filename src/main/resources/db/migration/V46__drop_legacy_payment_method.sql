-- ============================================================
-- V46: 删除遗留的单数表 payment_method。
--
-- 背景：
--   - V1 基线建了 `payment_method`（单数），V3 又把它镜像进 crawl。
--   - 但真正在用的是 `payment_methods`（复数）：实体 PaymentMethod
--     的 @TableName 指向 payment_methods，由 V5/V6 创建并带 14 行种子。
--   - 单数表 payment_method 无实体映射、无外键引用，test_movie 的
--     public 下根本不存在，crawl 下为 0 行，属于历史冗余。
--
-- 处理：
--   两个 schema 都 DROP（IF EXISTS 保证幂等、老环境也安全）。
-- ============================================================

DROP TABLE IF EXISTS public.payment_method;
DROP TABLE IF EXISTS crawl.payment_method;
