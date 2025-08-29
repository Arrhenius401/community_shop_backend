package com.community_shop.backend.VO;

import lombok.Data;

@Data
public class OrderCreateVO {
    // 商品ID（非空，必须是已发布且库存充足的商品）
    private Long productId;

    // 购买数量（非空，≥1且≤商品当前库存）
    private Integer quantity;

    // 收货地址ID（非空，必须是买家已保存的地址）
    private Long addressId;

    // 支付方式（非空，枚举值："ALIPAY"/"WECHAT_PAY"/"BANK_CARD"）
    private String payMethod;

    // 买家留言（可选，长度≤200）
    private String buyerMessage;
}
