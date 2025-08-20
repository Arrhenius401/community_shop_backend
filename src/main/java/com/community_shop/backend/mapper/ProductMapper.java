package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    // 基础 CRUD 操作

    /**
     * 发布商品
     * @param product 商品实体
     * @return 插入结果影响行数
     */
    @Insert("INSERT INTO product(title, category, price, stock, condition, seller_id, view_count) " +
            "VALUES(#{title}, #{category}, #{price}, #{stock}, #{condition}, #{sellerId}, #{viewCount})")
    int insert(Product product);

    /**
     * 查询商品详情
     * @param productId 商品ID
     * @return 商品实体
     */
    @Select("SELECT * FROM product WHERE product_id = #{productId}")
    Product selectById(Long productId);

    /**
     * 更新商品信息
     * @param product 商品实体
     * @return 更新结果影响行数
     */
    @Update("UPDATE product SET title = #{title}, category = #{category}, price = #{price}, " +
            "stock = #{stock}, condition = #{condition}, seller_id = #{sellerId}, view_count = #{viewCount} " +
            "WHERE product_id = #{productID}")
    int updateById(Product product);

    // 搜索与筛选功能

    /**
     * 按类别、价格、成色等条件查询商品
     * @param category 类别
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param condition 成色
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商品列表
     */
    @Select("<script>" +
            "SELECT * FROM product " +
            "WHERE 1=1 " +
            "<if test='category != null and category != \"\"'>AND category = #{category}</if> " +
            "<if test='minPrice != null'>AND price >= #{minPrice}</if> " +
            "<if test='maxPrice != null'>AND price <= #{maxPrice}</if> " +
            "<if test='condition != null and condition != \"\"'>AND condition = #{condition}</if> " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Product> selectByCondition(@Param("category") String category,
                                    @Param("minPrice") Double minPrice,
                                    @Param("maxPrice") Double maxPrice,
                                    @Param("condition") String condition,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    /**
     * 按关键词模糊搜索商品
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商品列表
     */
    @Select("SELECT * FROM product " +
            "WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "LIMIT #{offset}, #{limit}")
    List<Product> selectByKeyword(@Param("keyword") String keyword,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    // 库存与状态管理

    /**
     * 更新商品库存
     * @param productId 商品ID
     * @param stock 库存数量
     * @return 更新结果影响行数
     */
    @Update("UPDATE product SET stock = #{stock} WHERE product_id = #{productId}")
    int updateStock(@Param("productId") Long productId, @Param("stock") int stock);

    /**
     * 查询卖家发布的商品
     * @param sellerId 卖家ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商品列表
     */
    @Select("SELECT * FROM product WHERE seller_id = #{sellerId} LIMIT #{offset}, #{limit}")
    List<Product> selectBySellerId(@Param("sellerId") Long sellerId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

}
