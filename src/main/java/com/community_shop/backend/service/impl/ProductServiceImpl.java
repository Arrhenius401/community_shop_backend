package com.community_shop.backend.service.impl;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.vo.product.ProductUpdateVO;
import com.community_shop.backend.vo.product.ProductCreateVO;
import com.community_shop.backend.enums.codeEnum.UserRoleEnum;
import com.community_shop.backend.enums.errorcode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.ProductMapper;
import com.community_shop.backend.service.base.EvaluationService;
import com.community_shop.backend.service.base.ProductService;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 商品管理Service实现类，实现商品发布、搜索、库存管理等核心业务逻辑
 * 依赖ProductMapper、UserService、EvaluationService完成数据交互与业务协同
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    // 常量定义
    private static final Integer MIN_STOCK = 1; // 发布商品最小库存
    private static final Integer PUBLISH_CREDIT_LIMIT = 80; // 发布商品最低信用分
    private static final String CACHE_KEY_PRODUCT = "product:info:"; // 商品缓存Key前缀
    private static final Duration CACHE_TTL_PRODUCT = Duration.ofHours(1); // 商品缓存1小时

    // 依赖注入
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private OssUtil ossUtil; // OSS文件上传工具类

    /**
     * 新增商品（基础CRUD）
     * 核心逻辑：初始化库存≥1、浏览量为0，调用ProductMapper.insert插入数据
     */
    @Override
    public Long insertProduct(Product product) {
        try {
            // 1. 校验商品基础信息
            if (product == null || !StringUtils.hasText(product.getTitle()) || product.getPrice() == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            // 2. 初始化默认值
            product.setStock(Math.max(product.getStock(), MIN_STOCK)); // 库存至少为1
            product.setViewCount(0); // 初始浏览量为0
            // 3. 插入数据库
            int insertRows = productMapper.insert(product);
            if (insertRows <= 0) {
                log.error("新增商品失败，商品信息：{}", product);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }
            // 4. 缓存商品信息
            redisTemplate.opsForValue().set(CACHE_KEY_PRODUCT + product.getProductId(), product, CACHE_TTL_PRODUCT);
            log.info("新增商品成功，商品ID：{}", product.getProductId());
            return product.getProductId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增商品异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 按商品ID查询（基础CRUD）
     * 核心逻辑：调用ProductMapper.selectById查询，关联UserService获取卖家信用分
     */
    @Override
    public Product selectProductById(Long productId) {
        // 1. 参数校验
        if (productId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        // 2. 查询缓存
        Product product = (Product) redisTemplate.opsForValue().get(CACHE_KEY_PRODUCT + productId);
        if (Objects.nonNull(product)) {
            return product;
        }
        // 3. 缓存未命中，查询数据库
        product = productMapper.selectById(productId);
        if (product == null) {
            log.warn("商品不存在，商品ID：{}", productId);
            return null;
        }
        // 4. 关联查询卖家信用分
        User seller = userService.selectUserById(product.getSellerId());
        if (seller != null) {
            // 可扩展：将卖家信用分存入商品扩展字段，或返回DTO包含卖家信息
            log.debug("商品{}的卖家{}信用分为：{}", productId, seller.getUserId(), seller.getCreditScore());
        }
        // 5. 缓存商品信息
        redisTemplate.opsForValue().set(CACHE_KEY_PRODUCT + productId, product, CACHE_TTL_PRODUCT);
        return product;
    }

    /**
     * 按关键词搜索商品（基础CRUD，分页）
     * 核心逻辑：调用ProductMapper.selectByKeyword模糊搜索，按浏览量倒序排序
     */
    @Override
    public PageResult<Product> selectProductByKeyword(String keyword, PageParam pageParam) {
        try {
            // 1. 参数校验
            if (!StringUtils.hasText(keyword) || pageParam == null || pageParam.getPageNum() < 1 || pageParam.getPageSize() < 1) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 计算分页参数（offset = (页码-1)*每页条数）
            int offset = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
            int limit = pageParam.getPageSize();

            // 3. 关键词搜索商品
            List<Product> productList = productMapper.selectByKeyword(keyword, offset, limit);

            // 4. 查询总条数（用于计算总页数）
            int total = productMapper.countByKeyword(keyword);

            // 5. 构建分页结果
            PageResult<Product> pageResult = new PageResult<>();
            pageResult.setList(productList);
            pageResult.setTotal(total);
            pageResult.setPageNum(pageParam.getPageNum());
            pageResult.setPageSize(pageParam.getPageSize());
            pageResult.setTotalPages((total + limit - 1) / limit); // 向上取整计算总页数
            log.info("关键词[{}]搜索商品成功，第{}页，共{}条", keyword, pageParam.getPageNum(), total);
            return pageResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("关键词搜索商品异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 更新商品信息（基础CRUD）
     * 核心逻辑：校验仅卖家可操作，调用ProductMapper.updateById更新价格、库存等信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProductInfo(Long productId, ProductUpdateVO productUpdateVO, Long sellerId) {
        try {
            // 1. 参数校验
            if (productId == null || productUpdateVO == null || sellerId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验商品存在性
            Product product = selectProductById(productId);
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }

            // 3. 校验卖家权限（仅商品所属卖家可更新）
            if (!Objects.equals(product.getSellerId(), sellerId)) {
                log.error("更新商品无权限，商品ID：{}，操作卖家ID：{}，实际卖家ID：{}", productId, sellerId, product.getSellerId());
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 构建更新实体
            Product updateProduct = new Product();
            updateProduct.setProductId(productId);
            // 复制更新字段（价格、库存、描述等）
            BeanUtils.copyProperties(productUpdateVO, updateProduct);

            // 5. 执行更新
            int updateRows = productMapper.updateById(updateProduct);
            if (updateRows <= 0) {
                log.error("更新商品信息失败，商品ID：{}，更新参数：{}", productId, productUpdateVO);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 6. 清除缓存（下次查询重新加载最新数据）
            redisTemplate.delete(CACHE_KEY_PRODUCT + productId);
            log.info("更新商品信息成功，商品ID：{}", productId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新商品信息异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 按商品ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验卖家或管理员权限，调用ProductMapper.deleteById标记删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProductById(Long productId, Long operatorId) {
        try {
            // 1. 参数校验
            if (productId == null || operatorId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            // 2. 校验商品存在性
            Product product = selectProductById(productId);
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }
            // 3. 校验权限（卖家或管理员可删除）
            User operator = userService.selectUserById(operatorId);
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
     * 发布商品（业务方法）
     * 核心逻辑：校验卖家信用分≥80分，校验成色合法性，上传图片至OSS，调用insertProduct完成新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishProduct(ProductCreateVO productCreateVO, Long sellerId) {
        try {
            // 1. 参数校验
            if (productCreateVO == null || sellerId == null || !StringUtils.hasText(productCreateVO.getTitle()) ||
                    productCreateVO.getPrice() == null || productCreateVO.getCondition() == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验卖家存在性及信用分（信用分≥80可发布商品）
            User seller = userService.selectUserById(sellerId);
            if (seller == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }
            if (seller.getCreditScore() < PUBLISH_CREDIT_LIMIT) {
                log.error("发布商品信用分不足，卖家ID：{}，信用分：{}，最低要求：{}", sellerId, seller.getCreditScore(), PUBLISH_CREDIT_LIMIT);
                throw new BusinessException(ErrorCode.CREDIT_TOO_LOW);
            }

//            // 3. 上传商品图片至OSS（获取图片URL）
//            List<String> imageUrls = null;
//            if (!CollectionUtils.isEmpty(productCreateVO.getImageFiles())) {
//                imageUrls = ossUtil.uploadFiles(productCreateVO.getImageFiles(), "product/" + sellerId);
//                if (CollectionUtils.isEmpty(imageUrls)) {
//                    throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
//                }
//            }

            // 4. 构建商品实体
            Product product = new Product();
            BeanUtils.copyProperties(productCreateVO, product);
            product.setSellerId(sellerId);

            // 5. 调用基础方法新增商品
            Long productId = insertProduct(product);
            log.info("发布商品成功，商品ID：{}，卖家ID：{}", productId, sellerId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发布商品异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 更新商品库存（业务方法）
     * 核心逻辑：校验库存充足性，调用ProductMapper.updateStock更新，返回最新库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStock(Long productId, Integer stockChange, String reason) {
        try {
            // 1. 参数校验
            if (productId == null || stockChange == null || !StringUtils.hasText(reason)) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验商品存在性
            Product product = selectProductById(productId);
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }

            // 3. 校验库存充足性（库存变更为负数时，需确保当前库存≥变更绝对值）
            int currentStock = product.getStock();
            if (stockChange < 0 && currentStock < Math.abs(stockChange)) {
                log.error("库存不足，商品ID：{}，当前库存：{}，需扣减：{}", productId, currentStock, Math.abs(stockChange));
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
            }

            // 4. 计算新库存并更新
            int newStock = currentStock + stockChange;
            int updateRows = productMapper.updateStock(productId, newStock);
            if (updateRows <= 0) {
                log.error("更新库存失败，商品ID：{}，变更值：{}，原因：{}", productId, stockChange, reason);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 5. 更新缓存
            product.setStock(newStock);
            redisTemplate.opsForValue().set(CACHE_KEY_PRODUCT + productId, product, CACHE_TTL_PRODUCT);
            log.info("更新库存成功，商品ID：{}，原库存：{}，变更值：{}，新库存：{}，原因：{}",
                    productId, currentStock, stockChange, newStock, reason);
            return newStock;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新库存异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 多条件筛选商品（业务方法，分页）
     * 核心逻辑：调用ProductMapper.selectByCondition多条件查询，补充卖家好评率
     */
    @Override
    public PageResult<Product> selectProductByCondition(String category, Double minPrice, Double maxPrice, PageParam pageParam) {
        try {
            // 1. 参数校验
            if (pageParam == null || pageParam.getPageNum() < 1 || pageParam.getPageSize() < 1) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 计算分页参数
            int offset = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
            int limit = pageParam.getPageSize();

            // 3. 多条件筛选商品
            List<Product> productList = productMapper.selectByCondition(category, minPrice, maxPrice, null, offset, limit);

            // 4. 查询总条数
            int total = productMapper.countByCondition(category, minPrice, maxPrice, null);

            // 5. 构建分页结果
            PageResult<Product> pageResult = new PageResult<>();
            pageResult.setList(productList);
            pageResult.setTotal(total);
            pageResult.setPageNum(pageParam.getPageNum());
            pageResult.setPageSize(pageParam.getPageSize());
            pageResult.setTotalPages((total + limit - 1) / limit);
            log.info("多条件筛选商品成功，分类：{}，价格区间：{}-{}，第{}页，共{}条",
                    category, minPrice, maxPrice, pageParam.getPageNum(), total);
            return pageResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("多条件筛选商品异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }



}
