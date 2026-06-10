-- ============================================================
-- prod_movie：把 crawl schema 里 17 张共用 public.auto_increment 的表
-- 改成「每表独立序列 + id 从 1 开始」。
--
-- 背景：prod_movie 是从 test_movie 克隆来的，这些表的 id 默认值都是
--   nextval('public.auto_increment')（一个全局共享、未被列拥有的序列），
--   所以 id 是 6 位/7 位高位且跨表连号，TRUNCATE ... RESTART IDENTITY 对其无效。
--
-- 处理分两类：
--   A. 爬虫内容表（movie/character/staff/show_time/... 可重爬）：
--      建独立序列 → TRUNCATE RESTART IDENTITY → 之后用已爬好的 unified JSON 重导，
--      id 自然从 1 开始；内部关联(movie_id 等)由导入器按业务键重建。
--   B. 种子/权限/字典表（api/button/menu/role/position/level/dict/dict_item）：
--      不靠爬虫，在事务内「就地重排 1..N」并把逻辑引用列(role_menu/role_button/
--      user_role/dict_item.dict_id/menu.parent_id/button.menu_id)一起改对。
--      重排用「负值中转再翻正」避免主键瞬时冲突。
--
-- 不动：cinema / brand / areas / theater_hall / cinema_spec / cinema_spec_spec
--   （手动迁移的影院维度，已是独立序列且 1..N）；public.* 镜像（运行时不读）；
--   public.auto_increment 序列保留（crawl.dict_item.code 等仍引用它）。
--
-- 用法：psql -d prod_movie -f prod_per_table_sequences.sql
-- 之后必须重导内容： import:data --only-task=movie 与 --only-task=schedule。
-- ============================================================

BEGIN;

SET search_path TO crawl, public;

-- ---- 1) 17 张表各建独立序列并改 column default --------------------------
DO $$
DECLARE
  t       text;
  seqname text;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'api','button','character','dict','dict_item','level','menu','movie',
    'movie_comment','movie_reply','movie_show_time','movie_ticket_type',
    'position','role','seat','seat_area','staff'
  ]
  LOOP
    seqname := 'crawl.' || t || '_id_seq';
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS %s', seqname);
    EXECUTE format('ALTER SEQUENCE %s OWNED BY crawl.%I.id', seqname, t);
    EXECUTE format('ALTER TABLE crawl.%I ALTER COLUMN id SET DEFAULT nextval(%L)', t, seqname);
  END LOOP;
END $$;

-- ---- 2) 清空可重爬的内容表（id 由重导从 1 开始） ------------------------
-- RESTART IDENTITY 会把上面刚建的「列拥有」序列重置回 1。
-- CASCADE 仅会连带清空引用它们的空表（benefit/presale 等），不触及影院维度表。
TRUNCATE TABLE
  crawl."character",
  crawl.staff,
  crawl.movie_staff,
  crawl.movie_character,
  crawl.movie_spec,
  crawl.movie_version,
  crawl.re_release,
  crawl.movie,
  crawl.movie_show_time,
  crawl.movie_show_time_tag,
  crawl.movie_comment,
  crawl.movie_reply,
  crawl.crawl_movie_master_alias,
  crawl.crawl_movie_master_source_link,
  crawl.crawl_movie_master,
  crawl.crawl_raw_payload
  RESTART IDENTITY CASCADE;

-- 内容序列显式回 1（防御性；RESTART IDENTITY 已重置列拥有序列）。
SELECT setval('crawl.movie_id_seq',            1, false);
SELECT setval('crawl.character_id_seq',        1, false);
SELECT setval('crawl.staff_id_seq',            1, false);
SELECT setval('crawl.movie_show_time_id_seq',  1, false);
SELECT setval('crawl.movie_comment_id_seq',    1, false);
SELECT setval('crawl.movie_reply_id_seq',      1, false);
SELECT setval('crawl.movie_ticket_type_id_seq',1, false);
SELECT setval('crawl.seat_id_seq',             1, false);
SELECT setval('crawl.seat_area_id_seq',        1, false);

-- ---- 3) 种子/权限/字典表就地重排 1..N + 重映射引用列 --------------------
-- 通用手法：先把目标行/引用列设成 -(new_id)（负数与正的旧 id 不冲突），
-- 再统一翻成正数。

