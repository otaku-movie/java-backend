-- Prepare staging tables for the minimal prod_movie cinema migration.
--
-- Usage:
--   1) Run this on prod_movie after Flyway has created crawl.* tables.
--   2) Import old data into staging.*:
--        sed 's/crawl\./staging./g' cinema_src.sql | psql -d prod_movie
--   3) Run 02_migrate_cinema_from_staging.sql.

\set ON_ERROR_STOP on

DROP SCHEMA IF EXISTS staging CASCADE;
CREATE SCHEMA staging;

-- areas 是固定行政区字典；全新库的 crawl.areas 为空（V12 从空的 public.areas
-- 同步），所以也要迁。它按原 id 迁移，cinema 的 area_id/prefecture_id/region_id
-- 便可直接沿用旧值。
CREATE TABLE staging.areas            (LIKE crawl.areas            INCLUDING DEFAULTS);
CREATE TABLE staging.brand            (LIKE crawl.brand            INCLUDING DEFAULTS);
CREATE TABLE staging.cinema           (LIKE crawl.cinema           INCLUDING DEFAULTS);
CREATE TABLE staging.theater_hall     (LIKE crawl.theater_hall     INCLUDING DEFAULTS);
CREATE TABLE staging.cinema_spec_spec (LIKE crawl.cinema_spec_spec INCLUDING DEFAULTS);

SELECT 'staging tables ready' AS status;
