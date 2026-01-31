package xyz.graygoo401.trade.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.trade.enums.ProductConditionEnum;
import xyz.graygoo401.api.trade.enums.ProductStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("product")
public class Product {

    /** 商品ID */
    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;

    /** 卖家ID */
    @TableField("seller_id")
    private Long sellerId;

    /** 商品标题 */
    @TableField("title")
    private String title;

    /** 商品类别 */
    @TableField("category")
    private String category;

    /** 商品描述 */
    @TableField("description")
    private String description;

    /** 商品价格 */
    @TableField("price")
    private BigDecimal price;

    /** 商品库存 */
    @TableField("stock")
    private Integer stock;

    /** 浏览量 */
    @TableField("view_count")
    private Integer viewCount;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 商品状态 */
    @TableField("status")
    private ProductStatusEnum status;

    /** 商品成色 */
    @TableField("condition")
    private ProductConditionEnum condition;

}
