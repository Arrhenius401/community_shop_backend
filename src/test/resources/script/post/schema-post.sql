-- 1. 帖子表（Post实体，枚举字段status存储code，对应PostStatusEnum）
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
                        post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL COMMENT '发布者ID（关联user表user_id）',
                        like_count INT DEFAULT 0 COMMENT '点赞数',
                        post_follow_count INT DEFAULT 0 COMMENT '跟帖数',
                        title VARCHAR(100) NOT NULL COMMENT '帖子标题',
                        content TEXT NOT NULL COMMENT '帖子内容',
                        create_time DATETIME NOT NULL COMMENT '发布时间',
                        update_time DATETIME COMMENT '修改时间',
                        is_hot BOOLEAN DEFAULT FALSE COMMENT '是否热门帖',
                        is_essence BOOLEAN DEFAULT FALSE COMMENT '是否精华帖',
                        is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶帖',
                        status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '帖子状态（枚举PostStatusEnum的code：NORMAL/OFF_SHELF）',
                        FOREIGN KEY (user_id) REFERENCES `user`(user_id)
) COMMENT '社区帖子表';

-- 2. 跟帖表（PostFollow实体，枚举字段status存储code，对应PostFollowStatusEnum）
DROP TABLE IF EXISTS `post_follow`;
CREATE TABLE `post_follow` (
                               post_follow_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               post_id BIGINT NOT NULL COMMENT '所属帖子ID（关联post表post_id）',
                               user_id BIGINT NOT NULL COMMENT '跟帖者ID（关联user表user_id）',
                               parent_id BIGINT DEFAULT 0 COMMENT '父跟帖ID（0=顶级跟帖，非0=嵌套回复）',
                               content TEXT NOT NULL COMMENT '跟帖内容',
                               like_count INT DEFAULT 0 COMMENT '跟帖点赞数',
                               create_time DATETIME NOT NULL COMMENT '发布时间',
                               update_time DATETIME COMMENT '修改时间',
                               status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '跟帖状态（枚举PostFollowStatusEnum的code：NORMAL/HIDDEN）',
                               FOREIGN KEY (post_id) REFERENCES `post`(post_id),
                               FOREIGN KEY (user_id) REFERENCES `user`(user_id)
) COMMENT '帖子跟帖表';

-- 3. 帖子点赞表（UserPostLike实体，枚举字段status存储code，对应LikeStatusEnum）
DROP TABLE IF EXISTS `user_post_like`;
CREATE TABLE `user_post_like` (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  user_id BIGINT NOT NULL COMMENT '用户ID（关联user表user_id）',
                                  post_id BIGINT NOT NULL COMMENT '帖子ID（关联post表post_id）',
                                  like_time DATETIME NOT NULL COMMENT '点赞时间',
                                  status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '点赞状态（枚举LikeStatusEnum的code：ACTIVE/CANCELLED）',
                                  FOREIGN KEY (user_id) REFERENCES `user`(user_id),
                                  FOREIGN KEY (post_id) REFERENCES `post`(post_id),
                                  UNIQUE KEY uk_user_post (user_id, post_id)  -- 避免重复点赞
) COMMENT '用户帖子点赞关联表';