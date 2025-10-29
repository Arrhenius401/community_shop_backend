package com.community_shop.backend.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读消息统计DTO
 * 用于展示用户总未读消息数及各类型未读消息数
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "未读消息统计DTO，包含总未读及各类型未读数")
public class MessageStatDTO {

    /**
     * 总未读消息数
     */
    @Schema(description = "所有类型未读消息总数", example = "5")
    private Integer totalUnread = 0;

    /**
     * 订单通知类未读消息数（MessageTypeEnum.ORDER）
     */
    @Schema(description = "订单通知类型未读消息数", example = "2")
    private Integer orderUnread = 0;

    /**
     * 系统公告类未读消息数（MessageTypeEnum.SYSTEM）
     */
    @Schema(description = "系统公告类型未读消息数", example = "1")
    private Integer systemUnread = 0;

    /**
     * 售后提醒类未读消息数（MessageTypeEnum.AFTER_SALE）
     */
    @Schema(description = "售后提醒类型未读消息数", example = "2")
    private Integer afterSaleUnread = 0;
}