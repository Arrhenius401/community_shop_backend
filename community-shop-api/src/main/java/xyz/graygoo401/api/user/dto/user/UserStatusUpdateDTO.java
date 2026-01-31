package xyz.graygoo401.api.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import xyz.graygoo401.common.enums.UserStatusEnum;

@Data
@Schema(description = "用户状态更新请求数据模型")
public class UserStatusUpdateDTO {

    /** 目标用户ID */
    @Schema(description = "需要更新状态的用户ID", example = "1001")
    private Long userId;

    /** 目标状态 */
    @NotNull(message = "目标状态不能为空")
    @Schema(description = "目标状态（NORMAL-正常，BANNED-禁用）", example = "BANNED")
    private UserStatusEnum status;
}