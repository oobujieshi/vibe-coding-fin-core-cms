-- =====================================================
-- FinCoreCms 财务管理模块 - 全部 DDL
-- 共 17 张表
-- 执行方式（避免中文乱码）：
--   docker cp 02-create-tables.sql fincore-mysql:/tmp/
--   docker exec fincore-mysql mysql -uroot -proot123456 --default-character-set=utf8mb4 fincore -e "source /tmp/02-create-tables.sql"
-- 注意：不要用 PowerShell Get-Content | docker exec -i，会损毁中文
-- =====================================================

-- ==================== 通用域 ====================

CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1 COMMENT '1启用/2禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS t_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(32) NOT NULL UNIQUE COMMENT 'ADMIN/FINANCE/BIZ',
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS t_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS t_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_code VARCHAR(64) NOT NULL UNIQUE,
    perm_name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    perm_type TINYINT NOT NULL COMMENT '1菜单/2按钮/3接口',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS t_role_permission (
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    request_params TEXT,
    response_result TEXT,
    client_ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    cost_time BIGINT COMMENT '耗时ms',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_operation (operation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- ==================== 订单结算域 ====================

CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100),
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    settled_amount DECIMAL(18,2) DEFAULT 0.00,
    order_status TINYINT DEFAULT 1 COMMENT '1待结算/2已结算/3已关闭',
    contract_no VARCHAR(64),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_no (order_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (order_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fee_rule_id BIGINT,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS t_settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_no VARCHAR(32) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    calculated_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fee_detail JSON,
    settlement_status TINYINT DEFAULT 0 COMMENT '0草稿/1已确认/2已作废',
    settled_by BIGINT,
    settled_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_settlement_no (settlement_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算单表';

CREATE TABLE IF NOT EXISTS t_fee_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    rule_type TINYINT NOT NULL COMMENT '1合同条款/2阶梯费率',
    rule_config JSON NOT NULL,
    effective_date DATE,
    expire_date DATE,
    is_active TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费率规则表';

-- ==================== 收付款域 ====================

CREATE TABLE IF NOT EXISTS t_receipt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_no VARCHAR(32) NOT NULL UNIQUE,
    payer_name VARCHAR(100),
    amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    order_id BIGINT,
    bill_id BIGINT,
    receipt_method TINYINT COMMENT '1银行转账/2扫码支付/3现金',
    receipt_status TINYINT DEFAULT 1 COMMENT '1待确认/2已到账/3已核销',
    confirmed_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_receipt_no (receipt_no),
    INDEX idx_order_id (order_id),
    INDEX idx_status (receipt_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款记录表';

CREATE TABLE IF NOT EXISTS t_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(32) NOT NULL UNIQUE,
    payee_name VARCHAR(100),
    amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    supplier_id BIGINT,
    order_id BIGINT,
    payment_status TINYINT DEFAULT 1 COMMENT '1待审批/2审批中/3已付款/4已驳回',
    applied_by BIGINT,
    applied_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment_no (payment_no),
    INDEX idx_status (payment_status),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款记录表';

CREATE TABLE IF NOT EXISTS t_bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_no VARCHAR(32) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    bill_period_start DATE,
    bill_period_end DATE,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(18,2) DEFAULT 0.00,
    send_status TINYINT DEFAULT 0 COMMENT '0未发送/1已发送',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bill_no (bill_no),
    INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户账单表';

CREATE TABLE IF NOT EXISTS t_approval_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_type VARCHAR(32) NOT NULL COMMENT 'PAYMENT/RECEIPT',
    biz_id BIGINT NOT NULL,
    current_node INT DEFAULT 0,
    total_nodes INT DEFAULT 1,
    node_config JSON,
    approval_status TINYINT DEFAULT 1 COMMENT '1审批中/2已通过/3已驳回',
    approved_by BIGINT,
    approved_time DATETIME,
    comment VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_biz (biz_type, biz_id),
    INDEX idx_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程表';

-- ==================== 资金流水域 ====================

CREATE TABLE IF NOT EXISTS t_bank_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no VARCHAR(255) NOT NULL COMMENT '账号（AES加密存储）',
    bank_name VARCHAR(100),
    account_name VARCHAR(100),
    balance DECIMAL(18,2) DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'CNY',
    account_status TINYINT DEFAULT 1 COMMENT '1正常/2冻结/3注销',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bank_name (bank_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行账户表';

CREATE TABLE IF NOT EXISTS t_fund_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trans_no VARCHAR(64) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    trans_type TINYINT NOT NULL COMMENT '1收入/2支出/3转账',
    amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    balance_after DECIMAL(18,2),
    counterparty VARCHAR(100),
    trans_time DATETIME,
    source TINYINT DEFAULT 1 COMMENT '1手动录入/2银行接口',
    summary VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_trans_no (trans_no),
    INDEX idx_account_id (account_id),
    INDEX idx_trans_time (trans_time),
    INDEX idx_trans_type (trans_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

CREATE TABLE IF NOT EXISTS t_reconciliation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    system_trans_id BIGINT,
    actual_trans_id VARCHAR(64),
    diff_type TINYINT COMMENT '1金额不符/2系统独有/3银行独有',
    diff_amount DECIMAL(18,2) DEFAULT 0.00,
    handle_status TINYINT DEFAULT 0 COMMENT '0未处理/1已处理/2已忽略',
    handle_by BIGINT,
    handle_time DATETIME,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_system_trans (system_trans_id),
    INDEX idx_handle_status (handle_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账记录表';

-- ==================== 初始化数据 ====================

INSERT INTO t_role (role_code, role_name, description) VALUES
('ADMIN', '管理员', '全部功能 + 系统配置'),
('FINANCE', '财务专员', '订单结算、收付款操作、资金流水管理、报表查看'),
('BIZ', '业务员', '订单录入/查看、账单查询');

INSERT INTO t_user (username, password, real_name, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1);

INSERT INTO t_user_role (user_id, role_id) VALUES (1, 1);
