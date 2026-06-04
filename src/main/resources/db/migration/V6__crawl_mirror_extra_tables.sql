-- ============================================================
-- V6: 把 V5 新登记的 11 张 public 业务表镜像到 crawl schema
--
-- 背景：
--   V3 镜像了 V1 baseline 的全部业务表到 crawl schema，但当时
--   public 里这 11 张表未通过 Flyway 创建，所以 V3 没拷过去。
--   V5 把这些表纳入了 Flyway 管理，本迁移补齐对应的 crawl 镜像，
--   保持 "crawl 镜像 public 全部业务表" 这条约束。
--
-- 与 V3 一致：使用 `LIKE public.X INCLUDING ALL`
--   - 复制列定义、默认值、约束、索引、注释、generated 标识
--   - 不复制外键（FK 留在 public，crawl 侧若需要由后续迁移单独建）
--   - crawler 当前不写这些表，仅保证 schema 结构闭环
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

CREATE TABLE IF NOT EXISTS crawl.auth_provider_config        (LIKE public.auth_provider_config        INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.benefit                     (LIKE public.benefit                     INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.benefit_theater_stock       (LIKE public.benefit_theater_stock       INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.benefit_user_feedback       (LIKE public.benefit_user_feedback       INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.cinema_price_rules_config   (LIKE public.cinema_price_rules_config   INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_comment_like          (LIKE public.movie_comment_like          INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_show_time_ticket_type (LIKE public.movie_show_time_ticket_type INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.payment_methods             (LIKE public.payment_methods             INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.pricing_rule                (LIKE public.pricing_rule                INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.rls_audit_log               (LIKE public.rls_audit_log               INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.rule_conflict_log           (LIKE public.rule_conflict_log           INCLUDING ALL);

DO $$
BEGIN
  RAISE NOTICE 'V6: crawl mirror for V5 extra tables created';
END $$;
