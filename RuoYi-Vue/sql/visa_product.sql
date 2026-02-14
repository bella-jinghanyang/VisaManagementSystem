CREATE TABLE `visa_product`
(
    `id`                    BIGINT(20)     NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',

    `title`                 VARCHAR(100)   NOT NULL COMMENT '产品标题（如：美国B1/B2十年多次签）',
    `country`               VARCHAR(50)    NOT NULL COMMENT '国家（如：日本、美国）',
    `visa_type`             VARCHAR(50)    NOT NULL COMMENT '签证类型（旅游/商务/留学/探亲）',
    `price`                 DECIMAL(10, 2) NOT NULL COMMENT '销售价格（单位：元）',

    `image`                 VARCHAR(255)            DEFAULT NULL COMMENT '产品封面图片路径',

    `requirements_config`   JSON                    DEFAULT NULL COMMENT '核心字段：材料要求配置规则（JSON数组）',

    `is_interview_required` TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否需要面签（0:否, 1:是）',

    `processing_time`       VARCHAR(50)             DEFAULT NULL COMMENT '预计办理时长（如：5-7个工作日）',
    `validity_period`       VARCHAR(50)             DEFAULT NULL COMMENT '有效期（如：10年）',
    `max_stay_days`         VARCHAR(50)             DEFAULT NULL COMMENT '最长停留天数（如：90天）',
    `consular_district`     VARCHAR(50)             DEFAULT NULL COMMENT '所属领区（如：全国受理、北京领区）',

    `status`                TINYINT(1)     NOT NULL DEFAULT 1 COMMENT '上架状态（1:上架, 0:下架）',

    `create_time`           DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),

    -- 常用查询建议加索引（按你的业务可删）
    KEY `idx_country` (`country`),
    KEY `idx_visa_type` (`visa_type`),
    KEY `idx_status` (`status`)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='签证产品表';

-- 1. 国家表
DROP TABLE IF EXISTS visa_country;
CREATE TABLE visa_country
(
    id        BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    name      VARCHAR(50) NOT NULL COMMENT '国家名称(如:日本)',
    code      VARCHAR(20) COMMENT '国家代码(如:JP)',
    continent VARCHAR(20) COMMENT '所属大洲(如:亚洲)',
    flag_url  VARCHAR(255) COMMENT '国旗图片(可选)',
    sort      INT     DEFAULT 0 COMMENT '排序',
    status    CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    PRIMARY KEY (id)
) COMMENT ='国家配置表';

-- 2. 签证类型表
DROP TABLE IF EXISTS visa_type;
CREATE TABLE visa_type
(
    id     BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    name   VARCHAR(50) NOT NULL COMMENT '类型名称(如:旅游签, 商务签)',
    code   VARCHAR(50) COMMENT '编码(如:TOURIST)',
    sort   INT     DEFAULT 0 COMMENT '排序',
    status CHAR(1) DEFAULT '0' COMMENT '状态',
    PRIMARY KEY (id)
) COMMENT ='签证类型表';

-- 3. 领区表
DROP TABLE IF EXISTS visa_district;
CREATE TABLE visa_district
(
    id         BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    name       VARCHAR(50) NOT NULL COMMENT '领区名称(如:北京领区)',
    cover_area VARCHAR(500) COMMENT '覆盖省份(如:京,津,冀)',
    sort       INT     DEFAULT 0 COMMENT '排序',
    status     CHAR(1) DEFAULT '0' COMMENT '状态',
    PRIMARY KEY (id)
) COMMENT ='领区配置表';


-- 清空数据（开发阶段无所谓）
TRUNCATE TABLE visa_product;

-- 修改表结构
ALTER TABLE visa_product
    CHANGE COLUMN country country_id BIGINT(20) NOT NULL COMMENT '关联国家ID',
    CHANGE COLUMN visa_type type_id BIGINT(20) NOT NULL COMMENT '关联类型ID',
    CHANGE COLUMN consular_district district_id BIGINT(20) COMMENT '关联领区ID';

-- 这里的 requirements_config 和其他字段保持不变


-- 4. c端用户表 --
DROP TABLE IF EXISTS c_customer;

