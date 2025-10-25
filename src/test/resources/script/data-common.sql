-- 1. 初始化用户数据（枚举字段role/status/gender均使用枚举code）
INSERT INTO `user` (username, email, phone_number, password, gender, role, credit_score, create_time, activity_time, status)
VALUES
-- 普通用户（role=USER，status=NORMAL，用于Order/Product模块关联测试）
(
    'test_buyer',
    'buyer@test.com',
    '13800138001',
    '$2a$10$EixZaYb051F7EUvBa4sn5e965g3MfF1BuVx5F3k8WX2vF5G5H5H',  -- 密码：123456
    'MALE',  -- GenderEnum.MALE的code
    'USER',  -- UserRoleEnum.USER的code
    100,
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'NORMAL'  -- UserStatusEnum.NORMAL的code
),
-- 卖家用户（role=USER，status=NORMAL，用于发布商品）
(
    'test_seller',
    'seller@test.com',
    '13900139001',
    '$2a$10$EixZaYb051F7EUvBa4sn5e965g3MfF1BuVx5F3k8WX2vF5G5H5H',  -- 密码：123456
    'FEMALE',  -- GenderEnum.FEMALE的code
    'USER',
    90,
    '2024-01-01 09:00:00',
    '2024-01-01 09:00:00',
    'NORMAL'
),
-- 管理员用户（role=ADMIN，用于UserMapper.updateUserRole测试）
(
    'test_admin',
    'admin@test.com',
    '13700137001',
    '$2a$10$EixZaYb051F7EUvBa4sn5e965g3MfF1BuVx5F3k8WX2vF5G5H5H',  -- 密码：123456
    'UNKNOWN',  -- GenderEnum.UNKNOWN的code
    'ADMIN',  -- UserRoleEnum.ADMIN的code
    100,
    '2024-01-01 08:00:00',
    '2024-01-01 08:00:00',
    'NORMAL'
),
-- 封禁用户（status=BANNED，用于UserMapper.updateUserStatus测试）
(
    'test_banned',
    'banned@test.com',
    '13600136001',
    '$2a$10$EixZaYb051F7EUvBa4sn5e965g3MfF1BuVx5F3k8WX2vF5G5H5H',  -- 密码：123456
    'UNKNOWN',
    'USER',
    50,
    '2024-01-02 11:00:00',
    '2024-01-02 11:00:00',
    'BANNED'  -- UserStatusEnum.BANNED的code
);

-- 2. 初始化第三方账号关联数据（third_type使用枚举code）
INSERT INTO `user_third_party` (user_id, openid, third_type, access_token, bind_time, remark, is_valid)
VALUES
    (
        1,  -- test_buyer的user_id=1
        'o6_bmjrPTlm6_2sgVt7hMZOPfL2M=',
        'WECHAT',  -- ThirdPartyTypeEnum.WECHAT的code
        'sns_wa_xxx123',
        '2024-01-03 10:00:00',
        '微信账号绑定',
        1
    ),
    (
        1,
        '1234567890ABCDEF',
        'QQ',  -- ThirdPartyTypeEnum.QQ的code
        'qq_token_xxx456',
        '2024-01-04 14:00:00',
        'QQ账号绑定',
        1
    );