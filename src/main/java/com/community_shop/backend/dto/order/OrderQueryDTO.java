package com.community_shop.backend.dto.order;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import com.community_shop.backend.enums.SortEnum.OrderSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询请求DTO（用于订单列表分页查询）
 * 依据：
 * - 支持按买家/卖家/状态/支付方式筛选
 * - 支持分页和排序
 */
@Schema(description = "订单查询请求DTO，用于订单列表分页查询和筛选")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderQueryDTO extends PageParam {

    /** 买家人ID（非空） */
    @Schema(description = "买家用户ID（筛选条件）", example = "1001")
    private Long buyerId;

    /** 卖家ID（非空） */
    @Schema(description = "卖家用户ID（筛选条件）", example = "2001")
    private Long sellerId;

    /** 目标状态（枚举值，非空） */
    @Schema(description = "订单状态（筛选条件）", example = "PAID")
    private OrderStatusEnum status;

    /** 支付方式 */
    @Schema(description = "支付方式（筛选条件）", example = "ALIPAY")
    private PayTypeEnum payType;

    /** 排序字段（枚举：CREATE_TIME-发布时间；默认按创建时间排序） */
    @Schema(description = "排序字段（枚举）", example = "CREATE_TIME", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序字段不能为空")
    private OrderSortFieldEnum sortField = OrderSortFieldEnum.CREATE_TIME;

    /** 排序方向（枚举：ASC-升序；DESC-降序；默认降序） */
    @Schema(description = "排序方向（枚举）", example = "DESC", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序方向不能为空")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}