package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
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
@NoArgsConstructor
@Data
public class OrderDetailDTO {

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 订单商品数量 */
    private Integer quantity;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 支付金额（实际支付，可能含优惠） */
    private BigDecimal payAmount;

    /** 收货信息 */
    private String address;

    /** 买家留言 */
    private String buyerRemark;

    /** 支付时间（null表示未支付） */
    private LocalDateTime payTime;

    /** 发货时间（null表示未发货） */
    private LocalDateTime shipTime;

    /** 确认收货时间（null表示未确认） */
    private LocalDateTime receiveTime;

    /** 订单创建时间 */
    private LocalDateTime createTime;

    /** 订单备注 */
    private String remark;

    /** 订单状态 */
    private OrderStatusEnum status;

    /** 支付方式（ALIPAY/WECHAT） */
    private PayTypeEnum payType;

    /** 买家简易信息 */
    private BuyerSimpleDTO buyer;

    /** 卖家简易信息 */
    private SellerSimpleDTO seller;

    /** 订单商品简易信息 */
    private ProductSimpleDTO product;

    /**
     * 买家简易信息内部类
     */
    @Data
    public static class BuyerSimpleDTO {
        private Long userId;
        private String username;
        private String phone; // 脱敏展示，如138****5678
    }


    /**
     * 买家简易信息内部类
     */
    @Data
    public static class SellerSimpleDTO {
        private Long userId;
        private String username;
        private String phone; // 脱敏展示，如138****5678
    }

    /**
     * 订单商品详情内部类
     */
    @Data
    public static class ProductSimpleDTO {
        private Long productId; // 商品ID
        private String title;
        private Integer quantity;
        private BigDecimal price; // 单项总价（unitPrice*quantity）
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

//    /**
//     * 收货地址信息内部类
//     */
//    @Data
//    public static class AddressDTO {
//        private String receiverName;
//        private String receiverPhone;
//        private String fullAddress; // 省+市+区+详细地址
//    }
}
