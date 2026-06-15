-- 后台新增「电影去重合并」页面（contentManagement/movieManagement 下的同级页）。
--
-- 该页用于把重复 / 脏名 / 被软删的 tmdb 规范行合并成一行：自动按 tmdb_id / 名字
-- 分组，也支持手动搜索任意两部片合并。后端接口 /api/admin/movie/{duplicates,
-- mergeSearch,merge} 统一用 @CheckPermission(code='movie.merge')，因此本迁移要：
--   1) 登记菜单路由（前端 processPath('movieMerge') 取 path）；
--   2) 给已拥有「电影列表(movieList=72)」的角色授予该菜单；
--   3) 往 api 目录登记 movie.merge 权限码；
--   4) 建对应 button 并绑定 system 超管角色，否则 @CheckPermission 会让所有人 403。
--
-- 业务 datasource 走 crawl schema，这里直接操作 crawl.*（与 V63/V64 同口径）。
-- 全部 WHERE NOT EXISTS / ON CONFLICT 保证幂等。

-- 1) 菜单路由（movieManagement=35043 下，排在 movieList 之后）
INSERT INTO crawl.menu (id, name, show, path, path_name, parent_id, i18n_key, order_num, create_time, update_time)
SELECT 35090, '电影去重合并', true,
       '/contentManagement/movieManagement/movieMerge',
       'movieMerge', 35043,
       'menu2.contentManagement.children.movieManagement.children.movieMerge', 3, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM crawl.menu m WHERE m.path_name = 'movieMerge');

-- 2) 凡是已拥有「电影列表(movieList=72)」的角色，同步授予去重合并页访问权限
INSERT INTO crawl.role_menu (role_id, menu_id, create_time, update_time)
SELECT rm.role_id, 35090, now(), now()
FROM crawl.role_menu rm
WHERE rm.menu_id = 72
  AND NOT EXISTS (
    SELECT 1 FROM crawl.role_menu t
    WHERE t.role_id = rm.role_id AND t.menu_id = 35090
  );

-- 3) api 权限码目录
INSERT INTO crawl.api (name, code, create_time, update_time)
SELECT '合并重复电影', 'movie.merge', now(), now()
WHERE NOT EXISTS (SELECT 1 FROM crawl.api a WHERE a.code = 'movie.merge');

-- 4) button（关联到 movieMerge 菜单；i18n_key 用 button.* 翻译键）
INSERT INTO crawl.button (name, menu_id, api_code, i18n_key, create_time, update_time)
SELECT '合并重复电影', m.id, 'movie.merge', 'button.merge', now(), now()
FROM crawl.menu m
WHERE m.path_name = 'movieMerge'
  AND NOT EXISTS (SELECT 1 FROM crawl.button b WHERE b.api_code = 'movie.merge');

-- 5) 绑定到 system 超管角色
INSERT INTO crawl.role_button (role_id, button_id, create_time, update_time)
SELECT r.id, b.id, now(), now()
FROM crawl.role r
JOIN crawl.button b ON b.api_code = 'movie.merge'
WHERE r.name = 'system'
  AND NOT EXISTS (
    SELECT 1 FROM crawl.role_button rb
    WHERE rb.role_id = r.id AND rb.button_id = b.id
  );
