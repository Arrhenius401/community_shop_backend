package xyz.graygoo401.trade.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.trade.enums.OrderStatusEnum;
import xyz.graygoo401.api.trade.enums.PayTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("`order`")
public class Order {

    /** 订单ID */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /** 商品ID */
    @TableField("product_id")
    private Long productId;

    /** 买家ID */
    @TableField("buyer_id")
    private Long buyerId;

    /** 卖家ID */
    @TableField("seller_id")
    private Long sellerId;

    /** 订单编号 */
    @TableField("order_no")
    private String orderNo;

    /** 交易金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 订单数量 */
    @TableField("quantity")
    private Integer quantity;

    /** 买家名称 */
    @TableField("receiver_name")
    private String receiverName;

    /** 收货地址 */
    @TableField("address")
    private String address;

    /** 收获手机号码 */
    @TableField("phone_number")
    private String phoneNumber;

    /** 买家留言 */
    @TableField("buyer_remark")
    private String buyerRemark;

    /** 订单状态 */
    @TableField("status")
    private OrderStatusEnum status;

    /** 支付方式 */
    @TableField("pay_type")
    private PayTypeEnum payType;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 支付时间 */
    @TableField(value = "pay_time")
    private LocalDateTime payTime;

    /** 发货时间 */
    @TableField(value = "ship_time")
    private LocalDateTime shipTime;

    /** 收货时间 */
    @TableField(value = "receive_time")
    private LocalDateTime receiveTime;

    /** 订单取消时间 */
    @TableField(value = "cancel_time")
    private LocalDateTime cancelTime;

    /** 订单预计超时时间 */
    @TableField(value = "pay_expire_time")
    private LocalDateTime payExpireTime;


}
