package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单详情响应DTO（匹配OrderService.getOrderDetail方法）
 * 依据：
 * - 数据库order表+order_item表关联查询结果
 * - Service层规则：详情需包含商品列表、收货信息、支付状态等完整数据
 */
@Data
public class OrderDetailDTO {

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 订单所属用户ID */
    private Long buyerId;

    /** 订单所属卖家ID */
    private Long sellerId;

    /** 订单编号 */
    private String orderNo;

    /** 订单商品数量 */
    private Integer quantity;

    /** 订单总金额 */
    private Double totalAmount;

//    /** 支付金额（实际支付，可能含优惠） */
//    private Double payAmount;

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

    /** 订单状态（枚举：PENDING-待支付；PAID-已支付；SHIPPED-已发货；RECEIVED-已收货；CANCELLED-已取消） */
    private OrderStatusEnum status;

    /** 支付方式（ALIPAY/WECHAT） */
    private PayTypeEnum payType;

//    /**
//     * 买家简易信息内部类
//     */
//    @Data
//    public static class UserSimpleDTO {
//        private Long userId;
//        private String username;
//        private String phone; // 脱敏展示，如138****5678
//    }
//
//    /**
//     * 收货地址信息内部类
//     */
//    @Data
//    public static class AddressDTO {
//        private String receiverName;
//        private String receiverPhone;
//        private String fullAddress; // 省+市+区+详细地址
//    }
//
//    /**
//     * 订单商品详情内部类
//     */
//    @Data
//    public static class OrderItemDetailDTO {
//        private Long itemId; // 订单项ID
//        private Long productId;
//        private String productName;
//        private String productImage; // 商品图片
//        private Double unitPrice; // 购买时单价
//        private Integer quantity;
//        private Double totalPrice; // 单项总价（unitPrice*quantity）
//    }
}
