-- 1. 初始化订单数据（枚举字段status/payType使用枚举code）
INSERT INTO `order` (product_id, buyer_id, seller_id, order_no, total_amount, quantity, receiver_name, address, phone_number, buyer_remark, status, pay_type, create_time, pay_time, ship_time, receive_time, cancel_time, pay_expire_time)
VALUES
-- 待支付订单（status=PENDING_PAYMENT，用于OrderMapper.selectTimeoutPendingOrders测试）
(
    1,  -- 二手iPhone 13的product_id=1
    1,  -- test_buyer的user_id=1
    2,  -- test_seller的user_id=2
    'ORDER20240107001',
    4599.00,
    1,
    '张三',
    '北京市朝阳区XX小区',
    '17768276455',
    '请尽快发货',
    'PENDING_PAYMENT',  -- OrderStatusEnum.PENDING_PAYMENT的code
    NULL,
    '2024-01-07 09:00:00',
    NULL,
    NULL,
    NULL,
    NULL,
    '2024-01-07 15:00:00'  -- 支付超时时间
),
-- 已支付未发货订单（status=PAID，payType=WECHAT，用于OrderMapper.updateShipTime测试）
(
    1,
    1,
    2,
    'ORDER20240107002',
    4599.00,
    1,
    '张三',
    '北京市朝阳区XX小区',
    '13399645726',
    '无留言',
    'PENDING_SHIPMENT',  -- OrderStatusEnum.PAID的code
    'WECHAT_PAY',  -- PayTypeEnum.WECHAT的code
    '2024-01-07 10:00:00',
    '2024-01-07 10:05:00',
    NULL,
    NULL,
    NULL,
    '2024-01-07 16:00:00'
),
-- 已完成订单（status=RECEIVED，payType=ALIPAY，用于EvaluationMapper测试）
(
    2,  -- 小米手环8的product_id=2
    1,
    2,
    'ORDER20240106001',
    299.00,
    1,
    '张三',
    '北京市朝阳区XX小区',
    '13399645726',
    '尽快发货',
    'COMPLETED',  -- OrderStatusEnum.RECEIVED的code
    'ALIPAY',  -- PayTypeEnum.ALIPAY的code
    '2024-01-06 14:00:00',
    '2024-01-06 14:03:00',
    '2024-01-06 16:00:00',
    '2024-01-08 09:00:00',
    NULL,
    '2024-01-06 20:00:00'
),
-- 已取消订单（status=CANCELLED，用于OrderMapper.updateStatus测试）
(
    3,  -- 闲置笔记本的product_id=3
    1,
    2,
    'ORDER20240105001',
    2800.00,
    1,
    '张三',
    '北京市朝阳区XX小区',
    '13396773544',
    '暂时不需要了',
    'CANCELLED',  -- OrderStatusEnum.CANCELLED的code
    NULL,
    '2024-01-05 11:00:00',
    NULL,
    NULL,
    NULL,
    '2024-01-05 11:10:00',
    '2024-01-05 17:00:00'
),
-- 待收货订单（status=PENDING_RECEIVE，用于OrderMapper.selectTimeoutShippedOrders测试）
(3, -- 闲置笔记本的product_id=3
 1,
 2,
 'ORDER20240108001',
 1999.00,
 1,
 '张三',
 '北京市朝阳区XX小区',
 '13396773544',
 '请尽快发货',
 'PENDING_RECEIVE', -- OrderStatusEnum.PENDING_RECEIVE的code
 NULL,
 '2024-01-08 09:00:00',
 '2024-01-08 10:00:00',
 '2024-01-08 15:00:00',
 NULL,
 NULL,
 '2024-01-08 20:00:00'
);

-- 2. 初始化评价数据（枚举字段status使用枚举code）
INSERT INTO `evaluation` (order_id, user_id, evaluatee_id, content, score, status, create_time)
VALUES
    (
        3,  -- 已完成订单的order_id=3
        1,  -- test_buyer的user_id=1
        2,  -- test_seller的user_id=2
        '商品很好，物流快，满意！',
        5,
        'NORMAL',  -- EvaluationStatusEnum.NORMAL的code
        '2024-01-08 10:00:00'
    ),
    (
        4,  -- 已取消订单的order_id=4（测试异常场景）
        1,
        2,
        '订单取消，未收到商品',
        3,
        'HIDDEN',  -- EvaluationStatusEnum.HIDDEN的code
        '2024-01-05 11:15:00'
    ),
    (
         2,  -- 待收货订单的order_id=5
         1,
         2,
         '物流太慢了，希望可以取消订单',
         4,
         'NORMAL',
         '2024-01-08 10:15:00'
    ),
    (
         1,  -- 待支付订单的order_id=1（测试异常场景）
         1,
         2,
         '商品太差了，希望可以退款',
         3,
         'NORMAL',
         '2024-01-07 09:05:00'
    );