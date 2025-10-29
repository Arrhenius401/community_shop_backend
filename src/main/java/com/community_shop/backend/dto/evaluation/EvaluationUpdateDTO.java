package com.community_shop.backend.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * 评价更新请求VO（视图对象）
 * 用于接收前端传递的评价修改参数，适配评价内容更新的业务场景
 */
@Schema(description = "评价更新请求DTO，用于接收修改评价的参数")
@Data
public class EvaluationUpdateDTO implements Serializable {

    /**
     * 关于Serializable
     * Serializable是一个标记接口（没有任何抽象方法），它的作用是声明一个类的对象可以被序列化。
     * 序列化：将对象的状态（成员变量的值）转换为字节流的过程，便于存储到文件、数据库，或通过网络传输到其他系统。
     * 反序列化：将字节流恢复为对象的过程，使接收方能够重建原始对象。
     * -------------------
     * 应用场景：
     * 分布式系统中，服务间传输对象（如 RPC 调用、消息队列传递对象）。
     * 将对象持久化到磁盘（如保存用户会话状态）。
     * 缓存框架中存储对象（如 Redis 存储 Java 对象）。
     */


    /**
     * 序列化ID
     * 在实际开发中，凡是需要跨系统传输或持久化的类（如 VO、DTO、实体类），都建议实现Serializable并显式定义serialVersionUID。
     */
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    @NotNull(message = "评价ID不能为空")
    @Schema(description = "需要更新的评价ID", example = "987654", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long evalId;

    /** 新评价分数 */
    @NotNull(message = "评价分数不能为空")
    @Schema(description = "更新后的评价分数（1-5星）", example = "4", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"1", "2", "3", "4", "5"})
    private Integer newScore;

    /** 新评价内容 */
    @NotBlank(message = "评价内容不能为空")
    @Length(min = 1, max = 500, message = "评价内容长度必须在1-500字符之间")
    @Schema(description = "更新后的评价文本内容", example = "商品质量不错，就是物流有点慢，整体满意！", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    private String newContent;


    public EvaluationUpdateDTO() {}

}
