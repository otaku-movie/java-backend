-- 后台「合并详情对比」页（movieMerge 下的子详情页，从合并列表点「详情对比」进入）。
--
-- 该页并排展示候选电影的完整信息（海报 / 原名 / 关联计数 / 场次按影院 / staff / 角色），
-- 供人工核对后再合并。合并 / 读取详情接口仍复用 @CheckPermission(code='movie.merge')，
-- 故本迁移只需登记菜单路由并授予已有 movieMerge 的角色，不再新建 api / button。
--
-- 与 V67 同口径：show=false（仅作路由登记，不进左侧导航），业务 datasource 走 crawl schema，
-- 全部 WHERE NOT EXISTS 保证幂等。

-- 1) 菜单路由（movieMerge=35090 下的子页）
INSERT INTO crawl.menu (id, name, show, path, path_name, parent_id, i18n_key, order_num, create_time, update_time)
SELECT 35091, '合并详情', false,
       '/contentManagement/movieManagement/movieMerge/detail',
       'movieMergeDetail', 35090,
       'menu2.contentManagement.children.movieManagement.children.movieMergeDetail', 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM crawl.menu m WHERE m.path_name = 'movieMergeDetail');

-- 2) 凡是已拥有「电影去重合并(movieMerge=35090)」的角色，同步授予详情页访问权限
INSERT INTO crawl.role_menu (role_id, menu_id, create_time, update_time)
SELECT rm.role_id, 35091, now(), now()
FROM crawl.role_menu rm
WHERE rm.menu_id = 35090
  AND NOT EXISTS (
    SELECT 1 FROM crawl.role_menu t
    WHERE t.role_id = rm.role_id AND t.menu_id = 35091
  );
