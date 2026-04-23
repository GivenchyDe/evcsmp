-- ============================================================
-- 新能源汽车连锁充电站管理平台 数据库初始化脚本
-- MySQL 8.0
-- 共 13 张表
-- ============================================================

CREATE DATABASE IF NOT EXISTS evcsmp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE evcsmp;

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 管理员表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `admin` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username`    VARCHAR(50)   NOT NULL UNIQUE COMMENT '登录账号',
  `password`    VARCHAR(255)  NOT NULL COMMENT '密码(加密)',
  `name`        VARCHAR(50)   NOT NULL COMMENT '姓名',
  `phone`       VARCHAR(20)   DEFAULT NULL COMMENT '联系方式',
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='管理员表';

-- ------------------------------------------------------------
-- 2. 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`          INT UNSIGNED    NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)     NOT NULL UNIQUE COMMENT '用户名',
  `phone`       VARCHAR(20)     NOT NULL UNIQUE COMMENT '手机号',
  `nickname`    VARCHAR(50)     DEFAULT NULL COMMENT '昵称',
  `avatar`      VARCHAR(255)    DEFAULT NULL COMMENT '头像URL',
  `balance`     DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `password`    VARCHAR(255)    NOT NULL COMMENT '登录密码(加密)',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用 1=正常',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='用户表';

-- ------------------------------------------------------------
-- 3. 充电站网点表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `station` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '网点ID',
  `name`        VARCHAR(100)  NOT NULL COMMENT '网点名称',
  `address`     VARCHAR(255)  NOT NULL COMMENT '详细地址',
  `longitude`   DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude`    DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `phone`       VARCHAR(20)   DEFAULT NULL COMMENT '联系电话',
  `open_time`   TIME          DEFAULT NULL COMMENT '营业开始时间',
  `close_time`  TIME          DEFAULT NULL COMMENT '营业结束时间',
  `photos`      JSON          DEFAULT NULL COMMENT '网点照片URL列表',
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=维护中 1=正常运营 2=已停用',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='充电站网点表';

-- ------------------------------------------------------------
-- 4. 充电桩表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `charger` (
  `id`            INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '充电桩ID',
  `station_id`    INT UNSIGNED  NOT NULL COMMENT '所属网点ID',
  `device_no`     VARCHAR(50)   NOT NULL UNIQUE COMMENT '设备编号',
  `type`          TINYINT       NOT NULL COMMENT '类型: 0=慢充 1=快充',
  `manufacturer`  VARCHAR(100)  DEFAULT NULL COMMENT '生产厂家',
  `install_date`  DATE          DEFAULT NULL COMMENT '安装时间',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0=故障 1=正常 2=维护中',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_charger_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`id`)
) ENGINE=InnoDB COMMENT='充电桩表';

-- ------------------------------------------------------------
-- 5. 计费标准表（每个充电桩独立配置）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `billing_rule` (
  `id`            INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '计费规则ID',
  `charger_id`    INT UNSIGNED  NOT NULL UNIQUE COMMENT '充电桩ID（一桩一规则）',
  `price_per_kwh` DECIMAL(6,2)  NOT NULL COMMENT '电费单价(元/度)',
  `service_fee`   DECIMAL(6,2)  NOT NULL DEFAULT 0.00 COMMENT '服务费单价(元/度)',
  `free_minutes`  INT           NOT NULL DEFAULT 0 COMMENT '免费充电时长(分钟)',
  `remark`        VARCHAR(200)  DEFAULT NULL COMMENT '备注说明',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_billing_charger` FOREIGN KEY (`charger_id`) REFERENCES `charger` (`id`)
) ENGINE=InnoDB COMMENT='计费标准表';

-- ------------------------------------------------------------
-- 6. 充电枪表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `gun` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '充电枪ID',
  `charger_id`  INT UNSIGNED  NOT NULL COMMENT '所属充电桩ID',
  `gun_no`      VARCHAR(20)   NOT NULL COMMENT '枪号',
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=空闲 1=繁忙 2=故障',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gun` (`charger_id`, `gun_no`),
  CONSTRAINT `fk_gun_charger` FOREIGN KEY (`charger_id`) REFERENCES `charger` (`id`)
) ENGINE=InnoDB COMMENT='充电枪表';

