package xyz.graygoo401.trade.service.base;

import org.springframework.stereotype.Service;
import xyz.graygoo401.api.trade.dto.product.*;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.service.BaseService;
import xyz.graygoo401.trade.dao.entity.Product;

/**
 * 商品管理Service接口，实现《文档》中商品发布、搜索、库存管理等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：商品发布（多图/视频）、搜索筛选、库存管理
 * 2. 《文档4_数据库工作（新）.docx》：product表结构（product_id、price、stock、seller_id等）
 * 3. 《代码文档1 Mapper层设计.docx》：ProductMapper的CRUD及库存更新方法
 */
@Service
public interface ProductService extends BaseService<Product> {

    /**
     * 发布商品
     * @param publishDTO 商品发布参数
     * @return 发布成功的商品详情
     * @throws BusinessException 无发布权限、参数非法、图片超限等场景抛出
     */
    ProductDetailDTO publishProduct(Long userId, ProductPublishDTO publishDTO);

    /**
     * 按商品ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验卖家或管理员权限，调用ProductMapper.deleteById标记删除
     * @param userId 操作用户ID（卖家或管理员）
     * @param productId 待删除商品ID
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean deleteProduct(Long userId, Long productId);

    /**
     * 更新商品信息
     * @param userId 卖家ID
     * @param updateDTO 商品更新参数
     * @return 更新后的商品详情
     * @throws BusinessException 无权限（非卖家）、商品已下架等场景抛出
     */
    ProductDetailDTO updateProduct(Long userId, ProductUpdateDTO updateDTO);

    /**
     * 调整商品库存
     * @param userId 用户ID
     * @param stockUpdateDTO 库存调整参数
     * @return 调整后的库存数
     * @throws BusinessException 库存不足（扣减时）、无权限等场景抛出
     */
    Integer updateStock(Long userId, ProductStockUpdateDTO stockUpdateDTO);

    /**
     * 商品上下架操作
     * @param sellerId 卖家ID
     * @param statusUpdateDTO 状态调整参数
     * @return 是否操作成功
     * @throws BusinessException 无权限、状态无效等场景抛出
     */
    Boolean changeProductStatus(Long sellerId, ProductStatusUpdateDTO statusUpdateDTO);

    /**
     * 多条件搜索商品
     * @param queryDTO 搜索参数（含关键词、分类、价格区间、分页）
     * @return 分页商品列表
     */
    PageResult<ProductListItemDTO> queryProducts(ProductQueryDTO queryDTO);

    /**
     * 卖家查询自有商品列表
     * @param queryDTO 卖家商品查询参数
     * @return 分页商品列表
     * @throws BusinessException 无权限、状态无效等场景抛出
     */
    PageResult<ProductListItemDTO> getSellerProducts(SellerProductQueryDTO queryDTO);

    /**
     * 商品详情查询（同时自增浏览量）
     * @param productId 商品ID
     * @return 商品详情
     * @throws BusinessException 商品不存在、已下架等场景抛出
     */
    ProductDetailDTO getProductDetail(Long productId);


}
