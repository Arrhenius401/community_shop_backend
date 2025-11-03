package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.PostConvert;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.PostFollow;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.PostFollowMapper;
import com.community_shop.backend.service.base.PostFollowService;
import com.community_shop.backend.service.base.PostService;
import com.community_shop.backend.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 跟帖模块Service实现类
 * 实现跟帖全生命周期管理、互动数据维护及跨模块协同逻辑
 */
@Slf4j
@Service
public class PostFollowServiceImpl extends BaseServiceImpl<PostFollowMapper, PostFollow> implements PostFollowService {

    // 缓存相关常量
    private static final String CACHE_KEY_POST_FOLLOW = "post:follow:"; // 跟帖缓存Key前缀
    private static final long CACHE_TTL_POST_FOLLOW = 1800; // 跟帖缓存有效期（30分钟，单位：秒）

    @Autowired
    private PostFollowMapper postFollowMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PostConvert convertUtils;

    /**
     * 发布跟帖
     * @param userId 用户ID
     * @param postFollowPublishDTO 跟帖发布DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostFollowDetailDTO publishFollow(Long userId, PostFollowPublishDTO postFollowPublishDTO) {
        try {
            // 1. 参数校验
            if (postFollowPublishDTO == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            Long postId = postFollowPublishDTO.getPostId();
            String content = postFollowPublishDTO.getContent();
            if (postId == null) {
                throw new BusinessException(ErrorCode.POST_ID_NULL);
            }
            if (!StringUtils.hasText(content) || content.length() > 500) {
                throw new BusinessException(ErrorCode.POST_FOLLOW_CONTENT_ILLEGAL);
            }

            // 2. 校验帖子存在性（必校验：避免对无效帖子发跟帖）
            Post post = postService.getById(postId);
            if (post == null) {
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 3. 校验用户存在
            User user = userService.getById(userId);
            if (Objects.isNull(user)) {
                log.warn("发布跟帖失败：用户不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 4. 构建跟帖实体（默认非嵌套回复，parentId为null）
            PostFollow postFollow = new PostFollow();
            postFollow.setPostId(postId);
            postFollow.setUserId(userId); // 从DTO获取当前发跟帖用户ID
            postFollow.setParentId(null);
            postFollow.setContent(content);
            postFollow.setLikeCount(0);
            postFollow.setCreateTime(LocalDateTime.now());
            postFollow.setUpdateTime(LocalDateTime.now());
            postFollow.setStatus(PostFollowStatusEnum.NORMAL); // 初始状态为正常

            // 5. 插入跟帖记录
            int insertRows = postFollowMapper.insert(postFollow);
            if (insertRows <= 0) {
                log.error("发布跟帖失败，插入记录异常，帖子ID：{}，用户ID：{}", postId, userId);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 6. 转换为DTO并补充跟帖人信息
            PostFollowDetailDTO detailDTO = convertUtils.postFollowToPostFollowDetailDTO(postFollow);
            User follower = userService.getById(postFollow.getUserId());
            if (follower != null) {
                PostFollowDetailDTO.FollowerDTO followerDTO = new PostFollowDetailDTO.FollowerDTO();
                followerDTO.setUserId(follower.getUserId());
                followerDTO.setUsername(follower.getUsername());
                followerDTO.setAvatarUrl(follower.getAvatarUrl());
                detailDTO.setFollower(followerDTO);
            }
            detailDTO.setIsLiked(false); // 初始未点赞

            log.info("发布跟帖成功，跟帖ID：{}，帖子ID：{}", postFollow.getPostFollowId(), postId);
            return detailDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发布跟帖失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 更新跟帖内容
     * @param postFollowUpdateDTO 跟帖更新DTO
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostFollowDetailDTO updateFollow(Long userId, PostFollowUpdateDTO postFollowUpdateDTO) {
        try {
            // 1. 参数校验
            if (postFollowUpdateDTO == null || userId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            Long followId = postFollowUpdateDTO.getPostFollowId();
            String newContent = postFollowUpdateDTO.getNewContent();
            if (followId == null) {
                throw new BusinessException(ErrorCode.POST_FOLLOW_ID_NULL);
            }
            if (!StringUtils.hasText(newContent) || newContent.length() > 500) {
                throw new BusinessException(ErrorCode.POST_FOLLOW_CONTENT_ILLEGAL);
            }

            // 2. 校验跟帖存在性及状态
            PostFollow postFollow = postFollowMapper.selectById(followId);
            if (postFollow == null) {
                throw new BusinessException(ErrorCode.POST_FOLLOW_NOT_EXISTS);
            }
            if (!PostFollowStatusEnum.NORMAL.equals(postFollow.getStatus())) {
                throw new BusinessException(ErrorCode.FOLLOW_STATUS_ILLEGAL);
            }

            // 3. 权限校验（仅跟帖作者可编辑）
            if (!Objects.equals(postFollow.getUserId(), userId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 更新跟帖内容
            postFollow.setContent(newContent);
            postFollow.setUpdateTime(LocalDateTime.now());
            int updateRows = postFollowMapper.updateById(postFollow);
            if (updateRows <= 0) {
                log.error("编辑跟帖失败，更新记录异常，跟帖ID：{}，用户ID：{}", followId, userId);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 5. 转换DTO并补充信息
            PostFollowDetailDTO detailDTO = convertUtils.postFollowToPostFollowDetailDTO(postFollow);
            User follower = userService.getById(userId);
            if (follower != null) {
                PostFollowDetailDTO.FollowerDTO followerDTO = new PostFollowDetailDTO.FollowerDTO();
                followerDTO.setUserId(follower.getUserId());
                followerDTO.setUsername(follower.getUsername());
                followerDTO.setAvatarUrl(follower.getAvatarUrl());
                detailDTO.setFollower(followerDTO);
            }
            detailDTO.setIsLiked(false); // 点赞状态需单独查询，此处默认未点赞

            log.info("编辑跟帖成功，跟帖ID：{}，用户ID：{}", followId, userId);
            return detailDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("编辑跟帖失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 更新跟帖状态
     * @param userId 用户ID
     * @param statusUpdateDTO 跟帖状态更新DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFollowStatus(Long userId, PostFollowStatusUpdateDTO statusUpdateDTO) {
        try {
            // 1. 参数校验
            if (statusUpdateDTO == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            Long followId = statusUpdateDTO.getPostFollowId();
            PostFollowStatusEnum targetStatus = statusUpdateDTO.getTargetStatus();
            if (followId == null || targetStatus == null || userId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验管理员权限（通过User实体的isAdmin()方法判断）
            User admin = userService.getById(userId);
            if (admin == null || !admin.isAdmin()) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 3. 校验跟帖存在性
            PostFollow postFollow = postFollowMapper.selectById(followId);
            if (postFollow == null) {
                throw new BusinessException(ErrorCode.POST_FOLLOW_NOT_EXISTS);
            }

            // 4. 更新跟帖状态
            postFollow.setStatus(targetStatus);
            postFollow.setUpdateTime(LocalDateTime.now());
            int updateRows = postFollowMapper.updateById(postFollow);
            if (updateRows <= 0) {
                log.error("更新跟帖状态失败，跟帖ID：{}，目标状态：{}", followId, targetStatus);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            log.info("管理员更新跟帖状态成功，跟帖ID：{}，目标状态：{}，管理员ID：{}", followId, targetStatus, userId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新跟帖状态失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 查询帖子下的跟帖列表
     * @param postFollowQueryDTO 跟帖查询DTO
     */
    @Override
    public PageResult<PostFollowDetailDTO> queryFollows(PostFollowQueryDTO postFollowQueryDTO) {
        try {
            // 1. 参数校验
            if (postFollowQueryDTO == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            Long postId = postFollowQueryDTO.getPostId();
            if (postId == null) {
                throw new BusinessException(ErrorCode.POST_ID_NULL);
            }
            // 分页参数默认值处理
            int pageNum = postFollowQueryDTO.getPageNum() <= 0 ? 1 : postFollowQueryDTO.getPageNum();
            int pageSize = postFollowQueryDTO.getPageSize() <= 0 ? 10 : postFollowQueryDTO.getPageSize();
            int offset = (pageNum - 1) * pageSize;

            // 2. 校验帖子存在性
            if (postService.getById(postId) == null) {
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 3. 查询跟帖列表及总数
            long total = (long) postFollowMapper.countByPostId(postId);
            List<PostFollow> followList = postFollowMapper.selectByQuery(postFollowQueryDTO);

            // 4. 转换DTO并补充跟帖人信息
            List<PostFollowDetailDTO> detailList = CollectionUtils.isEmpty(followList) ? List.of() :
                    followList.stream().map(follow -> {
                        PostFollowDetailDTO dto = convertUtils.postFollowToPostFollowDetailDTO(follow);
                        User follower = userService.getById(follow.getUserId());
                        if (follower != null) {
                            PostFollowDetailDTO.FollowerDTO followerDTO = new PostFollowDetailDTO.FollowerDTO();
                            followerDTO.setUserId(follower.getUserId());
                            followerDTO.setUsername(follower.getUsername());
                            followerDTO.setAvatarUrl(follower.getAvatarUrl());
                            dto.setFollower(followerDTO);
                        }
                        dto.setIsLiked(false); // 需结合当前登录用户ID查询，此处默认未点赞
                        return dto;
                    }).collect(Collectors.toList());

            // 5. 构建分页结果
            PageResult<PostFollowDetailDTO> pageResult = new PageResult<>();
            pageResult.setList(detailList);
            pageResult.setPageNum(pageNum);
            pageResult.setPageSize(pageSize);
            pageResult.setTotal(total);
            pageResult.setTotalPages((total + pageSize - 1) / pageSize);

            log.info("查询帖子跟帖列表成功，帖子ID：{}，页码：{}，总数：{}", postId, pageNum, total);
            return pageResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询帖子跟帖列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 统计帖子下的跟帖数量
     * @param postFollowQueryDTO 跟帖查询DTO
     */
    public int countFollows(PostFollowQueryDTO postFollowQueryDTO) {
        return postFollowMapper.countByQuery(postFollowQueryDTO);
    }

    /**
     * 基础删除：逻辑删除跟帖
     * @param userId 用户ID
     * @param postFollowId 跟帖ID
     */
    @Override
    public Boolean deletePostFollowById(Long userId, Long postFollowId) {
        try {
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

            // 3. 管理员或作者权限校验
            if (!userService.verifyRole(userId, UserRoleEnum.ADMIN) && !postFollow.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 执行逻辑删除（更新is_deleted=1）
            int deleteRows = postFollowMapper.updateStatus(postFollowId, PostFollowStatusEnum.DELETED);
            if (deleteRows <= 0) {
                log.error("逻辑删除跟帖失败，跟帖ID：{}", postFollowId);
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }

            // 5. 清除缓存及关联缓存
            redisTemplate.delete(CACHE_KEY_POST_FOLLOW + postFollowId);
            // 清除所属帖子的跟帖列表缓存（格式：post:follow:{postId}:{offset}:{limit}）
            redisTemplate.delete(CACHE_KEY_POST_FOLLOW + postFollow.getPostId() + ":*");
            log.info("逻辑删除跟帖成功，跟帖ID：{}", postFollowId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("逻辑删除跟帖失败", e);
            throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
        }
    }


    /**
     * 批量删除：逻辑删除帖子关联的跟帖
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteByPostId(Long userId, Long postId) {
        try {
            // 1. 参数校验
            if (postId == null) {
                throw new BusinessException(ErrorCode.POST_ID_NULL);
            }

            // 2. 校验帖子存在性（可选：若帖子已物理删除，仍需清理关联跟帖）
            Post post = postService.getById(postId);
            if (post == null) {
                log.warn("批量删除跟帖：关联帖子不存在，帖子ID：{}", postId);
                throw new BusinessException(ErrorCode.POST_NOT_EXISTS);
            }

            // 3. 管理员或作者权限校验
            if (!userService.verifyRole(userId, UserRoleEnum.ADMIN) || !post.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 3. 批量删除跟帖记录（逻辑删除/物理删除，按系统设计选择）
            int deleteRows = postFollowMapper.batchDeleteByPostId(postId);
            if (deleteRows <= 0) {
                log.warn("批量删除跟帖：无匹配记录，帖子ID：{}", postId);
                return false;
            }

            log.info("批量删除帖子跟帖成功，帖子ID：{}，删除数量：{}", postId, deleteRows);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除帖子跟帖失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

}
