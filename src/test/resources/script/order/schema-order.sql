-- 1. 订单表（Order实体，枚举字段status/payType存储code，对应OrderStatusEnum/PayTypeEnum）
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '商品ID（关联product表product_id）',
    buyer_id BIGINT NOT NULL COMMENT '买家ID（关联user表user_id）',
    seller_id BIGINT NOT NULL COMMENT '卖家ID（关联user表user_id）',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    quantity INT NOT NULL COMMENT '购买数量',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货联系人',
    address VARCHAR(255) NOT NULL COMMENT '收货地址',
    phone_number VARCHAR(20) NOT NULL COMMENT '收货电话号码',
    buyer_remark VARCHAR(200) COMMENT '买家留言',
    status VARCHAR(20) NOT NULL COMMENT '订单状态（枚举OrderStatusEnum的code：PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CANCELLED）',
    pay_type VARCHAR(20) COMMENT '支付方式（枚举PayTypeEnum的code：WECHAT_PAY/ALIPAY）',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    pay_time DATETIME COMMENT '支付时间',
    ship_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '收货时间',
    cancel_time DATETIME COMMENT '取消时间',
    pay_expire_time DATETIME NOT NULL COMMENT '支付超时时间',
    FOREIGN KEY (product_id) REFERENCES `product`(product_id),
    FOREIGN KEY (buyer_id) REFERENCES `user`(user_id),
    FOREIGN KEY (seller_id) REFERENCES `user`(user_id)
) COMMENT '订单信息表';

-- 2. 评价表（Evaluation实体，枚举字段status存储code，对应EvaluationStatusEnum）
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
    eval_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE COMMENT '关联订单ID（一个订单对应一个评价）',
    user_id BIGINT NOT NULL COMMENT '评价者ID（买家，关联user表user_id）',
    seller_id BIGINT NOT NULL COMMENT '被评价卖家ID（关联user表user_id）',
    content TEXT COMMENT '评价内容',
    score INT NOT NULL CHECK (score BETWEEN 1 AND 5) COMMENT '评分（1-5星）',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '评价状态（枚举EvaluationStatusEnum的code：NORMAL/HIDDEN）',
    create_time DATETIME NOT NULL COMMENT '评价时间',
    update_time DATETIME COMMENT '修改时间',
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    FOREIGN KEY (user_id) REFERENCES `user`(user_id),
    FOREIGN KEY (seller_id) REFERENCES `user`(user_id)
) COMMENT '订单评价表';