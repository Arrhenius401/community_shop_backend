package xyz.graygoo401.api.infra.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件详情DTO
 */
@NoArgsConstructor
@Data
@Schema(description = "文件详情数据模型")
public class FileDetailDTO {

    /** 文件ID */
    @NotNull(message = "文件ID不能为空")
    @Schema(description = "文件唯一标识", example = "12")
    private Long fileId;

    /** 文件名 */
    @NotNull(message = "文件名不能为空")
    @Schema(description = "文件名", example = "avatar.png")
    private String fileName;

    /** 文件路径 */
    @NotNull(message = "文件路径不能为空")
    @Schema(description = "文件路径", example = "avatar/2023/10/01/avatar.png")
    private String filePath;

    /** 文件大小 */
    @Schema(description = "文件大小（字节数）", example = "1024")
    private Long fileSize;

    /** 文件类型 */
    @Schema(description = "文件类型", example = "image/png")
    private String fileType;

    /** 文件后缀 */
    @Schema(description = "文件后缀", example = "png")
    private String suffix;

    /** 文件上传者ID */
    @Schema(description = "文件上传者ID", example = "1")
    private Long userId;

    /** 文件上传时间 */
    @Schema(description = "文件上传时间", example = "2023-10-01T10:15:00")
    private LocalDateTime createTime;

    /** 文件更新时间 */
    @Schema(description = "文件更新时间", example = "2023-10-01T10:15:00")
    private LocalDateTime updateTime;

    /** 文件是否删除 */
    @Schema(description = "文件是否删除", example = "false")
    private Boolean isDelete;
}
