package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.code.OrderStatusEnum;
import com.community_shop.backend.enums.simple.PayTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情响应DTO（匹配OrderService.getOrderDetail方法）
 * 依据：
 * - 数据库order表+order_item表关联查询结果
 * - Service层规则：详情需包含商品列表、收货信息、支付状态等完整数据
 */
@Schema(description = "订单详情响应DTO，包含订单完整信息（商品、收货信息、支付状态等）")
@NoArgsConstructor
@Data
public class OrderDetailDTO {

    /** 订单ID */
    @Schema(description = "订单ID", example = "10001")
    private Long orderId;

    /** 订单编号 */
    @Schema(description = "订单编号（业务唯一标识）", example = "ORD20240520123456")
    private String orderNo;

    /** 订单商品数量 */
    @Schema(description = "订单商品总数量", example = "2")
    private Integer quantity;

    /** 订单总金额 */
    @Schema(description = "订单总金额（原价合计）", example = "99.99")
    private BigDecimal totalAmount;

    /** 支付金额（实际支付，可能含优惠） */
    @Schema(description = "实际支付金额（可能包含优惠）", example = "89.99")
    private BigDecimal payAmount;

    /** 收货信息 */
    @Schema(description = "买家收货地址信息", example = "北京市朝阳区XX街道XX小区1号楼1单元101")
    private String address;

    /** 买家留言 */
    @Schema(description = "买家对订单的特殊备注", example = "请在周末送货")
    private String buyerRemark;

    /** 支付时间（null表示未支付） */
    @Schema(description = "支付时间，未支付则为null", example = "2024-05-20T14:30:00")
    private LocalDateTime payTime;

    /** 发货时间（null表示未发货） */
    @Schema(description = "发货时间，未发货则为null", example = "2024-05-21T09:15:00")
    private LocalDateTime shipTime;

    /** 确认收货时间（null表示未确认） */
    @Schema(description = "确认收货时间，未确认则为null", example = "2024-05-23T16:45:00")
    private LocalDateTime receiveTime;

    /** 订单创建时间 */
    @Schema(description = "订单创建时间", example = "2024-05-20T10:00:00")
    private LocalDateTime createTime;

    /** 订单备注 */
    @Schema(description = "系统或商家对订单的备注信息", example = "加急处理订单")
    private String remark;

    /** 订单状态 */
    @Schema(description = "订单状态（枚举）", example = "PAID")
    private OrderStatusEnum status;

    /** 支付方式（ALIPAY/WECHAT） */
    @Schema(description = "支付方式（枚举）", example = "ALIPAY")
    private PayTypeEnum payType;

    /** 买家简易信息 */
    @Schema(description = "买家简易信息（包含ID、用户名、脱敏手机号）")
    private BuyerSimpleDTO buyer;

    /** 卖家简易信息 */
    @Schema(description = "卖家简易信息（包含ID、用户名、脱敏手机号）")
    private SellerSimpleDTO seller;

    /** 订单商品简易信息 */
    @Schema(description = "订单商品信息（包含ID、标题、数量、总价）")
    private ProductSimpleDTO product;

    /**
     * 买家简易信息内部类
     */
    @Schema(description = "买家简易信息封装")
    @Data
    public static class BuyerSimpleDTO {
        /** 买家用户ID */
        @Schema(description = "买家用户ID", example = "1001")
        private Long userId;

        /** 买家用户名 */
        @Schema(description = "买家用户名", example = "张三")
        private String username;

        /** 买家脱敏手机号 */
        @Schema(description = "买家脱敏手机号", example = "138****5678")
        private String phone;
    }


    /**
     * 卖家简易信息内部类
     */
    @Schema(description = "卖家简易信息封装")
    @Data
    public static class SellerSimpleDTO {
        /** 卖家用户ID */
        @Schema(description = "卖家用户ID", example = "2001")
        private Long userId;

        /** 卖家用户名 */
        @Schema(description = "卖家用户名", example = "XX店铺")
        private String username;

        /** 卖家脱敏手机号 */
        @Schema(description = "卖家脱敏手机号", example = "139****1234")
        private String phone;
    }

    /**
     * 订单商品详情内部类
     */
    @Schema(description = "订单商品简易信息封装")
    @Data
    public static class ProductSimpleDTO {
        /** 商品ID */
        @Schema(description = "商品ID", example = "3001")
        private Long productId;

        /** 商品标题 */
        @Schema(description = "商品标题", example = "华为Mate 60 Pro")
        private String title;

        /** 商品购买数量 */
        @Schema(description = "商品购买数量", example = "1")
        private Integer quantity;

        /** 商品单价 */
        @Schema(description = "商品单项总价（单价*数量）", example = "6999.00")
        private BigDecimal price;
    }

    /**
     * 初始化默认值
     */
    public void initDefaultValue() {
        this.buyer = new BuyerSimpleDTO();
        this.seller = new SellerSimpleDTO();
        this.product = new ProductSimpleDTO();

        this.buyer.setUserId(-1L);
        this.buyer.setUsername("未知");
        this.buyer.setPhone("未知");

        this.seller.setUserId(-1L);
        this.seller.setUsername("未知");
        this.seller.setPhone("未知");

        this.product.setPrice(BigDecimal.ZERO);
        this.product.setQuantity(0);
        this.product.setTitle("未知");
        this.product.setProductId(-1L);
    }
}