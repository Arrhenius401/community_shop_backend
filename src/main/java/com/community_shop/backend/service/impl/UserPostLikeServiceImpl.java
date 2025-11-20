package com.community_shop.backend.service.impl;

import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserPostLike;
import com.community_shop.backend.enums.CodeEnum.PostStatusEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.mapper.UserPostLikeMapper;
import com.community_shop.backend.service.base.PostService;
import com.community_shop.backend.service.base.UserPostLikeService;
import com.community_shop.backend.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用户点赞服务实现类
 */
@Slf4j
@Service
public class UserPostLikeServiceImpl extends BaseServiceImpl<UserPostLikeMapper, UserPostLike> implements UserPostLikeService {

    // 缓存相关常量
    private static final long CACHE_TTL_USER_LIKE_COUNT = 2; // 用户点赞总数缓存有效期（小时）
    private static final String CACHE_KEY_POST_LIKE_COUNT = "post:like:count:"; // 帖子点赞数缓存Key前缀
    private static final String CACHE_KEY_USER_LIKE_COUNT = "user:like:count:"; // 用户点赞总数缓存Key前缀
    private static final String CACHE_KEY_USER_LIKE_STATUS = "user:like:status:"; // 用户点赞状态缓存Key前缀
    private static final String CACHE_KEY_POST_LIKE_STATUS = "post:like:status:"; // 帖子点赞状态缓存Key前缀（userId:postId）
    private static final long CACHE_TTL_LIKE = 120; // 点赞相关缓存有效期（分钟）

