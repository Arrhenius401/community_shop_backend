package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.PostConvert;
import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.enums.CodeEnum.PostStatusEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.enums.SortEnum.PostSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserPostLike;
import com.community_shop.backend.mapper.PostMapper;
import com.community_shop.backend.service.base.*;
import com.community_shop.backend.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
public class PostServiceImpl extends BaseServiceImpl<PostMapper, Post> implements PostService {


    // 缓存相关常量
    private static final Duration CACHE_TTL_HOT_POST = Duration.ofHours(2); // 热门帖子缓存2小时

    // 业务常量
    private static final Integer PUBLISH_MIN_CREDIT = 60; // 发布帖子最低信用分
    private static final Integer MAX_TOP_POST_COUNT = 5; // 置顶帖子最大数量
    private static final Integer MAX_DAILY_LIKE_TIMES = 50; // 用户每日点赞最大次数

    private static final Integer MAX_POST_TITLE_LENGTH = 50; // 帖子标题最大长度（发布时）
    private static final Integer MAX_POST_CONTENT_LENGTH = 2000; // 帖子内容最大长度（发布时）
    private static final Integer MAX_UPDATE_TITLE_LENGTH = 100; // 帖子标题最大长度（编辑时）
    private static final Integer MAX_UPDATE_CONTENT_LENGTH = 5000; // 帖子内容最大长度（编辑时）
    private static final Integer MAX_IMAGE_COUNT = 9; // 帖子图片最大数量

    // 缓存相关常量
    private static final String CACHE_KEY_POST_DETAIL = "post:detail:"; // 帖子详情缓存Key前缀
    private static final String CACHE_KEY_POST_LIST = "post:list:"; // 帖子列表缓存Key前缀
    private static final String CACHE_KEY_USER_LIKE_TIMES = "post:like:daily:"; // 用户每日点赞次数缓存Key前缀
    private static final String CACHE_KEY_TOP_POSTS = "post:top:list"; // 置顶帖子列表缓存Key
    private static final long CACHE_TTL_POST_DETAIL = 1; // 帖子详情缓存有效期（小时）
    private static final long CACHE_TTL_POST_LIST = 30; // 帖子列表缓存有效期（分钟）
    private static final long CACHE_TTL_USER_LIKE_TIMES = 24; // 用户每日点赞次数缓存有效期（小时）
    private static final long CACHE_TTL_TOP_POSTS = 2; // 置顶帖子列表缓存有效期（小时）

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserPostLikeService userPostLikeService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PostConvert postConvert;

    @Autowired
    private OssUtil ossUtil;

