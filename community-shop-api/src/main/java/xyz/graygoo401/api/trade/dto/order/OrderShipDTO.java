package xyz.graygoo401.api.trade.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单发货信息dto
 */
@Schema(description = "订单发货请求DTO，用于提交发货信息")
@Data
public class OrderShipDTO {

    /** 订单ID */
    @Schema(description = "订单ID", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 快递公司 */
    @Schema(description = "快递公司名称", example = "顺丰速运", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    /** 快递单号 */
    @Schema(description = "物流单号", example = "SF1234567890123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "物流单号不能为空")
    private String expressNo;

    /** 发货备注 */
    @Schema(description = "发货备注信息", example = "易碎物品，请轻放")
    private String shipRemark;
}