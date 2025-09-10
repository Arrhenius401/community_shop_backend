package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.SimpleEnum.ThirdPartyTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 第三方绑定列表响应DTO（匹配Service层UserThirdPartyService.listBindings方法）
 */
@Data
public class ThirdPartyBindingListDTO {

    /** 用户ID */
    private Long userId;

    /** 有效绑定列表 */
    private List<BindingItemDTO> bindingItems;

    /**
     * 单条绑定信息内部类
     */
    @Data
    public static class BindingItemDTO {
        /** 绑定记录ID（user_third_party表主键） */
        private Long bindingId;

        /** 第三方平台类型（WECHAT/QQ/ALIPAY） */
        private ThirdPartyTypeEnum thirdType;

        /** 第三方账号标识（脱敏显示，如“微信用户****1234”） */
        private String openidDesensitized;

        /** 绑定时间 */
        private LocalDateTime bindTime;
    }
}
