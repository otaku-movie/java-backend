-- ============================================================
-- V51: 预售券主表增加「源站详情链接」字段
--
-- ムビチケ（MOVIE WALKER STORE）爬虫导入的预售券，需要在 C 端详情页
-- 提供「前往官网购买」入口。这里加一列 source_url 存源站商品详情 URL，
-- 由爬虫按代表商品 item_id 写入（https://store.moviewalker.jp/item/detail/{id}）。
-- 后台手动新建的预售券该列可为空。
-- ============================================================

SET timezone = 'Asia/Tokyo';

ALTER TABLE crawl.presale
    ADD COLUMN IF NOT EXISTS source_url VARCHAR(512);

COMMENT ON COLUMN crawl.presale.source_url IS '源站（MOVIE WALKER STORE）商品详情页 URL，用于 C 端跳转官网购买';
