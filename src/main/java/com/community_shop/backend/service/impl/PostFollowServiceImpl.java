package com.community_shop.backend.service.impl;

import com.community_shop.backend.dto.post.PostFollowPublishDTO;
import com.community_shop.backend.dto.post.PostFollowUpdateDTO;
import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import com.community_shop.backend.enums.CodeEnum.PostStatusEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.PostFollow;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.PostFollowMapper;
import com.community_shop.backend.mapper.PostMapper;
import com.community_shop.backend.service.base.PostFollowService;
import com.community_shop.backend.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 跟帖模块Service实现类
 * 实现跟帖全生命周期管理、互动数据维护及跨模块协同逻辑
 */
@Slf4j
@Service
public class PostFollowServiceImpl implements PostFollowService {

    // 缓存相关常量
    private static final String CACHE_KEY_POST_FOLLOW = "post:follow:"; // 跟帖缓存Key前缀
    private static final long CACHE_TTL_POST_FOLLOW = 1800; // 跟帖缓存有效期（30分钟，单位：秒）

    @Autowired
    private PostFollowMapper postFollowMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基础新增：插入跟帖记录
     */
    @Override
    public Long insertPostFollow(PostFollow postFollow) {
        try {
            // 1. 参数校验：核心字段非空校验
            if (postFollow == null || postFollow.getPostId() == null || postFollow.getUserId() == null
                    || !StringUtils.hasText(postFollow.getContent())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 初始化跟帖基础数据
            postFollow.setLikeCount(0); // 初始点赞数为0
            postFollow.setStatus(PostFollowStatusEnum.NORMAL); // 初始状态为正常
            postFollow.setCreateTime(LocalDateTime.now()); // 填充创建时间
            postFollow.setUpdateTime(LocalDateTime.now()); // 填充更新时间

            // 3. 插入数据库
            int insertRows = postFollowMapper.insert(postFollow);
            if (insertRows <= 0) {
                log.error("插入跟帖失败，跟帖信息：{}", postFollow);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 4. 返回自增主键（跟帖ID）
            log.info("插入跟帖成功，跟帖ID：{}，所属帖子ID：{}", postFollow.getPostFollowId(), postFollow.getPostId());
            return postFollow.getPostFollowId();
        } catch (BusinessException e) {
            throw e; // 抛出业务异常，由全局处理器处理
        } catch (Exception e) {
            log.error("插入跟帖异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 基础查询：通过跟帖ID查询详情（关联作者信息）
     */
    @Override
    public PostFollow selectPostFollowById(Long postFollowId) {
        // 1. 参数校验：跟帖ID非空
        if (postFollowId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 2. 先查缓存
        String cacheKey = CACHE_KEY_POST_FOLLOW + postFollowId;
        PostFollow postFollow = (PostFollow) redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(postFollow)) {
            return postFollow;
        }

        // 3. 缓存未命中：查询数据库
        postFollow = postFollowMapper.selectById(postFollowId);
        if (postFollow == null || postFollow.getStatus().equals(PostFollowStatusEnum.DELETED)) {
            log.warn("跟帖不存在或已删除，跟帖ID：{}", postFollowId);
            throw new BusinessException(ErrorCode.POST_FOLLOW_NOT_EXISTS);
        }

        // 4. 缓存跟帖详情
        redisTemplate.opsForValue().set(cacheKey, postFollow, CACHE_TTL_POST_FOLLOW);
        log.info("查询跟帖详情成功，跟帖ID：{}", postFollowId);
        return postFollow;
    }

    /**
     * 基础更新：编辑跟帖内容
     */
    @Override
    public int updatePostFollowContent(PostFollowUpdateDTO vo, Long userId) {
        try {
            // 1. 参数校验：VO、跟帖ID、操作用户ID非空，内容非空
            if (vo == null || vo.getPostFollowId() == null || userId == null
                    || !StringUtils.hasText(vo.getNewContent())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验跟帖存在且未删除
            PostFollow postFollow = postFollowMapper.selectById(vo.getPostFollowId());
            if (postFollow == null || postFollow.getStatus().equals(PostFollowStatusEnum.DELETED)) {
                log.warn("编辑失败：跟帖不存在或已删除，跟帖ID：{}", vo.getPostFollowId());
                throw new BusinessException(ErrorCode.POST_FOLLOW_NOT_EXISTS);
            }

            // 3. 权限校验：仅作者可编辑
            if (!postFollow.getUserId().equals(userId)) {
                log.warn("编辑失败：无权限，操作用户ID：{}，跟帖作者ID：{}", userId, postFollow.getUserId());
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 构建更新数据
            postFollow.setContent(vo.getNewContent());
            postFollow.setUpdateTime(LocalDateTime.now()); // 更新时间戳

            // 5. 执行更新
            int updateRows = postFollowMapper.updateById(postFollow);
            if (updateRows <= 0) {
                log.error("编辑跟帖内容失败，跟帖ID：{}，更新内容：{}", vo.getPostFollowId(), vo.getNewContent());
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 6. 清除缓存
            redisTemplate.delete(CACHE_KEY_POST_FOLLOW + vo.getPostFollowId());
            log.info("编辑跟帖内容成功，跟帖ID：{}", vo.getPostFollowId());
            return updateRows;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("编辑跟帖内容异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 基础删除：逻辑删除跟帖
     */
    @Override
    public int deletePostFollowById(Long postFollowId) {
        // 1. 参数校验：跟帖ID非空
        if (postFollowId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 2. 校验跟帖存在
        PostFollow postFollow = postFollowMapper.selectById(postFollowId);
        if (postFollow == null || postFollow.getStatus().equals(PostFollowStatusEnum.DELETED)) {
            log.warn("删除失败：跟帖不存在或已删除，跟帖ID：{}", postFollowId);
            throw new BusinessException(ErrorCode.POST_FOLLOW_NOT_EXISTS);
        }

        // 3. 执行逻辑删除（更新is_deleted=1）
        int deleteRows = postFollowMapper.deleteById(postFollowId);
        if (deleteRows <= 0) {
            log.error("逻辑删除跟帖失败，跟帖ID：{}", postFollowId);
            throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
        }

        // 4. 清除缓存及关联缓存
        redisTemplate.delete(CACHE_KEY_POST_FOLLOW + postFollowId);
        // 清除所属帖子的跟帖列表缓存（格式：post:follow:{postId}:{offset}:{limit}）
        redisTemplate.delete(CACHE_KEY_POST_FOLLOW + postFollow.getPostId() + ":*");
        log.info("逻辑删除跟帖成功，跟帖ID：{}", postFollowId);
        return deleteRows;
    }

    /**
     * 列表查询：按帖子ID分页查询有效跟帖
     */
    @Override
    public List<PostFollow> selectPostFollowsByPostId(Long postId, int offset, int limit) {
        // 1. 参数校验：帖子ID非空，分页参数合法
        if (postId == null || offset < 0 || limit <= 0) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 2. 校验帖子存在且未删除
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus().equals(PostStatusEnum.DELETED)) {
            log.warn("查询跟帖列表失败：帖子不存在或已删除，帖子ID：{}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
        }

        // 3. 构建缓存Key（包含分页参数）
        String cacheKey = CACHE_KEY_POST_FOLLOW + postId + ":" + offset + ":" + limit;

        // 4. 先查缓存
        List<PostFollow> postFollowList = (List<PostFollow>) redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(postFollowList) && !postFollowList.isEmpty()) {
//            // 5. 缓存命中：补充作者信息
//            fillAuthorInfo(postFollowList);
            return postFollowList;
        }

        // 6. 缓存未命中：查询数据库（有效跟帖：is_deleted=0、status=NORMAL，按创建时间倒序）
        postFollowList = postFollowMapper.selectByPostId(postId, offset, limit);
        if (Objects.isNull(postFollowList)) {
            log.warn("查询跟帖列表为空，帖子ID：{}，偏移量：{}，每页条数：{}", postId, offset, limit);
            return List.of();
        }

//        // 7. 补充作者信息
//        fillAuthorInfo(postFollowList);

        // 8. 缓存跟帖列表
        redisTemplate.opsForValue().set(cacheKey, postFollowList, CACHE_TTL_POST_FOLLOW);
        log.info("查询跟帖列表成功，帖子ID：{}，返回数量：{}", postId, postFollowList.size());
        return postFollowList;
    }

    /**
     * 统计查询：统计指定帖子的有效跟帖总数
     */
    @Override
    public int countPostFollowsByPostId(Long postId) {
        // 1. 参数校验：帖子ID非空
        if (postId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 2. 校验帖子存在且未删除
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus().equals(PostStatusEnum.DELETED)) {
            log.warn("统计跟帖数失败：帖子不存在或已删除，帖子ID：{}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
        }

        // 3. 查询数据库统计有效跟帖数
        int count = postFollowMapper.countByPostId(postId);
        log.info("统计帖子有效跟帖数成功，帖子ID：{}，跟帖总数：{}", postId, count);
        return count;
    }

    /**
     * 业务方法：发布跟帖（含帖子评论数同步）
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Long publishPostFollow(PostFollowPublishDTO vo, Long userId) {
        try {
            // 1. 参数校验：VO、帖子ID、操作用户ID非空，内容非空
            if (vo == null || vo.getPostId() == null || userId == null
                    || !StringUtils.hasText(vo.getContent())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验用户存在
            User user = userService.selectUserById(userId);
            if (Objects.isNull(user)) {
                log.warn("发布跟帖失败：用户不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 3. 校验帖子存在且未删除
            Post post = postMapper.selectById(vo.getPostId());
            if (post == null || post.getStatus().equals(PostStatusEnum.DELETED)) {
                log.warn("发布跟帖失败：帖子不存在或已删除，帖子ID：{}", vo.getPostId());
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 4. 构建跟帖实体
            PostFollow postFollow = new PostFollow();
            postFollow.setPostId(vo.getPostId());
            postFollow.setUserId(userId);
            postFollow.setContent(vo.getContent());

            // 5. 插入跟帖记录（调用基础新增方法）
            Long postFollowId = insertPostFollow(postFollow);

            // 6. 同步更新帖子的评论数（+1）
            int updateRows = postMapper.updatePostFollowCount(vo.getPostId(), post.getPostFollowCount() + 1);
            if (updateRows <= 0) {
                log.error("同步帖子评论数失败，帖子ID：{}，当前评论数：{}", vo.getPostId(), post.getPostFollowCount());
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED); // 触发事务回滚
            }

            // 7. 清除帖子的跟帖列表缓存
            redisTemplate.delete(CACHE_KEY_POST_FOLLOW + vo.getPostId() + ":*");
            log.info("发布跟帖成功，跟帖ID：{}，帖子评论数已同步更新", postFollowId);
            return postFollowId;
        } catch (BusinessException e) {
            throw e; // 抛出业务异常，触发事务回滚
        } catch (Exception e) {
            log.error("发布跟帖异常", e);
            throw new BusinessException(ErrorCode.FAILURE); // 触发事务回滚
        }
    }

//    /**
//     * 业务方法：更新跟帖点赞数
//     */
//    @Override
//    public int updatePostFollowLikeCount(Long postFollowId, int likeCount) {
//        try {
//            // 1. 参数校验：跟帖ID非空
//            if (postFollowId == null) {
//                throw new BusinessException(ErrorCode.PARAM_NULL);
//            }
//
//            // 2. 校验跟帖存在且未删除
//            PostFollow postFollow = postFollowMapper.selectById(postFollowId);
//            if (postFollow == null || postFollow.isDeleted()) {
//                log.warn("更新点赞数失败：跟帖不存在或已删除，跟帖ID：{}", postFollowId);
//                throw new BusinessException(ErrorCode.POST_FOLLOW_NOT_EXISTS);
//            }
//
//            // 3. 执行点赞数更新
//            int updateRows = postFollowMapper.updateLikeCount(postFollowId, likeCount);
//            if (updateRows <= 0) {
//                log.error("更新跟帖点赞数失败，跟帖ID：{}，目标点赞数：{}", postFollowId, likeCount);
//                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
//            }
//
//            // 4. 清除缓存
//            redisTemplate.delete(CACHE_KEY_POST_FOLLOW + postFollowId);
//            log.info("更新跟帖点赞数成功，跟帖ID：{}，新点赞数：{}", postFollowId, likeCount);
//            return updateRows;
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("更新跟帖点赞数异常", e);
//            throw new BusinessException(ErrorCode.FAILURE);
//        }
//    }

    /**
     * 业务方法：更新跟帖状态（管理员操作）
     */
    @Override
    public int updatePostFollowStatus(Long postFollowId, PostFollowStatusEnum status, Long adminId) {
        try {
            // 1. 参数校验：跟帖ID、状态、管理员ID非空，状态值合法
            if (postFollowId == null || status == null || adminId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 管理员权限校验（调用UserService验证用户是否为管理员角色）
            User admin = userService.selectUserById(adminId);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                log.warn("更新跟帖状态失败：非管理员操作，用户ID：{}", adminId);
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 3. 校验跟帖存在且未删除
            PostFollow postFollow = postFollowMapper.selectById(postFollowId);
            if (postFollow == null || postFollow.getStatus().equals(PostFollowStatusEnum.DELETED)) {
                log.warn("更新跟帖状态失败：跟帖不存在或已删除，跟帖ID：{}", postFollowId);
                throw new BusinessException(ErrorCode.POST_FOLLOW_NOT_EXISTS);
            }

            // 4. 执行状态更新
            int updateRows = postFollowMapper.updateStatus(postFollowId, status);
            if (updateRows <= 0) {
                log.error("更新跟帖状态失败，跟帖ID：{}，目标状态：{}", postFollowId, status);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 5. 清除缓存（跟帖详情缓存+所属帖子的跟帖列表缓存）
            redisTemplate.delete(CACHE_KEY_POST_FOLLOW + postFollowId);
            redisTemplate.delete(CACHE_KEY_POST_FOLLOW + postFollow.getPostId() + ":*");
            log.info("更新跟帖状态成功，跟帖ID：{}，旧状态：{}，新状态：{}，操作管理员ID：{}",
                    postFollowId, postFollow.getStatus(), status, adminId);
            return updateRows;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新跟帖状态异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

//    /**
//     * 辅助方法：为跟帖列表补充作者信息（昵称、头像）
//     * @param postFollowList 跟帖列表
//     */
//    private void fillAuthorInfo(List<PostFollow> postFollowList) {
//        if (postFollowList.isEmpty()) {
//            return;
//        }
//        // 批量查询作者信息（减少UserService调用次数，优化性能）
//        List<Long> authorIds = postFollowList.stream()
//                .map(PostFollow::getUserId)
//                .distinct()
//                .collect(Collectors.toList());
//        Map<Long, User> authorMap = userService.selectUsersByIds(authorIds).stream()
//                .collect(Collectors.toMap(User::getUserId, user -> user));
//
//        // 为每个跟帖填充作者信息
//        for (PostFollow postFollow : postFollowList) {
//            User author = authorMap.get(postFollow.getUserId());
//            if (author != null) {
//                postFollow.setAuthorName(author.getUsername());
//                postFollow.setAuthorAvatar(author.getAvatarUrl());
//            }
//        }
//    }

}