    /**
     * 发布帖子
     *
     * @param userId 发布者ID
     * @param postPublishDTO 帖子发布参数
     * @return 发布成功的帖子详情
     * @throws BusinessException 信用分不足（<60分）、内容违规时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailDTO publishPost(Long userId, PostPublishDTO postPublishDTO) {
        try {
            // 1. 参数校验（标题、内容、图片数量）
            validatePublishParam(postPublishDTO);

            // 2. 校验发布者存在且信用分达标（≥60分）
            User publisher = userService.getById(userId);
            if (Objects.isNull(publisher)) {
                log.error("发布帖子失败，发布者不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }
            if (publisher.getCreditScore() < PUBLISH_MIN_CREDIT) {
                log.error("发布帖子失败，用户信用分不足，用户ID：{}，信用分：{}，最低要求：{}",
                        userId, publisher.getCreditScore(), PUBLISH_MIN_CREDIT);
                throw new BusinessException(ErrorCode.CREDIT_TOO_LOW);
            }

            // 3. 处理图片URL（JSON格式转数组，校验数量≤9张）
            List<String> imageUrlList = parseAndValidateImageUrls(postPublishDTO.getImageUrls());

            // 4. 构建Post实体（初始化状态、时间、互动数）
            Post post = buildPostEntity(postPublishDTO, userId, imageUrlList, publisher);

            // 5. 插入数据库并更新用户发帖数
            int insertRows = postMapper.insert(post);
            if (insertRows <= 0) {
                log.error("发布帖子失败，数据库插入失败，发布参数：{}", postPublishDTO);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 6. 封装帖子详情DTO（关联发布者脱敏信息）
            PostDetailDTO detailDTO = buildPostDetailDTO(post, publisher, false);

            // 7. 缓存帖子详情（新发布帖子优先缓存）
            cachePostDetail(detailDTO);

            log.info("发布帖子成功，帖子ID：{}，发布者ID：{}，标题：{}",
                    post.getPostId(), userId, postPublishDTO.getTitle());
            return detailDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发布帖子异常，发布参数：{}，用户ID：{}", postPublishDTO, userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 编辑帖子
     *
     * @param postId 帖子ID
     * @param userId 操作用户ID
     * @param postUpdateDTO 编辑参数（标题、内容、图片等）
     * @return 编辑后的帖子详情
     * @throws BusinessException 无权限（非作者）、帖子已删除时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailDTO updatePost(Long postId, Long userId, PostUpdateDTO postUpdateDTO) {
        try {
            // 1. 基础参数校验
            if (Objects.isNull(postUpdateDTO) || Objects.isNull(postId) || Objects.isNull(userId)) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            // 校验编辑内容合法性
            validateUpdateParam(postUpdateDTO);

            // 2. 校验帖子存在且状态正常（未删除）
            Post post = postMapper.selectById(postId);
            if (Objects.isNull(post)) {
                log.error("编辑帖子失败，帖子不存在，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }
            if (PostStatusEnum.DELETED.equals(post.getStatus())) {
                log.error("编辑帖子失败，帖子已删除，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_ALREADY_DELETED);
            }

            // 3. 校验操作权限（仅作者可编辑）
            if (!post.getUserId().equals(userId)) {
                log.error("编辑帖子失败，无权限操作（非作者），帖子ID：{}，操作用户ID：{}，实际作者ID：{}",
                        postId, userId, post.getUserId());
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 构建更新实体（仅更新标题、内容、更新时间）
            Post updatePost = new Post();
            updatePost.setPostId(postId);
            updatePost.setTitle(postUpdateDTO.getTitle());
            updatePost.setContent(postUpdateDTO.getContent());
            updatePost.setUpdateTime(LocalDateTime.now());

            // 5. 执行更新
            int updateRows = postMapper.updateById(updatePost);
            if (updateRows <= 0) {
                log.error("编辑帖子失败，数据库更新无生效行数，帖子ID：{}，更新参数：{}", postId, postUpdateDTO);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 6. 查询更新后的数据及发布者信息
            Post updatedPost = postMapper.selectById(postId);
            User publisher = userService.getById(userId);
            if (Objects.isNull(publisher)) {
                log.error("编辑帖子后查询发布者失败，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 7. 封装详情DTO并更新缓存（清除旧缓存，缓存新数据）
            clearPostDetailCache(postId);
            PostDetailDTO detailDTO = buildPostDetailDTO(updatedPost, publisher, userPostLikeService.isLiked(userId, postId));
            cachePostDetail(detailDTO);

            log.info("编辑帖子成功，帖子ID：{}，操作用户ID：{}", postId, userId);
            return detailDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("编辑帖子异常，帖子ID：{}，更新参数：{}，操作用户ID：{}", postId, postUpdateDTO, userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 帖子点赞/取消点赞
     *
     * @param postLikeDTO 点赞参数（帖子ID、用户ID、操作类型）
     * @return 操作后的点赞数
     * @throws BusinessException 帖子不存在、每日点赞次数超限时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateLikeStatus(PostLikeDTO postLikeDTO) {
        try {
            // 1. 参数校验
            if (Objects.isNull(postLikeDTO) || Objects.isNull(postLikeDTO.getPostId())
                    || Objects.isNull(postLikeDTO.getUserId()) || Objects.isNull(postLikeDTO.getIsLike())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            Long postId = postLikeDTO.getPostId();
            Long userId = postLikeDTO.getUserId();
            boolean isLike = postLikeDTO.getIsLike();

            // 2. 校验帖子存在且状态正常
            Post post = postMapper.selectById(postId);
            if (Objects.isNull(post)) {
                log.error("帖子点赞失败，帖子不存在，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }
            if (!PostStatusEnum.NORMAL.equals(post.getStatus())) {
                log.error("帖子点赞失败，帖子状态异常，帖子ID：{}，当前状态：{}", postId, post.getStatus());
                throw new BusinessException(ErrorCode.POST_STATUS_INVALID);
            }

            // 3. 校验用户存在
            User user = userService.getById(userId);
            if (Objects.isNull(user)) {
                log.error("帖子点赞失败，用户不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 4. 点赞操作：校验每日点赞次数，取消点赞无需校验
            if (isLike && !checkDailyLikeLimit(userId)) {
                log.error("帖子点赞失败，用户每日点赞次数超限，用户ID：{}，最大次数：{}", userId, MAX_DAILY_LIKE_TIMES);
                throw new BusinessException(ErrorCode.DAILY_LIKE_TIMES_EXCEED);
            }

            // 5. 判断用户当前点赞状态（已点赞/未点赞）
            Boolean isLiked = userPostLikeService.isLiked(userId, postId);
            int likeCountChange; // 点赞数变更量（+1/-1）

            if (isLike) {
                // 点赞操作：未点赞则新增记录，已点赞则忽略
                if (Objects.isNull(isLiked)) {
                    UserPostLike userPostLike = new UserPostLike();
                    userPostLike.setUserId(userId);
                    userPostLike.setPostId(postId);
                    userPostLike.setCreateTime(LocalDateTime.now());
                    userPostLikeService.save(userPostLike);
                    likeCountChange = 1;
                    // 增加用户当日点赞次数
                    incrementDailyLikeTimes(userId);
                } else {
                    log.info("用户已点赞该帖子，无需重复操作，用户ID：{}，帖子ID：{}", userId, postId);
                    return post.getLikeCount();
                }
            } else {
                // 取消点赞操作：已点赞则删除记录，未点赞则忽略
                if (Objects.nonNull(isLiked)) {
                    userPostLikeService.cancelLike(userId, postId);
                    likeCountChange = -1;
                    // 减少用户当日点赞次数（可选，根据业务是否需要回滚次数）
                    decrementDailyLikeTimes(userId);
                } else {
                    log.info("用户未点赞该帖子，无需取消操作，用户ID：{}，帖子ID：{}", userId, postId);
                    return post.getLikeCount();
                }
            }

            // 6. 更新帖子点赞数
            int newLikeCount = post.getLikeCount() + likeCountChange;
            postMapper.updateLikeCount(postId, likeCountChange);

            // 7. 更新缓存（帖子详情、置顶列表缓存）
            clearPostDetailCache(postId);
            if (post.getIsTop()) {
                clearTopPostsCache();
            }

            log.info("帖子点赞状态更新成功，帖子ID：{}，用户ID：{}，操作类型：{}，更新后点赞数：{}",
                    postId, userId, isLike ? "点赞" : "取消点赞", newLikeCount);
            return newLikeCount;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("帖子点赞状态更新异常，参数：{}", postLikeDTO, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 管理员设置帖子精华/置顶
     *
     * @param userId 管理员ID
     * @param postEssenceTopDTO 状态设置参数（帖子ID、管理员ID、状态）
     * @return 设置是否成功
     * @throws BusinessException 无管理员权限、置顶数超5篇时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setEssenceOrTop(Long userId, PostEssenceTopDTO postEssenceTopDTO) {
        try {
            // 1. 参数校验（非空）
            if (Objects.isNull(postEssenceTopDTO) || Objects.isNull(postEssenceTopDTO.getPostId())
                    || Objects.isNull(postEssenceTopDTO.getIsEssence()) || Objects.isNull(postEssenceTopDTO.getIsTop())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            Long postId = postEssenceTopDTO.getPostId();
            boolean isEssence = postEssenceTopDTO.getIsEssence();
            boolean isTop = postEssenceTopDTO.getIsTop();

            // 2. 校验管理员权限
            User admin = userService.getById(userId);
            if (Objects.isNull(admin) || !UserRoleEnum.ADMIN.equals(admin.getRole())) {
                log.error("设置帖子精华/置顶失败，无管理员权限，操作人ID：{}，角色：{}",
                        userId, Objects.nonNull(admin) ? admin.getRole() : "未知");
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 3. 校验帖子存在且状态正常
            Post post = postMapper.selectById(postId);
            if (Objects.isNull(post)) {
                log.error("设置帖子精华/置顶失败，帖子不存在，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }
            if (PostStatusEnum.DELETED.equals(post.getStatus())) {
                log.error("设置帖子精华/置顶失败，帖子已删除，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_ALREADY_DELETED);
            }

            // 4. 置顶操作校验：若设置为置顶，需确保当前置顶数≤5
            if (isTop && !post.getIsTop()) {
                int currentTopCount = postMapper.countTopPosts();
                if (currentTopCount >= MAX_TOP_POST_COUNT) {
                    log.error("设置帖子置顶失败，当前置顶数已达上限，上限：{}，当前：{}", MAX_TOP_POST_COUNT, currentTopCount);
                    throw new BusinessException(ErrorCode.TOP_POST_COUNT_EXCEED);
                }
            }

            // 5. 执行状态更新（精华/置顶）
            int updateRows = postMapper.updatePostEssenceAndTop(postId, isEssence, isTop);
            if (updateRows <= 0) {
                log.error("设置帖子精华/置顶失败，数据库更新无生效行数，参数：{}", postEssenceTopDTO);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 6. 更新缓存（帖子详情、置顶列表缓存）
            clearPostDetailCache(postId);
            clearTopPostsCache();

            log.info("设置帖子精华/置顶成功，帖子ID：{}，管理员ID：{}，是否精华：{}，是否置顶：{}",
                    postId, userId, isEssence, isTop);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("设置帖子精华/置顶异常，参数：{}", postEssenceTopDTO, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 多条件查询帖子列表
     *
     * @param postQueryDTO 查询参数（关键词、排序、分页）
     * @return 分页帖子列表（轻量展示）
     */
    @Override
    public PageResult<PostListItemDTO> queryPosts(PostQueryDTO postQueryDTO) {
        try {
            // 1. 参数校验与默认值处理
            if (Objects.isNull(postQueryDTO)) {
                postQueryDTO = new PostQueryDTO();
            }
            // 分页参数默认值（pageNum=1，pageSize=10）
            int pageNum = Objects.nonNull(postQueryDTO.getPageNum()) ? postQueryDTO.getPageNum() : 1;
            int pageSize = Objects.nonNull(postQueryDTO.getPageSize()) ? postQueryDTO.getPageSize() : 10;
            // 排序参数默认值（按发布时间降序）
            PostSortFieldEnum sortField = Objects.nonNull(postQueryDTO.getSortField())
                    ? postQueryDTO.getSortField()
                    : PostSortFieldEnum.CREATE_TIME;
            SortDirectionEnum sortDir = Objects.nonNull(postQueryDTO.getSortDir())
                    ? postQueryDTO.getSortDir()
                    : SortDirectionEnum.DESC;
            postQueryDTO.setSortField(sortField);
            postQueryDTO.setSortDir(sortDir);

            // 2. 构建缓存Key（基于查询参数，确保不同条件缓存隔离）
            String cacheKey = buildPostListCacheKey(postQueryDTO, pageNum, pageSize);
            // 先查缓存
            PageResult<PostListItemDTO> cachedPageResult = (PageResult<PostListItemDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (Objects.nonNull(cachedPageResult)) {
                log.info("从缓存获取帖子列表成功，缓存Key：{}", cacheKey);
                return cachedPageResult;
            }

            // 3. 分页查询数据库（只查正常状态的帖子）
            long total = postMapper.countByQuery(postQueryDTO);
            List<Post> postList = postMapper.selectByQuery(postQueryDTO);
            Long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;

            // 4. 转换为PostListItemDTO（关联发布者极简信息，处理首图）
            List<PostListItemDTO> dtoList = convertToPostListItemDTO(postList);

            // 5. 封装分页结果
            PageResult<PostListItemDTO> pageResult = new PageResult<>();
            pageResult.setList(dtoList);
            pageResult.setTotal(total);
            pageResult.setTotalPages(totalPages);
            pageResult.setPageNum(pageNum);
            pageResult.setPageSize(pageSize);

            // 6. 缓存结果
            redisTemplate.opsForValue().set(cacheKey, pageResult, CACHE_TTL_POST_LIST, TimeUnit.MINUTES);

            log.info("查询帖子列表成功，查询参数：{}，分页：{}页/{}条，总条数：{}，总页数：{}",
                    postQueryDTO, pageNum, pageSize, total, totalPages);
            return pageResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询帖子列表异常，参数：{}", postQueryDTO, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 统计帖子数量
     *
     * @param postQueryDTO 帖子查询参数
     * @return 帖子数量
     */
    @Override
    public int countPosts(PostQueryDTO postQueryDTO) {
        return postMapper.countByQuery(postQueryDTO);
    }

    /**
     * 批量删除违规帖子（管理员操作）
     *
     * @param postIds  帖子ID列表
     * @param adminId  管理员ID
     * @return 删除成功数量
     * @throws BusinessException 无管理员权限时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeletePosts(Long adminId, List<Long> postIds) {
        try {
            // 1. 参数校验
            if (CollectionUtils.isEmpty(postIds)) {
                throw new BusinessException(ErrorCode.POST_ID_NULL);
            }
            if (Objects.isNull(adminId)) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 2. 校验管理员权限
            User admin = userService.getById(adminId);
            if (Objects.isNull(admin) || !UserRoleEnum.ADMIN.equals(admin.getRole())) {
                log.error("批量删除帖子失败，无管理员权限，操作人ID：{}", adminId);
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 3. 筛选存在且未删除的帖子（避免重复删除）
            List<Post> existingPosts = getByIds(postIds);
            List<Long> validPostIds = existingPosts.stream()
                    .filter(post -> !PostStatusEnum.DELETED.equals(post.getStatus()))
                    .map(Post::getPostId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(validPostIds)) {
                log.info("批量删除帖子，无有效帖子可删，传入ID列表：{}", postIds);
                return 0;
            }

            // 4. 批量逻辑删除帖子（更新状态为DELETED）
            int deleteRows = postMapper.batchUpdateStatus(validPostIds, PostStatusEnum.DELETED);
            if (deleteRows <= 0) {
                log.error("批量删除帖子失败，数据库更新无生效行数，有效帖子ID：{}", validPostIds);
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }

            // 5. 联动删除关联数据（跟帖、点赞记录）
            removeByIds(validPostIds); // 批量删除跟帖
            userPostLikeService.removeByIds(validPostIds); // 批量删除点赞记录

            // 6. 清除缓存（帖子详情、置顶列表、帖子列表）
            validPostIds.forEach(this::clearPostDetailCache);
            clearTopPostsCache();
            clearPostListCache();

            log.info("批量删除帖子成功，管理员ID：{}，传入ID数量：{}，有效删除数量：{}，删除帖子ID：{}",
                    adminId, postIds.size(), deleteRows, validPostIds);
            return deleteRows;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除帖子异常，帖子ID列表：{}，管理员ID：{}", postIds, adminId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }


    // ---------------------- 私有辅助方法 ----------------------

    /**
     * 校验帖子发布参数合法性
     */
    private void validatePublishParam(PostPublishDTO publishDTO) {
        // 标题非空且长度≤50字
        if (!StringUtils.hasText(publishDTO.getTitle())) {
            throw new BusinessException(ErrorCode.POST_TITLE_NULL);
        }
        if (publishDTO.getTitle().length() > MAX_POST_TITLE_LENGTH) {
            throw new BusinessException(ErrorCode.POST_TITLE_INVALID);
        }
        // 内容非空且长度≤2000字
        if (!StringUtils.hasText(publishDTO.getContent())) {
            throw new BusinessException(ErrorCode.POST_CONTENT_NULL);
        }
        if (publishDTO.getContent().length() > MAX_POST_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.POST_CONTENT_INVALID);
        }
    }

    /**
     * 校验帖子编辑参数合法性
     */
    private void validateUpdateParam(PostUpdateDTO updateDTO) {
        // 标题非空且长度≤100字
        if (!StringUtils.hasText(updateDTO.getTitle())) {
            throw new BusinessException(ErrorCode.POST_TITLE_NULL);
        }
        if (updateDTO.getTitle().length() > MAX_UPDATE_TITLE_LENGTH) {
            throw new BusinessException(ErrorCode.POST_TITLE_INVALID);
        }
        // 内容非空且长度≤5000字
        if (!StringUtils.hasText(updateDTO.getContent())) {
            throw new BusinessException(ErrorCode.POST_CONTENT_NULL);
        }
        if (updateDTO.getContent().length() > MAX_UPDATE_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.POST_CONTENT_INVALID);
        }
    }

    /**
     * 解析并校验帖子图片URL（JSON格式转列表，数量≤9张）
     */
    private List<String> parseAndValidateImageUrls(String imageUrlsJson) {
        List<String> imageUrlList = new ArrayList<>();
        if (StringUtils.hasText(imageUrlsJson)) {
            try {
                // 实际项目中使用JSON工具解析（如Jackson），此处简化处理
                imageUrlList = Arrays.stream(imageUrlsJson.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
                // 校验图片数量≤9张
                if (imageUrlList.size() > MAX_IMAGE_COUNT) {
                    throw new BusinessException(ErrorCode.POST_IMAGE_TOO_MANY);
                }
            } catch (Exception e) {
                log.error("解析帖子图片URL失败，JSON格式异常：{}", imageUrlsJson, e);
                throw new BusinessException(ErrorCode.POST_IMAGE_FORMAT_INVALID);
            }
        }
        return imageUrlList;
    }

    /**
     * 构建Post实体
     */
    private Post buildPostEntity(PostPublishDTO publishDTO, Long userId, List<String> imageUrlList, User publisher) {
        Post post = new Post();
        // 基础信息
        post.setUserId(userId);
        post.setTitle(publishDTO.getTitle());
        post.setContent(publishDTO.getContent());
//        post.setImageUrls(imageUrlList); // 图片URL列表
        // 状态与互动数初始化
        post.setStatus(isNewUser(publisher) ? PostStatusEnum.PENDING : PostStatusEnum.NORMAL); // 新用户帖子待审核
        post.setLikeCount(0);
        post.setPostFollowCount(0);
        post.setIsHot(false);
        post.setIsEssence(false);
        post.setIsTop(false);
        // 时间信息
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        return post;
    }

    /**
     * 判断是否为新用户（注册时间≤7天）
     */
    private boolean isNewUser(User user) {
        LocalDateTime registerTime = user.getCreateTime();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return registerTime.isAfter(sevenDaysAgo);
    }

    /**
     * 构建帖子详情DTO（关联发布者脱敏信息）
     */
    private PostDetailDTO buildPostDetailDTO(Post post, User publisher, boolean isLiked) {
        PostDetailDTO detailDTO = postConvert.postToPostDetailDTO(post);
        // 封装发布者脱敏信息
        PostDetailDTO.PublisherDTO publisherDTO = new PostDetailDTO.PublisherDTO();
        publisherDTO.setUserId(publisher.getUserId());
        publisherDTO.setUsername(publisher.getUsername());
        publisherDTO.setAvatarUrl(publisher.getAvatarUrl());
        publisherDTO.setCreditScore(publisher.getCreditScore());
        detailDTO.setPublisher(publisherDTO);
        // 设置当前用户点赞状态
        detailDTO.setIsLiked(isLiked);
        return detailDTO;
    }

    /**
     * 转换为PostListItemDTO列表（处理首图、发布者信息）
     */
    private List<PostListItemDTO> convertToPostListItemDTO(List<Post> postList) {
        if (CollectionUtils.isEmpty(postList)) {
            return new ArrayList<>();
        }
        // 批量查询发布者信息（减少SQL查询次数）
        Set<Long> publisherIds = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        List<User> publishers = userService.getByIds(new ArrayList<>(publisherIds));
        Map<Long, User> publisherMap = publishers.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // 转换DTO
        return postList.stream().map(post -> {
            PostListItemDTO listDTO = postConvert.postToPostListItemDTO(post);
//            // 处理首图（无图用默认图）
//            List<String> imageUrls = post.getImageUrls();
//            listDTO.setCoverImage(!CollectionUtils.isEmpty(imageUrls)
//                    ? imageUrls.get(0)
//                    : "/static/image/post_default.png"); // 默认首图
            // 封装发布者极简信息
            User publisher = publisherMap.get(post.getUserId());
            if (Objects.nonNull(publisher)) {
                PostListItemDTO.PublisherSimpleDTO publisherSimpleDTO = new PostListItemDTO.PublisherSimpleDTO();
                publisherSimpleDTO.setUserId(publisher.getUserId());
                publisherSimpleDTO.setUsername(publisher.getUsername());
                listDTO.setPublisher(publisherSimpleDTO);
            }
            return listDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 检查用户每日点赞次数是否超限
     */
    private boolean checkDailyLikeLimit(Long userId) {
        String cacheKey = CACHE_KEY_USER_LIKE_TIMES + userId;
        Integer dailyLikeTimes = (Integer) redisTemplate.opsForValue().get(cacheKey);
        return Objects.isNull(dailyLikeTimes) || dailyLikeTimes < MAX_DAILY_LIKE_TIMES;
    }

    /**
     * 增加用户当日点赞次数
     */
    private void incrementDailyLikeTimes(Long userId) {
        String cacheKey = CACHE_KEY_USER_LIKE_TIMES + userId;
        redisTemplate.opsForValue().increment(cacheKey);
        // 设置缓存有效期（24小时）
        redisTemplate.expire(cacheKey, CACHE_TTL_USER_LIKE_TIMES, TimeUnit.HOURS);
    }

    /**
     * 减少用户当日点赞次数
     */
    private void decrementDailyLikeTimes(Long userId) {
        String cacheKey = CACHE_KEY_USER_LIKE_TIMES + userId;
        Integer currentTimes = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(currentTimes) && currentTimes > 0) {
            redisTemplate.opsForValue().decrement(cacheKey);
        } else {
            redisTemplate.delete(cacheKey);
        }
    }

    /**
     * 缓存帖子详情
     */
    private void cachePostDetail(PostDetailDTO detailDTO) {
        String cacheKey = CACHE_KEY_POST_DETAIL + detailDTO.getPostId();
        redisTemplate.opsForValue().set(cacheKey, detailDTO, CACHE_TTL_POST_DETAIL, TimeUnit.HOURS);
    }

    /**
     * 清除帖子详情缓存
     */
    private void clearPostDetailCache(Long postId) {
        String cacheKey = CACHE_KEY_POST_DETAIL + postId;
        redisTemplate.delete(cacheKey);
        log.info("清除帖子详情缓存，帖子ID：{}，缓存Key：{}", postId, cacheKey);
    }

    /**
     * 清除置顶帖子列表缓存
     */
    private void clearTopPostsCache() {
        redisTemplate.delete(CACHE_KEY_TOP_POSTS);
        log.info("清除置顶帖子列表缓存，缓存Key：{}", CACHE_KEY_TOP_POSTS);
    }

    /**
     * 清除所有帖子列表缓存（简化处理，实际可按查询维度精准删除）
     */
    private void clearPostListCache() {
        Set<String> cacheKeys = redisTemplate.keys(CACHE_KEY_POST_LIST + "*");
        if (!CollectionUtils.isEmpty(cacheKeys)) {
            redisTemplate.delete(cacheKeys);
            log.info("清除帖子列表缓存，缓存Key数量：{}", cacheKeys.size());
        }
    }

    /**
     * 构建帖子列表缓存Key（基于查询参数）
     */
    private String buildPostListCacheKey(PostQueryDTO queryDTO, int pageNum, int pageSize) {
        StringBuilder cacheKey = new StringBuilder(CACHE_KEY_POST_LIST);
        cacheKey.append("keyword:").append(Objects.nonNull(queryDTO.getKeyword()) ? queryDTO.getKeyword() : "null");
        cacheKey.append("_sortField:").append(queryDTO.getSortField().name());
        cacheKey.append("_sortDir:").append(queryDTO.getSortDir().name());
        cacheKey.append("_pageNum:").append(pageNum);
        cacheKey.append("_pageSize:").append(pageSize);
        return cacheKey.toString();
    }

    //========================== v1 ===================================

    // ------------------------------ 基础CRUD方法（严格匹配设计文档定义） ------------------------------

    /**
     * 按ID查询帖子详情
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @Override
    public PostDetailDTO selectPostById(Long userId, Long postId) {
        try {
            if (postId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 1. 优先查询缓存（按设计文档缓存策略）
            String cacheKey = CACHE_KEY_POST_DETAIL + postId;
            PostDetailDTO postDetailDTO = (PostDetailDTO) redisTemplate.opsForValue().get(cacheKey);
            if (Objects.nonNull(postDetailDTO)) {
                return postDetailDTO;
            }

            // 2. 缓存未命中，查询数据库
            Post post = postMapper.selectById(postId);
            if (post == null) {
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 3. 权限检验（仅管理员和作者可以看到状态异常的帖子）
            if(post.getStatus() != PostStatusEnum.NORMAL &&
                    (!userService.verifyRole(userId, UserRoleEnum.ADMIN) && !userId.equals(post.getUserId()))){
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 封装详情DTO
            User publisher = userService.getById(post.getUserId());
            Boolean isliked = userPostLikeService.isLiked(userId, postId);
            postDetailDTO = buildPostDetailDTO(post, publisher, isliked);

            // 5. 更新缓存
            redisTemplate.opsForValue().set(cacheKey, postDetailDTO, CACHE_TTL_POST_DETAIL, TimeUnit.HOURS);
            return postDetailDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询帖子详情系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 删除帖子
     * @param postId 帖子ID
     * @param operatorId 操作人ID
     * @return 操作结果
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Boolean deletePostById(Long operatorId, Long postId) {
        try {
            if (postId == null || operatorId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 1. 校验帖子存在及操作权限（作者或管理员）
            Post post = getById(postId);
            User operator = userService.getById(operatorId);
            boolean isAuthor = post.getUserId().equals(operatorId);
            boolean isAdmin = UserRoleEnum.ADMIN.equals(operator.getRole());
            if (!isAuthor && !isAdmin) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 2. 逻辑删除帖子
            int postRows = postMapper.deleteById(postId);
            if (postRows <= 0) {
                log.error("删除帖子失败，帖子ID：{}，操作用户：{}", postId, operatorId);
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }

            // 3. 同步删除关联点赞记录（设计文档要求删除帖子时同步清理关联数据）
            userPostLikeService.batchDeleteByPostId(postId);

            // 4. 清除缓存（帖子详情+热门缓存）
            String cacheKey = CACHE_KEY_POST_DETAIL + postId;
            redisTemplate.delete(cacheKey);

            log.info("删除帖子成功，帖子ID：{}，操作用户：{}（角色：{}）",
                    postId, operatorId, isAdmin ? "管理员" : "作者");
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除帖子系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    // ------------------------------ 业务方法（严格匹配设计文档定义） ------------------------------

    /**
     * 查询热门帖子列表
     * 核心逻辑：按点赞数+评论数加权排序，优先从缓存获取，缓存未命中则查库并更新缓存
     * @param limit 最大返回数量（必填，建议10-30）
     * @return 热门帖子详情VO列表
     */
    @Override
    public List<PostDetailDTO> selectHotPosts(Integer limit) {
        // 参数校验
        if (limit == null || limit <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 构建缓存Key（全平台热门帖子）
        String cacheKey = "post:hot:all";

        // 1. 优先查询缓存
        List<Post> hotPosts = (List<Post>) redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(hotPosts) && !hotPosts.isEmpty()) {
            return hotPosts.stream()
                    .map(this::convertToDetailVO)
                    .collect(Collectors.toList());
        }

        // 2. 缓存未命中，查询数据库
        // 热门算法：点赞数*1 + 评论数*2 加权排序（可根据业务调整权重）
        hotPosts = postMapper.selectHotPosts(limit);
        if (hotPosts.isEmpty()) {
            log.info("未查询到热门帖子，数量：{}", limit);
            return Collections.emptyList();
        }

        // 3. 缓存查询结果（设置2小时过期）
        redisTemplate.opsForValue().set(cacheKey, hotPosts, CACHE_TTL_HOT_POST);
        log.info("查询热门帖子成功，数量：{}，已缓存", hotPosts.size());

        // 4. 转换为VO并返回
        return hotPosts.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询精华帖子列表（分页）
     * 核心逻辑：筛选isEssence=true的帖子，按创建时间倒序排列
     * @param pageParam 分页参数（页码+每页数量）
     * @return 精华帖子详情VO分页列表
     */
    @Override
    public PageResult<PostDetailDTO> selectEssencePosts(PageParam pageParam) {
        // 参数校验
        validatePageParam(pageParam);

        // 1. 计算分页偏移量
        int offset = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        int pageSize = pageParam.getPageSize();

        // 2. 查询精华帖子列表（isEssence=true）
        List<Post> essencePosts = postMapper.selectEssencePosts(offset, pageSize);

        // 3. 查询总记录数
        Long total = (long)postMapper.countEssencePosts();

        // 4. 转换为VO列表
        List<PostDetailDTO> voList = essencePosts.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        // 5. 构建分页结果
        PageResult<PostDetailDTO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(total);
        pageResult.setPageNum(pageParam.getPageNum());
        pageResult.setPageSize(pageSize);
        pageResult.setTotalPages((total + pageSize - 1) / pageSize);

        log.info("查询精华帖子成功，页码：{}，数量：{}，总记录数：{}",
                pageParam.getPageNum(), voList.size(), total);
        return pageResult;
    }

    /**
     * 查询置顶帖子列表
     * 核心逻辑：筛选isTop=true的帖子，按置顶时间倒序（最新置顶在前）
     * @return 置顶帖子详情VO列表（最多返回5条，符合MAX_TOP_POST_COUNT约束）
     */
    @Override
    public List<PostDetailDTO> selectTopPosts() {
        // 1. 查询置顶帖子（isTop=true），最多返回MAX_TOP_POST_COUNT条
        List<Post> topPosts = postMapper.selectTopPosts(MAX_TOP_POST_COUNT);
        if (topPosts.isEmpty()) {
            log.info("未查询到置顶帖子");
            return Collections.emptyList();
        }

        // 2. 转换为VO列表
        List<PostDetailDTO> voList = topPosts.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        log.info("查询置顶帖子成功，数量：{}", voList.size());
        return voList;
    }

    /**
     * 更新帖子内容
     * 逻辑：校验参数，更新帖子内容，更新缓存（帖子详情+热门缓存）
     * @param operatorId 操作用户ID
     * @param postId 帖子ID
     * @param status 目标帖子状态
     * @return 是否更新成功
     */
    @Override
    public Boolean updatePostStatus(Long operatorId, Long postId, PostStatusEnum status) {
        // 1.基本参数检验
        if(operatorId == null || postId == null || status == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 2. 获取帖子详情
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
        }

        // 3. 校验操作者权限，要么是管理员，要么是作者
        User user = userService.getById(operatorId);
        boolean isAdmin = user.isAdmin();
        boolean isAuthor = post.getUserId().equals(operatorId);
        if (!isAdmin && !isAuthor) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 4. 检验帖子前后状态的合法性
        // 不允许修改已删除的帖子状态
        if (post.getStatus() == PostStatusEnum.DELETED) {
            throw new BusinessException(ErrorCode.POST_STATUS_INVALID);
        }

        if (isAdmin && !isAuthor){
            // 管理员身份但非作者身份.不允许将帖子状态设置为草稿，隐藏
            if (status == PostStatusEnum.DRAFT || status == PostStatusEnum.HIDDEN) {
                throw new BusinessException(ErrorCode.POST_STATUS_INVALID);
            }
        } else if (!isAdmin && isAuthor){
            // 作者身份但非管理员身份，不允许修改已封禁的帖子状态
            if (post.getStatus() == PostStatusEnum.BLOCKED) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }
            // 作者身份但非管理员身份，不允许将帖子状态设置为草稿或隐藏
            if (status == PostStatusEnum.DRAFT) {
                throw new BusinessException(ErrorCode.POST_STATUS_INVALID);
            }
            if (status == PostStatusEnum.HIDDEN) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }
        } else {
            // 作者身份且管理员身份，不允许修改已封禁的帖子状态
            if (post.getStatus() == PostStatusEnum.BLOCKED) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }
        }

        // 5. 更新帖子状态
        post.setStatus(status);
        return postMapper.updateById(post) > 0;
    }

    // ------------------------------ 辅助方法保持不变 ------------------------------
    /**
     * 校验分页参数合法性
     */
    private void validatePageParam(PageParam pageParam) {
        if (pageParam == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        if (pageParam.getPageNum() == null || pageParam.getPageNum() < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (pageParam.getPageSize() == null || pageParam.getPageSize() < 1 || pageParam.getPageSize() > 50) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    /**
     * 帖子实体转换为详情VO
     */
    private PostDetailDTO convertToDetailVO(Post post) {
        PostDetailDTO vo = postConvert.postToPostDetailDTO(post);
        User publisher = userService.getById(post.getUserId());
        PostDetailDTO.PublisherDTO publisherVO = new PostDetailDTO.PublisherDTO(
                publisher.getUserId(), publisher.getUsername(),
                publisher.getAvatarUrl(), publisher.getCreditScore()
        );
        vo.setPublisher(publisherVO);
        vo.setIsLiked(userPostLikeService.isLiked(post.getUserId(), post.getPostId()));
        return vo;
    }

}
