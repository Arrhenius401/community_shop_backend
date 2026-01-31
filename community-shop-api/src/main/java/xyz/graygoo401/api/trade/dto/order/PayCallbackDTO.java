package xyz.graygoo401.api.trade.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.graygoo401.api.trade.enums.PayTypeEnum;

import java.math.BigDecimal;

/**
 * 支付回调参数DTO
 * 用于接收支付平台异步回调的参数信息
 */
@Schema(description = "支付回调参数DTO，接收第三方支付平台的异步回调信息")
@Data
public class PayCallbackDTO {

    /** 订单编号（系统内部订单号） */
    @Schema(description = "系统内部订单编号", example = "ORD20240520123456")
    private String orderNo;

    /** 支付金额（单位：元，保留两位小数） */
    @Schema(description = "实际支付金额", example = "89.99")
    private BigDecimal payAmount;

    /** 支付方式（如：WECHAT_PAY, ALIPAY, UNION_PAY等） */
    @Schema(description = "支付方式（枚举）", example = "ALIPAY")
    private PayTypeEnum payType;

    /** 第三方支付流水号 */
    @Schema(description = "第三方支付平台流水号", example = "2024052022001476540000001234")
    private String payNo;

    /** 支付状态（SUCCESS/FAIL） */
    @Schema(description = "支付状态", example = "SUCCESS")
    private String payStatus;

    /** 支付完成时间（格式：yyyy-MM-dd HH:mm:ss） */
    @Schema(description = "支付完成时间", example = "2024-05-20 14:30:00")
    private String payTime;

    /** 签名信息（用于验证回调的合法性） */
    @Schema(description = "回调签名（用于验证合法性）", example = "a1b2c3d4e5f67890")
    private String sign;

    /** 支付平台附加参数（JSON格式，可选） */
    @Schema(description = "支付平台附加参数（JSON格式）", example = "{\"tradeType\":\"APP\"}")
    private String attach;
}