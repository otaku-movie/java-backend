-- 清理 GDCS（grand-cinema-sunshine）旧影厅名归一化留下的脏数据。
--
-- 旧逻辑会把 `シアター12IMAX2D` 收成 `シアター12`，把 ULTRA 4DX 写成 `シアター4` / `シアター44`；
-- 现已改为保留 API / floor-guide 原文。本迁移软删仍挂在影院下、仅含纯厅号的 crawl 影厅。
-- crawl / public 两套 schema 同步执行。
--
-- 变更说明：
--   crawl.theater_hall 有 crawl_name（V43），public.theater_hall 仅有 name；
--   按列存在性选择匹配字段，避免 public 侧引用不存在的 crawl_name 导致迁移失败。

DO $$
DECLARE
  s text;
  hall_name_col text;
BEGIN
  FOREACH s IN ARRAY ARRAY['crawl', 'public']
  LOOP
    IF to_regclass(format('%I.theater_hall', s)) IS NULL
       OR to_regclass(format('%I.cinema', s)) IS NULL
    THEN
      CONTINUE;
    END IF;

    SELECT CASE
             WHEN EXISTS (
               SELECT 1 FROM information_schema.columns
                WHERE table_schema = s
                  AND table_name = 'theater_hall'
                  AND column_name = 'crawl_name'
             ) THEN 'crawl_name'
             ELSE 'name'
           END
      INTO hall_name_col;

    EXECUTE format($sql$
      UPDATE %I.theater_hall th
         SET deleted = 1,
             update_time = CURRENT_TIMESTAMP
        FROM %I.cinema c
       WHERE th.cinema_id = c.id
         AND th.deleted = 0
         AND th.%I IS NOT NULL
         AND c.cinema_key LIKE 'grand-cinema-sunshine:%%'
         AND (
           th.%I IN ('シアター44', 'シアター4')
           OR th.%I ~ '^シアター\d+$'
           OR th.%I ~ '^スクリーン\d+$'
         )
    $sql$, s, s, hall_name_col, hall_name_col, hall_name_col, hall_name_col);
  END LOOP;
END $$;
