-- 初始化消息数据（枚举字段type/status使用枚举code）
INSERT INTO `message` (sender_id, receiver_id, title, content, order_id, is_read, is_deleted, type, status, create_time)
VALUES
-- 系统消息（type=SYSTEM，status=NORMAL，用于MessageMapper.selectByReceiver测试）
(
    0,  -- 系统发送
    -1,  -- 所有用户
    '社区春节交易通知',
    '春节（1.20-2.10）物流延迟，下单前咨询卖家',
    NULL,
    FALSE,
    FALSE,
    'SYSTEM',  -- MessageTypeEnum.SYSTEM的code
    'NORMAL',  -- MessageStatusEnum.NORMAL的code
    '2024-01-15 08:00:00'
),
-- 订单消息（type=ORDER，status=NORMAL，用于MessageMapper.countUnread测试）
(
    0,
    1,  -- test_buyer的user_id=1
    '订单已收货',
    '您的订单ORDER20240106001已确认收货',
    3,  -- 已完成订单order_id=3
    TRUE,
    FALSE,
    'ORDER',  -- MessageTypeEnum.ORDER的code
    'NORMAL',
    '2024-01-08 09:30:00'
),
-- 失效消息（type=ORDER，status=INVALID，用于MessageMapper.updateStatus测试）
(
    0,
    1,
    '订单支付提醒',
    '您的订单ORDER20240105001即将超时',
    4,  -- 已取消订单order_id=4
    FALSE,
    FALSE,
    'ORDER',
    'INVALID',  -- MessageStatusEnum.INVALID的code
    '2024-01-05 16:30:00'
);