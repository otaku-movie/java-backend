-- 清理电影合并后残留的「同一物理场次多条」重复记录。
--
-- 成因：后台合并把输家电影的场次 movie_id 重指到 survivor，但旧逻辑未对 movie_show_time
--       去重；若输家与 survivor 各自持有同一物理场次（同影院+同影厅+同开始时间，合并前因
--       分属两部电影记录而各存一条，售票状态分别停在 on_sale / pre_sale），重指后即并存，
--       前端表现为「同一时间两张卡，一条能点、一条预售灰」。
--
-- 口径：按 (movie_id, cinema_id, theater_hall_id, start_time) 物理键分组，
--       每组保留「可售优先 → 开放优先 → 最近更新优先 → id 最大」的一条，其余软删（deleted=1）。
--       仅处理 deleted=0 且【未来】(start_time >= now) 的行——过去场次不再展示、且数量庞大，
--       全表窗口排序会很慢；只清未来即可消除前端可见的重复。
--       crawl 与 public 两套 schema 同步清理。用列存在性判断兼容较早库结构
--       （缺 can_sale/open 列时退化为按 update_time/id 排序）。

DO $$
DECLARE
  s text;
  has_can_sale boolean;
  has_open boolean;
  order_sql text;
BEGIN
  FOREACH s IN ARRAY ARRAY['crawl', 'public']
  LOOP
    IF to_regclass(format('%I.movie_show_time', s)) IS NOT NULL
       AND EXISTS (
         SELECT 1 FROM information_schema.columns
          WHERE table_schema = s AND table_name = 'movie_show_time'
            AND column_name IN ('id', 'movie_id', 'cinema_id', 'theater_hall_id',
                                'start_time', 'update_time', 'deleted')
          GROUP BY table_schema, table_name
         HAVING count(*) = 7
       )
    THEN
      SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = s AND table_name = 'movie_show_time' AND column_name = 'can_sale'
      ) INTO has_can_sale;
      SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = s AND table_name = 'movie_show_time' AND column_name = 'open'
      ) INTO has_open;

      order_sql :=
        CASE WHEN has_can_sale THEN '(can_sale IS TRUE) DESC, ' ELSE '' END ||
        CASE WHEN has_open     THEN '("open" IS TRUE) DESC, '   ELSE '' END ||
        'update_time DESC NULLS LAST, id DESC';

      EXECUTE format($sql$
        WITH ranked AS (
          SELECT id,
                 row_number() OVER (
                   PARTITION BY movie_id, cinema_id, theater_hall_id, start_time
                   ORDER BY %s
                 ) AS rn
            FROM %I.movie_show_time
           WHERE deleted = 0
             AND start_time::timestamp >= now()
        )
        UPDATE %I.movie_show_time t
           SET deleted = 1, update_time = CURRENT_TIMESTAMP
          FROM ranked r
         WHERE t.id = r.id AND r.rn > 1
      $sql$, order_sql, s, s);
    END IF;
  END LOOP;
END $$;
