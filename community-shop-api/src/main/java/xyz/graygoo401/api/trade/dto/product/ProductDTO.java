package xyz.graygoo401.api.trade.dto.product;

import lombok.Data;
import xyz.graygoo401.api.trade.enums.ProductConditionEnum;
import xyz.graygoo401.api.trade.enums.ProductStatusEnum;
import xyz.graygoo401.api.user.dto.user.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long productId;
    private Long sellerId; // 物理存储 ID
    private UserDTO seller; // 装配：通过 Feign 调用 User 服务填充
    private String title;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer viewCount;
    private ProductStatusEnum status;
    private ProductConditionEnum condition;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}