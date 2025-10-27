package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.ProductConvert;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.product.*;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.ProductMapper;
import com.community_shop.backend.service.base.EvaluationService;
import com.community_shop.backend.service.base.ProductService;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品管理Service实现类，实现商品发布、搜索、库存管理等核心业务逻辑
 * 依赖ProductMapper、UserService、EvaluationService完成数据交互与业务协同
 */
@Slf4j
@Service
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product> implements ProductService {

    // 常量定义
    private static final Integer MIN_STOCK = 1; // 发布商品最小库存
    private static final Integer PUBLISH_CREDIT_LIMIT = 80; // 发布商品最低信用分
    private static final String CACHE_KEY_PRODUCT = "product:info:"; // 商品缓存Key前缀
    private static final String CACHE_KEY_PRODUCT_LIST = "product:list:"; // 商品列表缓存Key前缀
    private static final Duration CACHE_TTL_PRODUCT = Duration.ofHours(1); // 商品缓存1小时
    private static final Duration CACHE_TTL_PRODUCT_LIST = Duration.ofMinutes(30); // 商品列表缓存有效期（分钟）

    // 商品相关常量
    private static final Integer MAX_IMAGE_COUNT = 5; // 最大图片数量
    private static final Integer MAX_DESCRIPTION_LENGTH = 2000; // 商品描述最大长度
    private static final Integer MAX_NAME_LENGTH = 100; // 商品名称最大长度

    // 依赖注入
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductConvert productConvert;

    @Autowired
    private OssUtil ossUtil; // OSS文件上传工具类



    /**
     * 发布商品
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public ProductDetailDTO publishProduct(Long userId, ProductPublishDTO publishDTO) {
        try {
            // 1. 参数校验
            validatePublishParam(publishDTO);

            // 2. 验证卖家权限（实际项目中从Token获取卖家ID）
            if (userId == null || !verifySellerPermission(userId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 3. 转换为实体并设置默认值
            Product product = productConvert.productPublishDtoToProduct(publishDTO);
            product.setSellerId(userId);
            product.setStatus(ProductStatusEnum.ON_SALE);
            product.setViewCount(0);
            product.setCreateTime(LocalDateTime.now());

            // 5. 插入数据库
            int insertRows = productMapper.insert(product);
            if (insertRows <= 0) {
                log.error("商品发布失败，发布信息：{}", publishDTO);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 6. 查询完整商品信息
            Product savedProduct = productMapper.selectById(product.getProductId());

            // 7. 转换为DTO并缓存
            ProductDetailDTO detailDTO = productConvert.productToProductDetailDTO(savedProduct);
            redisTemplate.opsForValue().set(CACHE_KEY_PRODUCT + savedProduct.getProductId(), detailDTO, CACHE_TTL_PRODUCT);

            log.info("商品发布成功，商品ID：{}，卖家ID：{}", savedProduct.getProductId(), userId);
            return detailDTO;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("商品发布异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 按商品ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验卖家或管理员权限，调用ProductMapper.deleteById标记删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProduct(Long operatorId, Long productId) {
        try {
            // 1. 参数校验
            if (productId == null || operatorId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            // 2. 校验商品存在性
            Product product = getById(productId);
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }
            // 3. 校验权限（卖家或管理员可删除）
            User operator = userService.getById(operatorId);
            if (operator == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }
            // 非商品卖家且非管理员，无删除权限
            if (!Objects.equals(product.getSellerId(), operatorId) && !UserRoleEnum.ADMIN.equals(operator.getRole())) {
                log.error("删除商品无权限，商品ID：{}，操作人ID：{}", productId, operatorId);
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }
            // 4. 执行逻辑删除
            int deleteRows = productMapper.deleteById(productId);
            if (deleteRows <= 0) {
                log.error("删除商品失败，商品ID：{}", productId);
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }
            // 5. 清除缓存
            redisTemplate.delete(CACHE_KEY_PRODUCT + productId);
            log.info("删除商品成功，商品ID：{}，操作人ID：{}", productId, operatorId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除商品异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 更新商品信息
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public ProductDetailDTO updateProduct(Long userId, ProductUpdateDTO updateDTO) {
        try {
            // 1. 参数校验
            if (userId == null || updateDTO == null || updateDTO.getProductId() == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            validateUpdateParam(updateDTO);

            // 2. 验证商品存在
            Product existingProduct = productMapper.selectById(updateDTO.getProductId());
            if (existingProduct == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }

            // 3. 验证卖家权限（必须是商品所属卖家）
            if (!Objects.equals(existingProduct.getSellerId(), userId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 6. 转换为实体并更新
            productConvert.updateProductFromUpdateDto(updateDTO, existingProduct);

            int updateRows = productMapper.updateById(existingProduct);
            if (updateRows <= 0) {
                log.error("商品更新失败，商品ID：{}，更新信息：{}", updateDTO.getProductId(), updateDTO);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 7. 查询更新后的商品
            Product updatedProduct = productMapper.selectById(updateDTO.getProductId());

            // 8. 转换为DTO并刷新缓存
            ProductDetailDTO detailDTO = productConvert.productToProductDetailDTO(updatedProduct);
            redisTemplate.opsForValue().set(CACHE_KEY_PRODUCT + updatedProduct.getProductId(), detailDTO, CACHE_TTL_PRODUCT);

            log.info("商品更新成功，商品ID：{}，卖家ID：{}", updatedProduct.getProductId(), userId);
            return detailDTO;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("商品更新异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 调整商品库存
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Integer updateStock(Long userId, ProductStockUpdateDTO stockUpdateDTO) {
        try {
            // 1. 参数校验
            if (stockUpdateDTO == null || stockUpdateDTO.getProductId() == null ||
                    stockUpdateDTO.getStockChange() == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 验证商品存在
            Product product = productMapper.selectById(stockUpdateDTO.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }

            // 3. 验证权限（必须是商品所属卖家）
            if (!Objects.equals(product.getSellerId(), userId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 验证库存是否充足（如果是减少库存）
            int newStock = product.getStock() + stockUpdateDTO.getStockChange();
            if (newStock < 0) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
            }

            // 5. 更新库存
            int updateRows = productMapper.updateStock(stockUpdateDTO.getProductId(), stockUpdateDTO.getStockChange());
            if (updateRows <= 0) {
                log.error("库存更新失败，商品ID：{}，变动量：{}",
                        stockUpdateDTO.getProductId(), stockUpdateDTO.getStockChange());
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 6. 刷新缓存
            product.setStock(newStock);
            redisTemplate.opsForValue().set(
                    CACHE_KEY_PRODUCT + product.getProductId(),
                    productConvert.productToProductDetailDTO(product),
                    CACHE_TTL_PRODUCT
            );

            log.info("库存更新成功，商品ID：{}，原库存：{}，变动量：{}，新库存：{}",
                    product.getProductId(), product.getStock() - stockUpdateDTO.getStockChange(),
                    stockUpdateDTO.getStockChange(), newStock);
            return newStock;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("库存更新异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 商品上下架操作
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Boolean changeProductStatus(Long userID, ProductStatusUpdateDTO statusDTO) {
        try {
            // 1. 参数校验
            Long productId = statusDTO.getProductId();
            ProductStatusEnum status = statusDTO.getStatus();
            if (userID == null || productId == null || status == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 验证商品存在
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }

            // 3. 验证权限（必须是商品所属卖家或者管理员）
            if (!Objects.equals(product.getSellerId(), userID) && !userService.verifyRole(userID, UserRoleEnum.ADMIN)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 验证状态是否相同（避免无效操作）
            if (status.equals(product.getStatus())) {
                log.warn("商品状态未变更，商品ID：{}，当前状态：{}", productId, status);
                return true;
            }

            // 5. 更新状态
            int updateRows = productMapper.updateStatus(productId, status);
            if (updateRows <= 0) {
                log.error("商品状态更新失败，商品ID：{}，目标状态：{}", productId, status);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 6. 刷新缓存
            product.setStatus(status);
            redisTemplate.opsForValue().set(
                    CACHE_KEY_PRODUCT + productId,
                    productConvert.productToProductDetailDTO(product),
                    CACHE_TTL_PRODUCT
            );

            log.info("商品状态更新成功，商品ID：{}，原状态：{}，新状态：{}",
                    productId, product.getStatus(), status);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("商品状态更新异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 多条件搜索商品
     */
    @Override
    public PageResult<ProductListItemDTO> queryProducts(ProductQueryDTO queryDTO) {
        try {
            // 1. 参数处理
            if (queryDTO == null) {
                queryDTO = new ProductQueryDTO();
            }
            int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
            int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
            int offset = (pageNum - 1) * pageSize;

            // 2. 构建缓存Key
            queryDTO.setOffset(offset);
            String cacheKey = buildProductListCacheKey(queryDTO, pageNum, pageSize);

            // 3. 尝试从缓存获取
            PageResult<ProductListItemDTO> pageResult = (PageResult<ProductListItemDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (Objects.nonNull(pageResult)) {
                log.info("商品列表缓存命中，查询条件：{}，页码：{}", queryDTO, pageNum);
                return pageResult;
            }

            // 4. 查询总数
            long total = productMapper.countByQuery(queryDTO);

            // 5. 查询商品列表
            List<Product> productList = productMapper.selectByQuery(queryDTO);

            // 6. 转换为DTO列表
            List<ProductListItemDTO> dtoList = productList.stream()
                    .map(productConvert::productToProductListItemDTO)
                    .collect(Collectors.toList());

            // 7. 构建分页结果
            long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
            pageResult = new PageResult<>(total, totalPages, dtoList, pageNum, pageSize);

            // 8. 缓存结果
            redisTemplate.opsForValue().set(
                    cacheKey,
                    pageResult,
                    CACHE_TTL_PRODUCT_LIST
            );

            log.info("商品搜索成功，条件：{}，页码：{}，总条数：{}", queryDTO, pageNum, total);
            return pageResult;

        } catch (Exception e) {
            log.error("商品搜索异常", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }
    }

    /**
     * 卖家查询自有商品列表
     */
    @Override
    public PageResult<ProductListItemDTO> getSellerProducts(SellerProductQueryDTO queryDTO) {
        try {
            // 1. 参数校验
            if (queryDTO == null || queryDTO.getSellerId() == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
            int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
            int offset = (pageNum - 1) * pageSize;

            // 2. 查询总数
            queryDTO.setOffset(offset);
            long total = productMapper.countBySellerQuery(queryDTO);

            // 3. 查询商品列表
            List<Product> productList = productMapper.selectBySellerQuery(queryDTO);

            // 4. 转换为DTO列表
            List<ProductListItemDTO> dtoList = productList.stream()
                    .map(productConvert::productToProductListItemDTO)
                    .collect(Collectors.toList());

            // 5. 构建分页结果
            long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;

            log.info("卖家商品查询成功，卖家ID：{}，页码：{}，总条数：{}",
                    queryDTO.getSellerId(), pageNum, total);
            return new PageResult<>(total, totalPages, dtoList, pageNum, pageSize);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("卖家商品查询异常", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }
    }

    /**
     * 商品详情查询（同时自增浏览量）
     * 普通用户查看
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public ProductDetailDTO getProductDetail(Long productId) {
        try {
            // 1. 参数校验
            if (productId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 尝试从缓存获取
            ProductDetailDTO detailDTO = (ProductDetailDTO) redisTemplate.opsForValue().get(CACHE_KEY_PRODUCT + productId);
            if (Objects.nonNull(detailDTO)) {
//                // 缓存命中但仍需更新浏览量（异步处理）
//                asyncIncrementViewCount(productId);
                return detailDTO;
            }

            // 3. 查询数据库
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }

            // 4. 验证商品状态（已下架商品不能查看详情）
            List<ProductStatusEnum> statusList = Arrays.asList(
                    ProductStatusEnum.PENDING,
                    ProductStatusEnum.OFF_SHELF,
                    ProductStatusEnum.DELETED,
                    ProductStatusEnum.BLOCKED,
                    ProductStatusEnum.HIDDEN
            );
            if (statusList.contains(product.getStatus())) {
                throw new BusinessException(ErrorCode.PRODUCT_ALREADY_OFF_SALE);
            }

//            // 5. 自增浏览量
//            productMapper.incrementViewCount(productId);
//            product.setViewCount(product.getViewCount() + 1);

            // 6. 转换为DTO并缓存
            detailDTO = productConvert.productToProductDetailDTO(product);
            redisTemplate.opsForValue().set(
                    CACHE_KEY_PRODUCT + productId,
                    detailDTO,
                    CACHE_TTL_PRODUCT
            );

            log.info("商品详情查询成功，商品ID：{}", productId);
            return detailDTO;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("商品详情查询异常", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }
    }


    // ---------------------- 私有辅助方法 ----------------------

    /**
     * 验证发布商品参数
     */
    private void validatePublishParam(ProductPublishDTO publishDTO) {
        if (publishDTO == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 商品名称校验
        if (!StringUtils.hasText(publishDTO.getTitle()) || publishDTO.getTitle().length() > MAX_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_TITLE_INVALID);
        }

        // 价格校验
        if (publishDTO.getPrice() == null || publishDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_INVALID);
        }

        // 库存校验
        if (publishDTO.getStock() == null || publishDTO.getStock() < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INVALID);
        }

        // 分类校验
        if (publishDTO.getCategory() == null) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_NULL);
        }

        // 描述校验
        if (StringUtils.hasText(publishDTO.getDescription()) &&
                publishDTO.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_DESCRIPTION_TOO_LONG);
        }

        // 图片校验
        if (publishDTO.getImageUrls() != null && publishDTO.getImageUrls().size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_TOO_MANY);
        }
