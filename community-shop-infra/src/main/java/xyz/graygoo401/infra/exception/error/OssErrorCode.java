package xyz.graygoo401.infra.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum OssErrorCode implements IErrorCode {

    // 对象存储服务模块
    OSS_SERVICE_FAILS("OSS_001", 500, "OSS服务不可用"),

    OSS_BUCKET_NOT_EXISTS("OSS_051", 404, "存储桶不存在"),

    OSS_FILE_NOT_EXISTS("OSS_101", 404, "文件不存在"),
    OSS_FILE_FORMAT_INVALID("OSS_102", 400, "文件格式不符合规范"),
    OSS_FILE_MINE_INVALID("OSS_103", 400, "文件mine类型不符合规范"),
    OSS_FILE_SIZE_INVALID("OSS_104", 400, "文件不符合规范"),

    OSS_PICTURE_NOT_EXISTS("OSS_102", 404, "图片不存在"),
    OSS_PICTURE_FORMAT_INVALID("OSS_103", 400, "图片格式不符合规范"),
    OSS_PICTURE_SIZE_INVALID("OSS_104", 400, "图片大小错误"),

    OSS_MULTIPART_FILE_NOT_EXISTS("OSS_101", 404, "分片文件不存在"),
    OSS_MULTIPART_FILE_SIZE_INVALID("OSS_102", 400, "分片文件不符合规范"),
    OSS_MULTIPART_FILE_UPLOAD_FAILS("OSS_103", 500, "分片文件上传失败"),

    OSS_PRESIGNED_URL_GENERATE_FAILS("OSS_105", 500, "预览访问链接生成失败"),

    OSS_FILE_DOWNLOAD_FAILS("OSS_106", 500, "文件下载失败"),
    OSS_FILE_DELETE_FAILS("OSS_107", 500, "文件删除失败");

    private final String code;
    private final int standardCode;
    private final String message;

}
