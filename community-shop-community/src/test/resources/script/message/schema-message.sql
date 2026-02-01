-- 消息表（Message实体，枚举字段type/status存储code，对应MessageTypeEnum/MessageStatusEnum）
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
                           msg_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           sender_id BIGINT NOT NULL COMMENT '发送者ID（0=系统）',
                           receiver_id BIGINT NOT NULL COMMENT '接收者ID（-1=所有用户）',
                           title VARCHAR(100) NOT NULL COMMENT '消息标题',
                           content TEXT NOT NULL COMMENT '消息内容',
                           order_id BIGINT COMMENT '关联订单ID（非订单消息为null）',
                           is_read BOOLEAN DEFAULT FALSE COMMENT '阅读状态（true=已读）',
                           is_deleted BOOLEAN DEFAULT FALSE COMMENT '删除状态（true=已删除）',
                           type VARCHAR(20) NOT NULL COMMENT '消息类型（枚举MessageTypeEnum的code：SYSTEM/ORDER/PRIVATE）',
                           create_time DATETIME NOT NULL COMMENT '创建时间',
                           update_time DATETIME COMMENT '更新时间'
) COMMENT '系统消息表';