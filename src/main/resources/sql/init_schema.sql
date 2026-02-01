-- 数据库初始化脚本
-- 如果表不存在则创建表
-- PostgreSQL 15+

-- 设置时区
SET timezone = 'Asia/Tokyo';

-- ============================================
-- 基础表
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    cover VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- 菜单表
CREATE TABLE IF NOT EXISTS menu (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(255),
    icon VARCHAR(255),
    parent_id INTEGER,
    order_num INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 按钮表
CREATE TABLE IF NOT EXISTS button (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS role_menu (
    role_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menu(id) ON DELETE CASCADE
);

-- 角色按钮关联表
CREATE TABLE IF NOT EXISTS role_button (
    role_id INTEGER NOT NULL,
    button_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, button_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (button_id) REFERENCES button(id) ON DELETE CASCADE
);

-- API 表
CREATE TABLE IF NOT EXISTS api (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- ============================================
-- 电影相关表
-- ============================================

-- 电影表
CREATE TABLE IF NOT EXISTS movie (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cover VARCHAR(255),
    description TEXT,
    duration INTEGER,
    release_date DATE,
    rating DECIMAL(3,1),
    status INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 电影版本表
CREATE TABLE IF NOT EXISTS movie_version (
    id SERIAL PRIMARY KEY,
    movie_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE
);

-- 电影规格表
CREATE TABLE IF NOT EXISTS movie_spec (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 电影标签表
CREATE TABLE IF NOT EXISTS movie_tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 电影标签关联表
CREATE TABLE IF NOT EXISTS movie_tag_tags (
    movie_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (movie_id, tag_id),
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES movie_tag(id) ON DELETE CASCADE
);

-- 电影场次表
CREATE TABLE IF NOT EXISTS movie_show_time (
    id SERIAL PRIMARY KEY,
    movie_id INTEGER NOT NULL,
    cinema_id INTEGER NOT NULL,
    theater_hall_id INTEGER NOT NULL,
    open BOOLEAN DEFAULT TRUE,
    start_time VARCHAR(50),
    end_time VARCHAR(50),
    status INTEGER DEFAULT 0,
    subtitle_id INTEGER[],
    show_time_tag_id INTEGER[],
    spec_ids INTEGER[],
    dimension_type INTEGER DEFAULT 1,
    movie_version_id INTEGER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
    FOREIGN KEY (cinema_id) REFERENCES cinema(id) ON DELETE CASCADE,
    FOREIGN KEY (theater_hall_id) REFERENCES theater_hall(id) ON DELETE CASCADE
);

-- 电影场次标签表
CREATE TABLE IF NOT EXISTS movie_show_time_tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 电影评论表
CREATE TABLE IF NOT EXISTS movie_comment (
    id SERIAL PRIMARY KEY,
    movie_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 电影评论回复表
CREATE TABLE IF NOT EXISTS movie_reply (
    id SERIAL PRIMARY KEY,
    comment_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (comment_id) REFERENCES movie_comment(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 电影评论反应表
CREATE TABLE IF NOT EXISTS movie_comment_reaction (
    id SERIAL PRIMARY KEY,
    comment_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    reaction_type INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (comment_id) REFERENCES movie_comment(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 电影评分表
CREATE TABLE IF NOT EXISTS movie_rate (
    id SERIAL PRIMARY KEY,
    movie_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 电影角色表
CREATE TABLE IF NOT EXISTS character (
    id SERIAL PRIMARY KEY,
    cover VARCHAR(255),
    original_name VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 电影角色关联表
CREATE TABLE IF NOT EXISTS movie_character (
    movie_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    PRIMARY KEY (movie_id, character_id),
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
);

-- 电影版本角色关联表
CREATE TABLE IF NOT EXISTS movie_version_character (
    movie_version_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    PRIMARY KEY (movie_version_id, character_id),
    FOREIGN KEY (movie_version_id) REFERENCES movie_version(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
);

-- 职位表
CREATE TABLE IF NOT EXISTS position (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 员工表
CREATE TABLE IF NOT EXISTS staff (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cover VARCHAR(255),
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 电影员工关联表
CREATE TABLE IF NOT EXISTS movie_staff (
    movie_id INTEGER NOT NULL,
    staff_id INTEGER NOT NULL,
    position_id INTEGER NOT NULL,
    PRIMARY KEY (movie_id, staff_id, position_id),
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    FOREIGN KEY (position_id) REFERENCES position(id) ON DELETE CASCADE
);

-- 电影版本角色员工关联表
CREATE TABLE IF NOT EXISTS movie_version_character_staff (
    movie_version_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    staff_id INTEGER NOT NULL,
    PRIMARY KEY (movie_version_id, character_id, staff_id),
    FOREIGN KEY (movie_version_id) REFERENCES movie_version(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE
);

-- 员工角色关联表
CREATE TABLE IF NOT EXISTS staff_character (
    staff_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    PRIMARY KEY (staff_id, character_id),
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
);

-- 重映表
CREATE TABLE IF NOT EXISTS re_release (
    id SERIAL PRIMARY KEY,
    movie_id INTEGER NOT NULL,
    release_date DATE NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE
);

-- ============================================
-- 影院相关表
-- ============================================

-- 品牌表
CREATE TABLE IF NOT EXISTS brand (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    logo VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 地区表
CREATE TABLE IF NOT EXISTS areas (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id INTEGER,
    level INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 影院表
CREATE TABLE IF NOT EXISTS cinema (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    area_id INTEGER,
    brand_id INTEGER,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (brand_id) REFERENCES brand(id) ON DELETE SET NULL
);

-- 影院规格表
CREATE TABLE IF NOT EXISTS cinema_spec (
    cinema_id INTEGER NOT NULL,
    spec_id INTEGER NOT NULL,
    PRIMARY KEY (cinema_id, spec_id),
    FOREIGN KEY (cinema_id) REFERENCES cinema(id) ON DELETE CASCADE,
    FOREIGN KEY (spec_id) REFERENCES movie_spec(id) ON DELETE CASCADE
);

-- 影院票价配置：3D 加价（2D 无加价，用票种价）
-- dimension_type 存 dict_item.code：1=2D, 2=3D（dimensionType 字典）
CREATE TABLE IF NOT EXISTS cinema_price_config (
    id SERIAL PRIMARY KEY,
    cinema_id INTEGER NOT NULL,
    dimension_type INTEGER NOT NULL DEFAULT 1,
    surcharge DECIMAL(10,2) NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (cinema_id) REFERENCES cinema(id) ON DELETE CASCADE,
    UNIQUE(cinema_id, dimension_type)
);
COMMENT ON TABLE cinema_price_config IS '放映类型加价：仅 3D 需配置';

-- 影院规格关联表（多对多）
CREATE TABLE IF NOT EXISTS cinema_spec_spec (
    cinema_spec_id INTEGER NOT NULL,
    spec_id INTEGER NOT NULL,
    PRIMARY KEY (cinema_spec_id, spec_id)
);

-- 影厅表
CREATE TABLE IF NOT EXISTS theater_hall (
    id SERIAL PRIMARY KEY,
    cinema_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    capacity INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (cinema_id) REFERENCES cinema(id) ON DELETE CASCADE
);

-- 座位区域表
CREATE TABLE IF NOT EXISTS seat_area (
    id SERIAL PRIMARY KEY,
    theater_hall_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (theater_hall_id) REFERENCES theater_hall(id) ON DELETE CASCADE
);

-- 座位表
CREATE TABLE IF NOT EXISTS seat (
    id SERIAL PRIMARY KEY,
    theater_hall_id INTEGER NOT NULL,
    row_name VARCHAR(10),
    seat_name VARCHAR(10) NOT NULL,
    x_axis INTEGER,
    y_axis INTEGER,
    z_axis INTEGER,
    seat_area_id INTEGER,
    show BOOLEAN DEFAULT TRUE,
    disabled BOOLEAN DEFAULT FALSE,
    wheel_chair BOOLEAN DEFAULT FALSE,
    seat_position_group VARCHAR(50),
    seat_type INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (theater_hall_id) REFERENCES theater_hall(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_area_id) REFERENCES seat_area(id) ON DELETE SET NULL
);

-- 座位过道表
CREATE TABLE IF NOT EXISTS seat_aisle (
    theater_hall_id INTEGER NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    PRIMARY KEY (theater_hall_id, x, y),
    FOREIGN KEY (theater_hall_id) REFERENCES theater_hall(id) ON DELETE CASCADE
);

-- ============================================
-- 订单和支付相关表
-- ============================================

-- 电影订单表
CREATE TABLE IF NOT EXISTS movie_order (
    id SERIAL PRIMARY KEY,
    movie_show_time_id INTEGER NOT NULL,
    user_id INTEGER,
    order_number VARCHAR(32) UNIQUE NOT NULL,
    order_total DECIMAL(10,2) DEFAULT 0,
    order_state INTEGER DEFAULT 0,
    pay_total DECIMAL(10,2) DEFAULT 0,
    pay_state INTEGER DEFAULT 0,
    pay_time TIMESTAMP,
    pay_method_id INTEGER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (movie_show_time_id) REFERENCES movie_show_time(id) ON DELETE CASCADE
);

-- 退款表
CREATE TABLE IF NOT EXISTS refund (
    id SERIAL PRIMARY KEY,
    order_number VARCHAR(32) NOT NULL,
    user_id INTEGER NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(500),
    apply_status INTEGER DEFAULT 1,
    refund_state INTEGER DEFAULT 1,
    apply_time TIMESTAMP,
    process_time TIMESTAMP,
    processor_id INTEGER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (order_number) REFERENCES movie_order(order_number) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_refund_order_number ON refund(order_number);

COMMENT ON TABLE refund IS '退款表';
COMMENT ON COLUMN refund.id IS '主键';
COMMENT ON COLUMN refund.order_number IS '订单号';
COMMENT ON COLUMN refund.user_id IS '申请人ID';
COMMENT ON COLUMN refund.amount IS '退款金额';
COMMENT ON COLUMN refund.reason IS '退款原因';
COMMENT ON COLUMN refund.apply_status IS '申请状态 1=无申请 2=已申请 3=处理中 4=已同意 5=已拒绝';
COMMENT ON COLUMN refund.refund_state IS '退款状态 1=无退款 2=退款中 3=已退款 4=退款失败';
COMMENT ON COLUMN refund.apply_time IS '申请时间';
COMMENT ON COLUMN refund.process_time IS '处理时间';
COMMENT ON COLUMN refund.processor_id IS '处理人ID';

-- 选座表
CREATE TABLE IF NOT EXISTS select_seat (
    user_id INTEGER NOT NULL,
    movie_order_id INTEGER,
    movie_ticket_type_id INTEGER,
    movie_show_time_id INTEGER NOT NULL,
    theater_hall_id INTEGER NOT NULL,
    x INTEGER,
    y INTEGER,
    select_seat_state INTEGER DEFAULT 0,
    seat_id INTEGER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_order_id) REFERENCES movie_order(id) ON DELETE SET NULL,
    FOREIGN KEY (movie_show_time_id) REFERENCES movie_show_time(id) ON DELETE CASCADE,
    FOREIGN KEY (theater_hall_id) REFERENCES theater_hall(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seat(id) ON DELETE SET NULL
);

-- 支付方式表
CREATE TABLE IF NOT EXISTS payment_method (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    icon VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 电影票类型表
CREATE TABLE IF NOT EXISTS movie_ticket_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 信用卡表（PCI DSS 合规：不存储完整卡号，仅 token + 前6位+后4位）
CREATE TABLE IF NOT EXISTS credit_cards (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    card_token VARCHAR(64) NOT NULL,
    first_six_digits VARCHAR(6) DEFAULT '',
    card_holder_name VARCHAR(100) NOT NULL,
    expiry_date VARCHAR(10) NOT NULL,
    card_type VARCHAR(50) NOT NULL,
    last_four_digits VARCHAR(4) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
COMMENT ON COLUMN credit_cards.card_token IS 'PCI DSS: 卡号令牌，不存储原始 PAN';
COMMENT ON COLUMN credit_cards.first_six_digits IS 'PCI DSS: 前6位 BIN，仅用于显示遮罩';

-- ============================================
-- 促销相关表
-- ============================================

-- 促销表
CREATE TABLE IF NOT EXISTS promotion (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type INTEGER DEFAULT 0,
    discount_value DECIMAL(10,2),
    start_date DATE,
    end_date DATE,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 促销特定日期表
CREATE TABLE IF NOT EXISTS promotion_specific_date (
    promotion_id INTEGER NOT NULL,
    specific_date DATE NOT NULL,
    PRIMARY KEY (promotion_id, specific_date),
    FOREIGN KEY (promotion_id) REFERENCES promotion(id) ON DELETE CASCADE
);

-- 促销月度日期表
CREATE TABLE IF NOT EXISTS promotion_monthly_day (
    promotion_id INTEGER NOT NULL,
    day_of_month INTEGER NOT NULL CHECK (day_of_month >= 1 AND day_of_month <= 31),
    PRIMARY KEY (promotion_id, day_of_month),
    FOREIGN KEY (promotion_id) REFERENCES promotion(id) ON DELETE CASCADE
);

-- 促销周日期表
CREATE TABLE IF NOT EXISTS promotion_weekly_day (
    promotion_id INTEGER NOT NULL,
    day_of_week INTEGER NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6),
    PRIMARY KEY (promotion_id, day_of_week),
    FOREIGN KEY (promotion_id) REFERENCES promotion(id) ON DELETE CASCADE
);

-- 促销时间范围表
CREATE TABLE IF NOT EXISTS promotion_time_range (
    promotion_id INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    PRIMARY KEY (promotion_id, start_time, end_time),
    FOREIGN KEY (promotion_id) REFERENCES promotion(id) ON DELETE CASCADE
);

-- ============================================
-- 其他表
-- ============================================

-- 字典表
CREATE TABLE IF NOT EXISTS dict (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 字典项表
CREATE TABLE IF NOT EXISTS dict_item (
    id SERIAL PRIMARY KEY,
    dict_id INTEGER NOT NULL,
    label VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (dict_id) REFERENCES dict(id) ON DELETE CASCADE
);

-- 语言表
CREATE TABLE IF NOT EXISTS language (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 等级表
CREATE TABLE IF NOT EXISTS level (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    min_points INTEGER DEFAULT 0,
    max_points INTEGER,
    benefits TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 应用版本表
CREATE TABLE IF NOT EXISTS app_version (
    id SERIAL PRIMARY KEY,
    version_code INTEGER NOT NULL,
    version_name VARCHAR(50) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    download_url VARCHAR(500),
    update_message TEXT,
    force_update BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- Hello Movie 表
CREATE TABLE IF NOT EXISTS hello_movie (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    image_url VARCHAR(500),
    link_url VARCHAR(500),
    sort_order INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- ============================================
-- 创建索引
-- ============================================

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted);

-- 电影表索引
CREATE INDEX IF NOT EXISTS idx_movie_status ON movie(status) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_movie_release_date ON movie(release_date) WHERE deleted = 0;

-- 电影场次表索引
CREATE INDEX IF NOT EXISTS idx_movie_show_time_movie_id ON movie_show_time(movie_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_movie_show_time_cinema_id ON movie_show_time(cinema_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_movie_show_time_status ON movie_show_time(status) WHERE deleted = 0;

-- 订单表索引
CREATE INDEX IF NOT EXISTS idx_movie_order_show_time_id ON movie_order(movie_show_time_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_movie_order_state ON movie_order(order_state) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_movie_order_pay_state ON movie_order(pay_state) WHERE deleted = 0;

-- 选座表索引（用于防止重复选座）
CREATE INDEX IF NOT EXISTS idx_select_seat_show_time_hall ON select_seat(movie_show_time_id, theater_hall_id, seat_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_select_seat_user_id ON select_seat(user_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_select_seat_state ON select_seat(select_seat_state) WHERE deleted = 0;
-- 优化 selectSeatList 查询：按场次、影厅、删除状态、选座状态查询选座信息
CREATE INDEX IF NOT EXISTS idx_select_seat_list_query ON select_seat(movie_show_time_id, theater_hall_id, deleted, select_seat_state);

-- 座位表索引
CREATE INDEX IF NOT EXISTS idx_seat_theater_hall_id ON seat(theater_hall_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_seat_area_id ON seat(seat_area_id) WHERE deleted = 0;

-- 信用卡表索引
CREATE INDEX IF NOT EXISTS idx_credit_cards_user_id ON credit_cards(user_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_credit_cards_is_default ON credit_cards(is_default) WHERE deleted = 0;

-- ============================================
-- 创建唯一约束
-- ============================================

-- 选座唯一约束（同一场次同一影厅的同一座位只能被选择一次，排除已删除的记录）
-- 注意：PostgreSQL 支持部分唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_select_seat_unique 
ON select_seat (movie_show_time_id, theater_hall_id, seat_id) 
WHERE deleted = 0 AND select_seat_state IN (0, 1); -- 0: selected, 1: locked

-- 信用卡唯一约束（同一用户只能有一张默认信用卡）
CREATE UNIQUE INDEX IF NOT EXISTS idx_credit_cards_unique_default 
ON credit_cards (user_id, is_default) 
WHERE deleted = 0 AND is_default = TRUE;

-- ============================================
-- 初始化完成
-- ============================================

-- 输出完成信息
DO $$
BEGIN
    RAISE NOTICE '数据库初始化完成！';
END $$;
