package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.product.*;
import com.community_shop.backend.service.base.ProductService;
import com.community_shop.backend.utils.RequestParseUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理模块Controller，负责商品发布、详情查询、库存更新、状态管理及搜索等接口实现
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "商品管理接口", description = "包含商品发布、详情查询、库存更新、上下架及搜索等功能")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发布商品接口
     * @param productPublishDTO 商品发布参数（标题、价格、库存、图片等）
     * @return 包含商品详情的统一响应
     */
    @PostMapping("/publish")
    @LoginRequired
    @Operation(
            summary = "发布商品接口",
            description = "登录用户发布商品，需校验信用分≥80，支持多图上传，必填标题、价格等核心信息"
    )
    public ResultVO<ProductDetailDTO> publishProduct(
            @Valid @RequestBody
            @Parameter(description = "商品发布参数，包含标题、价格、库存、图片URL及类别等", required = true)
            ProductPublishDTO productPublishDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        ProductDetailDTO productDetail = productService.publishProduct(currentSellerId, productPublishDTO);
        return ResultVO.success(productDetail);
    }

    /**
     * 获取商品详情接口
     * @param productId 商品ID
     * @return 包含商品完整信息的统一响应
     */
    @GetMapping("/{productId}")
    @Operation(
            summary = "获取商品详情接口",
            description = "查询指定商品的完整信息，自动累加浏览量，仅返回在售状态商品"
    )
    public ResultVO<ProductDetailDTO> getProductDetail(
            @PathVariable
            @Parameter(description = "商品唯一标识", required = true, example = "4001")
            Long productId
    ) {
        ProductDetailDTO productDetail = productService.getProductDetail(productId);
        return ResultVO.success(productDetail);
    }

    /**
     * 更新商品信息接口
     * @param productUpdateDTO 商品更新参数（标题、价格、详情等）
     * @return 包含更新后商品详情的统一响应
     */
    @PutMapping("/update")
    @LoginRequired
    @Operation(
            summary = "更新商品信息接口",
            description = "商品卖家可修改标题、价格等非库存信息，需校验卖家身份与商品状态"
    )
    public ResultVO<ProductDetailDTO> updateProduct(
            @Valid @RequestBody
            @Parameter(description = "商品更新参数，包含商品ID、新标题、新价格等", required = true)
            ProductUpdateDTO productUpdateDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        ProductDetailDTO updatedProduct = productService.updateProduct(currentSellerId, productUpdateDTO);
        return ResultVO.success(updatedProduct);
    }

    /**
     * 更新商品库存接口
     * @param productId 商品ID
     * @param productStockUpdateDTO 库存更新参数（调整数量、操作类型）
     * @return 包含最新库存的统一响应
     */
    @PatchMapping("/{productId}/stock")
    @LoginRequired
    @Operation(
            summary = "更新商品库存接口",
            description = "商品卖家可增加或减少库存，扣减时需校验库存充足性，返回最新库存数量"
    )
    public ResultVO<Integer> updateProductStock(
            @PathVariable
            @Parameter(description = "商品ID", required = true, example = "4001")
            Long productId,
            @Valid @RequestBody
            @Parameter(description = "库存更新参数，包含调整数量与操作类型（增加/扣减）", required = true)
            ProductStockUpdateDTO productStockUpdateDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        productStockUpdateDTO.setProductId(productId);
        Integer latestStock = productService.updateStock(currentSellerId, productStockUpdateDTO);
        return ResultVO.success(latestStock);
    }

    /**
     * 商品上下架接口
     * @param productId 商品ID
     * @param productStatusUpdateDTO 状态更新参数（目标状态：在售/下架）
     * @return 操作结果的统一响应
     */
    @PatchMapping("/{productId}/status")
    @LoginRequired
    @Operation(
            summary = "商品上下架接口",
            description = "商品卖家可将商品上架销售或下架暂停售卖，仅支持在售与下架状态切换"
    )
    public ResultVO<Boolean> changeProductStatus(
            @PathVariable
            @Parameter(description = "商品ID", required = true, example = "4001")
            Long productId,
            @Valid @RequestBody
            @Parameter(description = "状态更新参数，指定商品目标状态（在售/下架）", required = true)
            ProductStatusUpdateDTO productStatusUpdateDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        productStatusUpdateDTO.setProductId(productId);
        Boolean result = productService.changeProductStatus(currentSellerId, productStatusUpdateDTO);
        return ResultVO.success(result);
    }

    /**
     * 搜索商品接口
     * @param productQueryDTO 商品搜索参数（关键词、价格区间、类别等）
     * @return 包含分页商品列表的统一响应
     */
    @GetMapping("/search")
    @Operation(
            summary = "搜索商品接口",
            description = "支持按关键词、价格区间、商品类别筛选，默认按浏览量降序分页返回"
    )
    public ResultVO<PageResult<ProductListItemDTO>> searchProducts(
            @Valid @ModelAttribute
            @Parameter(description = "商品搜索参数，包含关键词、价格范围、类别及分页信息")
            ProductQueryDTO productQueryDTO
    ) {
        PageResult<ProductListItemDTO> productPage = productService.queryProducts(productQueryDTO);
        return ResultVO.success(productPage);
    }

    /**
     * 查询卖家商品列表接口
     * @param sellerProductQueryDTO 卖家商品查询参数（状态、分页信息）
     * @return 包含分页卖家商品列表的统一响应
     */
    @GetMapping("/seller/list")
    @LoginRequired
    @Operation(
            summary = "查询卖家商品列表接口",
            description = "登录卖家查询自己发布的商品列表，支持按商品状态筛选"
    )
    public ResultVO<PageResult<ProductListItemDTO>> getSellerProducts(
            @Valid @ModelAttribute
            @Parameter(description = "卖家商品查询参数，包含商品状态及分页信息", required = true)
            SellerProductQueryDTO sellerProductQueryDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        sellerProductQueryDTO.setSellerId(currentSellerId);
        PageResult<ProductListItemDTO> sellerProductPage = productService.getSellerProducts(sellerProductQueryDTO);
        return ResultVO.success(sellerProductPage);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID
     */
    private Long parseUserIdFromToken() {
        // 通过HttpServletRequest获取Authorization头，解析JWT令牌得到用户ID
        return requestParseUtil.parseUserIdFromRequest();
    }
}