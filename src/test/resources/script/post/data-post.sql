-- 1. 初始化帖子数据（枚举字段status使用枚举code）
INSERT INTO `post` (user_id, like_count, post_follow_count, title, content, create_time, is_hot, is_essence, is_top, status)
VALUES
-- 置顶精华帖（status=NORMAL，用于PostMapper.selectTopPosts测试）
(
    3,  -- test_admin的user_id=3
    100,
    20,
    '【置顶】社区交易规则',
    '规范交易行为，维护社区环境...',
    '2024-01-01 08:30:00',
    TRUE,
    TRUE,
    TRUE,
    'NORMAL'  -- PostStatusEnum.NORMAL的code
),
-- 普通用户帖子（status=NORMAL，用于PostMapper.selectByUserId测试）
(
    1,  -- test_buyer的user_id=1
    30,
    5,
    '求推荐二手安卓手机',
    '预算3000左右，求性价比高的机型',
    '2024-01-02 11:00:00',
    FALSE,
    FALSE,
    FALSE,
    'NORMAL'
),
-- 下架帖子（status=OFF_SHELF，用于PostMapper.updateStatus测试）
(
    1,
    5,
    1,
    '闲置物品转让（已售）',
    '闲置书架，自提100元',
    '2024-01-03 15:00:00',
    FALSE,
    FALSE,
    FALSE,
    'OFF_SHELF'  -- PostStatusEnum.OFF_SHELF的code
);

-- 2. 初始化跟帖数据（枚举字段status使用枚举code）
INSERT INTO `post_follow` (post_id, user_id, parent_id, content, like_count, create_time, status)
VALUES
-- 顶级跟帖（status=NORMAL）
(
    1,  -- 置顶帖post_id=1
    2,  -- test_seller的user_id=2
    0,
    '支持规则，共同维护社区！',
    15,
    '2024-01-01 09:00:00',
    'NORMAL'  -- PostFollowStatusEnum.NORMAL的code
),
-- 嵌套回复（status=NORMAL）
(
    1,
    1,
    1,  -- 父跟帖ID=1
    '纠纷处理流程在哪看？',
    3,
    '2024-01-01 09:10:00',
    'NORMAL'
),
-- 隐藏跟帖（status=HIDDEN，用于PostFollowMapper.updateStatus测试）
(
    2,  -- 普通帖子post_id=2
    4,  -- 封禁用户user_id=4
    0,
    '违规广告内容',
    0,
    '2024-01-02 11:30:00',
    'HIDDEN'  -- PostFollowStatusEnum.HIDDEN的code
);

-- 3. 初始化点赞数据（枚举字段status使用枚举code）
INSERT INTO `user_post_like` (user_id, post_id, like_time, status)
VALUES
-- 有效点赞（status=ACTIVE，用于UserPostLikeMapper.selectByUserAndPost测试）
(
    1,
    2,  -- 普通帖子post_id=2
    '2024-01-02 11:10:00',
    'ACTIVE'  -- LikeStatusEnum.ACTIVE的code
),
-- 取消点赞（status=CANCELLED，用于UserPostLikeMapper.deleteByUserAndPost测试）
(
    1,
    3,  -- 下架帖子post_id=3
    '2024-01-03 15:10:00',
    'CANCELLED'  -- LikeStatusEnum.CANCELLED的code
);