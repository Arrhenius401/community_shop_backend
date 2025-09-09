package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 订单支付请求DTO（匹配OrderPayService.createPayment方法）
 * 用于接收前端支付参数，包含订单标识、支付方式、金额校验等核心信息
 */
@Data
public class OrderPayDTO {

    /**
     * 订单ID（非空，关联订单表order_id字段）
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 支付用户ID（当前登录用户，非空，需与订单的user_id一致）
     */
    @NotNull(message = "支付用户ID不能为空")
    private Long userId;

    /**
     * 支付方式（枚举：ALIPAY-支付宝；WECHAT-微信支付；CARD-银行卡）
     */
    @NotBlank(message = "支付方式不能为空")
    private PayTypeEnum payType;

    /**
     * 支付金额（必须与订单实际金额一致，用于校验防篡改）
     */
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private Double payAmount;

    /**
     * 支付密码（可选，部分支付方式需要，如银行卡）
     */
    private String payPassword;

    /**
     * 第三方支付平台所需的额外参数（JSON格式，如支付宝的authCode）
     */
    private String extraParams;
}