-- role：被 role_menu / role_button / user_role 引用
CREATE TEMP TABLE _map_role ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.role;
UPDATE crawl.role_menu   c SET role_id = -m.new_id FROM _map_role m WHERE c.role_id = m.old_id;
UPDATE crawl.role_button c SET role_id = -m.new_id FROM _map_role m WHERE c.role_id = m.old_id;
UPDATE crawl.user_role   c SET role_id = -m.new_id FROM _map_role m WHERE c.role_id = m.old_id;
UPDATE crawl.role        p SET id      = -m.new_id FROM _map_role m WHERE p.id      = m.old_id;
UPDATE crawl.role        SET id      = -id      WHERE id      < 0;
UPDATE crawl.role_menu   SET role_id = -role_id WHERE role_id < 0;
UPDATE crawl.role_button SET role_id = -role_id WHERE role_id < 0;
UPDATE crawl.user_role   SET role_id = -role_id WHERE role_id < 0;
SELECT setval('crawl.role_id_seq', (SELECT MAX(id) FROM crawl.role));

-- menu：被 role_menu / button.menu_id 引用，且 menu.parent_id 自引用
CREATE TEMP TABLE _map_menu ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.menu;
UPDATE crawl.role_menu c SET menu_id   = -m.new_id FROM _map_menu m WHERE c.menu_id   = m.old_id;
UPDATE crawl.button    c SET menu_id   = -m.new_id FROM _map_menu m WHERE c.menu_id   = m.old_id;
UPDATE crawl.menu      c SET parent_id = -m.new_id FROM _map_menu m WHERE c.parent_id = m.old_id;
UPDATE crawl.menu      p SET id        = -m.new_id FROM _map_menu m WHERE p.id        = m.old_id;
UPDATE crawl.menu      SET id        = -id        WHERE id        < 0;
UPDATE crawl.menu      SET parent_id = -parent_id WHERE parent_id < 0;
UPDATE crawl.role_menu SET menu_id   = -menu_id   WHERE menu_id   < 0;
UPDATE crawl.button    SET menu_id   = -menu_id   WHERE menu_id   < 0;
SELECT setval('crawl.menu_id_seq', (SELECT MAX(id) FROM crawl.menu));

-- button：被 role_button 引用
CREATE TEMP TABLE _map_button ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.button;
UPDATE crawl.role_button c SET button_id = -m.new_id FROM _map_button m WHERE c.button_id = m.old_id;
UPDATE crawl.button      p SET id        = -m.new_id FROM _map_button m WHERE p.id        = m.old_id;
UPDATE crawl.button      SET id        = -id        WHERE id        < 0;
UPDATE crawl.role_button SET button_id = -button_id WHERE button_id < 0;
SELECT setval('crawl.button_id_seq', (SELECT MAX(id) FROM crawl.button));

-- api：id 不被任何表按 id 引用（button.api_code 引用的是 api.code，不是 id）
CREATE TEMP TABLE _map_api ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.api;
UPDATE crawl.api p SET id = -m.new_id FROM _map_api m WHERE p.id = m.old_id;
UPDATE crawl.api SET id = -id WHERE id < 0;
SELECT setval('crawl.api_id_seq', (SELECT MAX(id) FROM crawl.api));

-- dict：被 dict_item.dict_id 引用
CREATE TEMP TABLE _map_dict ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.dict;
UPDATE crawl.dict_item c SET dict_id = -m.new_id FROM _map_dict m WHERE c.dict_id = m.old_id;
UPDATE crawl.dict      p SET id      = -m.new_id FROM _map_dict m WHERE p.id      = m.old_id;
UPDATE crawl.dict      SET id      = -id      WHERE id      < 0;
UPDATE crawl.dict_item SET dict_id = -dict_id WHERE dict_id < 0;
SELECT setval('crawl.dict_id_seq', (SELECT MAX(id) FROM crawl.dict));

-- dict_item：id 不被按 id 引用（业务引用的是 dict_item.code）
CREATE TEMP TABLE _map_di ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.dict_item;
UPDATE crawl.dict_item p SET id = -m.new_id FROM _map_di m WHERE p.id = m.old_id;
UPDATE crawl.dict_item SET id = -id WHERE id < 0;
SELECT setval('crawl.dict_item_id_seq', (SELECT MAX(id) FROM crawl.dict_item));

-- level：children(movie.level_id) 已随内容表清空，无需重映射
CREATE TEMP TABLE _map_level ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.level;
UPDATE crawl.level p SET id = -m.new_id FROM _map_level m WHERE p.id = m.old_id;
UPDATE crawl.level SET id = -id WHERE id < 0;
SELECT setval('crawl.level_id_seq', (SELECT MAX(id) FROM crawl.level));

-- position：children(movie_staff.position_id) 已随内容表清空，无需重映射
CREATE TEMP TABLE _map_pos ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.position;
UPDATE crawl.position p SET id = -m.new_id FROM _map_pos m WHERE p.id = m.old_id;
UPDATE crawl.position SET id = -id WHERE id < 0;
SELECT setval('crawl.position_id_seq', (SELECT MAX(id) FROM crawl.position));

COMMIT;