//        if (publishDTO.getImageUrls() != null) {
//            for (String url : publishDTO.getImageUrls()) {
//                if (!url.matches("^https?://.+$")) {
//                    throw new BusinessException(ErrorCode.PRODUCT_IMAGE_URL_INVALID);
//                }
//            }
//        }
    }

    /**
     * 验证更新商品参数
     */
    private void validateUpdateParam(ProductUpdateDTO updateDTO) {
        // 商品名称校验（如果有更新）
        if (StringUtils.hasText(updateDTO.getTitle()) && updateDTO.getTitle().length() > MAX_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_TITLE_INVALID);
        }

        // 价格校验（如果有更新）
        if (updateDTO.getPrice() != null && updateDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_INVALID);
        }

        // 库存校验（如果有更新）
        if (updateDTO.getStock() != null && updateDTO.getStock() < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INVALID);
        }

        // 描述校验（如果有更新）
        if (StringUtils.hasText(updateDTO.getDescription()) &&
                updateDTO.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_DESCRIPTION_TOO_LONG);
        }

        // 图片校验（如果有更新）
        if (updateDTO.getDetailImageUrls() != null && updateDTO.getDetailImageUrls().size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_TOO_MANY);
        }
//        if (updateDTO.getDetailImageUrls() != null) {
//            for (String url : updateDTO.getImageUrls()) {
//                if (!url.matches("^https?://.+$")) {
//                    throw new BusinessException(ErrorCode.PRODUCT_IMAGE_URL_INVALID);
//                }
//            }
//        }
    }

    /**
     * 验证卖家权限
     */
    private boolean verifySellerPermission(Long sellerId) {
        // 实际项目中应查询数据库验证卖家身份是否有效
        return productMapper.verifySellerExists(sellerId) > 0;
    }

    /**
     * 构建商品列表缓存Key
     */
    private String buildProductListCacheKey(ProductQueryDTO queryDTO, int pageNum, int pageSize) {
        StringBuilder cacheKey = new StringBuilder(CACHE_KEY_PRODUCT_LIST);

        // 添加查询条件到缓存Key
        if (queryDTO.getKeyword() != null) {
            cacheKey.append("keyword_").append(queryDTO.getKeyword()).append("_");
        }
        if (queryDTO.getCategory() != null) {
            cacheKey.append("category_").append(queryDTO.getCategory()).append("_");
        }
        if (queryDTO.getMinPrice() != null) {
            cacheKey.append("minPrice_").append(queryDTO.getMinPrice()).append("_");
        }
        if (queryDTO.getMaxPrice() != null) {
            cacheKey.append("maxPrice_").append(queryDTO.getMaxPrice()).append("_");
        }
        if (queryDTO.getSortField() != null) {
            cacheKey.append("sort_").append(queryDTO.getSortField()).append("_")
                    .append(queryDTO.getSortDir()).append("_");
        }

        // 添加分页参数
        cacheKey.append("page_").append(pageNum).append("_size_").append(pageSize);

        return cacheKey.toString();
    }

//    /**
//     * 异步增加浏览量（避免影响主查询性能）
//     */
//    private void asyncIncrementViewCount(Long productId) {
//        // 实际项目中应使用@Async注解实现异步处理
//        new Thread(() -> {
//            try {
//                productMapper.incrementViewCount(productId);
//                log.info("异步更新浏览量成功，商品ID：{}", productId);
//            } catch (Exception e) {
//                log.error("异步更新浏览量失败，商品ID：{}", productId, e);
//            }
//        }).start();
//    }

}
