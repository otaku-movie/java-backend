-- 为新补充 @CheckPermission 的后台接口登记权限码。
--
-- 背景：AppVersion / Agreement / AuthProvider 等后台接口此前缺少后端权限控制，
-- 现已在 Controller 上补 @SaCheckLogin + @CheckPermission。运行时鉴权查的是
-- button.api_code（经 role_button 绑定角色），api 表只是「权限码目录」。
-- 因此这里要：1) 往 api 目录登记新码；2) 建对应 button；3) 绑定到 system 超管角色，
-- 否则新加的 @CheckPermission 会让所有人（含超管）都 403。
--
-- 业务 datasource 使用 crawl schema，这里直接对 crawl.* 操作（与 V55/V58 同口径）。
-- 全部用 WHERE NOT EXISTS 保证幂等。

-- 1) api 权限码目录
INSERT INTO crawl.api (name, code)
SELECT v.name, v.code
FROM (VALUES
  ('保存应用版本', 'appVersion.save'),
  ('删除应用版本', 'appVersion.remove'),
  ('保存协议',     'agreement.save'),
  ('发布协议',     'agreement.publish'),
  ('删除协议',     'agreement.remove'),
  ('保存登录方式', 'authProvider.save')
) AS v(name, code)
WHERE NOT EXISTS (SELECT 1 FROM crawl.api a WHERE a.code = v.code);

-- 2) button（按菜单 path_name 关联，菜单不存在则跳过，避免 menu_id 外键报错）
--    i18n_key 必须用 button.* 翻译键（前端按钮 label = common(i18nKey)，否则会显示原始码）
INSERT INTO crawl.button (name, menu_id, api_code, i18n_key)
SELECT v.name, m.id, v.api_code, v.i18n_key
FROM (VALUES
  ('保存应用版本', 'appVersionList',   'appVersion.save',   'button.save'),
  ('删除应用版本', 'appVersionList',   'appVersion.remove', 'button.remove'),
  ('保存协议',     'agreementList',    'agreement.save',    'button.save'),
  ('发布协议',     'agreementList',    'agreement.publish', 'button.publish'),
  ('删除协议',     'agreementList',    'agreement.remove',  'button.remove'),
  ('保存登录方式', 'authProviderList', 'authProvider.save', 'button.save')
) AS v(name, menu_path_name, api_code, i18n_key)
JOIN crawl.menu m ON m.path_name = v.menu_path_name
WHERE NOT EXISTS (SELECT 1 FROM crawl.button b WHERE b.api_code = v.api_code);

-- 2.1) 修复存量按钮：i18n_key 被错填成 api_code，导致权限配置弹窗显示原始码而非「保存/删除」
UPDATE crawl.button SET i18n_key = 'button.save'
 WHERE api_code = 'presale.save'   AND i18n_key = 'presale.save';
UPDATE crawl.button SET i18n_key = 'button.remove'
 WHERE api_code = 'presale.remove' AND i18n_key = 'presale.remove';

-- 3) 绑定到 system 超管角色
INSERT INTO crawl.role_button (role_id, button_id)
SELECT r.id, b.id
FROM crawl.role r
JOIN crawl.button b
  ON b.api_code IN (
    'appVersion.save', 'appVersion.remove',
    'agreement.save', 'agreement.publish', 'agreement.remove',
    'authProvider.save'
  )
WHERE r.name = 'system'
  AND NOT EXISTS (
    SELECT 1 FROM crawl.role_button rb
    WHERE rb.role_id = r.id AND rb.button_id = b.id
  );
