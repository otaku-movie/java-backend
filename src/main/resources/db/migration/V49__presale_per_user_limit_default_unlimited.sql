-- ============================================================
-- V49: 预售券「单人限购」默认改为不限制
--
-- per_user_limit 约定 0 = 不限制。此前建表默认值为 1，且爬虫导入
-- 时把该字段写死成 1，导致所有预售券都显示「单人限购 1」，但实际
-- 并无任何下单/领取逻辑校验该限制（纯展示）。
-- 这里把默认值改为 0，并把存量中沿用旧默认值 1 的记录回填为 0。
-- ============================================================

SET timezone = 'Asia/Tokyo';

ALTER TABLE crawl.presale
    ALTER COLUMN per_user_limit SET DEFAULT 0;

UPDATE crawl.presale
SET per_user_limit = 0
WHERE per_user_limit = 1;
