package xyz.graygoo401.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果封装，适配《文档》中分页方法的返回数据（如selectPostListByBar、selectProductByKeyword）
 * @param <T> 泛型参数，存储分页数据列表（如Post、Product）
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "分页查询结果封装")
public class PageResult<T> {
    /** 总记录数 */
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /** 总页数 */
    @Schema(description = "总页数", example = "10")
    private Long totalPages;

    /** 当前页数据列表 */
    @Schema(description = "当前页数据列表")
    private List<T> list;

    /** 当前页码 */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    /** 每页条数 */
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize;
}