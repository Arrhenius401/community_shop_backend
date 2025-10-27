package com.community_shop.backend.dto.order;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import com.community_shop.backend.enums.SortEnum.OrderSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单状态更新请求DTO（匹配OrderService.updateOrderStatus方法）
 * 依据：
 * - 数据库order表：status字段状态流转规则
 * - Service层规则：状态变更需校验权限（买家/卖家/系统）、验证状态合法性
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderQueryDTO extends PageParam {

    /** 买家人ID（非空） */
    private Long buyerId;

    /** 卖家ID（非空） */
    private Long sellerId;

    /** 目标状态（枚举值，非空） */
    private OrderStatusEnum status;

    /** 支付方式 */
    private PayTypeEnum payType;

    /** 排序字段（枚举：CREATE_TIME-发布时间；LIKE_COUNT-点赞数；COMMENT_COUNT-评论数） */
    @NotNull(message = "排序字段不能为空")
    private OrderSortFieldEnum sortField = OrderSortFieldEnum.CREATE_TIME;

    /** 排序方向（枚举：ASC-升序；DESC-降序） */
    @NotNull(message = "排序方向不能为空")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}
