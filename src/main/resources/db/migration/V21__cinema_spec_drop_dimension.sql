-- ============================================================
-- V21: cinema_spec 字典里清掉 2D / 3D（spec_id = 1 / 2）
--
-- 背景：
--   `movie_show_time.dimension_type` 已经用 dict_item 表示放映类型
--   （1=2D / 2=3D），与 `cinema_spec` 里的 2D / 3D 条目语义完全重叠。
--   app 的"规格"筛选 + 场次卡片把 2D / 3D 当作普通 spec chip 渲染时，
--   就会和"放映类型"那栏出现重复。
--
--   解决办法：直接从 `cinema_spec` 字典里软删这两个，并把所有
--   `movie_show_time.spec_ids` 数组里的 1 / 2 一并剔除；importer 那边
--   同步移除 seed，下次 import 也不会再把它们写回来。
--
--   两个 schema（crawl 和 public）都要清，因为：
--     - crawl 由 importer 每次 TRUNCATE 重建，本次只是先打扫一下；
--     - public 是历史人工录入数据，长期累积。
--
-- 注意：
--   `spec_ids` 是 INTEGER[]，PG 没有原生"数组减法"，但 `array_remove`
--   每次只能去掉一个值，连用两次即可。
-- ============================================================

-- ---------------- public schema ----------------

-- 1) 从 spec_ids 数组里移除 1 / 2
UPDATE public.movie_show_time
SET spec_ids = array_remove(array_remove(spec_ids, 1), 2),
    update_time = CURRENT_TIMESTAMP
WHERE spec_ids IS NOT NULL
  AND (1 = ANY(spec_ids) OR 2 = ANY(spec_ids));

-- 2) 软删除 cinema_spec 字典里的 2D / 3D
UPDATE public.cinema_spec
SET deleted = 1,
    update_time = CURRENT_TIMESTAMP
WHERE id IN (1, 2)
  AND deleted = 0;

-- ---------------- crawl schema ----------------

UPDATE crawl.movie_show_time
SET spec_ids = array_remove(array_remove(spec_ids, 1), 2),
    update_time = CURRENT_TIMESTAMP
WHERE spec_ids IS NOT NULL
  AND (1 = ANY(spec_ids) OR 2 = ANY(spec_ids));

UPDATE crawl.cinema_spec
SET deleted = 1,
    update_time = CURRENT_TIMESTAMP
WHERE id IN (1, 2)
  AND deleted = 0;

-- 顺手也把 theater_hall.spec_ids 里的 1 / 2 清掉（importer 同样会写入）
UPDATE crawl.theater_hall
SET spec_ids = array_remove(array_remove(spec_ids, 1), 2),
    update_time = CURRENT_TIMESTAMP
WHERE spec_ids IS NOT NULL
  AND (1 = ANY(spec_ids) OR 2 = ANY(spec_ids));
