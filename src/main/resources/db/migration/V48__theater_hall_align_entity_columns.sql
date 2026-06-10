-- ============================================================
-- V48: 把 theater_hall 对齐 TheaterHall 实体。
--
-- 背景：V1 建的 theater_hall 是早期“选座版”（只有 capacity），
-- 而实体 com.example.backend.entity.TheaterHall 实际使用
--   seat_naming_rules / row_count / column_count / seat_count / cinema_spec_id，
-- 且不使用 capacity。这些列在旧 dev 库里是 Flyway 之外（手工/hibernate）
-- 加上的，baseline 没收录，导致全新库（如 prod）建出来的表与实体不一致，
-- 运行期与数据迁移都会缺列。这里补齐并去掉无用的 capacity。
--
-- 幂等：ADD COLUMN IF NOT EXISTS / DROP COLUMN IF EXISTS，对已存在的环境安全。
-- public 与 crawl 两个 schema 都处理，保持一致。
-- ============================================================

DO $$
DECLARE
  sch text;
BEGIN
  FOREACH sch IN ARRAY ARRAY['public', 'crawl'] LOOP
    IF EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = sch AND table_name = 'theater_hall'
    ) THEN
      EXECUTE format('ALTER TABLE %I.theater_hall
        ADD COLUMN IF NOT EXISTS seat_naming_rules VARCHAR(255),
        ADD COLUMN IF NOT EXISTS row_count         INTEGER,
        ADD COLUMN IF NOT EXISTS column_count      INTEGER,
        ADD COLUMN IF NOT EXISTS seat_count        INTEGER,
        ADD COLUMN IF NOT EXISTS cinema_spec_id    INTEGER', sch);

      EXECUTE format('ALTER TABLE %I.theater_hall DROP COLUMN IF EXISTS capacity', sch);
    END IF;
  END LOOP;
END $$;