    @Autowired
    private UserPostLikeMapper userPostLikeMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean like(Long userId, Long postId) {
        try {
            // 1. 参数校验
            if (userId == null || postId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            if(userService.getById(userId) == null){
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }
            if(postService.getById(postId) == null){
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 2. 判断是否已点赞（先查缓存，再查数据库）
            String statusCacheKey = CACHE_KEY_USER_LIKE_STATUS + userId + ":" + postId;
            Boolean isLiked = (Boolean) redisTemplate.opsForValue().get(statusCacheKey);
            if (Boolean.TRUE.equals(isLiked)) {
                log.warn("用户已点赞该帖子，用户ID：{}，帖子ID：{}", userId, postId);
                return true; // 已点赞视为成功
            }
            Integer exist = userPostLikeMapper.selectIsLiked(userId, postId);
            if (exist != null && exist > 0) {
                // 更新缓存状态
                redisTemplate.opsForValue().set(statusCacheKey, true, CACHE_TTL_LIKE, TimeUnit.MINUTES);
                log.warn("用户已点赞该帖子，用户ID：{}，帖子ID：{}", userId, postId);
                return true;
            }

            // 3. 插入点赞记录
            UserPostLike userPostLike = new UserPostLike();
            userPostLike.setUserId(userId);
            userPostLike.setPostId(postId);
            userPostLike.setCreateTime(LocalDateTime.now());
            int insertRows = userPostLikeMapper.insert(userPostLike);
            if (insertRows <= 0) {
                log.error("点赞失败，插入记录异常，用户ID：{}，帖子ID：{}", userId, postId);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 4. 更新缓存（点赞状态+帖子点赞数）
            redisTemplate.opsForValue().set(statusCacheKey, true, CACHE_TTL_LIKE, TimeUnit.MINUTES);
            String likeCountCacheKey = CACHE_KEY_POST_LIKE_COUNT + postId;
            redisTemplate.opsForValue().increment(likeCountCacheKey);   //实现对指定键（key）的数值值进行自增操作

            log.info("用户点赞成功，用户ID：{}，帖子ID：{}", userId, postId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户点赞失败", e);
            throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
        }
    }

    /**
     * 取消用户对帖子的点赞
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否取消成功
     * @throws BusinessException 用户/帖子不存在、未点赞等场景抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelLike(Long userId, Long postId) {
        try {
            // 1. 基础参数校验
            if (Objects.isNull(userId)) {
                throw new BusinessException(ErrorCode.USER_ID_NULL);
            }
            if (Objects.isNull(postId)) {
                throw new BusinessException(ErrorCode.POST_ID_NULL);
            }

            // 2. 校验用户存在
            User user = userService.getById(userId);
            if (Objects.isNull(user)) {
                log.error("取消帖子点赞失败，用户不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 3. 校验帖子存在且状态正常（未删除/未隐藏）
            Post post = postService.getById(postId);
            if (Objects.isNull(post)) {
                log.error("取消帖子点赞失败，帖子不存在，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }
            if (!PostStatusEnum.NORMAL.equals(post.getStatus())) {
                log.error("取消帖子点赞失败，帖子状态异常（非正常状态），帖子ID：{}，当前状态：{}",
                        postId, post.getStatus());
                throw new BusinessException(ErrorCode.POST_STATUS_ABNORMAL);
            }

            // 4. 先查缓存判断是否已点赞（减少数据库查询）
            String likeStatusCacheKey = CACHE_KEY_POST_LIKE_STATUS + userId + ":" + postId;
            Boolean isLiked = (Boolean) redisTemplate.opsForValue().get(likeStatusCacheKey);
            if (Boolean.FALSE.equals(isLiked) || Objects.isNull(isLiked)) {
                // 缓存未命中或已取消，查询数据库确认
                UserPostLike existingLike = userPostLikeMapper.selectByUserAndPost(userId, postId);
                if (Objects.isNull(existingLike)) {
                    log.error("取消帖子点赞失败，用户未对该帖子点赞，用户ID：{}，帖子ID：{}", userId, postId);
                    throw new BusinessException(ErrorCode.USER_NOT_LIKED_POST);
                }
            }

            // 5. 删除数据库中的点赞记录
            int deleteRows = userPostLikeMapper.deleteByUserAndPost(userId, postId);
            if (deleteRows <= 0) {
                log.error("取消帖子点赞失败，数据库删除点赞记录失败，用户ID：{}，帖子ID：{}", userId, postId);
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }

            // 6. 更新缓存：清除点赞状态缓存，更新用户点赞总数缓存（-1）
            // 清除点赞状态缓存
            redisTemplate.delete(likeStatusCacheKey);
            // 更新用户点赞总数缓存
            updateUserLikeCountCache(userId, -1);

            log.info("取消帖子点赞成功，用户ID：{}，帖子ID：{}，帖子点赞数更新后为：{}",
                    userId, postId, post.getLikeCount() - 1);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消帖子点赞异常，用户ID：{}，帖子ID：{}", userId, postId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public Boolean isLiked(Long userId, Long postId) {
        try {
            // 1. 参数校验
            if (userId == null || postId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 优先查询缓存
            String statusCacheKey = CACHE_KEY_USER_LIKE_STATUS + userId + ":" + postId;
            Boolean isLiked = (Boolean) redisTemplate.opsForValue().get(statusCacheKey);
            if (Objects.nonNull(isLiked)) {
                return isLiked;
            }

            // 3. 缓存未命中，查询数据库
            Integer exist = userPostLikeMapper.selectIsLiked(userId, postId);
            boolean result = exist != null && exist > 0;

            // 4. 更新缓存
            redisTemplate.opsForValue().set(statusCacheKey, result, CACHE_TTL_LIKE, TimeUnit.MINUTES);

            log.info("查询用户点赞状态，用户ID：{}，帖子ID：{}，结果：{}", userId, postId, result);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询用户点赞状态失败", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }
    }

    /**
     * 统计用户的点赞总数
     *
     * @param userId 用户ID
     * @return 点赞总数
     * @throws BusinessException 用户不存在场景抛出
     */
    @Override
    public int countLikesByUserId(Long userId) {
        try {
            // 1. 参数校验
            if (Objects.isNull(userId)) {
                throw new BusinessException(ErrorCode.USER_ID_NULL);
            }

            // 2. 校验用户存在
            User user = userService.getById(userId);
            if (Objects.isNull(user)) {
                log.error("统计用户点赞总数失败，用户不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 3. 先查缓存
            String cacheKey = CACHE_KEY_USER_LIKE_COUNT + userId;
            Integer cachedCount = (Integer) redisTemplate.opsForValue().get(cacheKey);
            if (Objects.nonNull(cachedCount)) {
                log.info("从缓存获取用户点赞总数，用户ID：{}，点赞总数：{}", userId, cachedCount);
                return cachedCount;
            }

            // 4. 缓存未命中，查询数据库
            int likeCount = userPostLikeMapper.countByUserId(userId);

            // 5. 缓存结果（有效期2小时）
            redisTemplate.opsForValue().set(cacheKey, likeCount, CACHE_TTL_USER_LIKE_COUNT, TimeUnit.HOURS);

            log.info("统计用户点赞总数成功，用户ID：{}，点赞总数：{}", userId, likeCount);
            return likeCount;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("统计用户点赞总数异常，用户ID：{}", userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public int countLikesByPostId(Long userId, Long postId) {
        try {
            // 1. 参数校验
            if (postId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验帖子存在
            Post post = postService.getById(postId);
            if (Objects.isNull(post)) {
                log.error("统计帖子点赞数失败，帖子不存在，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 3. 优先查询缓存
            String likeCountCacheKey = CACHE_KEY_POST_LIKE_COUNT + postId;
            Integer count = (Integer) redisTemplate.opsForValue().get(likeCountCacheKey);
            if (Objects.nonNull(count)) {
                return count;
            }

            // 4. 缓存未命中，查询数据库
            count = userPostLikeMapper.countByPostId(postId);

            // 5. 更新缓存
            redisTemplate.opsForValue().set(likeCountCacheKey, count, CACHE_TTL_LIKE, TimeUnit.MINUTES);

            log.info("统计帖子点赞数，帖子ID：{}，总数：{}", postId, count);
            return count;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("统计帖子点赞数失败", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }
    }

    /**
     * 批量删除某帖子的所有点赞记录
     *
     * @param postId 帖子ID
     * @return 删除成功数量
     * @throws BusinessException 帖子不存在场景抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteByPostId(Long postId) {
        try {
            // 1. 参数校验
            if (Objects.isNull(postId)) {
                throw new BusinessException(ErrorCode.POST_ID_NULL);
            }

            // 2. 校验帖子存在（允许已删除/隐藏的帖子，支持帖子删除时联动清理点赞）
            Post post = postService.getById(postId);
            if (Objects.isNull(post)) {
                log.error("批量删除帖子点赞记录失败，帖子不存在，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 3. 查询该帖子的所有点赞记录（用于后续更新用户点赞总数缓存）
            int totalDeleteCount = userPostLikeMapper.countByPostId(postId);
            if (totalDeleteCount <= 0) {
                log.info("批量删除帖子点赞记录，该帖子无点赞记录，帖子ID：{}", postId);
                return 0;
            }

            // 4. 批量删除数据库中的点赞记录
            int deleteRows = userPostLikeMapper.batchDeleteByPostId(postId);
            if (deleteRows <= 0) {
                log.error("批量删除帖子点赞记录失败，数据库删除操作无生效行数，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }

            // 5. 清理缓存：删除该帖子的所有点赞状态缓存，批量更新用户点赞总数缓存
            // 5.1 清理帖子点赞状态缓存（简化处理：实际项目可通过Redis模糊匹配删除，此处需结合缓存设计优化）
            log.warn("批量删除帖子点赞记录后，需手动清理该帖子的所有点赞状态缓存，帖子ID：{}（建议通过Redis批量删除工具实现）", postId);

            return deleteRows;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除帖子点赞记录异常，帖子ID：{}", postId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }


    // ---------------------- 私有辅助方法 ----------------------

    /**
     * 更新用户点赞总数缓存（支持增减）
     *
     * @param userId     用户ID
     * @param changeCount 变更数量（+1：新增点赞；-1：取消点赞）
     */
    private void updateUserLikeCountCache(Long userId, int changeCount) {
        String cacheKey = CACHE_KEY_USER_LIKE_COUNT + userId;
        // 先尝试从缓存获取当前总数并更新
        Integer currentCount = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(currentCount)) {
            int newCount = Math.max(currentCount + changeCount, 0); // 确保点赞总数不小于0
            redisTemplate.opsForValue().set(cacheKey, newCount, CACHE_TTL_USER_LIKE_COUNT, TimeUnit.HOURS);
            log.info("更新用户点赞总数缓存，用户ID：{}，变更数量：{}，更新后总数：{}",
                    userId, changeCount, newCount);
        } else {
            // 缓存未命中，触发主动查询（后续请求会命中缓存）
            countLikesByUserId(userId);
            log.info("用户点赞总数缓存未命中，触发主动查询统计，用户ID：{}", userId);
        }
    }
}
