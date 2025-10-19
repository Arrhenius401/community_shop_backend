package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import lombok.Data;

@Data
public class UserStatusUpdateDTO {

    /** 目标用户ID */
    private Long userId;

    /** 目标状态 */
    private UserStatusEnum status;
}
