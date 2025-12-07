package com.community_shop.backend.utils;

import com.community_shop.backend.enums.error.ErrorCode;
import com.community_shop.backend.enums.code.OssModuleEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.exception.OssException;
import io.minio.*;
import io.minio.http.Method;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO 工具类（替换原来的 OssUtil）
 */
@Component
@Slf4j
public class MinioUtil {

    // 从配置文件读取 MinIO 信息
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.bucket-name}")
    private String bucketName;
    @Value("${minio.base-path}")
    private String basePath;
    @Value("${minio.expire-seconds}")
    private Integer expireSeconds;

    /**
     * 初始化 MinIO 客户端（单例，避免重复创建连接）
     */
    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 生成图片存储路径（避免文件名重复，和 OSS 逻辑一致）
     * 格式：basePath/模块/日期/UUID.后缀（如 images/product/20240520/123e4567.jpg）
     * @param module 模块名（如 "avatar" 头像、"product" 商品图）
     * @param originalFilename 文件名（如 "123e4567.jpg"）
     * @return 图片在 MinIO 中的相对路径（存储到数据库）
     */
    private String generateFilePath(OssModuleEnum module, String originalFilename) {
        // 1. 获取文件后缀（如 .jpg、.png）
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 2. 获取当前日期（如 2024-05-20）
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 3. 生成 UUID 作为文件名（避免重复）
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 4. 拼接完整路径
        return basePath + module + "/" + date + "/" + uuid + suffix;
    }

    /**
     * 上传图片（支持 MultipartFile，和 OSS 用法完全一致）
     * @param file 前端上传的文件
     * @param module 模块名（如 "avatar" 头像、"product" 商品图）
     * @return 图片在 MinIO 中的相对路径（存储到数据库）
     */
    public String uploadImage(MultipartFile file, OssModuleEnum module) throws Exception {
        // 1. 校验文件类型（仅允许 jpg/png/webp，避免恶意文件）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|png|webp)$")) {
            throw new OssException(ErrorCode.OSS_PICTURE_FORMAT_INVALID, "仅支持 jpg/png/webp 格式图片");
        }

        // 2. 校验文件大小（根据模块限制，和你的业务逻辑一致）
        long fileSize = file.getSize();
        if (OssModuleEnum.PICTURE_AVATAR.equals(module) && fileSize > OssModuleEnum.PICTURE_AVATAR.getMaxSize()) { // 头像≤1MB
            throw new OssException(ErrorCode.OSS_PICTURE_SIZE_INVALID,"头像大小不能超过 " + OssModuleEnum.PICTURE_AVATAR.getMaxSize() / 1024 /1024 + "MB");
        } else if (OssModuleEnum.PICTURE_PRODUCT.equals(module) && fileSize > OssModuleEnum.PICTURE_PRODUCT.getMaxSize()) { // 商品图≤5MB
            throw new OssException(ErrorCode.OSS_PICTURE_SIZE_INVALID, "商品图片大小不能超过 " + OssModuleEnum.PICTURE_PRODUCT.getMaxSize() / 1024 /1024 + "MB");
        } else if (OssModuleEnum.PICTURE_POST.equals(module) && fileSize > OssModuleEnum.PICTURE_POST.getMaxSize()) { // 帖子图≤5MB
            throw new OssException(ErrorCode.OSS_PICTURE_SIZE_INVALID, "帖子图片大小不能超过 " + OssModuleEnum.PICTURE_POST.getMaxSize() / 1024 /1024 + "MB");
        }

        // 3. 生成存储路径
        String filePath = generateFilePath(module, originalFilename);

        // 4. 上传到 MinIO
        try (InputStream inputStream = file.getInputStream()) {
            MinioClient minioClient = getMinioClient();
            // 检查存储桶是否存在（不存在则创建）
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath) // 图片在 MinIO 中的路径
                            .stream(inputStream, fileSize, -1) // 输入流和文件大小
                            .contentType(file.getContentType()) // 文件类型（如 image/jpeg）
                            .build()
            );
            log.info("图片上传成功，路径：{}", filePath);
            return filePath; // 返回相对路径，存储到数据库
        } catch (MinioException e) {
            log.error("MinIO 上传失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS);
        } catch (IOException e) {
            log.error("文件流读取失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.FAILURE, "图片处理失败");
        }
    }

    /**
     * 生成临时访问 URL（和 OSS 签名 URL 逻辑一致，避免匿名访问）
     * @param filePath 图片在 MinIO 中的相对路径（从数据库读取）
     * @return 带签名的临时访问 URL（前端直接用这个 URL 显示图片）
     */
    public String generatePresignedUrl(String filePath) throws Exception {
        try {
            MinioClient minioClient = getMinioClient();
            // 生成带签名的 URL，有效期为 expireSeconds 秒
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .method(Method.GET) // 只读权限
                            .expiry(expireSeconds) // 有效期（秒）
                            .build()
            );
        } catch (MinioException e) {
            log.error("MinIO 生成签名 URL 失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_PICTURE_URL_GENERATE_FAILS);
        }
    }

    /**
     * 删除图片（如商品下架、用户删除头像时调用）
     * @param filePath 图片在 MinIO 中的相对路径
     */
    public void deleteImage(String filePath) throws Exception {
        try {
            MinioClient minioClient = getMinioClient();
            // 检查图片是否存在
            boolean objectExists = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            ) != null;
            if (objectExists) {
                // 删除图片
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .build()
                );
                log.info("图片删除成功，路径：{}", filePath);
            }
        } catch (MinioException e) {
            log.error("MinIO 删除图片失败：{}", e.getMessage());
            throw new OssException(ErrorCode.OSS_SERVICE_FAILS);
        }
    }
}