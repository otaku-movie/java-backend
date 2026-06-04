-- ============================================================
-- V20: crawl 影院名归一化函数 + public→crawl 回填的索引支撑
--
-- 背景：
--   `crawl.cinema` 是 importer 每次 `npm run import:data` 时 TRUNCATE
--   重建的（参见 import-data-pg.ts:resetGeneratedTables），所以爬虫只能
--   给出影院官网暴露的字段；经纬度 / 全地址 / 邮编 / 电话等"补齐"信息
--   长期沉淀在 `public.cinema`（历史人工录入），但用户实际看到的 app
--   走 crawl schema，所以这些字段必须在每次 import 末尾从 public 回填。
--
--   回填的匹配键是「影院名」，但两侧命名风格存在差异：
--     - 全角 / 半角 ASCII 混用（`TOHOシネマズ` vs `ＴＯＨＯシネマズ`）
--     - 空格 / 中点 / 连字符位置不同
--     - 一侧带括号注释（`イオンシネマ 八王子滝山（2026年6月26日オープン！）`）
--
--   把这套归一化逻辑做成 IMMUTABLE 函数，便于：
--     1) importer 里 UPDATE ... WHERE 用同一份归一化
--     2) 后续做模糊查询时可以 CREATE INDEX 在表达式上
--
-- 设计：
--   - translate(): 全角 A-Z a-z 0-9 + 全角空格 → 半角
--   - 去掉所有括号包起来的内容（半角/全角小括号、中括号）
--   - 去掉所有空白
--   - 去掉中点 ・·、斜杠、连字符（含日文长音 / em / en-dash / 波浪线）
--
--   只用于「匹配键」，不会被写回数据列，所以可以放心地把分隔符全清掉。
-- ============================================================

CREATE OR REPLACE FUNCTION crawl.normalize_cinema_name(name TEXT)
RETURNS TEXT
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
AS $$
  SELECT regexp_replace(
           regexp_replace(
             regexp_replace(
               translate(
                 COALESCE(name, ''),
                 'ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ０１２３４５６７８９　',
                 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 '
               ),
               '[\(（\[].*?[\)）\]]', '', 'g'
             ),
             '\s+', '', 'g'
           ),
           '[　・·／/\-‐−–—~〜～]+', '', 'g'
         );
$$;

COMMENT ON FUNCTION crawl.normalize_cinema_name(TEXT)
  IS '影院名归一化（全角→半角 / 去括号注释 / 去所有空白与分隔符），用于跨 schema 模糊匹配';