CREATE TABLE c_customer
(
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号码(登录账号)',
    `password`    VARCHAR(100) NOT NULL COMMENT '登录密码(加密)',
    `nickname`    VARCHAR(50)  DEFAULT '' COMMENT '用户昵称',
    `avatar`      VARCHAR(255) DEFAULT '' COMMENT '头像路径',

    -- ★ 业务字段 (用于下单自动回填)
    `real_name`   VARCHAR(50)  DEFAULT '' COMMENT '真实姓名',
    `id_card`     VARCHAR(20)  DEFAULT '' COMMENT '身份证号',
    `email`       VARCHAR(50)  DEFAULT '' COMMENT '电子邮箱',
    `sex`         CHAR(1)      DEFAULT '0' COMMENT '性别(0男 1女 2未知)',

    -- ★ 系统字段 (若依标准)
    `status`      CHAR(1)      DEFAULT '0' COMMENT '帐号状态(0正常 1停用)',
    `del_flag`    CHAR(1)      DEFAULT '0' COMMENT '删除标志(0代表存在 2代表删除)',
    `login_ip`    VARCHAR(128) DEFAULT '' COMMENT '最后登录IP',
    `login_date`  DATETIME COMMENT '最后登录时间',
    `create_by`   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME COMMENT '更新时间',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`) USING BTREE COMMENT '手机号唯一索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='C端客户表';

-- 5. 订单表 --
DROP TABLE IF EXISTS `visa_order`;
CREATE TABLE `visa_order`
(
    `id`                  BIGINT(20)     NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`            VARCHAR(32)    NOT NULL COMMENT '订单编号(唯一)',
    `customer_id`         BIGINT(20)     NOT NULL COMMENT '客户ID(关联c_customer)',
    `product_id`          BIGINT(20)     NOT NULL COMMENT '产品ID(关联visa_product)',

    -- ★ 快照与金额
    `product_snapshot`    JSON         DEFAULT NULL COMMENT '产品信息快照(存下单时的标题和价格)',
    `total_amount`        DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',

    -- ★ 核心状态机
    `status`              INT(2)       DEFAULT 0 COMMENT '订单状态(0待支付 1待上传 2待审核 3待补交 4办理中 5待收货 6已完成 7待面试)',

    -- ★ 支付相关 (支付宝)
    `alipay_no`           VARCHAR(64)  DEFAULT NULL COMMENT '支付宝交易流水号',
    `pay_time`            DATETIME     DEFAULT NULL COMMENT '支付成功时间',

    -- ★ 办签业务材料 (核心数据)
    `submitted_materials` JSON         DEFAULT NULL COMMENT '用户提交的动态材料数据(JSON)',
    `audit_remark`        VARCHAR(500) DEFAULT NULL COMMENT '审核备注/驳回原因',

    -- ★ 面试分支字段 (针对美签等)
    `interview_time`      DATETIME     DEFAULT NULL COMMENT '预约面试时间',
    `interview_location`  VARCHAR(200) DEFAULT NULL COMMENT '面试地点',
    `interview_file`      VARCHAR(255) DEFAULT NULL COMMENT '面试预约单文件路径',
    `interview_feedback`  TINYINT(1)   DEFAULT 0 COMMENT '面试反馈(0未反馈 1过签 2拒签 3Check)',

    -- ★ 结果与物流
    `visa_result`         TINYINT(1)   DEFAULT 0 COMMENT '最终签证结果(1出签 2拒签)',
    `tracking_number`     VARCHAR(64)  DEFAULT NULL COMMENT '寄回护照的快递单号',
    `mailing_address`     JSON         DEFAULT NULL COMMENT '收货地址快照(收件人,电话,地址)',

    -- ★ 系统审计字段
    `create_time`         DATETIME     DEFAULT NULL COMMENT '下单时间',
    `update_time`         DATETIME     DEFAULT NULL COMMENT '更新时间',
    `del_flag`            CHAR(1)      DEFAULT '0' COMMENT '删除标志(0代表存在 2代表删除)',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
    INDEX `idx_customer_id` (`customer_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='签证订单表';

ALTER TABLE `visa_order` ADD COLUMN `quantity` INT DEFAULT 1 AFTER `product_id`;
UPDATE visa_order SET del_flag = '0';
ALTER TABLE `visa_order` ADD COLUMN `interview_slots` JSON COMMENT '管理员提供的备选时间点';
ALTER TABLE `visa_order` ADD COLUMN `supplementary_materials` JSON COMMENT '面试后补交的特殊材料';
-- 6. 购物车表 --
DROP TABLE IF EXISTS `visa_cart`;
CREATE TABLE `visa_cart`
(
    `id`          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `customer_id` BIGINT(20) NOT NULL COMMENT '客户ID',
    `product_id`  BIGINT(20) NOT NULL COMMENT '产品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购物车表';

ALTER TABLE `visa_cart` ADD COLUMN `quantity` INT DEFAULT 1 COMMENT '产品数量';