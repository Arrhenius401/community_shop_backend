-- 初始化商品数据（枚举字段status/condition使用枚举code）
INSERT INTO `product` (seller_id, title, category, description, price, stock, create_time, update_time, status, `condition`)
VALUES
-- 在售商品（status=ON_SALE，condition=NINETY_FIVE_PERCENT_NEW，用于ProductMapper.selectByCondition测试）
(
    2,  -- test_seller的user_id=2
    '二手iPhone 13 128G',
    '数码产品',
    '95新，无划痕，电池健康90%',
    4599.00,
    5,
    '2024-01-05 14:00:00',
    '2024-01-05 14:00:00',
    'ON_SALE',  -- ProductStatusEnum.ON_SALE的code
    'NINETY_FIVE_PERCENT_NEW'   -- ProductConditionEnum.NINETY_FIVE_PERCENT_NEW的code
),
-- 已售罄商品（status=SOLD_OUT，condition=NEW，用于ProductMapper.updateStock测试）
(
    2,
    '全新小米手环8',
    '智能穿戴',
    '未拆封，官方正品',
    299.00,
    0,
    '2024-01-06 10:00:00',
    '2024-01-06 10:00:00',
    'SOLD_OUT',  -- ProductStatusEnum.SOLD_OUT的code
    'NEW'            -- ProductConditionEnum.NEW的code
),
-- 下架商品（status=OFF_SHELF，condition=EIGHTY_PERCENT_NEW，用于ProductMapper.updateStatus测试）
(
    2,
    '闲置笔记本电脑',
    '电脑设备',
    '使用2年，i5+8G，正常使用',
    2800.00,
    1,
    '2024-01-04 09:30:00',
    '2024-01-04 09:30:00',
    'OFF_SHELF',  -- ProductStatusEnum.OFF_SHELF的code
    'EIGHTY_PERCENT_NEW'      -- ProductConditionEnum.EIGHTY_PERCENT_NEW的code
);