-- ------------------------------------------------------------
-- 7. 充电订单表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `charging_order` (
  `id`            INT UNSIGNED    NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`      VARCHAR(32)     NOT NULL UNIQUE COMMENT '订单号',
  `user_id`       INT UNSIGNED    NOT NULL COMMENT '用户ID',
  `station_id`    INT UNSIGNED    NOT NULL COMMENT '网点ID',
  `charger_id`    INT UNSIGNED    NOT NULL COMMENT '充电桩ID',
  `gun_id`        INT UNSIGNED    NOT NULL COMMENT '充电枪ID',
  `price_per_kwh` DECIMAL(6,2)    DEFAULT NULL COMMENT '下单时电费单价(快照)',
  `service_fee`   DECIMAL(6,2)    DEFAULT NULL COMMENT '下单时服务费单价(快照)',
  `start_time`    DATETIME        DEFAULT NULL COMMENT '开始充电时间',
  `end_time`      DATETIME        DEFAULT NULL COMMENT '结束充电时间',
  `duration`      INT             DEFAULT NULL COMMENT '充电时长(秒)',
  `electricity`   DECIMAL(8,3)    DEFAULT NULL COMMENT '充电量(度)',
  `amount`        DECIMAL(10,2)   DEFAULT NULL COMMENT '总费用(元)',
  `pay_type`      TINYINT         DEFAULT NULL COMMENT '支付方式: 0=余额 1=模拟微信 2=模拟支付宝',
  `status`        TINYINT         NOT NULL DEFAULT 0 COMMENT '状态: 0=充电中 1=待结算 2=已完成 3=已取消 4=退款中 5=已退款',
  `refund_reason` VARCHAR(255)    DEFAULT NULL COMMENT '退款原因',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at`       DATETIME        DEFAULT NULL COMMENT '结算时间',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_cord_user`    FOREIGN KEY (`user_id`)    REFERENCES `user`    (`id`),
  CONSTRAINT `fk_cord_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`id`),
  CONSTRAINT `fk_cord_charger` FOREIGN KEY (`charger_id`) REFERENCES `charger` (`id`),
  CONSTRAINT `fk_cord_gun`     FOREIGN KEY (`gun_id`)     REFERENCES `gun`     (`id`)
) ENGINE=InnoDB COMMENT='充电订单表';

-- ------------------------------------------------------------
-- 8. 充值订单表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `recharge_order` (
  `id`          INT UNSIGNED    NOT NULL AUTO_INCREMENT COMMENT '充值订单ID',
  `order_no`    VARCHAR(32)     NOT NULL UNIQUE COMMENT '订单号',
  `user_id`     INT UNSIGNED    NOT NULL COMMENT '用户ID',
  `amount`      DECIMAL(10,2)   NOT NULL COMMENT '充值金额(元)',
  `gift`        DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT '赠送金额(元)',
  `pay_type`    TINYINT         NOT NULL COMMENT '支付方式: 1=模拟微信 2=模拟支付宝',
  `status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '状态: 0=待支付 1=已完成 2=已取消',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at`     DATETIME        DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_rord_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB COMMENT='充值订单表';

-- ------------------------------------------------------------
-- 9. 订单评价表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_review` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id`    INT UNSIGNED  NOT NULL COMMENT '充电订单ID',
  `user_id`     INT UNSIGNED  NOT NULL COMMENT '用户ID',
  `station_id`  INT UNSIGNED  NOT NULL COMMENT '网点ID',
  `score`       TINYINT       NOT NULL COMMENT '评分 1-5',
  `content`     VARCHAR(500)  DEFAULT NULL COMMENT '评价内容',
  `images`      JSON          DEFAULT NULL COMMENT '评价图片URL列表',
  `is_deleted`  TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除: 0=正常 1=已删除',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_review_order`   FOREIGN KEY (`order_id`)   REFERENCES `charging_order` (`id`),
  CONSTRAINT `fk_review_user`    FOREIGN KEY (`user_id`)    REFERENCES `user`           (`id`),
  CONSTRAINT `fk_review_station` FOREIGN KEY (`station_id`) REFERENCES `station`        (`id`)
) ENGINE=InnoDB COMMENT='订单评价表';

