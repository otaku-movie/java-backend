-- 创建信用卡表
CREATE TABLE IF NOT EXISTS credit_cards (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    card_number VARCHAR(255) NOT NULL COMMENT '信用卡号(加密存储)',
    card_holder_name VARCHAR(100) NOT NULL COMMENT '持卡人姓名',
    expiry_date VARCHAR(10) NOT NULL COMMENT '有效期(MM/YY)',
    cvv VARCHAR(255) NOT NULL COMMENT 'CVV安全码(加密存储)',
    card_type VARCHAR(50) NOT NULL COMMENT '卡类型(Visa, MasterCard, JCB, UnionPay)',
    last_four_digits VARCHAR(4) NOT NULL COMMENT '卡号后四位',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认信用卡',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标志(0-未删除, 1-已删除)',
    
    INDEX idx_user_id (user_id),
    INDEX idx_deleted (deleted),
    INDEX idx_is_default (is_default),
    
    CONSTRAINT fk_credit_cards_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT='信用卡表';

-- 添加唯一约束：同一用户只能有一张默认信用卡
ALTER TABLE credit_cards 
ADD CONSTRAINT unique_default_card 
UNIQUE KEY (user_id, is_default, deleted);
