package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.dto.product.ProductQueryDTO;
import com.community_shop.backend.dto.product.SellerProductQueryDTO;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 商品模块Mapper接口，对应product表操作
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    // ==================== 基础CRUD ====================
    /**
     * 发布商品（插入商品数据）
     * @param product 商品实体（含标题、类别、价格等核心字段）
     * @return 影响行数
     */
    int insert(Product product);

    /**
     * 通过商品ID查询商品详情
     * @param productId 商品唯一标识
     * @return 商品完整实体
     */
    Product selectById(@Param("productId") Long productId);

    /**
     * 更新商品完整信息
     * @param product 商品实体（含需更新的字段）
     * @return 影响行数
     */
    int updateById(Product product);

    /**
     * 删除商品（逻辑删除，更新status状态）
     * @param productId 商品ID
     * @param status 目标状态（如"DELETED"）
     * @return 影响行数
     */
    int deleteById(@Param("productId") Long productId, @Param("status") ProductStatusEnum status);

    /**
     * 删除商品信息
     * @param productId 商品ID
     * @return 删除结果影响行数
     */
    @Delete("DELETE FROM product WHERE product_id = #{productId}")
    int deleteById(Long productId);


    // ==================== 搜索与筛选 ====================

    /**
     * 验证卖家ID是否存在
     * @param sellerId
     * @return
     */
    @Select("SELECT * FROM product WHERE seller_id = #{sellerId}")
    int verifySellerExists(@Param("sellerId") Long sellerId);

    /**
     * 多条件分页查询商品（按类别、价格、成色筛选）
     * @param category 商品类别（如"二手手机"）
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param condition 商品成色（枚举）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 商品分页列表
     */
    List<Product> selectByCondition(
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("condition") ProductConditionEnum condition,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 按关键词模糊搜索商品
     * @param keyword 搜索关键词（匹配标题/描述）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 商品分页列表
     */
    List<Product> selectByKeyword(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计关键词搜索的商品总数
     * @param keyword 搜索关键词
     * @return 商品总数
     */
    int countByKeyword(@Param("keyword") String keyword);

    /**
     * 统计多条件筛选后的商品总数
     * @param category 商品类别
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param condition 商品成色
     * @return 商品总数
     */
    int countByCondition(
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("condition") ProductConditionEnum condition
    );

    /**
     * 按复杂查询条件统计商品数量
     * @param productQueryDTO 封装查询条件的DTO
     * @return 符合条件的商品总数
     */
    int countByQuery(@Param("query") ProductQueryDTO productQueryDTO);

    /**
     * 按复杂查询条件分页查询商品
     * @param productQueryDTO 封装查询条件的DTO（含分页参数）
     * @return 符合条件的商品列表
     */
    List<Product> selectByQuery(@Param("query") ProductQueryDTO productQueryDTO);

    /**
     * 按卖家视角的查询条件统计商品数量
     * @param productQueryDTO 卖家商品查询DTO
     * @return 符合条件的商品总数
     */
    int countBySellerQuery(@Param("query") SellerProductQueryDTO productQueryDTO);

    /**
     * 按卖家视角的查询条件分页查询商品
     * @param productQueryDTO 卖家商品查询DTO（含分页参数）
     * @return 符合条件的商品列表
     */
    List<Product> selectBySellerQuery(@Param("query") SellerProductQueryDTO productQueryDTO);


    // ==================== 库存与卖家查询 ====================
    /**
     * 更新商品库存
     * @param productId 商品ID
     * @param stock 调整后的库存数量
     * @return 影响行数
     */
    int updateStock(@Param("productId") Long productId, @Param("stock") int stock);

    /**
     * 更新商品浏览量（自增1）
     * @param productId 商品ID
     * @return 影响行数
     */
    int updateViewCount(@Param("productId") Long productId);

    /**
     * 分页查询卖家发布的商品
     * @param sellerId 卖家ID
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 商品分页列表
     */
    List<Product> selectBySellerId(
            @Param("sellerId") Long sellerId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 按卖家ID+商品状态分页查询商品
     * @param sellerId 卖家ID
     * @param status 商品状态（枚举）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 商品分页列表
     */
    List<Product> selectBySellerIdAndStatus(
            @Param("sellerId") Long sellerId,
            @Param("status") ProductStatusEnum status,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // ==================== 管理操作 ===================

    /**
     * 更新商品状态
     * @param productId
     * @param status
     * @return
     */
    @Update("UPDATE product SET status = #{status} WHERE product_id = #{productId}")
    int updateStatus(@Param("productId") Long productId, @Param("status") ProductStatusEnum status);

}
