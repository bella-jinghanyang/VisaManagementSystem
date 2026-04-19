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

-- 7. 签证知识库表 --
DROP TABLE IF EXISTS `visa_knowledge`;
CREATE TABLE `visa_knowledge`
(
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category`    VARCHAR(50)  DEFAULT NULL COMMENT '分类(如:日本/美国/通用)',
    `title`       VARCHAR(200) NOT NULL COMMENT '知识点标题',
    `keywords`    VARCHAR(500) DEFAULT NULL COMMENT '搜索关键词(逗号分隔)',
    `content`     TEXT         NOT NULL COMMENT '详细内容(给AI看的资料)',
    `status`      CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='签证知识库表';

-- 8. 签证评价表 --
DROP TABLE IF EXISTS `visa_comment`;
CREATE TABLE `visa_comment`
(
    `id`          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `order_id`    BIGINT(20) NOT NULL COMMENT '关联订单ID',
    `product_id`  BIGINT(20) NOT NULL COMMENT '关联产品ID',
    `customer_id` BIGINT(20) NOT NULL COMMENT '关联客户ID',
    `rating`      TINYINT(1) DEFAULT 5 COMMENT '评分(1-5星)',
    `content`     TEXT       NOT NULL COMMENT '评价内容',
    `images`      JSON       DEFAULT NULL COMMENT '晒图(JSON数组)',
    `admin_reply` TEXT       DEFAULT NULL COMMENT '管理员回复',
    `reply_time`  DATETIME   DEFAULT NULL COMMENT '回复时间',
    `create_time` DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`) USING BTREE, -- ★ 确保一笔订单只能评价一次
    INDEX `idx_product_id` (`product_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='签证评价表';

ALTER TABLE `visa_order` ADD COLUMN `is_commented` CHAR(1) DEFAULT '0' COMMENT '是否评价(0未评 1已评)';
ALTER TABLE `visa_order` ADD COLUMN `express_to_agency` VARCHAR(64) COMMENT '用户寄给中介的快递单号';

ALTER TABLE `visa_comment` ADD COLUMN `additional_content` TEXT COMMENT '追加评价内容';
ALTER TABLE `visa_comment` ADD COLUMN `additional_time` DATETIME COMMENT '追加评价时间';

DROP TABLE IF EXISTS `order_message`;
CREATE TABLE `order_message`
(
    `id`          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `order_id`    BIGINT(20)   DEFAULT 0 COMMENT '关联订单ID(0为通用咨询)',
    `sender_type` TINYINT(1) NOT NULL COMMENT '发送者类型(1:客户 2:管理员)',
    `content`     TEXT       NOT NULL COMMENT '消息内容',
    `image_url`   VARCHAR(255) DEFAULT NULL COMMENT '图片路径(可选)',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单留言记录表';

-- 1. 增加客户ID字段
ALTER TABLE `order_message` ADD COLUMN `customer_id` BIGINT(20) DEFAULT 0 COMMENT '所属客户ID';

-- 2. 刷新旧数据（可选，把之前的0消息归给管理员或测试号）
UPDATE `order_message` SET `customer_id` = 0 WHERE `customer_id` IS NULL;

ALTER TABLE `order_message` ADD COLUMN `is_ai` CHAR(1) DEFAULT '0' COMMENT '是否为AI消息(0否 1是)';

CREATE TABLE `visa_order_applicant`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id`    bigint(20)  NOT NULL COMMENT '关联订单ID',
    `order_no`    varchar(32) NOT NULL COMMENT '关联订单号',
    `name`        varchar(50) NOT NULL COMMENT '姓名',
    `id_card`     varchar(20) DEFAULT NULL COMMENT '身份证号',
    `passport_no` varchar(20) DEFAULT NULL COMMENT '护照号',
    `create_time` datetime    DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单申请人基本信息表';

ALTER TABLE `visa_order_applicant`
    ADD COLUMN `identity` varchar(20) DEFAULT 'ADULT' COMMENT '身份类型: ADULT(在职), CHILD(未成年), STUDENT(学生), RETIRED(退休)' AFTER `passport_no`;

CREATE TABLE `visa_order_logistics`
(
    `id`              bigint(20)  NOT NULL AUTO_INCREMENT,
    `order_id`        bigint(20)  NOT NULL COMMENT '关联订单ID',
    `order_no`        varchar(32) NOT NULL COMMENT '关联订单号',
    `direction`       tinyint(1)  DEFAULT '1' COMMENT '方向: 1-用户寄给中介, 2-中介寄回用户',
    `courier_company` varchar(50) DEFAULT NULL COMMENT '快递公司(顺丰/EMS等)',
    `tracking_no`     varchar(64) DEFAULT NULL COMMENT '快递单号',
    `sender_name`     varchar(50) DEFAULT NULL COMMENT '寄件人姓名',
    `sender_phone`    varchar(20) DEFAULT NULL COMMENT '寄件人电话',
    `mail_address`    text        DEFAULT NULL COMMENT '详细邮寄地址快照',
    `status`          tinyint(1)  DEFAULT '0' COMMENT '状态: 0-待寄出, 1-已寄出, 2-已签收',
    `create_time`     datetime    DEFAULT NULL,
    `receive_time`    datetime    DEFAULT NULL COMMENT '签收时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单物流信息表';

ALTER TABLE `visa_product` ADD COLUMN `is_physical_required` tinyint(1) DEFAULT 0 COMMENT '是否需要寄送原件(0-否, 1-是)';

ALTER TABLE `visa_order`
    ADD COLUMN `user_remark` varchar(500) DEFAULT NULL COMMENT '用户备注/面试时间反馈要求';

ALTER TABLE visa_knowledge
    ADD COLUMN embedding LONGTEXT NULL COMMENT '知识点语义向量(JSON格式浮点数组，由Embedding API生成)';

INSERT INTO visa_knowledge (category, title, keywords, content, status, create_time)
VALUES ('美国', '美国B1/B2签证材料要求', '美国签证,B1,B2,材料,申请',
        '申请美国B1/B2旅游/商务签证需要准备以下材料：\n1. 有效护照（有效期须超过赴美预定停留日期后6个月）\n2. DS-160在线申请表（已提交并打印确认页）\n3. 签证照片（白底，2英寸×2英寸）\n4. 银行存款证明（近3个月流水及存款余额，建议10万元以上）\n5. 在职证明/营业执照（证明有稳定工作或生意）\n6. 房产证/车产证（资产证明）\n7. 行程单（机票预订单、酒店预订单）\n8. 往来记录（如有赴美历史记录，效果更佳）',
        '0', NOW()),

       ('美国', '美国签证面签流程', '美国签证,面签,大使馆,预约,流程',
        '美国签证面签流程如下：\n1. 缴纳签证申请费（MRV费用，约1105元人民币）\n2. 登录美国签证预约网站完成DS-160表格\n3. 在预约网站创建账户并缴费\n4. 预约面签时间（北京/上海/广州/沈阳/成都均设有使馆）\n5. 面签当日提前15分钟到达，携带所有材料\n6. 安检入场→排队等候→窗口面谈（约2-5分钟）\n7. 当场告知是否获批，或通知补充材料\n8. 护照邮寄返还（通常3-5个工作日）',
        '0', NOW()),

       ('日本', '日本旅游签证材料清单', '日本签证,旅游,材料,申请,所需材料',
        '申请日本旅游签证（个人旅游）所需材料：\n1. 护照原件（有效期需超过申请日6个月以上）\n2. 签证申请表（日本领事馆官网下载填写）\n3. 近期白底彩色照片（4.5cm×4.5cm）\n4. 在职证明或学校在读证明\n5. 近3个月银行流水（余额建议2万元以上）\n6. 行程计划书（标注每天游览地点）\n7. 机票预订单和酒店预订单\n注意：日本签证无需面试，通常5-7个工作日出签。',
        '0', NOW()),

       ('通用', '签证被拒签后如何处理', '拒签,被拒,重新申请,拒签原因',
        '签证被拒后的处理建议：\n1. 查明原因：仔细阅读拒签信，常见原因包括：材料不足、资产证明不充分、行程可信度低、有违规记录等。\n2. 补充材料：针对拒签原因，补充相应的证明材料，如增加银行存款、提供更详细的行程安排等。\n3. 等待冷静期：部分国家建议拒签后等待3-6个月再次申请，避免连续拒签记录。\n4. 选择合适时机：在有明确出行计划时申请，避免过早申请导致行程不确定。\n5. 寻求专业帮助：可委托专业签证中介协助准备材料，提高获批率。\n重要提示：被拒签不代表永久无法获签，补充充分材料后重新申请成功率依然较高。',
        '0', NOW()),

       ('通用', '签证有效期和停留期的区别', '有效期,停留,入境,区别,签证期限',
        '很多申请人容易混淆"签证有效期"和"允许停留期"：\n【签证有效期】：指您必须在这个期限内入境目的地国家，过了这个日期签证作废。例如有效期至2025年12月31日，则必须在此日期前完成入境。\n【允许停留期】：指您每次入境后在该国可以合法停留的天数，从入境之日起计算。例如美国B2签证通常允许停留180天，日本旅游签通常为15天或30天。\n【多次入境】：持多次入境签证，可在有效期内多次进出，每次停留不超过规定天数。\n建议在入境时仔细核对护照上的"ADMITTED UNTIL"或"PERMITTED TO STAY UNTIL"日期，超期停留将影响未来签证申请。',
        '0', NOW()),

       ('申根', '申根签证需要什么材料', '申根,欧洲,法国,德国,意大利,签证材料',
        '申根签证（以法国为例）申请材料：\n1. 护照原件及复印件（近10年内签发，入境后有效期超过3个月）\n2. 签证申请表（法国领事馆官网填写并打印）\n3. 近6个月2寸白底照片\n4. 往返机票预订单\n5. 酒店预订单或邀请函\n6. 行程单\n7. 旅行保险（保额不低于30万元或3万欧元，覆盖全程）\n8. 在职证明和近3个月工资单\n9. 银行存款证明（近3个月流水，余额建议5万以上）\n10. 户口本及身份证复印件\n申根签证通常10-15个工作日出签，旺季建议提前1个月申请。',
        '0', NOW());