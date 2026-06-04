-- ============================================================
-- V12: 同步 public.areas 到 crawl.areas，并修复 crawl.cinema 的地区映射
--
-- 背景：
--   - public.areas 是 3 层日本行政区字典（地方 8 → 都道府县 47 → 市区町村
--     272），共 327 行，name 用标准全称（"東京都"/"大阪府"/"北海道" 等）。
--   - V3 只用 `LIKE public.areas INCLUDING ALL` 镜像了结构，没拷数据，新
--     环境跑完 Flyway 后 crawl.areas 是空的。
--   - 爬虫的 import-data-pg.ts `upsertArea()` 在写 crawl.cinema 时如果
--     找不到 prefecture 就会现造一行 areas（短 name + parent_id=NULL），
--     当前线上残留了 12 行非标 prefecture（id 617-628，name 如"新潟"/
--     "東京"），并使 20 条 cinema 的 prefecture_id 指向这堆孤行。
--
--   本迁移把 public.areas 全量 seed 进 crawl.areas（id 一致，
--   ON CONFLICT DO NOTHING），并把 crawl.cinema 重新映射到 public
--   标准 id，再用 address 前缀 best-effort 给剩余 NULL prefecture_id
--   的 cinema 回填一次。
--
-- 依赖：
--   - public.areas 必须有数据。本仓库的 V1 已经包含该字典，本地 dev
--     和 prod 都满足该前提；若新环境 public.areas 真为空，本脚本只
--     会跳过 seed，不会报错。
--
-- 幂等性：
--   - INSERT ... SELECT ... ON CONFLICT DO NOTHING：重复跑零成本。
--   - UPDATE 都带 WHERE prefecture_id IS NULL / region_id IS NULL，
--     第二次跑只会更新仍是空的行。
--   - 不删除 crawl.areas 中老的 617-628 行（保留作审计 / 排查），
--     因为已无 cinema 再引用它们。
-- ============================================================

SET timezone = 'Asia/Tokyo';
CREATE SCHEMA IF NOT EXISTS crawl;

-- Step 1: 把 public.areas 全量 seed 到 crawl.areas
INSERT INTO crawl.areas (id, name, name_kana, parent_id)
SELECT id, name, name_kana, parent_id
FROM public.areas
ON CONFLICT (id) DO NOTHING;

-- Step 2: 推进 crawl.areas IDENTITY 序列到 max(id) + 1，避免后续 INSERT
-- 不带 id 时冲突。
SELECT setval(
  pg_get_serial_sequence('crawl.areas', 'id'),
  COALESCE((SELECT MAX(id) FROM crawl.areas), 1),
  true
);

-- Step 3: 把 cinema 现有指向 12 行非标 prefecture (parent_id IS NULL
-- 的短 name 行，例如 "新潟" / "東京") 重映射到 public 标准 prefecture id
-- ("新潟県" / "東京都")。LIKE old.name || '%' 在 47 个标准 prefecture
-- 之间无前缀冲突；同时仅匹配 parent_id IN (1..8) 的 prefecture 层。
WITH mapping AS (
  SELECT old.id AS old_id, MIN(std.id) AS new_id
  FROM crawl.areas old
  JOIN crawl.areas std
    ON std.parent_id BETWEEN 1 AND 8
   AND std.name LIKE old.name || '%'
  WHERE old.parent_id IS NULL
    AND old.id NOT BETWEEN 1 AND 8                  -- 排除 8 个地方
  GROUP BY old.id
)
UPDATE crawl.cinema c
SET prefecture_id = m.new_id
FROM mapping m
WHERE c.prefecture_id = m.old_id;

-- Step 4: best-effort 给 prefecture_id IS NULL 的 cinema 补 prefecture
-- 仅看 full_address（设计上的完整地址；当前 address 是同值的简写副本，
-- 不作为独立信号）。匹配方式用 "包含" 而不是 "前缀"，因为部分 full_address
-- 以邮编开头（例：'〒496-0027 愛知県津島市...'）。
--
-- 47 个标准 prefecture name 互不为子串（"東京都" / "京都府" 共享 "京都"
-- 但不互相包含等），所以包含匹配在 prefecture 层不会产生歧义。
WITH addr_match AS (
  SELECT c.id AS cinema_id, MIN(a.id) AS prefecture_id
  FROM crawl.cinema c
  JOIN crawl.areas a
    ON a.parent_id BETWEEN 1 AND 8
   AND c.full_address LIKE '%' || a.name || '%'
  WHERE c.prefecture_id IS NULL
    AND c.deleted = 0
    AND c.full_address IS NOT NULL
    AND c.full_address <> ''
  GROUP BY c.id
)
UPDATE crawl.cinema c
SET prefecture_id = m.prefecture_id
FROM addr_match m
WHERE c.id = m.cinema_id;

-- Step 5: 根据 prefecture_id 反推 region_id（areas.parent_id 即上一层）。
UPDATE crawl.cinema c
SET region_id = a.parent_id
FROM crawl.areas a
WHERE c.prefecture_id = a.id
  AND a.parent_id BETWEEN 1 AND 8
  AND c.region_id IS NULL;
