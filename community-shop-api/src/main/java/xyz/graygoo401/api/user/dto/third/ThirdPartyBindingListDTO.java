package xyz.graygoo401.api.user.dto.third;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.graygoo401.api.user.enums.ThirdPartyTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 第三方绑定列表响应DTO（匹配Service层UserThirdPartyService.listBindings方法）
 */
@Data
@Schema(description = "第三方账号绑定列表响应数据模型")
public class ThirdPartyBindingListDTO {

    /** 用户ID */
    @Schema(description = "用户唯一标识ID", example = "1001")
    private Long userId;

    /** 有效绑定列表 */
    @Schema(description = "用户已绑定的第三方账号列表")
    private List<BindingItemDTO> bindingItems;

    /**
     * 单条绑定信息内部类
     */
    @Data
    @Schema(description = "第三方账号绑定详情")
    public static class BindingItemDTO {
        /** 绑定记录ID（user_third_party表主键） */
        @Schema(description = "绑定记录唯一ID", example = "2001")
        private Long bindingId;

        /** 第三方平台类型（WECHAT/QQ/ALIPAY） */
        @Schema(description = "第三方平台类型", example = "WECHAT")
        private ThirdPartyTypeEnum thirdType;

        /** 第三方账号标识（脱敏显示，如“微信用户****1234”） */
        @Schema(description = "脱敏后的第三方账号标识", example = "微信用户****1234")
        private String openidDesensitized;

        /** 绑定时间 */
        @Schema(description = "账号绑定时间", example = "2023-01-01T12:00:00")
        private LocalDateTime bindTime;
    }
}