package xyz.graygoo401.trade.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 商品模块错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum ProductErrorCode implements IErrorCode {

    // 商品模块
    PRODUCT_NOT_EXISTS("PRODUCT_001", 404, "商品不存在"),
    PRODUCT_TITLE_NULL("PRODUCT_002", 400, "商品标题为空"),
    PRODUCT_PRICE_NULL("PRODUCT_003", 400, "商品价格为空"),
    PRODUCT_ID_NULL("PRODUCT_004", 400, "商品ID为空"),
    PRODUCT_CATEGORY_NULL("PRODUCT_005", 400, "商品分类为空"),

    PRODUCT_TITLE_INVALID("PRODUCT_011", 400, "商品标题不符合规范"),
    PRODUCT_STATUS_INVALID("PRODUCT_012", 400, "商品状态参数错误"),
    PRODUCT_CONDITION_INVALID("PRODUCT_013", 400, "商品成色参数错误"),
    PRODUCT_PRICE_INVALID("PRODUCT_014", 400, "商品价格参数错误"),
    PRODUCT_STOCK_INVALID("PRODUCT_015", 400, "商品库存参数错误"),
    PRODUCT_IMAGE_URL_INVALID("PRODUCT_016", 400, "商品图片URL参数错误"),

    PRODUCT_DESCRIPTION_TOO_LONG("PRODUCT_021", 400, "商品描述过长"),
    PRODUCT_IMAGE_TOO_MANY("PRODUCT_022", 400, "商品图片数量过多"),

    PRODUCT_ALREADY_OFF_SALE("PRODUCT_091", 400, "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT("PRODUCT_092", 400, "商品库存不足"),
    ORDER_AMOUNT_ABNORMAL("ORDER_021", 400, "订单金额异常");

    private final String code;
    private final int standardCode;
    private final String message;

}
