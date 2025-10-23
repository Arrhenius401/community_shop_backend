package com.community_shop.backend.dto.message;

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
public class MessageStatDTO {

    /**
     * 总未读消息数
     */
    private Integer totalUnread = 0;

    /**
     * 订单通知类未读消息数（MessageTypeEnum.ORDER）
     */
    private Integer orderUnread = 0;

    /**
     * 系统公告类未读消息数（MessageTypeEnum.SYSTEM）
     */
    private Integer systemUnread = 0;

    /**
     * 售后提醒类未读消息数（MessageTypeEnum.AFTER_SALE）
     */
    private Integer afterSaleUnread = 0;
}
