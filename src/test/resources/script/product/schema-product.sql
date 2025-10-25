-- 商品表（Product实体，枚举字段status/condition存储code，对应ProductStatusEnum/ProductConditionEnum）
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
                           product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           seller_id BIGINT NOT NULL COMMENT '卖家ID（关联user表user_id）',
                           title VARCHAR(100) NOT NULL COMMENT '商品标题',
                           category VARCHAR(50) COMMENT '商品类别（如"二手手机""家居用品"）',
                           description TEXT COMMENT '商品详细描述',
                           price DECIMAL(10,2) NOT NULL COMMENT '商品价格（BigDecimal）',
                           stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
                           view_count INT DEFAULT 0 COMMENT '浏览量',
                           create_time DATETIME NOT NULL COMMENT '发布时间',
                           status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE' COMMENT '商品状态（枚举ProductStatusEnum的code：ON_SALE/OFF_SHELF/OUT_OF_STOCK）',
                           `condition` VARCHAR(20) COMMENT '商品成色（枚举ProductConditionEnum的code：NEW/USED_95/USED_9/USED_8）',
                           FOREIGN KEY (seller_id) REFERENCES `user`(user_id)
) COMMENT '商品信息表';