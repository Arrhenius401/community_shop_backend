package com.community_shop.backend.utils;

import com.community_shop.backend.config.MinioConfig;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.enums.code.OssModuleEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.exception.OssException;
import io.minio.*;
import io.minio.http.Method;
import io.minio.errors.MinioException;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MinIO 工具类（替换原来的 OssUtil）
 */
@Component
@Slf4j
public class MinioUtil {

    /** 默认文件有效期（秒） */
    private static final Integer DEFAULT_EXPIRE_SECONDS = 3600;

    /** 日期格式 */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /** 文件最大大小 */
    private static final long FILE_MAX_SIZE = 100 * 1024 * 1024;

    /** 文件扩展名白名单（小写，统一格式） */
    private static final List<String> ALLOWED_EXTENSION_TYPES = Arrays.asList(
            // 图片格式（web 常用）
            "jpg", "jpeg", "png", "gif", "webp", "avif", "svg", "bmp", "tiff",
            // 文档格式
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv",
            // 压缩包格式
            "zip", "rar", "7z", "tar", "gz",
            // 音视频格式
            "mp3", "wav", "ogg", "mp4", "avi", "mov", "wmv", "flv", "mkv",
            // 其他常用格式
            "json", "xml", "yaml", "yml"
    );

    // MIME 类型（Multipurpose Internet Mail Extensions，多用途互联网邮件扩展），最初是为了解决邮件中传输非文本数据（如图片、音频）的问题
    // 后来被 HTTP 协议等广泛采用，成为标识文件 / 数据类型的标准化方式
    // 简单来说，MIME 类型的作用是：让客户端（浏览器、服务器、应用程序）知道 “当前处理的数据是什么类型”，从而采用正确的方式解析和处理
    // （例如浏览器看到 image/jpeg 就渲染为图片，看到 text/html 就解析为网页，看到 application/pdf 就调用 PDF 阅读器）。
    /** MIME 类型白名单（部分核心类型，用于交叉校验,MIME 类型的格式为：类型/子类型） */
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            // 图片
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/avif", "image/svg+xml", "image/bmp", "image/tiff",
            // 文档
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "text/markdown", "text/csv",
            // 压缩包
            "application/zip", "application/x-rar-compressed", "application/x-7z-compressed", "application/x-tar", "application/gzip",
            // 音视频
            "audio/mpeg", "audio/wav", "audio/ogg", "video/mp4", "video/x-msvideo", "video/quicktime", "video/x-ms-wmv", "video/x-flv", "video/x-matroska",
            // 其他
            "application/json", "application/xml", "text/yaml", "text/x-yaml"
    );

    // 安全注意事项
    // 1. 禁止高危类型：无论何种场景，均需禁用可执行文件（exe、sh、bat、cmd）、脚本文件（php、jsp、py、asp）、恶意格式（scr、pif）；
    // 2. SVG 处理：若业务需支持 SVG，需额外过滤 SVG 中的 <script> 标签，防止 XSS 攻击；
    // 3. 旧版 Office 文件：doc、xls、ppt 格式易携带宏病毒，建议仅允许新版格式（docx、xlsx、pptx）；
    // 4. MIME 类型校验：仅校验扩展名不够，需结合 Content-Type 或读取文件魔数（Magic Number）进一步校验（例如 JPG 文件魔数为 FF D8 FF）；
    // 5. 文件大小限制：配合白名单设置文件大小上限，防止超大文件攻击。

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Autowired
    public MinioUtil(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    /* ==================== 存储桶操作系列方法 ==================== */

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在
     * @throws Exception Minio操作异常
     */
    public boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
    }

    /**
     * 创建新存储桶
     *
     * @param bucketName 存储桶名称
     * @return 是否创建成功
     * @throws Exception Minio操作异常
     */
    public boolean createBucket(String bucketName) throws Exception {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        }
        return false;
    }

    /**
     * 删除存储桶（存储桶必须为空）
     *
     * @param bucketName 存储桶名称
     * @throws Exception Minio操作异常
     */
    public void removeBucket(String bucketName) throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket(bucketName)
                .build());
    }

    /**
     * 获取全部存储桶列表
     *
     * @return 存储桶信息列表
     * @throws Exception Minio操作异常
     */
    public List<Bucket> listAllBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    /* ==================== 文件操作系列方法 ==================== */

    /**
     * 上传图片（支持 MultipartFile，和 OSS 用法完全一致）
     * @param file 前端上传的文件
     * @param module 模块名（如 "avatar" 头像、"product" 商品图）
     * @param bucketName 存储桶名称
     * @return 图片在 MinIO 中的相对路径（存储到数据库）
     */
    public String uploadImage(MultipartFile file, OssModuleEnum module, String bucketName) {
        // 1. 校验文件类型（仅允许 jpg/png/webp，避免恶意文件）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|png|webp)$")) {
            throw new OssException(ErrorCode.OSS_PICTURE_FORMAT_INVALID, "仅支持 jpg/png/webp 格式图片");
        }

        // 2. 调用普通文件的上传方法
        return uploadFile(file, module, bucketName);
    }

    /**
     * 上传图片（支持 MultipartFile，和 OSS 用法完全一致）
     * @param file 前端上传的文件
     * @param module 模块名（如 "avatar" 头像、"product" 商品图）
     * @return 图片在 MinIO 中的相对路径（存储到数据库）
     */
    public String uploadImage(MultipartFile file, OssModuleEnum module) {
        // 调用默认存储桶
        return uploadImage(file, module, minioConfig.getBucketName());
    }

    /**
     * 批量上传图片（支持 MultipartFile，和 OSS 用法完全一致）
     * @param files 前端上传的文件列表
     * @param module 模块名（如 "avatar" 头像、"product" 商品图）
     * @param bucketName 存储桶名称
     * @return 图片在 MinIO 中的相对路径列表（存储到数据库）
     */
    public List<String> uploadImages(List<MultipartFile> files, OssModuleEnum module, String bucketName) {
        return files.stream()
                .map(file -> uploadImage(file, module, bucketName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 批量上传图片（支持 MultipartFile，和 OSS 用法完全一致）
     * @param files 前端上传的文件列表
     * @param module 模块名（如 "avatar" 头像、"product" 商品图）
     * @return 图片在 MinIO 中的相对路径列表（存储到数据库）
     */
    public List<String> uploadImages(List<MultipartFile> files, OssModuleEnum module) {
        // 调用默认存储桶
        return uploadImages(files, module, minioConfig.getBucketName());
    }

    /**
     * 上传文件到指定存储桶
     * @param file       上传的文件对象
     * @param module     文件模块枚举
     * @param bucketName 存储桶名称
     * @return 图片在 MinIO 中的相对路径（存储到数据库）
     */
    public String uploadFile(MultipartFile file, OssModuleEnum module, String bucketName) {
        this.validateFile(file);

        // 1. 校验基本参数，若为空值则使用默认值
        if (module == null) {
            module = OssModuleEnum.DEFAULT;
        }

        if (bucketName == null) {
            bucketName = minioConfig.getBucketName();
        }

        // 2. 校验文件大小
        long fileSize = file.getSize();
        if (fileSize > module.getMaxSize()) { // 头像≤1MB
            throw new OssException(ErrorCode.OSS_FILE_SIZE_INVALID, module.getDesc() + "数据大小不能超过 " +
                    OssModuleEnum.PICTURE_AVATAR.getMaxSize() / 1024 /1024 + "MB");
        }

        // 3. 生成存储路径
        String originalFilename = file.getOriginalFilename();
        String filePath = generateFilePath(module, originalFilename);

        try (InputStream inputStream = file.getInputStream()) {
            // 4. 确保存储桶存在
            boolean bucketExists = bucketExists(bucketName);
            // 不存在则创建
            if (!bucketExists) {
                createBucket(bucketName);
            }

            // 5. 上传文件
            // putObject() 方法，已内置分片操作
            // 如果 fileSize > 5 MB（默认阈值），SDK 内部会自动触发 multipart upload
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath) // 图片在 MinIO 中的路径
                            .stream(inputStream, fileSize, -1) // 输入流和文件大小
                            .contentType(file.getContentType()) // 文件类型（如 image/jpeg）
                            .build()
            );

            // 6. 返回文件相对路径
            log.info("文件上传成功，相对路径：{}", filePath);
            return filePath;
        } catch (MinioException e) {
            log.error("MinIO 服务异常：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS);
        } catch (IOException e) {
            log.error("文件流读取失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.FAILURE, "图片处理失败");
        } catch (Exception e) {
            log.error("图片上传失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.FAILURE, "图片上传失败");
        }
    }

    /**
     * 上传文件到默认存储桶
     * @param file       上传的文件对象
     * @param module     文件模块枚举
     * @return 图片在 MinIO 中的相对路径（存储到数据库）
     */
    public String uploadFile(MultipartFile file, OssModuleEnum module) {
        // 调用默认存储桶
        return uploadFile(file, module, minioConfig.getBucketName());
    }

    /**
     * 批量文件上传
     * @param files      文件列表
     * @param module     文件模块枚举
     * @param bucketName 存储桶名称
     * @return 文件信息列表
     */
    public List<String> uploadFiles(List<MultipartFile> files, OssModuleEnum module, String bucketName) {
        return files.stream()
                .map(file -> uploadFile(file, module, bucketName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 批量文件上传（使用默认存储桶）
     * @param files 文件列表
     * @param module 文件模块枚举
     * @return 文件信息列表
     */
    public List<String> uploadFiles(List<MultipartFile> files, OssModuleEnum module) {
        return uploadFiles(files, module, minioConfig.getBucketName());
    }

    /**
     * 获取文件输入流
     * @param bucketName 存储桶名称
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     * @return 文件流
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            // 1. 获取存储桶名称
            if (bucketName == null) {
                bucketName = minioConfig.getBucketName();
            }

            // 2. 获取文件输入流
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件获取失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS);
        }
    }

    /**
     * 获取文件输入流
     * @param objectName 存储对象名称
     * @return 文件流
     */
    public InputStream downloadFile(String objectName) {
        // 使用默认存储桶
        return this.downloadFile(minioConfig.getBucketName(), objectName);
    }

    /**
     * 直接下载文件到HttpServletResponse（浏览器下载）
     * @param bucketName  存储桶名称
     * @param objectName 存储对象名称
     * @param response   HttpServletResponse
     */
    public void downloadToResponse(String bucketName, String objectName, HttpServletResponse response) {
        try {
            // 1. 获取存储桶名称
            if (bucketName == null) {
                bucketName = minioConfig.getBucketName();
            }

            // 2. 获取文件元数据
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // 3. 设置响应头
            response.setContentType(stat.contentType());
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + URLEncoder.encode(objectName, StandardCharsets.UTF_8) + "\"");
            response.setContentLengthLong(stat.size());

            // 4. 流式传输文件内容
            InputStream is = downloadFile(objectName);
            OutputStream os = response.getOutputStream();
            IOUtils.copy(is, os);
            os.flush();
        } catch (Exception e) {
            log.error("文件获取失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS, "文件获取失败");
        }
    }

    /**
     * 直接下载文件到HttpServletResponse（浏览器下载）
     * @param objectName 存储对象名称
     * @param response   HttpServletResponse
     */
    public void downloadToResponse(String objectName, HttpServletResponse response) {
        // 使用默认存储桶
        this.downloadToResponse(minioConfig.getBucketName(), objectName, response);
    }

    /**
     * 生成临时访问 URL（和 OSS 签名 URL 逻辑一致，禁止生成永久访问URL，避免匿名访问）
     * @param bucketName 存储桶名称
     * @param filePath 文件在 MinIO 中的相对路径（从数据库读取）
     * @return 带签名的临时访问 URL（前端直接用这个 URL 显示图片）
     */
    public String generatePresignedUrl(String bucketName, String filePath) {
        try {
            // 1. 获取存储桶名称
            if (bucketName == null) {
                bucketName = minioConfig.getBucketName();
            }

            // 2. 生成带签名的 URL，有效期为 DEFAULT_EXPIRE_SECONDS 秒
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filePath)
                            .method(Method.GET) // 只读权限
                            .expiry(DEFAULT_EXPIRE_SECONDS) // 有效期（秒）
                            .build()
            );
        } catch (MinioException e) {
            log.error("MinIO 生成签名 URL 失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_PRESIGNED_URL_GENERATE_FAILS);
        } catch (Exception e) {
            log.error("MinIO 生成签名 URL 失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS, "MinIO 生成签名 URL 失败");
        }
    }

    /**
     * 生成临时访问 URL（和 OSS 签名 URL 逻辑一致，禁止生成永久访问URL，避免匿名访问）
     * @param filePath 文件在 MinIO 中的相对路径（从数据库读取）
     * @return 带签名的临时访问 URL（前端直接用这个 URL 显示图片）
     */
    public String generatePresignedUrl(String filePath) {
        // 使用默认存储桶
        return this.generatePresignedUrl(minioConfig.getBucketName(), filePath);
    }

    /**
     * 删除文件
     * @param bucketName 存储桶名称
     * @param objectName 存储对象名称
     */
    public void deleteFile(String bucketName, String objectName) {
        try {
            // 1. 获取存储桶名称
            if (bucketName == null) {
                bucketName = minioConfig.getBucketName();
            }

            // 2. 删除文件
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("删除文件失败：{}", e.getMessage());
            throw new OssException(ErrorCode.FAILURE, "删除文件失败");
        }
    }

    /**
     * 删除文件
     * @param objectName 存储对象名称
     */
    public void deleteFile(String objectName) {
        // 使用默认存储桶
        this.deleteFile(minioConfig.getBucketName(), objectName);
    }

    /**
     * 批量删除文件
     * @param bucketName  存储桶名称
     * @param objectNames 对象名称列表
     * @return 删除错误列表
     */
    public List<DeleteError> deleteFiles(String bucketName, List<String> objectNames) {
        try {
            // 1. 获取存储桶名称
            if (bucketName == null) {
                bucketName = minioConfig.getBucketName();
            }

            // 2. 批量删除文件
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(objects)
                    .build());

            List<DeleteError> errors = new ArrayList<>();
            for (Result<DeleteError> result : results) {
                errors.add(result.get());
            }
            return errors;
        } catch (Exception e) {
            log.error("批量删除文件失败：{}", e.getMessage());
            throw new OssException(ErrorCode.FAILURE, "批量删除文件失败");
        }
    }

    /**
     * 批量删除文件（使用默认存储桶）
     * @param objectNames 对象名称列表
     * @return 删除错误列表
     */
    public List<DeleteError> deleteFiles(List<String> objectNames) {
        return deleteFiles(minioConfig.getBucketName(), objectNames);
    }

    /**
     * 列出存储桶中的所有文件
     *
     * @param bucketName 存储桶名称
     * @return 文件信息列表
     */
    public List<String> listAllFiles(String bucketName) {
        try {
            List<String> list = new ArrayList<>();
            for (Result<Item> result : minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build())) {
                list.add(result.get().objectName());
            }
            return list;
        } catch (Exception e) {
            log.error("列出存储桶中的所有文件失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS, "列出存储桶中的所有文件失败");
        }
    }

    /**
     * 获取文件元数据
     * @param objectName 存储对象名称
     * @return 文件元数据
     */
    public StatObjectResponse getObjectStat(String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("获取文件元数据失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS, "获取文件元数据失败");
        }
    }

    /**
     * 校验文件类型是否合法
     * @param file 上传的文件
     * @throws IllegalArgumentException 校验失败时抛出异常
     */
    public void validateFile(MultipartFile file) {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.OSS_FILE_NOT_EXISTS,"上传文件不能为空");
        }

        // 2. 获取文件扩展名（小写）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.OSS_FILE_FORMAT_INVALID, "文件名称格式非法，无扩展名");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // 3. 校验扩展名白名单
        if (!ALLOWED_EXTENSION_TYPES.contains(extension)) {
            throw new BusinessException(ErrorCode.OSS_FILE_FORMAT_INVALID,
                    "不支持的文件类型：" + extension + "，允许的类型：" + String.join(",", ALLOWED_EXTENSION_TYPES));
        }

        // 4. 校验 MIME 类型（双重校验，防止扩展名伪造）
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.OSS_FILE_FORMAT_INVALID, "文件 MIME 类型非法：" + contentType + "，允许的类型：" + String.join(",", ALLOWED_MIME_TYPES));
        }

        // 5. （可选）校验文件大小（例如限制 10MB 以内）
        long fileSize = file.getSize();
        if (fileSize > FILE_MAX_SIZE) {
            throw new BusinessException(ErrorCode.OSS_FILE_SIZE_INVALID,
                    "文件大小超过限制，当前大小：" + (fileSize / 1024 / 1024) + "MB；" + "，允许的最大大小：" + (FILE_MAX_SIZE / 1024 / 1024) + "MB");
        }
    }

    /**
     * 检查文件是否存在
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查文件是否存在（使用默认存储桶）
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean objectExists(String objectName) {
        // 使用默认存储桶
        return objectExists(minioConfig.getBucketName(), objectName);
    }

    /* ==================== 扩展功能方法 ==================== */

    /**
     * 复制文件到新位置
     *
     * @param sourceBucket 源存储桶
     * @param sourceObject 源文件
     * @param destBucket   目标存储桶
     * @param destObject   目标文件
     */
    public void copyObject(String sourceBucket, String sourceObject, String destBucket, String destObject) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .source(CopySource.builder()
                            .bucket(sourceBucket)
                            .object(sourceObject)
                            .build())
                    .bucket(destBucket)
                    .object(destObject)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("源文件不存在");
        }
    }

    /* ==================== 辅助方法 ==================== */

    /**
     * 生成文件的相对存储路径（避免文件名重复，和 OSS 逻辑一致）
     * 格式：模块/日期/UUID.后缀（如 product/20240520/123e4567.jpg）
     * @param module 模块名（如 "avatar" 头像、"product" 商品图）
     * @param originalFilename 文件名（如 "123e4567.jpg"）
     * @return 图片在 MinIO 中的相对路径（存储到数据库）
     */
    private String generateFilePath(OssModuleEnum module, String originalFilename) {
        // 1. 对为空的模块名，使用默认模块
        if (module == null){
            module = OssModuleEnum.DEFAULT;
        }

        // 2. 获取文件后缀（如 .jpg、.png）
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 3. 获取当前日期（如 2024-05-20）
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        // 4. 生成 UUID 作为文件名（避免重复）
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 5. 拼接完整路径
        return module + "/" + date + "/" + uuid + suffix;
    }


    /**
     * 获取文件的绝对存储路径
     * @param bucketName 存储桶名称
     * @param objectName 文件对象名称
     * @return 文件的绝对存储路径
     */
    private String generateFileAbsolutePath(String bucketName, String objectName) {
        // 获取存储桶名称
        if (bucketName == null) {
            bucketName = minioConfig.getBucketName();
        }
        // 获取文件绝对路径
        return minioConfig.getEndpoint() + "/" + bucketName + "/" + objectName;
    }
}