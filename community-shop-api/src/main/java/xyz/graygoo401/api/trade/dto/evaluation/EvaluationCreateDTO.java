package xyz.graygoo401.api.trade.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;

/**
 * 评价创建请求VO（视图对象）
 * 用于接收前端传递的创建参数，适配评价内容更新的业务场景
 */
@NoArgsConstructor
@Data
@Schema(description = "评价创建请求DTO，用于接收创建评价的参数")
public class EvaluationCreateDTO implements Serializable {

    /** 序列化ID */
    private static final long serialVersionUID = 1L;

    /** 评价关联的订单ID（非空） */
    @NotNull(message = "评价ID不能为空")
    @Schema(description = "关联的订单ID，用于定位评价对象", example = "654321", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    /** 新评价分数 */
    @NotNull(message = "评价分数不能为空")
    @Schema(description = "评价分数（1-5星）", example = "5", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"1", "2", "3", "4", "5"})
    private Integer score;

    /** 新评价内容 */
    @NotBlank(message = "评价内容不能为空")
    @Length(min = 1, max = 500, message = "评价内容长度必须在1-500字符之间")
    @Schema(description = "评价文本内容", example = "商品质量很好，物流也很快，非常满意！", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    private String content;

    /** 评价图片URL列表（最多5张，可选） */
    @Size(max = 5, message = "最多上传5张图片")
    @Schema(description = "评价图片URL列表（最多5张）", example = "[\"https://example.com/img1.jpg\", \"https://example.com/img2.jpg\"]")
    private List<String> imageUrls;

    /** 评价标签（如“质量好”“物流快”，最多3个，可选） */
    @Size(max = 3, message = "最多选择3个标签")
    @Schema(description = "评价标签列表（最多3个）", example = "[\"质量好\", \"物流快\"]")
    private List<String> tags;
}