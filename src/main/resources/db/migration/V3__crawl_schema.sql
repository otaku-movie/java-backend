-- ============================================================
-- V3: 爬虫入库 schema（crawl）
--
-- 目标：
--   1) 在同一个 test_movie database 内创建 crawl schema；
--   2) 镜像 public 现有业务表，尽量复用 test_movie 结构；
--   3) 仅给爬虫需要的核心表补最少扩展列；
--   4) 工程留痕 / TMDb 主数据表保留 crawl_ 前缀，避免污染业务表名。
--
-- 约束：
--   - 不修改 public schema 的任何表；
--   - 时间字段与 public 保持一致，使用 TIMESTAMP；
--   - 软删除与 public 保持一致，使用 deleted INTEGER DEFAULT 0。
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

-- ----------------------------------------------------------------
-- 1. 镜像 public 业务表
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS crawl.users (LIKE public.users INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.user_oauth_binding (LIKE public.user_oauth_binding INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.role (LIKE public.role INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.user_role (LIKE public.user_role INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.menu (LIKE public.menu INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.button (LIKE public.button INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.role_menu (LIKE public.role_menu INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.role_button (LIKE public.role_button INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.api (LIKE public.api INCLUDING ALL);

CREATE TABLE IF NOT EXISTS crawl.movie (LIKE public.movie INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_version (LIKE public.movie_version INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_spec (LIKE public.movie_spec INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_tag (LIKE public.movie_tag INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_tag_tags (LIKE public.movie_tag_tags INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_show_time (LIKE public.movie_show_time INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_show_time_tag (LIKE public.movie_show_time_tag INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_comment (LIKE public.movie_comment INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_reply (LIKE public.movie_reply INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_comment_reaction (LIKE public.movie_comment_reaction INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_rate (LIKE public.movie_rate INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.character (LIKE public.character INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_character (LIKE public.movie_character INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_version_character (LIKE public.movie_version_character INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.position (LIKE public.position INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.staff (LIKE public.staff INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_staff (LIKE public.movie_staff INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_version_character_staff (LIKE public.movie_version_character_staff INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.staff_character (LIKE public.staff_character INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.re_release (LIKE public.re_release INCLUDING ALL);

CREATE TABLE IF NOT EXISTS crawl.brand (LIKE public.brand INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.areas (LIKE public.areas INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.cinema (LIKE public.cinema INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.user_cinema (LIKE public.user_cinema INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.cinema_spec (LIKE public.cinema_spec INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.cinema_price_config (LIKE public.cinema_price_config INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.cinema_spec_spec (LIKE public.cinema_spec_spec INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.theater_hall (LIKE public.theater_hall INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.seat_area (LIKE public.seat_area INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.seat (LIKE public.seat INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.seat_aisle (LIKE public.seat_aisle INCLUDING ALL);

CREATE TABLE IF NOT EXISTS crawl.movie_order (LIKE public.movie_order INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.refund (LIKE public.refund INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.select_seat (LIKE public.select_seat INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.payment_method (LIKE public.payment_method INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.movie_ticket_type (LIKE public.movie_ticket_type INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.credit_cards (LIKE public.credit_cards INCLUDING ALL);

CREATE TABLE IF NOT EXISTS crawl.promotion (LIKE public.promotion INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.promotion_specific_date (LIKE public.promotion_specific_date INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.promotion_monthly_day (LIKE public.promotion_monthly_day INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.promotion_weekly_day (LIKE public.promotion_weekly_day INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.promotion_time_range (LIKE public.promotion_time_range INCLUDING ALL);

CREATE TABLE IF NOT EXISTS crawl.dict (LIKE public.dict INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.dict_item (LIKE public.dict_item INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.language (LIKE public.language INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.level (LIKE public.level INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.app_version (LIKE public.app_version INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.hello_movie (LIKE public.hello_movie INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.agreement (LIKE public.agreement INCLUDING ALL);
CREATE TABLE IF NOT EXISTS crawl.user_agreement_acceptance (LIKE public.user_agreement_acceptance INCLUDING ALL);

-- ----------------------------------------------------------------
-- 2. 核心业务镜像表扩展列（只加 crawl 所需，不动 public）
-- ----------------------------------------------------------------
ALTER TABLE crawl.cinema
  ADD COLUMN IF NOT EXISTS cinema_key VARCHAR(96),
  ADD COLUMN IF NOT EXISTS crawl_raw_json JSONB,
  ADD COLUMN IF NOT EXISTS crawl_fetched_at TIMESTAMP;

ALTER TABLE crawl.movie
  ADD COLUMN IF NOT EXISTS movie_key VARCHAR(160),
  ADD COLUMN IF NOT EXISTS tmdb_id INTEGER,
  ADD COLUMN IF NOT EXISTS imdb_id VARCHAR(16),
  ADD COLUMN IF NOT EXISTS crawl_raw_json JSONB,
  ADD COLUMN IF NOT EXISTS crawl_fetched_at TIMESTAMP;

ALTER TABLE crawl.theater_hall
  ADD COLUMN IF NOT EXISTS spec_ids INTEGER[],
  ADD COLUMN IF NOT EXISTS crawl_raw_json JSONB,
  ADD COLUMN IF NOT EXISTS crawl_fetched_at TIMESTAMP;

ALTER TABLE crawl.movie_show_time
  ADD COLUMN IF NOT EXISTS showtime_key VARCHAR(255),
  ADD COLUMN IF NOT EXISTS reservation_params_json JSONB,
  ADD COLUMN IF NOT EXISTS crawl_raw_json JSONB,
  ADD COLUMN IF NOT EXISTS crawl_fetched_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_cinema_key
  ON crawl.cinema (cinema_key) WHERE cinema_key IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_movie_key
  ON crawl.movie (movie_key) WHERE movie_key IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_theater_hall_cinema_name
  ON crawl.theater_hall (cinema_id, name) WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_showtime_key
  ON crawl.movie_show_time (showtime_key) WHERE showtime_key IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_cinema_spec_spec
  ON crawl.cinema_spec_spec (cinema_id, spec_id) WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_crawl_movie_tmdb
  ON crawl.movie (tmdb_id) WHERE tmdb_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_crawl_showtime_movie
  ON crawl.movie_show_time (movie_id) WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_crawl_showtime_cinema
  ON crawl.movie_show_time (cinema_id) WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_crawl_showtime_start
  ON crawl.movie_show_time (start_time) WHERE deleted = 0;

-- ----------------------------------------------------------------
-- 3. 工程留痕与 TMDb 主数据表（public 中不存在）
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS crawl.crawl_job (
  id            INTEGER       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  source_code   VARCHAR(32)   NOT NULL,
  endpoint_kind VARCHAR(32)   NOT NULL,
  name          VARCHAR(64)   NOT NULL,
  cron_expr     VARCHAR(64)   NOT NULL,
  params_json   JSONB,
  priority      INTEGER       NOT NULL DEFAULT 0,
  enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
  create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
  update_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
  deleted       INTEGER       DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crawl.crawl_job_run (
  id                BIGINT        GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  job_id            INTEGER       NOT NULL REFERENCES crawl.crawl_job(id),
  attempt           INTEGER       NOT NULL DEFAULT 1,
  retry_of_run_id   BIGINT        REFERENCES crawl.crawl_job_run(id),
  status            SMALLINT      NOT NULL,
  started_at        TIMESTAMP,
  finished_at       TIMESTAMP,
  http_status       INTEGER,
  items_fetched     INTEGER       NOT NULL DEFAULT 0,
  items_inserted    INTEGER       NOT NULL DEFAULT 0,
  items_updated     INTEGER       NOT NULL DEFAULT 0,
  items_skipped     INTEGER       NOT NULL DEFAULT 0,
  error_code        VARCHAR(64),
  error_msg         VARCHAR(1024),
  create_time       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
  update_time       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crawl.crawl_raw_payload (
  id                    BIGINT        GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  job_run_id            BIGINT        REFERENCES crawl.crawl_job_run(id),
  source_code           VARCHAR(32)   NOT NULL,
  endpoint_kind         VARCHAR(32)   NOT NULL,
  request_url           VARCHAR(1024) NOT NULL,
  request_method        VARCHAR(8)    NOT NULL DEFAULT 'GET',
  response_status       INTEGER,
  content_hash          CHAR(64)      NOT NULL,
  storage_kind          VARCHAR(16)   NOT NULL DEFAULT 'inline',
  storage_uri           VARCHAR(512),
  body_inline           TEXT,
  bytes                 INTEGER,
  fetched_at            TIMESTAMP     NOT NULL,
  create_time           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crawl.crawl_alert (
  id              BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  job_run_id      BIGINT       REFERENCES crawl.crawl_job_run(id),
  level           VARCHAR(16)  NOT NULL,
  code            VARCHAR(64)  NOT NULL,
  message         TEXT         NOT NULL,
  context_json    JSONB,
  resolved        BOOLEAN      NOT NULL DEFAULT FALSE,
  resolved_at     TIMESTAMP,
  create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crawl.crawl_movie_master (
  tmdb_id            INTEGER       PRIMARY KEY,
  imdb_id            VARCHAR(16),
  title_ja           VARCHAR(255)  NOT NULL,
  title_en           VARCHAR(255),
  original_title     VARCHAR(255),
  release_date_jp    DATE,
  runtime_min        SMALLINT,
  poster_url         VARCHAR(512),
  backdrop_url       VARCHAR(512),
  genres_json        JSONB         NOT NULL DEFAULT '[]'::jsonb,
  country            VARCHAR(8),
  original_language  VARCHAR(8),
  akas_json          JSONB         NOT NULL DEFAULT '[]'::jsonb,
  external_ids_json  JSONB         NOT NULL DEFAULT '{}'::jsonb,
  overview           TEXT,
  popularity         NUMERIC(10,4),
  vote_average       NUMERIC(4,2),
  vote_count         INTEGER,
  raw_json           JSONB         NOT NULL,
  tmdb_fetched_at    TIMESTAMP,
  create_time        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
  update_time        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crawl.crawl_movie_master_source_link (
  id                 BIGINT         GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  tmdb_id            INTEGER        NOT NULL REFERENCES crawl.crawl_movie_master(tmdb_id) ON DELETE CASCADE,
  brand_code         VARCHAR(32)    NOT NULL,
  cinema_external_id VARCHAR(64),
  source_movie_id    VARCHAR(64),
  display_title_seen VARCHAR(255)   NOT NULL,
  match_method       VARCHAR(32)    NOT NULL,
  confidence         NUMERIC(3,2)   NOT NULL DEFAULT 1.00,
  first_seen_at      TIMESTAMP,
  last_seen_at       TIMESTAMP,
  create_time        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crawl.crawl_movie_master_alias (
  normalized_title   VARCHAR(255)   PRIMARY KEY,
  tmdb_id            INTEGER        NOT NULL REFERENCES crawl.crawl_movie_master(tmdb_id) ON DELETE CASCADE,
  source             VARCHAR(32)    NOT NULL DEFAULT 'tmdb',
  create_time        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_crawl_job_run_job_started
  ON crawl.crawl_job_run (job_id, started_at);
CREATE INDEX IF NOT EXISTS idx_crawl_raw_payload_run
  ON crawl.crawl_raw_payload (job_run_id);
CREATE INDEX IF NOT EXISTS idx_crawl_alert_unresolved
  ON crawl.crawl_alert (resolved, level) WHERE resolved = FALSE;
CREATE INDEX IF NOT EXISTS idx_crawl_movie_master_imdb
  ON crawl.crawl_movie_master (imdb_id) WHERE imdb_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_crawl_movie_master_link_tmdb
  ON crawl.crawl_movie_master_source_link (tmdb_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_crawl_movie_master_source_link
  ON crawl.crawl_movie_master_source_link (brand_code, COALESCE(cinema_external_id, ''), COALESCE(source_movie_id, ''));

-- ----------------------------------------------------------------
-- 4. 基础 seed 数据：品牌、场次状态、规格
-- ----------------------------------------------------------------
INSERT INTO crawl.brand (name, create_time, update_time, deleted)
SELECT v.name, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
  ('TOHOシネマズ'),
  ('イオンシネマ'),
  ('グランドシネマサンシャイン'),
  ('T・ジョイ'),
  ('109シネマズ'),
  ('ユナイテッド・シネマ'),
  ('MOVIX'),
  ('HUMAXシネマズ')
) AS v(name)
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.brand b WHERE b.name = v.name AND b.deleted = 0
);

-- 注意：1=2D / 2=3D 已在 V21 迁移里从字典移除——dimensionType（dict）已经承担
-- 2D / 3D 的语义，这里不再 seed 以免规格 chip 重复。
-- id 从 3 起跳，保持与历史 spec_ids 数组的兼容。
INSERT INTO crawl.cinema_spec (id, name, description, create_time, update_time, deleted)
SELECT v.id, v.name, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
  (3, 'IMAX'), (4, 'IMAX GT'), (5, 'IMAX レーザー'),
  (6, '4DX'), (7, 'ULTRA 4DX'), (8, 'MX4D'), (9, 'Screen X'),
  (10, 'Dolby Cinema'), (11, 'Dolby Atmos'), (12, 'TCX'),
  (13, '轟音'), (14, 'BESTIA'), (15, 'PREMIUM')
) AS v(id, name)
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.cinema_spec s WHERE s.id = v.id OR (s.name = v.name AND s.deleted = 0)
);

INSERT INTO crawl.dict (name, code)
SELECT '爬虫场次售卖状态', 'crawlTicketStatus'
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.dict d WHERE d.code = 'crawlTicketStatus'
);

INSERT INTO crawl.dict_item (dict_id, name, description, code)
SELECT d.id, v.label, v.value, v.status_code
FROM crawl.dict d
JOIN (VALUES
  ('未知', 'unknown', 0),
  ('可售', 'on_sale', 1),
  ('余票紧张', 'few', 2),
  ('已售罄', 'sold_out', 3),
  ('预售前', 'pre_sale', 4),
  ('销售结束', 'sale_ended', 5),
  ('已关闭', 'closed', 6)
) AS v(label, value, status_code) ON TRUE
WHERE d.code = 'crawlTicketStatus'
  AND NOT EXISTS (
    SELECT 1
    FROM crawl.dict_item di
    WHERE di.dict_id = d.id
      AND di.description = v.value
  );

DO $$
BEGIN
  RAISE NOTICE 'crawl schema initialized';
END $$;
