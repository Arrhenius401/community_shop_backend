package com.community_shop.backend.dto.order;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付回调参数DTO
 * 用于接收支付平台异步回调的参数信息
 */
@Data
public class PayCallbackDTO {

    /**
     * 订单编号（系统内部订单号）
     */
    private String orderNo;

    /**
     * 支付金额（单位：元，保留两位小数）
     */
    private BigDecimal payAmount;

    /**
     * 支付方式（如：WECHAT_PAY, ALIPAY, UNION_PAY等）
     */
    private String payType;

    /**
     * 第三方支付流水号
     */
    private String payNo;

    /**
     * 支付状态（SUCCESS/FAIL）
     */
    private String payStatus;

    /**
     * 支付完成时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String payTime;

    /**
     * 签名信息（用于验证回调的合法性）
     */
    private String sign;

    /**
     * 支付平台附加参数（JSON格式，可选）
     */
    private String attach;
}
