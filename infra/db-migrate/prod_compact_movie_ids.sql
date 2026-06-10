-- ============================================================
-- prod_movie：把 crawl.movie 的 id 压成连续 1..N（消除 upsert 烧序列产生的空洞）
-- 同步重映射所有引用 movie.id 的列。负值中转再翻正，避免主键瞬时冲突。
-- 用法：psql -d prod_movie -f prod_compact_movie_ids.sql
-- ============================================================
BEGIN;
SET search_path TO crawl, public;

CREATE TEMP TABLE _map_movie ON COMMIT DROP AS
  SELECT id AS old_id, (row_number() OVER (ORDER BY id))::bigint AS new_id FROM crawl.movie;

-- 1) 所有引用列先置负
UPDATE crawl.benefit         c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.hello_movie     c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_character c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_comment   c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_rate      c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_reply     c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_show_time c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_spec      c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_staff     c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_tag_tags  c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.movie_version   c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.presale         c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;
UPDATE crawl.re_release      c SET movie_id = -m.new_id FROM _map_movie m WHERE c.movie_id = m.old_id;

-- 2) 主键置负
UPDATE crawl.movie p SET id = -m.new_id FROM _map_movie m WHERE p.id = m.old_id;

-- 3) 统一翻正
UPDATE crawl.movie           SET id       = -id       WHERE id       < 0;
UPDATE crawl.benefit         SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.hello_movie     SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_character SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_comment   SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_rate      SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_reply     SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_show_time SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_spec      SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_staff     SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_tag_tags  SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.movie_version   SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.presale         SET movie_id = -movie_id WHERE movie_id < 0;
UPDATE crawl.re_release      SET movie_id = -movie_id WHERE movie_id < 0;

SELECT setval('crawl.movie_id_seq', (SELECT MAX(id) FROM crawl.movie));

COMMIT;
