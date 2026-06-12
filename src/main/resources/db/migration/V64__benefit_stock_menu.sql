-- 入场者特典「影院库存」改为独立页面，需在菜单表登记对应路由，
-- 否则前端 processPath('benefitStock') 取不到 path 会退化为 /${lng}undefined。
--
-- 该页面是 benefitList(35017) 的隐藏子页（show=false，不在侧边栏展示），
-- 仅通过阶段列表「影院库存」按钮跳转进入。沿用 crawl schema，全部幂等。
-- 页面内的「分配库存/编辑」按钮复用既有权限码 benefit.stock.save(menu 35017)，无需新增。

-- 1) 菜单路由
INSERT INTO crawl.menu (id, name, show, path, path_name, parent_id, i18n_key, order_num, create_time, update_time)
SELECT 35080, NULL, false,
       '/contentManagement/movieManagement/movieList/benefitList/stockList',
       'benefitStock', 35017,
       'menu2.operationsCenter.children.benefitStock', 2, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM crawl.menu m WHERE m.path_name = 'benefitStock');

-- 2) 凡是已拥有「入场者特典列表(benefitList)」的角色，同步授予该子页访问权限
INSERT INTO crawl.role_menu (role_id, menu_id, create_time, update_time)
SELECT rm.role_id, 35080, now(), now()
FROM crawl.role_menu rm
WHERE rm.menu_id = 35017
  AND NOT EXISTS (
    SELECT 1 FROM crawl.role_menu t
    WHERE t.role_id = rm.role_id AND t.menu_id = 35080
  );
