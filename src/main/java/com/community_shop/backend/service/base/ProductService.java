package com.community_shop.backend.service.base;

import com.community_shop.backend.DTO.param.PageParam;
import com.community_shop.backend.DTO.result.PageResult;
import com.community_shop.backend.VO.ProductUpdateVO;
import com.community_shop.backend.VO.ProductVO;
import com.community_shop.backend.component.enums.codeEnum.ProductConditionEnum;
import com.community_shop.backend.entity.Product;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品管理Service接口，实现《文档》中商品发布、搜索、库存管理等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：商品发布（多图/视频）、搜索筛选、库存管理
 * 2. 《文档4_数据库工作（新）.docx》：product表结构（product_id、price、stock、seller_id等）
 * 3. 《代码文档1 Mapper层设计.docx》：ProductMapper的CRUD及库存更新方法
 */
@Service
public interface ProductService {
    // 获取所有商品
    List<Product> getAllProducts();

    // 获取商品详情
    Product getProductById(int id);

    // 添加商品
    int addProduct(Product product);

    // 更新商品信息
    int updateProduct(Product product);

    // 删除商品
    int deleteProduct(int id);

    /**
     * 新增商品（基础CRUD）
     * 核心逻辑：初始化库存≥1、浏览量为0，调用ProductMapper.insert插入数据
     * @param product 商品实体（含title、category、price、sellerId，不含product_id）
     * @return 新增商品ID
     * @see com.community_shop.backend.mapper.ProductMapper#insert(Product)
     */
    Long insertProduct(Product product);

    /**
     * 按商品ID查询（基础CRUD）
     * 核心逻辑：调用ProductMapper.selectById查询，关联UserService获取卖家信用分
     * @param productId 商品ID（主键）
     * @return 含卖家信用分的商品详情
     * @see com.community_shop.backend.mapper.ProductMapper#selectById(Long)
     * @see UserService#selectUserById(Long)
     */
    Product selectProductById(Long productId);

    /**
     * 按关键词搜索商品（基础CRUD，分页）
     * 核心逻辑：调用ProductMapper.selectByKeyword模糊搜索，按浏览量倒序排序
     * @param keyword 搜索关键词（匹配商品标题/描述）
     * @param pageParam 分页参数（页码、每页条数）
     * @return 分页商品列表
     * @see com.community_shop.backend.mapper.ProductMapper#selectByKeyword(String, int, int)
     */
    PageResult<Product> selectProductByKeyword(String keyword, PageParam pageParam);

    /**
     * 更新商品信息（基础CRUD）
     * 核心逻辑：校验仅卖家可操作，调用ProductMapper.updateById更新价格、库存等信息
     * @param productId 商品ID
     * @param productUpdateVO 商品更新参数（价格、库存、描述）
     * @param sellerId 卖家ID（需与商品seller_id一致）
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.ProductMapper#updateById(Product)
     */
    Boolean updateProductInfo(Long productId, ProductUpdateVO productUpdateVO, Long sellerId);

    /**
     * 按商品ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验卖家或管理员权限，调用ProductMapper.deleteById标记删除
     * @param productId 待删除商品ID
     * @param sellerId 操作用户ID（卖家或管理员）
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.ProductMapper#deleteById(Long)
     */
    Boolean deleteProductById(Long productId, Long sellerId);

    /**
     * 发布商品（业务方法）
     * 核心逻辑：校验卖家信用分≥80分，校验成色合法性（仅"全新"/"9成新"等枚举），上传图片至OSS
     * @param productVO 商品发布参数（类别、价格、成色、多图列表）
     * @param sellerId 卖家ID
     * @return "发布成功" 或抛出异常
     * @see #insertProduct(Product)
     * @see UserService#selectUserById(Long)
     * @see ProductConditionEnum （商品成色枚举）
     */
    String publishProduct(ProductVO productVO, Long sellerId);

    /**
     * 更新商品库存（业务方法）
     * 核心逻辑：校验库存充足性（扣减时库存≥|stockChange|），调用ProductMapper.updateStock更新
     * @param productId 商品ID
     * @param stockChange 库存变更值（正数增加，负数减少）
     * @param reason 变更原因（如"下单扣减"/"售后退款增加"）
     * @return 更新后的库存
     * @see com.community_shop.backend.mapper.ProductMapper#updateStock(Long, int)
     * @see #selectProductById(Long)
     */
    Integer updateStock(Long productId, Integer stockChange, String reason);

    /**
     * 多条件筛选商品（业务方法，分页）
     * 核心逻辑：调用ProductMapper.selectByCondition多条件查询，补充卖家好评率（关联EvaluationService）
     * @param category 商品类别（如"数码产品"，可为null）
     * @param minPrice 最低价格（可为null，不限制下限）
     * @param maxPrice 最高价格（可为null，不限制上限）
     * @param pageParam 分页参数（页码、每页条数）
     * @return 含卖家好评率的分页商品列表
     * @see com.community_shop.backend.mapper.ProductMapper#selectByCondition(String, Double, Double, String, int, int)
     * @see EvaluationService#calculateSellerScore(Long)
     */
    PageResult<Product> selectProductByCondition(String category, Double minPrice, Double maxPrice, PageParam pageParam);
}
