package com.community_shop.backend.service.impl;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.PostDetailDTO;
import com.community_shop.backend.dto.post.PostUpdateDTO;
import com.community_shop.backend.enums.CodeEnum.PostStatusEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserPostLike;
import com.community_shop.backend.mapper.PostMapper;
import com.community_shop.backend.mapper.UserPostLikeMapper;
import com.community_shop.backend.service.base.MessageService;
import com.community_shop.backend.service.base.PostService;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {

    // 常量定义
    private static final Integer MIN_CREDIT_FOR_PUBLISH = 60; // 发帖最低信用分
    private static final Integer MAX_TOP_POST_COUNT = 5;      // 最大置顶帖子数

    // 缓存相关常量
    private static final String CACHE_KEY_POST_DETAIL = "post:detail:%s"; // 帖子详情缓存Key（%s替换为帖子ID）
    private static final Duration CACHE_TTL_HOT_POST = Duration.ofHours(2); // 热门帖子缓存2小时
    private static final Duration CACHE_TTL_POST_DETAIL = Duration.ofHours(1); // 帖子详情缓存1小时

    // Redis缓存Key前缀常量
    private static final String CACHE_HOT_POST_KEY = "post:hot:";
    private static final String CACHE_POST_DETAIL_KEY = "post:detail:";
    private static final String CACHE_POST_FOLLOW_KEY = "post:follow:";

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserPostLikeMapper userPostLikeMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OssUtil ossUtil;

    // ------------------------------ 基础CRUD方法（严格匹配设计文档定义） ------------------------------
    /**
     * 新增帖子（基础方法）
     * @param post 帖子实体
     * @return 帖子ID
     */
    @Override
    public Long insertPost(Post post) {
        // 参数校验
        if (post == null || !StringUtils.hasText(post.getTitle()) || !StringUtils.hasText(post.getContent())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        try {
            // 1. 自动填充基础字段
            post.setCreateTime(LocalDateTime.now());
            post.setLikeCount(0);
            post.setPostFollowCount(0);
            post.setStatus(PostStatusEnum.NORMAL); // 默认正常状态，新用户校验在publishPost中处理

            // 2. 调用Mapper插入
            int rows = postMapper.insert(post);
            if (rows <= 0) {
                log.error("插入帖子失败，帖子信息：{}", post);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 3. 缓存帖子详情
            String cacheKey = String.format(CACHE_KEY_POST_DETAIL, post.getPostId());
            redisTemplate.opsForValue().set(cacheKey, post, CACHE_TTL_POST_DETAIL);
            log.info("插入帖子成功，帖子ID：{}", post.getPostId());
            return post.getPostId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("插入帖子系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 按ID查询帖子详情
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @Override
    public Post selectPostById(Long postId) {
        if (postId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 1. 优先查询缓存（按设计文档缓存策略）
        String cacheKey = String.format(CACHE_KEY_POST_DETAIL, postId);
        Post post = (Post) redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(post)) {
            return post;
        }

        // 2. 缓存未命中，查询数据库
        post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
        }

        // 3. 更新缓存
        redisTemplate.opsForValue().set(cacheKey, post, CACHE_TTL_POST_DETAIL);
        return post;
    }


    /**
     * 更新帖子内容
     * @param postVO 帖子更新信息
     * @param userId 操作人ID
     * @return 操作结果
     */
    @Override
    public Boolean updatePostContent(PostUpdateDTO postVO, Long userId) {
        Long postId = postVO.getPostId();
        String newTitle = postVO.getTitle();
        String newContent = postVO.getContent();
        if (postId == null || !StringUtils.hasText(newContent) || userId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 1. 校验帖子存在及作者权限
        Post post = selectPostById(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 2. 调用Mapper更新内容
        int rows = postMapper.updatePostContent(postId, newTitle, newContent, LocalDateTime.now());
        if (rows <= 0) {
            log.error("更新帖子内容失败，帖子ID：{}，操作用户：{}", postId, userId);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        // 3. 清除缓存（设计文档要求缓存更新触发点为内容变更）
        redisTemplate.delete(String.format(CACHE_KEY_POST_DETAIL, postId));
        log.info("更新帖子内容成功，帖子ID：{}，操作用户：{}", postId, userId);
        return true;
    }

    /**
     * 删除帖子
     * @param postId 帖子ID
     * @param operatorId 操作人ID
     * @return 操作结果
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Boolean deletePostById(Long postId, Long operatorId) {
        if (postId == null || operatorId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 1. 校验帖子存在及操作权限（作者或管理员）
        Post post = selectPostById(postId);
        User operator = userService.selectUserById(operatorId);
        boolean isAuthor = post.getUserId().equals(operatorId);
        boolean isAdmin = "ROLE_ADMIN".equals(operator.getRole());
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
        userPostLikeMapper.deleteByPostId(postId);

        // 4. 清除缓存（帖子详情+热门缓存）
        redisTemplate.delete(String.format(CACHE_KEY_POST_DETAIL, postId));

        log.info("删除帖子成功，帖子ID：{}，操作用户：{}（角色：{}）",
                postId, operatorId, isAdmin ? "管理员" : "作者");
        return true;
    }

    // ------------------------------ 业务方法（严格匹配设计文档定义） ------------------------------
    /**
     * 发布帖子（业务方法）
     * @param postVO 帖子VO
     * @param userId 发布者ID
     * @return 帖子ID字符串
     */
    @Override
    public String publishPost(PostDetailDTO postVO, Long userId) {
        if (postVO == null || userId == null || !StringUtils.hasText(postVO.getTitle()) || !StringUtils.hasText(postVO.getContent())) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 1. 校验用户信用分≥60（设计文档核心约束）
        User user = userService.selectUserById(userId);
        if (user.getCreditScore() < MIN_CREDIT_FOR_PUBLISH) {
            throw new BusinessException(ErrorCode.CREDIT_TOO_LOW);
        }

        // 2. 判断是否为新用户（首次发帖标记待审核）
        int userPostCount = postMapper.countPostsByUserId(userId);
        PostStatusEnum postStatus = (userPostCount == 0) ?  PostStatusEnum.PENDING: PostStatusEnum.NORMAL;

//        // 3. 图片上传（若有图片，通过OSS工具上传并获取URL）
//        List<String> imageUrls = null;
//        if (postVO.getImages() != null && !postVO.getImages().isEmpty()) {
//            imageUrls = ossUtil.uploadFiles(postVO.getImages(), "post/" + userId);
//            log.info("帖子图片上传完成，用户ID：{}，图片数量：{}", userId, imageUrls.size());
//        }

        // 4. 构建Post实体并调用基础方法插入
        Post post = new Post();
        post.setTitle(postVO.getTitle());
        post.setContent(postVO.getContent());
        post.setUserId(userId);
        post.setStatus(postStatus);

        Long postId = insertPost(post);

        // 5. 返回发布结果（区分待审核/直接发布）
        String resultMsg = postStatus.equals(PostStatusEnum.PENDING)
                ? "帖子发布成功，待管理员审核后可见"
                : "帖子发布成功，已实时可见";
        log.info("帖子发布完成，帖子ID：{}，用户ID：{}，发布状态：{}", postId, userId, postStatus);
        return resultMsg;
    }

    /**
     * 更新帖子点赞数
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param isLike 点赞/取消点赞
     * @return 最新点赞数
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Integer updatePostLikeCount(Long postId, Long userId, Boolean isLike) {
        if (postId == null || userId == null || isLike == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 1. 校验帖子存在
        selectPostById(postId);

        // 2. 查询用户是否已点赞
        Integer liked = userPostLikeMapper.selectIsLiked(userId, postId);
        int changeCount = isLike ? 1 : -1;

        // 3. 处理点赞/取消点赞逻辑
        if (isLike && liked == null) {
            // 未点赞：新增点赞记录+更新帖子点赞数
            UserPostLike userPostLike = new UserPostLike(userId, postId, LocalDateTime.now());
            userPostLikeMapper.insert(userPostLike);
            postMapper.updateLikeCount(postId, changeCount);
        } else if (!isLike && liked != null) {
            // 已点赞：删除点赞记录+更新帖子点赞数
            userPostLikeMapper.deleteByUserAndPost(userId, postId);
            postMapper.updateLikeCount(postId, changeCount);
        } else {
            // 重复操作：直接返回当前点赞数
            Integer currentLikeCount = postMapper.selectById(postId).getLikeCount();
            log.warn("重复点赞操作，用户ID：{}，帖子ID：{}，当前点赞数：{}", userId, postId, currentLikeCount);
            return currentLikeCount;
        }

        // 4. 获取更新后的点赞数
        Integer newLikeCount = postMapper.selectLikeCountById(postId);

        // 5. 清除缓存（帖子详情+主题吧热门缓存，匹配设计文档缓存更新触发点）
        redisTemplate.delete(String.format(CACHE_KEY_POST_DETAIL, postId));

        log.info("更新点赞数成功，帖子ID：{}，用户ID：{}，操作：{}，新点赞数：{}",
                postId, userId, isLike ? "点赞" : "取消点赞", newLikeCount);
        return newLikeCount;
    }

    /**
     * 设置帖子精华/置顶
     * @param postId 帖子ID
     * @param isEssence 是否精华
     * @param isTop 是否置顶
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @Override
    public Boolean setPostEssenceOrTop(Long postId, Boolean isEssence, Boolean isTop, Long adminId) {
        if (postId == null || isEssence == null || isTop == null || adminId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 1. 校验管理员权限（设计文档核心约束）
        User admin = userService.selectUserById(adminId);
        if (!"ROLE_ADMIN".equals(admin.getRole())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 2. 校验帖子存在
        selectPostById(postId);

        // 3. 置顶数校验（若置顶，需确保当前置顶数≤5，匹配设计文档约束）
        if (isTop) {
            int currentTopCount = postMapper.countTopPosts();
            if (currentTopCount >= MAX_TOP_POST_COUNT) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
        }

        // 4. 更新帖子加精/置顶状态
        int rows = postMapper.updateEssenceAndTopById(postId, isEssence, isTop, LocalDateTime.now());
        if (rows <= 0) {
            log.error("帖子加精/置顶失败，帖子ID：{}，管理员ID：{}", postId, adminId);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        // 5. 清除缓存（帖子详情+主题吧热门缓存）
        redisTemplate.delete(String.format(CACHE_KEY_POST_DETAIL, postId));

        log.info("帖子加精/置顶成功，帖子ID：{}，管理员ID：{}，加精：{}，置顶：{}",
                postId, adminId, isEssence, isTop);
        return true;
    }

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
        Integer total = postMapper.countEssencePosts();

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
     * 辅助方法：将Post实体+关联User信息转换为PostDetailVO（补充发帖者信息）
     */
    @Override
    public PostDetailDTO convertToDetailVO(Post post) {
        if (post == null) {
            return null;
        }

        // 1. 构建VO并填充帖子基础信息
        PostDetailDTO postVO = new PostDetailDTO();
        BeanUtils.copyProperties(post, postVO); // 使用Spring的BeanUtils快速复制同名字段
        postVO.getPublisher().setUserId(post.getUserId()); // 发帖者ID即Post的userId

        // 2. 关联查询发帖者信息并填充VO
        try {
            User publisher = userService.selectUserById(post.getUserId());
            if (publisher != null) {
                postVO.getPublisher().setUsername(publisher.getUsername());       // 用户名
                postVO.getPublisher().setAvatarUrl(publisher.getAvatarUrl()); // 头像
                postVO.getPublisher().setCreditScore(publisher.getCreditScore());   // 信用分（可选）
            }
        } catch (Exception e) {
            log.error("转换帖子VO时查询用户信息失败，帖子ID：{}", post.getPostId(), e);
            // 即使用户信息查询失败，仍返回帖子基础信息，避免整体功能不可用
            postVO.getPublisher().setUsername("未知用户");
            postVO.getPublisher().setAvatarUrl("/default-avatar.png");
        }

        return postVO;
    }
}
