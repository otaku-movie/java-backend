-- 数据库初始化脚本
-- 只创建数据库，不创建表结构
-- PostgreSQL 15+

-- 设置时区
SET timezone = 'Asia/Tokyo';

-- 创建数据库（如果不存在）
-- 注意：PostgreSQL 中，数据库通常在容器启动时通过环境变量 POSTGRES_DB 创建
-- 此脚本主要用于确保数据库存在，如果数据库已存在则不会报错

-- 检查并创建数据库（需要在 postgres 数据库中执行）
-- 由于我们在 docker-entrypoint-initdb.d 中执行，此时数据库已经创建
-- 所以这里只需要输出信息即可

DO $$
BEGIN
    -- 检查当前数据库
    IF current_database() = 'test_movie' THEN
        RAISE NOTICE '数据库 test_movie 已存在并已连接';
    ELSE
        RAISE NOTICE '当前数据库: %', current_database();
    END IF;
    
    RAISE NOTICE '数据库初始化完成！';
END $$;
