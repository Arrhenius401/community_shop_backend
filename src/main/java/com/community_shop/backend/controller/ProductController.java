package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.product.*;
import com.community_shop.backend.service.base.ProductService;
import com.community_shop.backend.utils.RequestParseUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(
        name = "商品管理接口",
        description = "包含商品发布、详情查询、信息更新、库存调整、上下架及搜索等功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发布商品接口
     * 对应Service层：ProductServiceImpl.publishProduct()，校验信用分≥80、标题/价格/库存合法性、图片≤5张
     */
    @PostMapping("/publish")
    @LoginRequired
    @Operation(
            summary = "发布商品接口",
            description = "登录卖家发布商品，业务规则：1.卖家信用分需≥80分（低于则无法发布）；2.标题1-100字非空；3.价格>0（支持两位小数）；4.库存≥0；5.图片最多5张（URL列表）；6.分类不能为空；7.描述≤2000字；发布后默认状态为在售（ON_SALE）",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "商品发布参数，含标题、价格、库存、分类、图片等核心信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductPublishDTO.class))
            ),
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发布成功，返回商品详情（含卖家脱敏信息）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（标题为空=PRODUCT_002、价格≤0=PRODUCT_014、库存<0=PRODUCT_015、图片超5张=PRODUCT_022、描述超2000字=PRODUCT_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "信用分不足（<80分=USER_081）/无卖家权限（SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "卖家不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<ProductDetailDTO> publishProduct(
            @Valid @RequestBody
            @Parameter(description = "商品发布参数，title/price/stock/category为必填，imageUrls可选（最多5张）", required = true)
            ProductPublishDTO productPublishDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        ProductDetailDTO productDetail = productService.publishProduct(currentSellerId, productPublishDTO);
        return ResultVO.success(productDetail);
    }

    /**
     * 获取商品详情接口
     * 对应Service层：ProductServiceImpl.getProductDetail()，校验商品存在、仅返回在售状态，自动累加浏览量
     */
    @GetMapping("/{productId}")
    @Operation(
            summary = "获取商品详情接口",
            description = "查询指定商品的完整信息，业务规则：1.仅返回在售状态（ON_SALE）商品；2.已下架/删除/封禁商品无法查看；3.自动累加浏览量（异步处理，不阻塞查询）；返回信息含卖家脱敏信息、库存、图片列表等",
            parameters = @Parameter(
                    name = "productId",
                    description = "商品唯一标识（路径参数）",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "integer", format = "int64", example = "4001")
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回商品完整详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（商品ID为空，对应错误码：PRODUCT_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "商品不存在（对应错误码：PRODUCT_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "商品已下架/删除（对应错误码：PRODUCT_091）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<ProductDetailDTO> getProductDetail(
            @PathVariable
            @Parameter(description = "商品ID，需为整数", required = true, example = "4001")
            Long productId
    ) {
        ProductDetailDTO productDetail = productService.getProductDetail(productId);
        return ResultVO.success(productDetail);
    }

    /**
     * 更新商品信息接口
     * 对应Service层：ProductServiceImpl.updateProduct()，校验卖家身份、商品存在、非库存信息修改
     */
    @PutMapping("/update")
    @LoginRequired
    @Operation(
            summary = "更新商品信息接口",
            description = "商品卖家修改非库存信息，业务规则：1.仅商品所属卖家可操作；2.支持修改标题（1-100字）、价格（>0）、描述（≤2000字）、图片（≤5张）；3.不支持直接修改库存（需通过库存更新接口）；4.商品需存在且状态正常",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "商品更新参数，含商品ID及待修改字段",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductUpdateDTO.class))
            ),
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功，返回更新后商品详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（商品ID为空=PRODUCT_004、标题超100字=PRODUCT_011、价格≤0=PRODUCT_014、图片超5张=PRODUCT_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非商品所属卖家，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "商品不存在（对应错误码：PRODUCT_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<ProductDetailDTO> updateProduct(
            @Valid @RequestBody
            @Parameter(description = "商品更新参数，productId为必填，其他字段按需填写（非空则更新）", required = true)
            ProductUpdateDTO productUpdateDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        ProductDetailDTO updatedProduct = productService.updateProduct(currentSellerId, productUpdateDTO);
        return ResultVO.success(updatedProduct);
    }

    /**
     * 更新商品库存接口
     * 对应Service层：ProductServiceImpl.updateStock()，校验卖家身份、库存充足性（扣减时）
     */
    @PatchMapping("/{productId}/stock")
    @LoginRequired
    @Operation(
            summary = "更新商品库存接口",
            description = "商品卖家调整库存，业务规则：1.仅商品所属卖家可操作；2.支持增加（stockChange>0）或扣减（stockChange<0）库存；3.扣减时需校验库存充足（新库存≥0）；4.返回调整后最新库存数量",
            parameters = @Parameter(
                    name = "productId",
                    description = "商品ID（路径参数）",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "integer", format = "int64", example = "4001")
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "库存更新参数，含调整数量（正数增加/负数扣减）",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductStockUpdateDTO.class))
            ),
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "库存更新成功，返回最新库存数量",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（商品ID为空=PRODUCT_004、调整数量为空=SYSTEM_003、扣减后库存<0=STOCK_INSUFFICIENT）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非商品所属卖家，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "商品不存在（对应错误码：PRODUCT_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "库存更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Integer> updateProductStock(
            @PathVariable
            @Parameter(description = "商品ID", required = true, example = "4001")
            Long productId,
            @Valid @RequestBody
            @Parameter(description = "库存更新参数，stockChange为调整数量（正数增加，负数扣减）", required = true)
            ProductStockUpdateDTO productStockUpdateDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        productStockUpdateDTO.setProductId(productId);
        Integer latestStock = productService.updateStock(currentSellerId, productStockUpdateDTO);
        return ResultVO.success(latestStock);
    }

    /**
     * 商品上下架接口
     * 对应Service层：ProductServiceImpl.changeProductStatus()，校验卖家/管理员身份、状态流转合法性
     */
    @PatchMapping("/{productId}/status")
    @LoginRequired
    @Operation(
            summary = "商品上下架接口",
            description = "卖家/管理员切换商品状态，业务规则：1.卖家权限：仅可操作自己的商品，支持ON_SALE（在售）↔OFF_SHELF（下架）切换；2.管理员权限：可操作所有商品，支持ON_SALE/OFF_SHELF/DELETED/BLOCKED切换；3.相同状态重复操作视为成功（无实际更新）",
            parameters = @Parameter(
                    name = "productId",
                    description = "商品ID（路径参数）",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "integer", format = "int64", example = "4001")
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "状态更新参数，指定目标状态（需符合权限对应的状态范围）",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductStatusUpdateDTO.class))
            ),
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "状态更新成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（商品ID为空=PRODUCT_004、目标状态非法=PRODUCT_012）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非商品所属卖家/非管理员，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "商品不存在（对应错误码：PRODUCT_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "状态更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> changeProductStatus(
            @PathVariable
            @Parameter(description = "商品ID", required = true, example = "4001")
            Long productId,
            @Valid @RequestBody
            @Parameter(description = "状态更新参数，targetStatus需符合权限范围（卖家：ON_SALE/OFF_SHELF；管理员：所有状态）", required = true)
            ProductStatusUpdateDTO productStatusUpdateDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        productStatusUpdateDTO.setProductId(productId);
        Boolean result = productService.changeProductStatus(currentSellerId, productStatusUpdateDTO);
        return ResultVO.success(result);
    }

    /**
     * 搜索商品接口
     * 对应Service层：ProductServiceImpl.queryProducts()，支持多条件筛选、分页与排序，优先从缓存获取
     */
    @GetMapping("/search")
    @Operation(
            summary = "搜索商品接口",
            description = "多条件查询在售商品列表，业务规则：1.仅返回在售状态（ON_SALE）商品；2.支持关键词模糊匹配标题/描述、价格区间（minPrice≤price≤maxPrice）、商品分类筛选；3.排序支持浏览量（VIEW_COUNT）/价格（PRICE）/发布时间（CREATE_TIME）；4.分页默认pageNum=1、pageSize=10；结果优先从缓存获取",
            parameters = {
                    @Parameter(name = "keyword", description = "搜索关键词（模糊匹配标题/描述）", in = ParameterIn.QUERY),
                    @Parameter(name = "minPrice", description = "最低价格（≥0，支持两位小数）", in = ParameterIn.QUERY, schema = @Schema(type = "number", format = "double")),
                    @Parameter(name = "maxPrice", description = "最高价格（>minPrice，支持两位小数）", in = ParameterIn.QUERY, schema = @Schema(type = "number", format = "double")),
                    @Parameter(name = "category", description = "商品分类ID（精准匹配）", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
                    @Parameter(name = "sortField", description = "排序字段（VIEW_COUNT=浏览量，PRICE=价格，CREATE_TIME=发布时间）", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "VIEW_COUNT")),
                    @Parameter(name = "sortDir", description = "排序方向（ASC=升序，DESC=降序）", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "DESC")),
                    @Parameter(name = "pageNum", description = "页码（默认1）", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "1")),
                    @Parameter(name = "pageSize", description = "每页条数（默认10）", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "10"))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "搜索成功，返回分页商品列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（价格≤0=PRODUCT_014、分页参数为负数=SYSTEM_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<ProductListItemDTO>> searchProducts(
            @Valid @ModelAttribute
            @Parameter(description = "商品搜索参数，支持关键词、价格区间、分类筛选及排序")
            ProductQueryDTO productQueryDTO
    ) {
        PageResult<ProductListItemDTO> productPage = productService.queryProducts(productQueryDTO);
        return ResultVO.success(productPage);
    }

    /**
     * 查询卖家商品列表接口
     * 对应Service层：ProductServiceImpl.getSellerProducts()，仅查询当前卖家的商品，支持状态筛选
     */
    @GetMapping("/seller/list")
    @LoginRequired
    @Operation(
            summary = "查询卖家商品列表接口",
            description = "登录卖家查询自己发布的商品列表，业务规则：1.仅返回当前卖家的商品；2.支持按状态筛选（ON_SALE/OFF_SHELF/DELETED等）；3.分页默认pageNum=1、pageSize=10；默认按发布时间降序排序",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回分页卖家商品列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（分页参数为负数=SYSTEM_002、状态非法=PRODUCT_012）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "卖家不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<ProductListItemDTO>> getSellerProducts(
            @Valid @ModelAttribute
            @Parameter(description = "卖家商品查询参数，支持状态筛选及分页，sellerId自动从Token解析")
            SellerProductQueryDTO sellerProductQueryDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        sellerProductQueryDTO.setSellerId(currentSellerId);
        PageResult<ProductListItemDTO> sellerProductPage = productService.getSellerProducts(sellerProductQueryDTO);
        return ResultVO.success(sellerProductPage);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID（未登录时返回null）
     */
    private Long parseUserIdFromToken() {
        return requestParseUtil.parseUserIdFromRequest();
    }
}