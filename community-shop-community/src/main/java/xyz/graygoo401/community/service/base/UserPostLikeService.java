package xyz.graygoo401.community.service.base;

import xyz.graygoo401.common.service.BaseService;
import xyz.graygoo401.community.dao.entity.UserPostLike;

/**
 * 用户帖子点赞Service接口，实现《文档》中点赞等核心功能
 */
public interface UserPostLikeService extends BaseService<UserPostLike> {

    /**
     * 用户点赞某帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否点赞成功
     */
    Boolean like(Long userId, Long postId);

    /**
     * 取消用户对帖子的点赞
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否取消成功
     */
    Boolean cancelLike(Long userId, Long postId);

    /**
     * 判断用户是否点赞某帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 点赞数
     */
    Boolean isLiked(Long userId, Long postId);

    /**
     * 统计用户的点赞总数
     * @param userId 用户ID
     * @return 点赞总数
     */
    int countLikesByUserId(Long userId);

    /**
     * 统计帖子的点赞数
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 点赞数
     */
    int countLikesByPostId(Long userId, Long postId);

    /**
     * 批量删除某帖子的所有点赞记录
     * @param postId 帖子ID
     * @return 删除成功数量
     */
    int batchDeleteByPostId(Long postId);
}
