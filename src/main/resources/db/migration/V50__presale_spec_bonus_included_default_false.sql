-- ============================================================
-- V50: 预售券规格「含特典」默认改为 false
--
-- bonus_included 只应在明确有特典内容时为 TRUE。爬虫导入会根据抓到
-- 的特典标题 / 图片 / 说明显式写入该字段；后台手动新建和数据库默认
-- 不应假设「含特典」。
-- ============================================================

SET timezone = 'Asia/Tokyo';

ALTER TABLE crawl.presale_specification
    ALTER COLUMN bonus_included SET DEFAULT FALSE;
