-- ============================================================
-- V45: 给 public / crawl 两个 schema 的业务表补充中文表注释。
--
-- 目的：
--   DB 工具（Navicat / DBeaver 等）的「注释」列此前为空，排查表用途
--   只能靠表名猜。这里集中维护一份表级 COMMENT，供运维 / 后续接手者
--   快速理解每张表的职责。
--
-- 设计：
--   - 用 (表名, 注释) 列表 + 循环，对 public 与 crawl 两个 schema 中
--     “存在的表”才执行 COMMENT；crawl 专属表（crawl_*/presale/
--     movie_manual_extras）在 public 不存在时自动跳过。
--   - COMMENT ON TABLE 是幂等的（重复执行只是覆盖为同一文本），整段
--     迁移可安全重跑。
-- ============================================================

DO $$
DECLARE
  rec   record;
  sch   text;
BEGIN
  FOR rec IN
    SELECT * FROM (VALUES
      ('agreement',                      '用户协议 / 隐私政策（多语言、多版本）'),
      ('api',                            '后台接口级权限点'),
      ('app_version',                    '客户端版本发布与更新检查'),
      ('areas',                          '日本行政区字典（地方 / 都道府县 / 市区町村）'),
      ('auth_provider_config',           '第三方登录（OAuth）提供商配置'),
      ('benefit',                        '观影特典 / 赠品活动'),
      ('benefit_theater_stock',          '特典按影院的库存'),
      ('benefit_user_feedback',          '用户对特典的反馈'),
      ('brand',                          '影院品牌 / 院线'),
      ('button',                         '后台按钮级权限点'),
      ('character',                      '影视角色'),
      ('cinema',                         '影院'),
      ('cinema_price_config',            '影院放映类型加价（如 3D）'),
      ('cinema_price_rules_config',      '影院票价规则配置'),
      ('cinema_spec',                    '影院规格字典（IMAX / 4DX 等）'),
      ('cinema_spec_spec',               '影院与规格关联及加价'),
      ('crawl_alert',                    '爬虫告警'),
      ('crawl_artifact',                 '爬虫 JSON 工件（schedule / info / movies 等）'),
      ('crawl_job',                      '爬虫作业定义'),
      ('crawl_job_run',                  '爬虫作业执行记录'),
      ('crawl_movie_master',             'TMDb 电影主数据'),
      ('crawl_movie_master_alias',       '电影标题别名 → TMDb 映射'),
      ('crawl_movie_master_source_link', '影院来源标题 → TMDb 关联'),
      ('crawl_raw_payload',              '爬虫原始响应留痕'),
      ('crawl_run_log',                  '爬虫批量作业执行留痕'),
      ('credit_cards',                   '用户信用卡（令牌化，PCI DSS 合规）'),
      ('dict',                           '字典'),
      ('dict_item',                      '字典项'),
      ('hello_movie',                    '首页运营位'),
      ('language',                       '语言字典'),
      ('level',                          '映倫分级（G / PG-12 / R-15 / R-18）'),
      ('menu',                           '后台菜单'),
      ('movie',                          '电影'),
      ('movie_character',                '电影与角色关联'),
      ('movie_comment',                  '电影评论'),
      ('movie_comment_like',             '评论点赞'),
      ('movie_comment_reaction',         '评论反应'),
      ('movie_manual_extras',            '电影手工补录（crawl 专用）'),
      ('movie_order',                    '电影订单'),
      ('movie_rate',                     '电影评分'),
      ('movie_reply',                    '评论回复'),
      ('movie_show_time',                '电影场次'),
      ('movie_show_time_tag',            '场次标签（字幕 / 吹替 / 舞台挨拶 等）'),
      ('movie_show_time_ticket_type',    '场次与票种关联'),
      ('movie_spec',                     '电影规格'),
      ('movie_staff',                    '电影与演职人员关联'),
      ('movie_tag',                      '电影标签'),
      ('movie_tag_tags',                 '电影与标签关联'),
      ('movie_ticket_type',              '影院票种与价格'),
      ('movie_version',                  '电影版本（配音 / 字幕版 等）'),
      ('movie_version_character',        '电影版本与角色关联'),
      ('movie_version_character_staff',  '电影版本-角色-演职人员关联'),
      ('payment_methods',                '支付方式'),
      ('position',                       '演职人员职位'),
      ('presale',                        '预售券（crawl 专用）'),
      ('presale_specification',          '预售券规格（crawl 专用）'),
      ('pricing_rule',                   '定价规则'),
      ('promotion',                      '促销活动'),
      ('promotion_monthly_day',          '促销-每月指定日'),
      ('promotion_specific_date',        '促销-指定日期'),
      ('promotion_time_range',           '促销-时间段'),
      ('promotion_weekly_day',           '促销-每周指定日'),
      ('re_release',                     '重映 / 特别上映计划'),
      ('refund',                         '退款'),
      ('rls_audit_log',                  '行级安全（RLS）审计日志'),
      ('role',                           '角色'),
      ('role_button',                    '角色与按钮权限关联'),
      ('role_menu',                      '角色与菜单权限关联'),
      ('rule_conflict_log',              '规则冲突日志'),
      ('seat',                           '座位'),
      ('seat_aisle',                     '座位过道'),
      ('seat_area',                      '座位区域（价格区）'),
      ('select_seat',                    '选座记录'),
      ('staff',                          '演职人员'),
      ('staff_character',                '演职人员与角色关联'),
      ('theater_hall',                   '影厅'),
      ('user_agreement_acceptance',      '用户协议接受记录'),
      ('user_cinema',                    '用户与影院授权（影院级数据权限）'),
      ('user_favorite_cinema',           '用户收藏影院'),
      ('user_oauth_binding',             '用户第三方登录绑定'),
      ('user_role',                      '用户与角色关联'),
      ('users',                          '用户账号')
    ) AS t(tbl, cmt)
  LOOP
    FOREACH sch IN ARRAY ARRAY['public', 'crawl'] LOOP
      IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = sch AND table_name = rec.tbl
      ) THEN
        EXECUTE format('COMMENT ON TABLE %I.%I IS %L', sch, rec.tbl, rec.cmt);
      END IF;
    END LOOP;
  END LOOP;
END $$;