-- ------------------------------------------------------------
-- 10. 故障反馈表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `feedback` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `feedback_no` VARCHAR(32)   NOT NULL UNIQUE COMMENT '反馈编号',
  `user_id`     INT UNSIGNED  NOT NULL COMMENT '用户ID',
  `type`        TINYINT       NOT NULL COMMENT '类型: 0=网点问题 1=设备故障 2=软件异常 3=使用体验 4=其他',
  `content`     VARCHAR(1000) NOT NULL COMMENT '反馈内容',
  `images`      JSON          DEFAULT NULL COMMENT '图片URL列表',
  `station_id`  INT UNSIGNED  DEFAULT NULL COMMENT '关联网点ID',
  `charger_id`  INT UNSIGNED  DEFAULT NULL COMMENT '关联充电桩ID',
  `contact`     VARCHAR(50)   DEFAULT NULL COMMENT '联系方式',
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理 1=处理中 2=已处理 3=已驳回',
  `handler_id`  INT UNSIGNED  DEFAULT NULL COMMENT '处理管理员ID',
  `reply`       VARCHAR(500)  DEFAULT NULL COMMENT '处理意见',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at`  DATETIME      DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_fb_user`    FOREIGN KEY (`user_id`)    REFERENCES `user`    (`id`),
  CONSTRAINT `fk_fb_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`id`),
  CONSTRAINT `fk_fb_charger` FOREIGN KEY (`charger_id`) REFERENCES `charger` (`id`),
  CONSTRAINT `fk_fb_handler` FOREIGN KEY (`handler_id`) REFERENCES `admin`   (`id`)
) ENGINE=InnoDB COMMENT='故障反馈表';

-- ------------------------------------------------------------
-- 11. 公告表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `announcement` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`       VARCHAR(100)  NOT NULL COMMENT '标题',
  `content`     TEXT          NOT NULL COMMENT '内容(富文本)',
  `admin_id`    INT UNSIGNED  NOT NULL COMMENT '发布管理员ID',
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布 1=已发布 2=已过期',
  `view_count`  INT           NOT NULL DEFAULT 0 COMMENT '阅读量',
  `publish_at`  DATETIME      DEFAULT NULL COMMENT '发布时间',
  `expire_at`   DATETIME      DEFAULT NULL COMMENT '过期时间',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_ann_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`)
) ENGINE=InnoDB COMMENT='公告表';

-- ------------------------------------------------------------
-- 12. 消息通知表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `message` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id`     INT UNSIGNED  NOT NULL COMMENT '接收用户ID',
  `type`        TINYINT       NOT NULL COMMENT '类型: 0=订单 1=充值 2=反馈 3=公告 4=故障',
  `title`       VARCHAR(100)  NOT NULL COMMENT '消息标题',
  `content`     VARCHAR(500)  NOT NULL COMMENT '消息内容',
  `is_read`     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已读: 0=未读 1=已读',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_at`   DATETIME      DEFAULT NULL COMMENT '过期时间(30天后)',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_msg_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB COMMENT='消息通知表';

-- ------------------------------------------------------------
-- 13. 客服聊天记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id`     INT UNSIGNED  NOT NULL COMMENT '用户ID',
  `sender`      TINYINT       NOT NULL COMMENT '发送方: 0=用户 1=AI客服 2=人工客服',
  `admin_id`    INT UNSIGNED  DEFAULT NULL COMMENT '人工客服管理员ID',
  `content`     TEXT          NOT NULL COMMENT '消息内容',
  `msg_type`    TINYINT       NOT NULL DEFAULT 0 COMMENT '消息类型: 0=文字 1=图片',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_at`   DATETIME      DEFAULT NULL COMMENT '过期时间(30天后)',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_chat_user`  FOREIGN KEY (`user_id`)  REFERENCES `user`  (`id`),
  CONSTRAINT `fk_chat_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`)
) ENGINE=InnoDB COMMENT='客服聊天记录表';

SET FOREIGN_KEY_CHECKS = 1;




-- ------------------------------------------------------------
-- 14. 消息推送记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `message_push` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '推送记录ID',
  `type`        TINYINT       NOT NULL COMMENT '消息类型: 0=订单 1=充值 2=反馈 3=公告 4=故障',
  `title`       VARCHAR(100)  NOT NULL COMMENT '消息标题',
  `content`     VARCHAR(500)  NOT NULL COMMENT '消息内容',
  `user_count`  INT           NOT NULL DEFAULT 0 COMMENT '推送用户数',
  `read_count`  INT           NOT NULL DEFAULT 0 COMMENT '已读用户数',
  `read_rate`   DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '阅读率',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='消息推送记录表';

SET FOREIGN_KEY_CHECKS = 1;