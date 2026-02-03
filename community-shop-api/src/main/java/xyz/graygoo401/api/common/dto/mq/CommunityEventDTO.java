package xyz.graygoo401.api.common.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 社区事件DTO
 */
@Data
@AllArgsConstructor
public class CommunityEventDTO {
    private Long postId;
    private Long operatorId; // 操作人
    private Long authorId;   // 被通知人（作者）
    private String action;   // LIKE, FOLLOW
}
