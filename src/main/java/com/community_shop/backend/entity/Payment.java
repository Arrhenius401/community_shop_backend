package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community_shop.backend.enums.code.PayStatusEnum;
import com.community_shop.backend.enums.code.PayTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付信息实体类
 */
@Data
@NoArgsConstructor
@TableName("payment")
public class Payment {

    /** 支付ID */
    @TableId(value = "payment_id", type = IdType.AUTO)
    private Long paymentId;

    /** 订单ID */
    @TableField("order_id")
    private Long orderId;

    /** 订单号 */
    @TableField("order_no")
    private String orderNo;

    /** 支付方式 */
    @TableField("pay_type")
    private PayTypeEnum payType;

    /** 第三方平台交易流水号 */
    @TableField("platform_trade_no")
    private String platformTradeNo;

    /** 支付金额 */
    @TableField("pay_amount")
    private BigDecimal payAmount;

    /** 支付状态 */
    @TableField("pay_status")
    private PayStatusEnum payStatus;

    /** 支付时间 */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /** 回调时间 */
    @TableField("callback_time")
    private LocalDateTime callbackTime;

    /** 回调内容 */
    @TableField("callback_content")
    private String callbackContent;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
