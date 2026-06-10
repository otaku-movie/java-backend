-- ============================================================
-- V53: users.email 改为可空
--
-- 背景：X（Twitter）OAuth 不返回邮箱，部分 Apple 二次登录也无邮箱。
-- 原 users.email 为 NOT NULL，导致首次用 X 登录创建用户时插入 email=null 触发
-- 非空约束错误（500）。第三方身份的唯一标识由 user_oauth_binding(provider, subject)
-- 保证，email 不再是必填项，这里去掉 NOT NULL 约束。
--
-- public 与 crawl 两个 schema 下都存在 users 表，需同时处理。
-- ============================================================

SET timezone = 'Asia/Tokyo';

ALTER TABLE public.users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE crawl.users ALTER COLUMN email DROP NOT NULL;

COMMENT ON COLUMN public.users.email IS '邮箱；邮箱密码登录用户必填，X 等无邮箱的第三方登录用户可为 null';
COMMENT ON COLUMN crawl.users.email IS '邮箱；邮箱密码登录用户必填，X 等无邮箱的第三方登录用户可为 null';
