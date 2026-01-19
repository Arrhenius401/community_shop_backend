package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.code.PayTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * 订单创建请求VO（视图对象）
 * 用于接收前端传递的下单参数，适配创建订单的业务场景
 * 对应文档中"订单创建"功能的前端入参封装
 */
@Schema(description = "订单创建请求DTO，用于提交下单参数")
@Data
public class OrderCreateDTO {

    /** 商品ID */
    @Schema(description = "商品ID", example = "3001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 收货地址 */
    @Schema(description = "买家收货地址", example = "北京市朝阳区XX街道XX小区1号楼1单元101",
            maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货地址不能为空")
    @Length(max = 500, message = "收货地址长度不能超过500字符")
    private String address;

    /** 购买数量 */
    @Schema(description = "商品购买数量（默认1）", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    private Integer quantity = 1;

    /** 订单总价 */
    @Schema(description = "订单总价", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal totalAmount;

    /** 买家留言 */
    @Schema(description = "买家留言", example = "请在周末送货", maxLength = 200)
    @Length(max = 200, message = "买家留言长度不能超过200字符")
    private String buyerRemark;

    /** 支付方式 */
    @Schema(description = "支付方式（枚举）", example = "WECHAT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付方式不能为空")
    private PayTypeEnum payType;
}