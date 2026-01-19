package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件大小 */
    private Long fileSize;

    /** 文件类型 */
    private String fileType;

    /** 文件后缀 */
    private String suffix;

    /** 文件上传者ID */
    private Long userId;

    /** 文件上传时间 */
    private LocalDateTime createTime;

    /** 文件更新时间 */
    private LocalDateTime updateTime;

    /** 文件是否删除 */
    private Boolean isDelete;
}
