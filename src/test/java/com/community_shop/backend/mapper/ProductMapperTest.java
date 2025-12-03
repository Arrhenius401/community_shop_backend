package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.community_shop.backend.dao.mapper.ProductMapper;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import com.community_shop.backend.dto.product.ProductQueryDTO;
import com.community_shop.backend.dto.product.SellerProductQueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductMapper单元测试
 * 适配文档：
 * 1. 《代码文档1 Mapper层设计.docx》2.4节 ProductMapper接口规范
 * 2. 《代码文档0 实体类设计.docx》2.2节 Product实体属性与枚举依赖
 * 3. 《中间件文档3 自定义枚举类设计.docx》枚举TypeHandler自动转换
 * 4. 《测试文档1 基础SQL脚本设计.docx》PRODUCT模块初始化数据
 */
@MybatisPlusTest  // 仅加载MyBatis相关Bean，轻量化测试
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 禁用默认数据库替换，使用H2配置
@ActiveProfiles("test")  // 启用test环境配置（加载application-test.properties）
public class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;  // 注入待测试的ProductMapper

    // 测试复用的基础数据（从data-product.sql初始化数据中获取）
    private Product onSaleProduct;    // 在售商品（productId=1，status=ON_SALE，condition=NINETY_FIVE_PERCENT_NEW）
    private Product outOfStockProduct;// 已售罄商品（productId=2，status=SOLD_OUT，condition=NEW）
    private Product offShelfProduct;  // 下架商品（productId=3，status=OFF_SHELF，condition=EIGHTY_PERCENT_NEW）

    /**
     * 测试前初始化：从数据库查询基础测试商品，确保与data-product.sql数据一致
     * 适配《代码文档0》中Product实体的枚举属性（status/condition）与业务属性（price/stock）
     */
    @BeforeEach
    void setUp() {
        // 按productId查询（基于BaseMapper的selectById方法）
        onSaleProduct = productMapper.selectById(1L);
        outOfStockProduct = productMapper.selectById(2L);
        offShelfProduct = productMapper.selectById(3L);

        // 断言初始化成功（确保PRODUCT模块SQL脚本已正确执行）
        assertNotNull(onSaleProduct, "初始化失败：未查询到在售商品（data-product.sql中productId=1）");
        assertNotNull(outOfStockProduct, "初始化失败：未查询到已售罄商品（data-product.sql中productId=2）");
        assertNotNull(offShelfProduct, "初始化失败：未查询到下架商品（data-product.sql中productId=3）");
    }

    /**
     * 测试selectById：查询商品详情（正常场景）
     * 适配《代码文档1》2.4.2节 基础操作与状态管理 - selectById方法
     */
    @Test
    void selectById_existProductId_returnsProductDetail() {
        // 1. 执行测试方法（查询在售商品productId=1）
        Product result = productMapper.selectById(1L);

        // 2. 断言结果（匹配data-product.sql中在售商品数据）
        assertNotNull(result);
        assertEquals(onSaleProduct.getProductId(), result.getProductId());
        assertEquals(2L, result.getSellerId(), "商品卖家ID应为test_seller（userId=2）");
        assertEquals("二手iPhone 13 128G", result.getTitle());
        assertEquals("数码产品", result.getCategory());
        assertEquals(new BigDecimal("4599.00"), result.getPrice(), "商品价格应为4599.00");
        assertEquals(5, result.getStock(), "在售商品库存应为5");
        assertEquals(ProductStatusEnum.ON_SALE, result.getStatus(), "商品状态应为ON_SALE");
        assertEquals(ProductConditionEnum.NINETY_FIVE_PERCENT_NEW, result.getCondition(), "商品成色应为NINETY_FIVE_PERCENT_NEW");
    }

    /**
     * 测试selectById：查询不存在的商品（异常场景）
     */
    @Test
    void selectById_nonExistProductId_returnsNull() {
        // 执行测试方法（查询不存在的productId=100）
        Product result = productMapper.selectById(100L);
        assertNull(result, "查询不存在的商品应返回null");
    }

    /**
     * 测试selectByKeyword：关键词模糊搜索商品（正常场景）
     * 适配《代码文档1》2.4.2节 搜索与筛选 - selectByKeyword方法
     */
    @Test
    void selectByKeyword_validKeyword_returnsMatchedProductList() {
        // 1. 执行测试方法（关键词"iPhone"，分页：offset=0，limit=10）
        List<Product> matchedList = productMapper.selectByKeyword("iPhone", 0, 10);

        // 2. 断言结果（data-product.sql中仅productId=1标题含"iPhone"）
        assertNotNull(matchedList);
        assertEquals(1, matchedList.size(), "关键词搜索应返回1个匹配商品");

        Product matchedProduct = matchedList.get(0);
        assertEquals(onSaleProduct.getProductId(), matchedProduct.getProductId());
        assertTrue(matchedProduct.getTitle().contains("iPhone"), "商品标题应包含关键词'iPhone'");
    }

    /**
     * 测试selectByKeyword：无匹配关键词（异常场景）
     */
    @Test
    void selectByKeyword_noMatchKeyword_returnsEmptyList() {
        // 执行测试方法（关键词"华为"，无匹配商品）
        List<Product> emptyList = productMapper.selectByKeyword("华为", 0, 10);
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty(), "无匹配关键词时应返回空列表");
    }

    /**
     * 测试updateStatus：更新商品状态为下架（枚举参数，正常场景）
     * 适配《代码文档1》2.4.2节 基础操作与状态管理 - updateStatus方法
     * 适配《中间件文档3》枚举TypeHandler自动转换（枚举→数据库code）
     */
    @Test
    void updateStatus_changeToOffShelf_returnsAffectedRows1() {
        // 1. 准备参数（将在售商品productId=1的状态从ON_SALE改为OFF_SHELF）
        Long productId = 1L;
        ProductStatusEnum newStatus = ProductStatusEnum.OFF_SHELF;

        // 2. 执行更新方法（直接传递枚举对象，TypeHandler自动转换为"OFF_SHELF"）
        int affectedRows = productMapper.updateStatus(productId, newStatus);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新商品状态应影响1行数据");

        // 4. 验证状态已更新（查询结果自动转换为枚举）
        Product updatedProduct = productMapper.selectById(productId);
        assertEquals(newStatus, updatedProduct.getStatus(), "商品状态未更新为OFF_SHELF");
    }

    /**
     * 测试updateStock：更新商品库存（正常场景，减库存）
     * 适配《代码文档1》2.4.2节 卖家专属查询与库存管理 - updateStock方法
     */
    @Test
    void updateStock_reduceStock_returnsAffectedRows1() {
        // 1. 准备参数（在售商品productId=1当前库存5，更新为3，模拟下单减库存）
        Long productId = 1L;
        int newStock = 3;

        // 2. 执行更新方法
        int affectedRows = productMapper.updateStock(productId, newStock);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新商品库存应影响1行数据");

        // 4. 验证库存已更新
        Product updatedProduct = productMapper.selectById(productId);
        assertEquals(newStock, updatedProduct.getStock(), "商品库存未更新为3");
    }

    /**
     * 测试updateViewCount：自增商品浏览量（正常场景）
     * 适配《代码文档1》2.4.2节 卖家专属查询与库存管理 - updateViewCount方法
     */
    @Test
    void updateViewCount_incrementView_returnsAffectedRows1() {
        // 1. 记录更新前浏览量（初始为0）
        Long productId = 1L;
        int oldViewCount = productMapper.selectById(productId).getViewCount();

        // 2. 执行更新方法（自增1）
        int affectedRows = productMapper.updateViewCount(productId);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "自增浏览量应影响1行数据");

        // 4. 验证浏览量已自增
        Product updatedProduct = productMapper.selectById(productId);
        assertEquals(oldViewCount + 1, updatedProduct.getViewCount(), "商品浏览量未自增1");
    }

    /**
     * 测试selectBySellerId：分页查询卖家商品（正常场景）
     * 适配《代码文档1》2.4.2节 卖家专属查询与库存管理 - selectBySellerId方法
     */
    @Test
    void selectBySellerId_existSellerId_returnsSellerProductList() {
        // 1. 执行测试方法（查询test_seller（userId=2）的商品，分页：offset=0，limit=10）
        List<Product> sellerProductList = productMapper.selectBySellerId(2L, 0, 10);

        // 2. 断言结果（data-product.sql中sellerId=2有3个商品：productId=1/2/3）
        assertNotNull(sellerProductList);
        assertEquals(3, sellerProductList.size(), "卖家userId=2应拥有3个商品");

        // 验证商品ID匹配
        boolean hasOnSale = sellerProductList.stream().anyMatch(p -> p.getProductId().equals(1L));
        boolean hasOutOfStock = sellerProductList.stream().anyMatch(p -> p.getProductId().equals(2L));
        boolean hasOffShelf = sellerProductList.stream().anyMatch(p -> p.getProductId().equals(3L));
        assertTrue(hasOnSale, "查询结果应包含在售商品（productId=1）");
        assertTrue(hasOutOfStock, "查询结果应包含已售罄商品（productId=2）");
        assertTrue(hasOffShelf, "查询结果应包含下架商品（productId=3）");
    }

    /**
     * 测试selectBySellerIdAndStatus：按状态查询卖家商品（正常场景）
     * 适配《代码文档1》2.4.2节 卖家专属查询与库存管理 - selectBySellerIdAndStatus方法
     */
    @Test
    void selectBySellerIdAndStatus_filterOnSale_returnsOnSaleList() {
        // 1. 执行测试方法（查询test_seller（userId=2）的在售商品，分页：offset=0，limit=10）
        List<Product> onSaleList = productMapper.selectBySellerIdAndStatus(2L, ProductStatusEnum.ON_SALE, 0, 10);

        // 2. 断言结果（data-product.sql中sellerId=2的在售商品仅productId=1）
        assertNotNull(onSaleList);
        assertEquals(1, onSaleList.size(), "卖家在售商品数量应为1");
        assertEquals(ProductStatusEnum.ON_SALE, onSaleList.get(0).getStatus(), "查询结果应为在售状态");
    }

    /**
     * 测试selectByQuery：复杂条件查询商品（DTO参数，正常场景）
     * 适配《代码文档1》2.4.2节 搜索与筛选 - selectByQuery方法
     */
    @Test
    void selectByQuery_complexCondition_returnsMatchedList() {
        // 1. 构建查询DTO（筛选：status=SOLD_OUT，condition=NEW；分页：第1页，每页10条）
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setStatus(ProductStatusEnum.SOLD_OUT);  // 枚举参数
        queryDTO.setCondition(ProductConditionEnum.NEW);      // 枚举参数
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setOffset((queryDTO.getPageNum() - 1) * queryDTO.getPageSize());  // 计算偏移量

        // 2. 执行查询方法
        List<Product> matchedList = productMapper.selectByQuery(queryDTO);

        // 3. 断言结果（data-product.sql中仅productId=2符合条件）
        assertNotNull(matchedList);
        assertEquals(1, matchedList.size(), "复杂条件查询应返回1个匹配商品");
        assertEquals(outOfStockProduct.getProductId(), matchedList.get(0).getProductId());
    }

    /**
     * 测试countBySellerQuery：统计卖家商品总数（DTO参数，正常场景）
     * 适配《代码文档1》2.4.2节 搜索与筛选 - countBySellerQuery方法
     */
    @Test
    void countBySellerQuery_sellerCondition_returnsCorrectCount() {
        // 1. 构建卖家查询DTO（筛选：sellerId=2，status=OFF_SHELF）
        SellerProductQueryDTO queryDTO = new SellerProductQueryDTO();
        queryDTO.setSellerId(2L);
        queryDTO.setStatus(ProductStatusEnum.OFF_SHELF);

        // 2. 执行统计方法
        int count = productMapper.countBySellerQuery(queryDTO);

        // 3. 断言结果（data-product.sql中sellerId=2的下架商品仅productId=3，总数应为1）
        assertEquals(1, count, "卖家下架商品总数应为1");
    }

    /**
     * 测试verifySellerExists：验证卖家是否有已发布商品（正常场景）
     * 适配《代码文档1》2.4.2节 基础操作与状态管理 - verifySellerExists方法
     */
    @Test
    void verifySellerExists_hasProduct_returnsCountGreaterThan0() {
        // 执行测试方法（验证test_seller（userId=2）是否有商品）
        int count = productMapper.verifySellerExists(2L);
        assertTrue(count > 0, "有已发布商品的卖家应返回大于0的计数");
    }

    /**
     * 测试verifySellerExists：验证无商品的卖家（异常场景）
     */
    @Test
    void verifySellerExists_noProduct_returns0() {
        // 执行测试方法（验证test_buyer（userId=1）是否有商品，该用户无发布记录）
        int count = productMapper.verifySellerExists(1L);
        assertEquals(0, count, "无已发布商品的卖家应返回0");
    }
}