package xyz.graygoo401.community.service.base;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.graygoo401.api.community.dto.follow.*;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.service.BaseService;
import xyz.graygoo401.community.dao.entity.PostFollow;

/**
 * 跟帖模块Service接口
 * 核心职责：跟帖全生命周期管理、互动数据维护、权限校验及跨模块协同
 * 依赖：PostFollowMapper、PostService、UserService
 */
@Service
public interface PostFollowService extends BaseService<PostFollow> {

    /**
     * 发布跟帖
     * @param userId 创建用户ID
     * @param postFollowPublishDTO 跟帖发布参数（帖子ID、内容、@用户）
     * @return 跟帖详情
     * @throws BusinessException 帖子不存在、内容为空时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    PostFollowDetailDTO publishFollow(Long userId, PostFollowPublishDTO postFollowPublishDTO);

    /**
     * 编辑跟帖
     * @param userId 操作用户ID
     * @param postFollowUpdateDTO 跟帖更新参数（跟帖ID、新内容）
     * @return 编辑后的跟帖详情
     * @throws BusinessException 无权限（非作者）、跟帖已删除时抛出
     */
    PostFollowDetailDTO updateFollow(Long userId, PostFollowUpdateDTO postFollowUpdateDTO);

    /**
     * 管理员更新跟帖状态（正常/隐藏）
     * @param userId 操作用户ID
     * @param statusUpdateDTO 状态更新参数（跟帖ID、目标状态、管理员ID）
     * @return 状态更新是否成功
     * @throws BusinessException 无管理员权限时抛出
     */
    Boolean updateFollowStatus(Long userId, PostFollowStatusUpdateDTO statusUpdateDTO);

    /**
     * 按帖子ID查询跟帖列表
     * @param postFollowQueryDTO 查询参数（帖子ID、分页、状态）
     * @return 分页跟帖列表
     */
    PageResult<PostFollowDetailDTO> queryFollows(PostFollowQueryDTO postFollowQueryDTO);

    /**
     * 多条件查询跟帖数量
     * @param postFollowQueryDTO 跟帖查询参数（关键词、排序、分页）
     * @return 跟帖数量
     */
    int countFollows(PostFollowQueryDTO postFollowQueryDTO);

    /**
     * 基础删除：逻辑删除跟帖
     * @param userId 操作用户ID
     * @param postFollowId 跟帖唯一标识
     * @return 影响行数（1=成功，0=未找到）
     */
    Boolean deletePostFollowById(Long userId, Long postFollowId);

    /**
     * 批量删除某帖子的所有跟帖（帖子删除时联动）
     * @param userId 操作用户ID
     * @param postId 帖子ID
     * @return 删除成功数量
     */
    Boolean batchDeleteByPostId(Long userId, Long postId);

}
