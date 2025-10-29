package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 订单支付请求DTO（匹配OrderPayService.createPayment方法）
 * 用于接收前端支付参数，包含订单标识、支付方式、金额校验等核心信息
 */
@Schema(description = "订单支付请求DTO，用于提交支付参数")
@Data
public class OrderPayDTO {

    /**
     * 订单ID（非空，关联订单表order_id字段）
     */
    @Schema(description = "订单ID", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 支付方式（枚举：ALIPAY-支付宝；WECHAT-微信支付；CARD-银行卡）
     */
    @Schema(description = "支付方式（枚举）", example = "ALIPAY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "支付方式不能为空")
    private PayTypeEnum payType;

    /**
     * 支付金额（必须与订单实际金额一致，用于校验防篡改）
     */
    @Schema(description = "支付金额（需与订单实际金额一致）", example = "89.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private Double payAmount;

    /**
     * 支付密码（可选，部分支付方式需要，如银行卡）
     */
    @Schema(description = "支付密码（部分支付方式必填）", example = "******")
    private String payPassword;

    /**
     * 第三方支付平台所需的额外参数（JSON格式，如支付宝的authCode）
     */
    @Schema(description = "第三方支付额外参数（JSON格式）", example = "{\"authCode\":\"28763443825664394\"}")
    private String extraParams;
}