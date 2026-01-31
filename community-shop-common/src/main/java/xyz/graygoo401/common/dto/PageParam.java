package xyz.graygoo401.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询通用参数，适配《文档》中所有分页查询方法（如帖子列表、商品搜索）
 */
@Data
@Schema(description = "分页查询通用参数")
public class PageParam {
    /** 当前页码（默认1） */
    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    /** 每页数量（默认10） */
    @Schema(description = "每页数量", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    /** 偏移量（默认0） */
    @Schema(description = "查询偏移量（自动计算，无需手动设置）", example = "0", hidden = true)
    private Integer offset = 0;

    /**
     * 计算偏移量
     */
    public void setOffset() {
        this.offset = (this.pageNum - 1) * this.pageSize;
    }
}