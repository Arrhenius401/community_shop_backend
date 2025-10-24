package com.community_shop.backend.service;

import com.community_shop.backend.convert.ProductConvert;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.product.*;
import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.ProductMapper;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.service.impl.ProductServiceImpl;
import com.community_shop.backend.utils.OssUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceTest {

    // 模拟依赖组件
    @Mock
    private ProductMapper productMapper;
    @Mock
    private UserService userService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ProductConvert productConvert;
    @Mock
    private OssUtil ossUtil;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    // 注入测试目标服务
    @InjectMocks
    private ProductServiceImpl productService;

    // 测试数据
    private User testAdminUser;
    private User testSellerUser;
    private User testNormalUser;
    private Product testProduct;
    private ProductPublishDTO testPublishDTO;
    private ProductUpdateDTO testUpdateDTO;
    private ProductStockUpdateDTO testStockUpdateDTO;
    private ProductStatusUpdateDTO testStatusUpdateDTO;
    private ProductQueryDTO testQueryDTO;
    private SellerProductQueryDTO testSellerQueryDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试用户数据
        initTestUsers();
        // 初始化测试商品数据
        initTestProduct();
        // 初始化测试DTO数据
        initTestDTOs();
        // 注入MyBatis-Plus父类baseMapper
        injectBaseMapper();
        // 模拟Redis依赖行为
        mockRedisBehavior();
    }

    /**
     * 初始化测试用户数据
     */
    private void initTestUsers() {
        // 管理员用户
        testAdminUser = new User();
        testAdminUser.setUserId(1L);
        testAdminUser.setUsername("admin");
        testAdminUser.setCreditScore(100);
        testAdminUser.setRole(UserRoleEnum.ADMIN);
        testAdminUser.setCreateTime(LocalDateTime.now().minusMonths(1));

        // 卖家用户（信用分达标）
        testSellerUser = new User();
        testSellerUser.setUserId(2L);
        testSellerUser.setUsername("testSeller");
        testSellerUser.setCreditScore(90);
        testSellerUser.setRole(UserRoleEnum.USER);
        testSellerUser.setCreateTime(LocalDateTime.now().minusMonths(2));

        // 普通用户（非卖家）
        testNormalUser = new User();
        testNormalUser.setUserId(3L);
        testNormalUser.setUsername("testUser");
        testNormalUser.setCreditScore(70);
        testNormalUser.setRole(UserRoleEnum.USER);
        testNormalUser.setCreateTime(LocalDateTime.now().minusMonths(1));
    }

    /**
     * 初始化测试商品数据
     */
    private void initTestProduct() {
        testProduct = new Product();
        testProduct.setProductId(1001L);
        testProduct.setSellerId(2L);
        testProduct.setTitle("测试商品标题");
        testProduct.setCategory("二手手机");
        testProduct.setPrice(BigDecimal.valueOf(1999.0));
        testProduct.setStock(10);
        testProduct.setDescription("测试商品描述，功能正常");
        testProduct.setStatus(ProductStatusEnum.ON_SALE);
        testProduct.setViewCount(50);
        testProduct.setCondition(ProductConditionEnum.NINETY_PERCENT_NEW);
        testProduct.setCreateTime(LocalDateTime.now().minusDays(3));
    }

    /**
     * 初始化测试DTO数据
     */
    private void initTestDTOs() {
        // 商品发布DTO
        testPublishDTO = new ProductPublishDTO();
        testPublishDTO.setTitle("新发布商品标题");
        testPublishDTO.setCategory("家居用品");
        testPublishDTO.setPrice(BigDecimal.valueOf(2999.0));
        testPublishDTO.setStock(20);
        testPublishDTO.setDescription("新发布商品描述，全新未使用");
        testPublishDTO.setCondition(ProductConditionEnum.NEW);
        testPublishDTO.setImageUrls(Arrays.asList("https://test-img1.jpg", "https://test-img2.jpg"));

        // 商品更新DTO
        testUpdateDTO = new ProductUpdateDTO();
        testUpdateDTO.setProductId(1001L);
        testUpdateDTO.setTitle("更新后的商品标题");
        testUpdateDTO.setPrice(BigDecimal.valueOf(3999.0));
        testUpdateDTO.setStock(15);
        testUpdateDTO.setDescription("更新后的商品描述，功能正常，送配件");

        // 库存调整DTO
        testStockUpdateDTO = new ProductStockUpdateDTO();
        testStockUpdateDTO.setProductId(1001L);
        testStockUpdateDTO.setStockChange(-2);
        testStockUpdateDTO.setReason("订单扣减");

        // 商品状态更新DTO
        testStatusUpdateDTO = new ProductStatusUpdateDTO();
        testStatusUpdateDTO.setProductId(1001L);
        testStatusUpdateDTO.setStatus(ProductStatusEnum.OFF_SHELF);

        // 商品查询DTO
        testQueryDTO = new ProductQueryDTO();
        testQueryDTO.setKeyword("手机");
        testQueryDTO.setCategory("二手手机");
        testQueryDTO.setMinPrice(BigDecimal.valueOf(1000.0));
        testQueryDTO.setMaxPrice(BigDecimal.valueOf(5000.0));
        testQueryDTO.setStatus(ProductStatusEnum.ON_SALE);
        testQueryDTO.setPageNum(1);
        testQueryDTO.setPageSize(10);

        // 卖家商品查询DTO
        testSellerQueryDTO = new SellerProductQueryDTO();
        testSellerQueryDTO.setSellerId(2L);
        testSellerQueryDTO.setStatus(ProductStatusEnum.ON_SALE);
        testSellerQueryDTO.setPageNum(1);
        testSellerQueryDTO.setPageSize(10);
    }

    /**
     * 注入MyBatis-Plus父类的baseMapper字段
     */
    private void injectBaseMapper() {
        try {
            Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(productService, productMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("初始化ProductService baseMapper失败", e);
        }
    }

    /**
     * 模拟Redis相关行为
     */
    private void mockRedisBehavior() {
        // 模拟RedisTemplate的opsForValue()返回ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 模拟Redis的set操作
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        // 模拟Redis的delete操作
        doReturn(true).when(redisTemplate).delete(anyString());
        // 模拟Redis的get操作（默认返回null，可在具体测试方法中覆盖）
        when(valueOperations.get(anyString())).thenReturn(null);
    }

    // ==================== 测试用例 ====================

    /**
     * 测试商品发布功能 - 成功场景（卖家用户、参数合法）
     */
    @Test
    void testPublishProduct_Success() {
        // 1. 模拟依赖行为
        when(userService.getById(2L)).thenReturn(testSellerUser);
        when(productMapper.verifySellerExists(2L)).thenReturn(1);
        when(productConvert.productPublishDtoToProduct(testPublishDTO)).thenAnswer(invocation -> {
            Product product = new Product();
            BeanUtils.copyProperties(testPublishDTO, product);
            product.setSellerId(2L);
            return product;
        });
        when(productMapper.insert(any(Product.class))).thenReturn(1);
        when(productMapper.selectById(anyLong())).thenReturn(testProduct);
        when(productConvert.productToProductDetailDTO(testProduct)).thenAnswer(invocation -> {
            ProductDetailDTO dto = new ProductDetailDTO();
            BeanUtils.copyProperties(testProduct, dto);
            return dto;
        });

        // 捕获插入的Product对象，手动模拟主键回填
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productMapper.insert(productCaptor.capture())).thenAnswer(invocation -> {
            Product capturedEval = productCaptor.getValue();
            capturedEval.setProductId(3001L); // 手动设置商品ID（模拟数据库自增）
            return 1; // 返回插入成功行数
        });

        // 2. 执行测试方法
        ProductDetailDTO result = productService.publishProduct(2L, testPublishDTO);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(testProduct.getTitle(), result.getTitle());
        assertEquals(testProduct.getPrice(), result.getPrice());
        assertEquals(ProductStatusEnum.ON_SALE, result.getStatus());

        // 4. 验证依赖调用
        verify(userService, times(0)).getById(2L);
        verify(productMapper, times(1)).verifySellerExists(2L);
        verify(productMapper, times(1)).insert(any(Product.class));
        verify(productMapper, times(1)).selectById(anyLong());
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    /**
     * 测试商品发布功能 - 失败场景（非卖家用户）
     */
    @Test
    void testPublishProduct_NotSeller() {
        // 1. 模拟依赖行为（普通用户非卖家）
        when(userService.getById(3L)).thenReturn(testNormalUser);
        when(productMapper.verifySellerExists(3L)).thenReturn(0);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.publishProduct(3L, testPublishDTO);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), exception.getCode());
        verify(productMapper, never()).insert(any(Product.class));
    }

    /**
     * 测试商品发布功能 - 失败场景（参数非法：价格为负）
     */
    @Test
    void testPublishProduct_InvalidPrice() {
        // 1. 构造非法参数（价格为负）
        ProductPublishDTO invalidDTO = new ProductPublishDTO();
        BeanUtils.copyProperties(testPublishDTO, invalidDTO);
        invalidDTO.setPrice(BigDecimal.valueOf(-100.0));

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.publishProduct(2L, invalidDTO);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PRODUCT_PRICE_INVALID.getCode(), exception.getCode());
        verify(productMapper, never()).insert(any(Product.class));
    }

    /**
     * 测试商品删除功能 - 成功场景（商品所属卖家删除）
     */
    @Test
    void testDeleteProduct_Success_Seller() {
        // 1. 模拟依赖行为（卖家删除自有商品）
        when(productService.getById(1001L)).thenReturn(testProduct);
        when(userService.getById(2L)).thenReturn(testSellerUser);
        when(productMapper.deleteById(1001L)).thenReturn(1);

        // 2. 执行测试方法
        Boolean result = productService.deleteProduct(2L, 1001L);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(productMapper, times(1)).selectById(1001L);
        verify(userService, times(1)).getById(2L);
        verify(productMapper, times(1)).deleteById(1001L);
        verify(redisTemplate, times(1)).delete(anyString());
    }

    /**
     * 测试商品删除功能 - 失败场景（非商品所属用户且非管理员）
     */
    @Test
    void testDeleteProduct_NoPermission() {
        // 1. 模拟依赖行为（普通用户删除他人商品）
        when(productService.getById(1001L)).thenReturn(testProduct);
        when(userService.getById(3L)).thenReturn(testNormalUser);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.deleteProduct(3L, 1001L);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), exception.getCode());
        verify(productMapper, never()).deleteById(1001L);
    }

    /**
     * 测试商品删除功能 - 失败场景（商品不存在）
     */
    @Test
    void testDeleteProduct_ProductNotFound() {
        // 1. 模拟依赖行为（商品不存在）
        when(productService.getById(9999L)).thenReturn(null);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.deleteProduct(2L, 9999L);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PRODUCT_NOT_EXISTS.getCode(), exception.getCode());
        verify(productMapper, never()).deleteById(9999L);
    }

    /**
     * 测试商品更新功能 - 成功场景（商品所属卖家更新）
     */
    @Test
    void testUpdateProduct_Success_Seller() {
        // 1.准备更新后的商品对象
        Product updatedProduct = new Product();
        BeanUtils.copyProperties(testProduct, updatedProduct);
        updatedProduct.setTitle(testUpdateDTO.getTitle());
        updatedProduct.setDescription(testUpdateDTO.getDescription());
        updatedProduct.setPrice(testUpdateDTO.getPrice());
        updatedProduct.setStock(testUpdateDTO.getStock());

        // 1. 模拟依赖行为（卖家更新自有商品）
        // 第一次查询返回原始商品，第二次查询返回更新后的商品
        when(productMapper.selectById(1001L)).thenReturn(testProduct, updatedProduct);
        when(userService.getById(2L)).thenReturn(testSellerUser);
        doNothing().when(productConvert).updateProductFromUpdateDto(testUpdateDTO, testProduct);
        when(productMapper.updateById(testProduct)).thenReturn(1);
        when(productConvert.productToProductDetailDTO(updatedProduct)).thenAnswer(invocation -> {
            ProductDetailDTO dto = new ProductDetailDTO();
            BeanUtils.copyProperties(invocation.getArgument(0), dto);
            return dto;
        });

        // 2. 执行测试方法
        ProductDetailDTO result = productService.updateProduct(2L, testUpdateDTO);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(testUpdateDTO.getTitle(), result.getTitle());
        assertEquals(testUpdateDTO.getPrice(), result.getPrice());
        assertEquals(testUpdateDTO.getStock(), result.getStock());

        // 4. 验证依赖调用
        verify(productMapper, times(2)).selectById(1001L);
        verify(userService, times(0)).getById(2L);
        verify(productConvert, times(1)).updateProductFromUpdateDto(testUpdateDTO, testProduct);
        verify(productMapper, times(1)).updateById(testProduct);
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    /**
     * 测试商品更新功能 - 失败场景（非商品所属卖家）
     */
    @Test
    void testUpdateProduct_NoPermission() {
        // 1. 模拟依赖行为（普通用户更新他人商品）
        when(productMapper.selectById(1001L)).thenReturn(testProduct);
        when(userService.getById(3L)).thenReturn(testNormalUser);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateProduct(3L, testUpdateDTO);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), exception.getCode());
        verify(productMapper, never()).updateById(any(Product.class));
    }

    /**
     * 测试库存调整功能 - 成功场景（库存扣减，库存充足）
     */
    @Test
    void testUpdateStock_Success_Deduct() {
        // 1. 模拟依赖行为（库存充足，扣减2个）
        when(productMapper.selectById(1001L)).thenReturn(testProduct);
        when(userService.getById(2L)).thenReturn(testSellerUser);
        when(productMapper.updateStock(1001L, -2)).thenReturn(1);
        when(productConvert.productToProductDetailDTO(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ProductDetailDTO dto = new ProductDetailDTO();
            BeanUtils.copyProperties(product, dto);
            return dto;
        });

        // 2. 执行测试方法
        Integer newStock = productService.updateStock(2L, testStockUpdateDTO);

        // 3. 验证结果
        assertNotNull(newStock);
        assertEquals(8, newStock); // 原库存10 - 扣减2 = 8

        // 4. 验证依赖调用
        verify(productMapper, times(1)).selectById(1001L);
        verify(userService, times(0)).getById(2L);
        verify(productMapper, times(1)).updateStock(1001L, -2);
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    /**
     * 测试库存调整功能 - 失败场景（库存不足）
     */
    @Test
    void testUpdateStock_Fail_Insufficient() {
        // 1. 构造库存不足的DTO（扣减15个，原库存10）
        ProductStockUpdateDTO insufficientDTO = new ProductStockUpdateDTO();
        insufficientDTO.setProductId(1001L);
        insufficientDTO.setStockChange(-15);
        insufficientDTO.setReason("订单扣减");

        // 2. 模拟依赖行为
        when(productMapper.selectById(1001L)).thenReturn(testProduct);
        when(userService.getById(2L)).thenReturn(testSellerUser);

        // 3. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateStock(2L, insufficientDTO);
        });

        // 4. 验证结果
        assertEquals(ErrorCode.STOCK_INSUFFICIENT.getCode(), exception.getCode());
        verify(productMapper, never()).updateStock(1001L, -15);
    }

    /**
     * 测试商品状态更新功能 - 成功场景（管理员下架商品）
     */
    @Test
    void testChangeProductStatus_Success_Admin() {
        // 1. 模拟依赖行为（管理员操作）
        when(userService.getById(1L)).thenReturn(testAdminUser);
        when(productMapper.selectById(1001L)).thenReturn(testProduct);
        when(userService.verifyRole(1L, UserRoleEnum.ADMIN)).thenReturn(true);
        when(productMapper.updateStatus(1001L, ProductStatusEnum.OFF_SHELF)).thenReturn(1);
        when(productConvert.productToProductDetailDTO(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ProductDetailDTO dto = new ProductDetailDTO();
            BeanUtils.copyProperties(product, dto);
            return dto;
        });

        // 2. 执行测试方法
        Boolean result = productService.changeProductStatus(1L, testStatusUpdateDTO);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(productMapper, times(1)).selectById(1001L);
        verify(userService, times(0)).getById(1L);
        verify(userService, times(1)).verifyRole(1L, UserRoleEnum.ADMIN);
        verify(productMapper, times(1)).updateStatus(1001L, ProductStatusEnum.OFF_SHELF);
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    /**
     * 测试商品状态更新功能 - 失败场景（状态未变更）
     */
    @Test
    void testChangeProductStatus_Fail_NoChange() {
        // 1. 构造状态未变更的DTO（当前状态已为下架）
        ProductStatusUpdateDTO noChangeDTO = new ProductStatusUpdateDTO();
        noChangeDTO.setProductId(1001L);
        noChangeDTO.setStatus(ProductStatusEnum.ON_SALE);

        // 2. 模拟依赖行为
        when(userService.getById(2L)).thenReturn(testSellerUser);
        when(productMapper.selectById(1001L)).thenReturn(testProduct);
        when(userService.verifyRole(2L, UserRoleEnum.ADMIN)).thenReturn(false);

        // 3. 执行测试方法
        Boolean result = productService.changeProductStatus(2L, noChangeDTO);

        // 4. 验证结果
        assertTrue(result);
        verify(productMapper, never()).updateStatus(1001L, ProductStatusEnum.ON_SALE);
    }

    /**
     * 测试商品搜索功能 - 成功场景（从数据库查询）
     */
    @Test
    void testSearchProducts_Success_FromDb() {
        // 1. 准备测试数据
        List<Product> productList = Arrays.asList(testProduct);
        ProductListItemDTO listItemDTO = new ProductListItemDTO();
        BeanUtils.copyProperties(testProduct, listItemDTO);
        long total = 1;
        long totalPages = 1;

        // 2. 模拟依赖行为（缓存未命中，从数据库查询）
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productMapper.countByQuery(testQueryDTO)).thenReturn((int) total);
        when(productMapper.selectByQuery(testQueryDTO)).thenReturn(productList);
        when(productConvert.productToProductListItemDTO(any(Product.class))).thenReturn(listItemDTO);

        // 3. 执行测试方法
        PageResult<ProductListItemDTO> result = productService.searchProducts(testQueryDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(total, result.getTotal());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(1, result.getList().size());
        assertEquals(testProduct.getTitle(), result.getList().get(0).getTitle());

        // 5. 验证依赖调用
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(productMapper, times(1)).countByQuery(testQueryDTO);
        verify(productMapper, times(1)).selectByQuery(testQueryDTO);
        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    /**
     * 测试商品搜索功能 - 成功场景（从缓存查询）
     */
    @Test
    void testSearchProducts_Success_FromCache() {
        // 1. 准备缓存数据
        ProductListItemDTO listItemDTO = new ProductListItemDTO();
        BeanUtils.copyProperties(testProduct, listItemDTO);
        List<ProductListItemDTO> dtoList = Arrays.asList(listItemDTO);
        PageResult<ProductListItemDTO> cacheResult = new PageResult<>(1L, 1L, dtoList, 1, 10);

        // 2. 模拟依赖行为（缓存命中）
        when(valueOperations.get(anyString())).thenReturn(cacheResult);

        // 3. 执行测试方法
        PageResult<ProductListItemDTO> result = productService.searchProducts(testQueryDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(cacheResult.getTotal(), result.getTotal());
        assertEquals(cacheResult.getList().size(), result.getList().size());

        // 5. 验证依赖调用（未查询数据库）
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(productMapper, never()).countByQuery(any(ProductQueryDTO.class));
        verify(productMapper, never()).selectByQuery(any(ProductQueryDTO.class));
    }

    /**
     * 测试卖家商品查询功能 - 成功场景
     */
    @Test
    void testGetSellerProducts_Success() {
        // 1. 准备测试数据
        List<Product> productList = Arrays.asList(testProduct);
        ProductListItemDTO listItemDTO = new ProductListItemDTO();
        BeanUtils.copyProperties(testProduct, listItemDTO);
        List<ProductListItemDTO> dtoList = Arrays.asList(listItemDTO);
        long total = 1;
        long totalPages = 1;

        // 2. 模拟依赖行为
        when(productMapper.countBySellerQuery(testSellerQueryDTO)).thenReturn((int) total);
        when(productMapper.selectBySellerQuery(testSellerQueryDTO)).thenReturn(productList);
        when(productConvert.productToProductListItemDTO(any(Product.class))).thenReturn(listItemDTO);

        // 3. 执行测试方法
        PageResult<ProductListItemDTO> result = productService.getSellerProducts(testSellerQueryDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(total, result.getTotal());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(dtoList.size(), result.getList().size());
        assertEquals(testSellerQueryDTO.getSellerId(), productList.get(0).getSellerId());

        // 5. 验证依赖调用
        verify(productMapper, times(1)).countBySellerQuery(testSellerQueryDTO);
        verify(productMapper, times(1)).selectBySellerQuery(testSellerQueryDTO);
    }

    /**
     * 测试卖家商品查询功能 - 失败场景（参数缺失：卖家ID为空）
     */
    @Test
    void testGetSellerProducts_Fail_NullSellerId() {
        // 1. 构造参数缺失的DTO（卖家ID为空）
        SellerProductQueryDTO nullSellerIdDTO = new SellerProductQueryDTO();
        nullSellerIdDTO.setStatus(ProductStatusEnum.ON_SALE);
        nullSellerIdDTO.setPageNum(1);
        nullSellerIdDTO.setPageSize(10);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.getSellerProducts(nullSellerIdDTO);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PARAM_NULL.getCode(), exception.getCode());
        verify(productMapper, never()).countBySellerQuery(any(SellerProductQueryDTO.class));
        verify(productMapper, never()).selectBySellerQuery(any(SellerProductQueryDTO.class));
    }

    /**
     * 测试商品详情查询功能 - 成功场景（商品存在且在售）
     */
    @Test
    void testGetProductDetail_Success_OnSale() {
        // 1. 模拟依赖行为（缓存未命中，商品在售）
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productMapper.selectById(1001L)).thenReturn(testProduct);
        when(productConvert.productToProductDetailDTO(testProduct)).thenAnswer(invocation -> {
            ProductDetailDTO dto = new ProductDetailDTO();
            BeanUtils.copyProperties(testProduct, dto);
            return dto;
        });

        // 2. 执行测试方法
        ProductDetailDTO result = productService.getProductDetail(1001L);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        assertEquals(testProduct.getTitle(), result.getTitle());
        assertEquals(ProductStatusEnum.ON_SALE, result.getStatus());

        // 4. 验证依赖调用
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(productMapper, times(1)).selectById(1001L);
        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    /**
     * 测试商品详情查询功能 - 失败场景（商品已永久下架）
     */
    @Test
    void testGetProductDetail_Fail_PermanentOffSale() {
        // 1. 构造已永久下架的商品
        Product offSaleProduct = new Product();
        BeanUtils.copyProperties(testProduct, offSaleProduct);
        offSaleProduct.setStatus(ProductStatusEnum.BLOCKED);

        // 2. 模拟依赖行为
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productMapper.selectById(1001L)).thenReturn(offSaleProduct);

        // 3. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.getProductDetail(1001L);
        });

        // 4. 验证结果
        assertEquals(ErrorCode.PRODUCT_ALREADY_OFF_SALE.getCode(), exception.getCode());
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    /**
     * 测试商品详情查询功能 - 失败场景（商品不存在）
     */
    @Test
    void testGetProductDetail_Fail_ProductNotFound() {
        // 1. 模拟依赖行为（商品不存在）
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productMapper.selectById(9999L)).thenReturn(null);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.getProductDetail(9999L);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PRODUCT_NOT_EXISTS.getCode(), exception.getCode());
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }
}