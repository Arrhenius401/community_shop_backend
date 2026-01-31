-- 0. 纯测试环境专用：清空数据库所有对象（表、约束、索引等），消除依赖问题
DROP ALL OBJECTS;

-- 1. 用户表（User实体，枚举字段status/role存储枚举code，对应UserStatusEnum/UserRoleEnum）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL COMMENT '用户名',
                        email VARCHAR(100) UNIQUE COMMENT '邮箱（登录用）',
                        phone_number VARCHAR(20) UNIQUE COMMENT '手机号（登录用）',
                        password VARCHAR(100) NOT NULL COMMENT 'BCrypt加密后的密码',
                        avatar_url VARCHAR(255) COMMENT '头像URL',
                        bio VARCHAR(200) COMMENT '个人简介',
                        gender VARCHAR(10) DEFAULT 'UNKNOWN' COMMENT '性别（枚举GenderEnum的code：MALE/FEMALE/UNKNOWN）',
                        role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色（枚举UserRoleEnum的code：USER/ADMIN）',
                        credit_score INT DEFAULT 100 COMMENT '信用分',
                        follower_count INT DEFAULT 0 COMMENT '粉丝数',
                        post_count INT DEFAULT 0 COMMENT '发帖数',
                        create_time DATETIME NOT NULL COMMENT '注册时间',
                        update_time DATETIME NOT NULL COMMENT '最后活跃时间',
                        status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '账号状态（枚举UserStatusEnum的code：NORMAL/BANNED）'
) COMMENT '用户核心信息表';

-- 2. 第三方账号关联表（UserThirdParty实体，枚举third_type存储code，对应ThirdPartyTypeEnum）
DROP TABLE IF EXISTS `user_third_party`;
CREATE TABLE `user_third_party` (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    user_id BIGINT NOT NULL COMMENT '关联用户ID（user表user_id）',
                                    openid VARCHAR(100) NOT NULL COMMENT '第三方唯一标识（如微信openid）',
                                    third_type VARCHAR(20) NOT NULL COMMENT '第三方类型（枚举ThirdPartyTypeEnum的code：WECHAT/QQ/ALIPAY）',
                                    access_token VARCHAR(255) COMMENT '第三方临时凭证（加密存储）',
                                    bind_time DATETIME NOT NULL COMMENT '绑定时间',
                                    remark VARCHAR(200) COMMENT '备注',
                                    is_valid INT DEFAULT 1 COMMENT '绑定状态（1=有效，0=已解绑）',
                                    FOREIGN KEY (user_id) REFERENCES `user`(user_id)
) COMMENT '用户第三方账号关联表';