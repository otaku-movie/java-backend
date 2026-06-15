-- Fix production environments where older menu ids differ from the ids assumed
-- by V67/V73. The movie merge menu must live under the real movieManagement
-- node and should be visible to roles that already have movieList access.

WITH movie_management AS (
    SELECT id
    FROM crawl.menu
    WHERE path_name = 'movieManagement'
    ORDER BY id
    LIMIT 1
),
movie_merge AS (
    SELECT id
    FROM crawl.menu
    WHERE path_name = 'movieMerge'
    ORDER BY id
    LIMIT 1
)
UPDATE crawl.menu m
SET parent_id = mm.id,
    update_time = NOW()
FROM movie_management mm
WHERE m.id = (SELECT id FROM movie_merge)
  AND m.parent_id IS DISTINCT FROM mm.id;

INSERT INTO crawl.role_menu (role_id, menu_id, create_time, update_time)
SELECT rm.role_id, merge_menu.id, NOW(), NOW()
FROM crawl.role_menu rm
JOIN crawl.menu movie_list ON movie_list.id = rm.menu_id
JOIN crawl.menu merge_menu ON merge_menu.path_name = 'movieMerge'
WHERE movie_list.path_name = 'movieList'
  AND NOT EXISTS (
      SELECT 1
      FROM crawl.role_menu existing
      WHERE existing.role_id = rm.role_id
        AND existing.menu_id = merge_menu.id
  );

INSERT INTO crawl.role_menu (role_id, menu_id, create_time, update_time)
SELECT rm.role_id, detail_menu.id, NOW(), NOW()
FROM crawl.role_menu rm
JOIN crawl.menu merge_menu ON merge_menu.id = rm.menu_id
JOIN crawl.menu detail_menu ON detail_menu.path_name = 'movieMergeDetail'
WHERE merge_menu.path_name = 'movieMerge'
  AND NOT EXISTS (
      SELECT 1
      FROM crawl.role_menu existing
      WHERE existing.role_id = rm.role_id
        AND existing.menu_id = detail_menu.id
  );
