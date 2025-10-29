package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户状态更新请求数据模型")
public class UserStatusUpdateDTO {

    /** 目标用户ID */
    @Schema(description = "需要更新状态的用户ID", example = "1001")
    private Long userId;

    /** 目标状态 */
    @Schema(description = "目标状态（NORMAL-正常，DISABLED-禁用）", example = "DISABLED")
    private UserStatusEnum status;
}