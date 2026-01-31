package xyz.graygoo401.api.community.dto.post;

import lombok.Data;
import xyz.graygoo401.api.user.dto.user.UserDTO;

import java.time.LocalDateTime;

/**
 * 帖子详情VO
 */
@Data
public class PostDTO {
    private Long postId;
    private UserDTO publisher; // 装配：通过 userId 远程调用 User 服务获取
    private String title;
    private String content;
    private Integer likeCount;
    private Integer postFollowCount;
    private Boolean isHot;
    private Boolean isEssence;
    private Boolean isTop;
    private Boolean isLiked; // 装配：查询点赞表确定当前用户状态
    private LocalDateTime createTime;
}
