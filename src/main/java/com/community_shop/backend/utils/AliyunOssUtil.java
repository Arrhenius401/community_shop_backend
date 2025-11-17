package com.community_shop.backend.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 阿里云OSS文件上传工具类
 * 用于处理用户头像、帖子图片、商品图片等文件的上传
 */
@Component
public class AliyunOssUtil {
    // 阿里云OSS配置参数（从配置文件读取）
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.domain}")
    private String domain;  // OSS访问域名（如：https://bucket-name.oss-cn-beijing.aliyuncs.com）

    // OSS客户端实例
    private OSS ossClient;

    // 日期格式化工具（用于生成文件存储路径）
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    // 允许上传的图片格式
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    // 单个文件最大大小（5MB）
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 初始化OSS客户端（项目启动时执行）
     */
    @PostConstruct
    public void init() {
        // 创建OSS客户端实例
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 上传图片文件到OSS
     * @param file 图片文件（MultipartFile）
     * @param folder 存储文件夹（如："avatar/"、"post/"、"product/"）
     * @return 图片访问URL
     * @throws IOException 文件处理异常
     * @throws IllegalArgumentException 不支持的文件格式或文件过大
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        // 获取文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 验证文件格式
        if (!isAllowedImageExtension(extension)) {
            throw new IllegalArgumentException("不支持的图片格式，仅支持jpg、jpeg、png、gif、bmp");
        }

        // 生成唯一文件名（避免重复）
        String fileName = generateUniqueFileName(extension);

        // 生成文件存储路径（文件夹/日期/文件名）
        String datePath = sdf.format(new Date());
        String key = folder + datePath + "/" + fileName;

        // 设置文件元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(getContentType(extension));

        // 获取文件输入流
        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到OSS
            PutObjectResult result = ossClient.putObject(bucketName, key, inputStream, metadata);

            // 返回文件访问URL
            return domain + "/" + key;
        }
    }

    /**
     * 删除OSS上的文件
     * @param fileUrl 文件访问URL
     * @return true=删除成功，false=删除失败
     */
    public boolean deleteFile(String fileUrl) {
        // 从URL中提取文件路径（key）
        if (fileUrl == null || !fileUrl.startsWith(domain)) {
            return false;
        }

        String key = fileUrl.substring(domain.length() + 1);

        // 判断文件是否存在
        if (ossClient.doesObjectExist(bucketName, key)) {
            // 删除文件
            ossClient.deleteObject(bucketName, key);
            return true;
        }
        return false;
    }

    /**
     * 生成唯一文件名
     * @param extension 文件扩展名（带点，如".jpg"）
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String extension) {
        // 格式：时间戳 + 随机数 + 扩展名
        return System.currentTimeMillis() + "_" + new Random().nextInt(1000) + extension;
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名（带点，如".jpg"），无扩展名则返回空字符串
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 判断是否为允许的图片格式
     * @param extension 文件扩展名
     * @return true=允许，false=不允许
     */
    private boolean isAllowedImageExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        for (String allowed : ALLOWED_IMAGE_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据文件扩展名获取Content-Type
     * @param extension 文件扩展名
     * @return Content-Type字符串
     */
    private String getContentType(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }
        switch (extension.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".bmp":
                return "image/bmp";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 销毁OSS客户端（项目关闭时执行）
     */
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
