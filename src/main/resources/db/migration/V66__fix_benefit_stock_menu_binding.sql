-- 修正入场者特典「影院库存」隐藏子页的父菜单和角色授权。
--
-- V64 初版按开发库旧 id 写死 benefitList=35017；生产库实际 benefitList id=38，
-- 导致 benefitStock 创建后 parent_id 错误，且没有同步 role_menu，前端进入隐藏子页会 403。
-- 这里改为按 path_name 查找真实父菜单，避免不同环境菜单 id 不一致。

UPDATE crawl.menu stock
SET parent_id = benefit.id,
    update_time = now()
FROM crawl.menu benefit
WHERE stock.path_name = 'benefitStock'
  AND benefit.path_name = 'benefitList'
  AND stock.parent_id IS DISTINCT FROM benefit.id;

INSERT INTO crawl.role_menu (role_id, menu_id, create_time, update_time)
SELECT rm.role_id, stock.id, now(), now()
FROM crawl.role_menu rm
JOIN crawl.menu benefit ON benefit.id = rm.menu_id
JOIN crawl.menu stock ON stock.path_name = 'benefitStock'
WHERE benefit.path_name = 'benefitList'
  AND NOT EXISTS (
    SELECT 1
    FROM crawl.role_menu existing
    WHERE existing.role_id = rm.role_id
      AND existing.menu_id = stock.id
  );
