package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("file")
public class File {

    /** 文件ID */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    /** 文件名 */
    @TableField("file_name")
    private String fileName;

    /** 文件路径 */
    @TableField("file_path")
    private String filePath;

    /** 文件大小 */
    @TableField("file_size")
    private Long fileSize;

    /** 文件类型 */
    @TableField("file_type")
    private String fileType;

    /** 文件后缀 */
    @TableField("suffix")
    private String suffix;

    /** 文件上传者ID */
    @TableField("user_id")
    private Long userId;

    /** 文件上传时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 文件更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 文件是否删除 */
    @TableField("is_delete")
    private Boolean isDelete;
}
