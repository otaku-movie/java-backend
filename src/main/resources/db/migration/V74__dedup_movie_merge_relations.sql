-- 清理电影合并后可能残留的历史重复关联行。
--
-- 口径与后台合并详情页「合并后结果」一致：
--   * movie_version：同一电影下 (version_code, language_id) 只保留一条
--   * movie_tag_tags：同一电影下同一标签只保留一条
--   * movie_staff：同一电影下同一 staff + position 只保留一条
--   * movie_character：同一电影下同一角色只保留一条
--
-- 业务 datasource 主要使用 crawl schema；public 同步清理，便于本地/历史环境保持一致。
-- 使用列存在性判断，兼容较早库结构；只软删 deleted=0 的重复行。

DO $$
DECLARE
  s text;
BEGIN
  FOREACH s IN ARRAY ARRAY['crawl', 'public']
  LOOP
    IF to_regclass(format('%I.movie_version', s)) IS NOT NULL
       AND EXISTS (
         SELECT 1 FROM information_schema.columns
          WHERE table_schema = s AND table_name = 'movie_version'
            AND column_name IN ('id', 'movie_id', 'version_code', 'language_id', 'deleted')
          GROUP BY table_schema, table_name
         HAVING count(*) = 5
       )
    THEN
      EXECUTE format($sql$
        WITH ranked AS (
          SELECT id,
                 row_number() OVER (
                   PARTITION BY movie_id, version_code, language_id
                   ORDER BY id
                 ) AS rn
            FROM %I.movie_version
           WHERE deleted = 0
        )
        UPDATE %I.movie_version t
           SET deleted = 1
          FROM ranked r
         WHERE t.id = r.id AND r.rn > 1
      $sql$, s, s);
    END IF;

    IF to_regclass(format('%I.movie_tag_tags', s)) IS NOT NULL
       AND EXISTS (
         SELECT 1 FROM information_schema.columns
          WHERE table_schema = s AND table_name = 'movie_tag_tags'
            AND column_name IN ('id', 'movie_id', 'movie_tag_id', 'deleted')
          GROUP BY table_schema, table_name
         HAVING count(*) = 4
       )
    THEN
      EXECUTE format($sql$
        WITH ranked AS (
          SELECT id,
                 row_number() OVER (
                   PARTITION BY movie_id, movie_tag_id
                   ORDER BY id
                 ) AS rn
            FROM %I.movie_tag_tags
           WHERE deleted = 0
        )
        UPDATE %I.movie_tag_tags t
           SET deleted = 1
          FROM ranked r
         WHERE t.id = r.id AND r.rn > 1
      $sql$, s, s);
    END IF;

    IF to_regclass(format('%I.movie_staff', s)) IS NOT NULL
       AND EXISTS (
         SELECT 1 FROM information_schema.columns
          WHERE table_schema = s AND table_name = 'movie_staff'
            AND column_name IN ('movie_id', 'staff_id', 'position_id', 'deleted')
          GROUP BY table_schema, table_name
         HAVING count(*) = 4
       )
    THEN
      EXECUTE format($sql$
        WITH ranked AS (
          SELECT ctid,
                 row_number() OVER (
                   PARTITION BY movie_id, staff_id, position_id
                   ORDER BY ctid
                 ) AS rn
            FROM %I.movie_staff
           WHERE deleted = 0
        )
        UPDATE %I.movie_staff t
           SET deleted = 1
          FROM ranked r
         WHERE t.ctid = r.ctid AND r.rn > 1
      $sql$, s, s);
    END IF;

    IF to_regclass(format('%I.movie_character', s)) IS NOT NULL
       AND EXISTS (
         SELECT 1 FROM information_schema.columns
          WHERE table_schema = s AND table_name = 'movie_character'
            AND column_name IN ('movie_id', 'character_id', 'deleted')
          GROUP BY table_schema, table_name
         HAVING count(*) = 3
       )
    THEN
      EXECUTE format($sql$
        WITH ranked AS (
          SELECT ctid,
                 row_number() OVER (
                   PARTITION BY movie_id, character_id
                   ORDER BY ctid
                 ) AS rn
            FROM %I.movie_character
           WHERE deleted = 0
        )
        UPDATE %I.movie_character t
           SET deleted = 1
          FROM ranked r
         WHERE t.ctid = r.ctid AND r.rn > 1
      $sql$, s, s);
    END IF;
  END LOOP;
END $$;
