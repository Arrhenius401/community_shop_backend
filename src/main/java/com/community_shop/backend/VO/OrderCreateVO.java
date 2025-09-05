package com.community_shop.backend.VO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 订单创建请求VO（视图对象）
 * 用于接收前端传递的下单参数，适配创建订单的业务场景
 * 对应文档中"订单创建"功能的前端入参封装
 */
@Data
public class OrderCreateVO {

    /**
     * 商品ID
     * 关联product表的product_id，必传，用于定位下单商品
     * 对应文档4中product表的主键字段
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 收货地址
     * 买家收货地址，必传，用于订单配送
     * 对应文档2中订单创建流程的"收货地址"参数
     */
    @NotBlank(message = "收货地址不能为空")
    @Length(max = 500, message = "收货地址长度不能超过500字符")
    private String address;

    /**
     * 购买数量
     * 商品购买数量，默认1件，支持批量购买
     * 对应文档4中product表的stock字段扣减依据
     */
    private Integer quantity = 1;

    /**
     * 买家留言
     * 买家对订单的特殊备注（如配送时间要求），非必传
     */
    @Length(max = 200, message = "买家留言长度不能超过200字符")
    private String buyerRemark;

    /**
     * 支付方式
     * 支付渠道选择（如ALIPAY-支付宝、WECHAT-微信支付）
     * 对应文档2中支付流程的"支付平台"选择
     */
    @NotBlank(message = "支付方式不能为空")
    private String payType;
}
