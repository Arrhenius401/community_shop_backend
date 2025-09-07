package com.community_shop.backend.service.base;

import com.community_shop.backend.vo.post.PostFollowCreateVO;
import com.community_shop.backend.vo.post.PostFollowUpdateVO;
import com.community_shop.backend.component.enums.codeEnum.PostFollowStatusEnum;
import com.community_shop.backend.entity.PostFollow;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 跟帖模块Service接口
 * 核心职责：跟帖全生命周期管理、互动数据维护、权限校验及跨模块协同
 * 依赖：PostFollowMapper、PostService、UserService
 */
@Service
public interface PostFollowService {
    /**
     * 基础新增：插入跟帖记录
     * @param postFollow 跟帖实体（需包含postId、userId、content等核心字段）
     * @return 新增跟帖的自增主键postFollowId
     */
    Long insertPostFollow(PostFollow postFollow);

    /**
     * 基础查询：通过跟帖ID查询详情（关联作者信息）
     * @param postFollowId 跟帖唯一标识
     * @return 跟帖详情实体（含作者昵称、头像等扩展信息）
     */
    PostFollow selectPostFollowById(Long postFollowId);

    /**
     * 基础更新：编辑跟帖内容
     * @param vo 跟帖更新VO（含postFollowId、newContent等字段）
     * @param userId 操作用户ID（需为跟帖作者）
     * @return 影响行数（1=成功，0=未找到或无权限）
     */
    int updatePostFollowContent(PostFollowUpdateVO vo, Long userId);

    /**
     * 基础删除：逻辑删除跟帖
     * @param postFollowId 跟帖唯一标识
     * @return 影响行数（1=成功，0=未找到）
     */
    int deletePostFollowById(Long postFollowId);

    /**
     * 列表查询：按帖子ID分页查询有效跟帖
     * @param postId 关联帖子ID
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 有效跟帖列表（is_deleted=0、status=NORMAL，按创建时间倒序）
     */
    List<PostFollow> selectPostFollowsByPostId(Long postId, int offset, int limit);

    /**
     * 统计查询：统计指定帖子的有效跟帖总数
     * @param postId 关联帖子ID
     * @return 有效跟帖数量
     */
    int countPostFollowsByPostId(Long postId);

    /**
     * 业务方法：发布跟帖（含帖子评论数同步）
     * @param vo 跟帖创建VO（含postId、content等字段）
     * @param userId 发布用户ID
     * @return 新增跟帖的自增主键postFollowId
     */
    Long publishPostFollow(PostFollowCreateVO vo, Long userId);

//    /**
//     * 业务方法：更新跟帖点赞数
//     * @param postFollowId 跟帖唯一标识
//     * @param likeCount 最新点赞数（支持+1/-1调整）
//     * @return 影响行数（1=成功，0=未找到或已删除）
//     */
//    int updatePostFollowLikeCount(Long postFollowId, int likeCount);

    /**
     * 业务方法：更新跟帖状态（管理员操作）
     * @param postFollowId 跟帖唯一标识
     * @param status 目标状态（NORMAL/HIDDEN）
     * @param adminId 管理员ID
     * @return 影响行数（1=成功，0=未找到或无权限）
     */
    int updatePostFollowStatus(Long postFollowId, PostFollowStatusEnum status, Long adminId);
}
