package com.community_shop.backend.service.base;

import com.community_shop.backend.entity.File;
import com.community_shop.backend.enums.code.OssModuleEnum;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理Service接口
 */
@Service
public interface FileService extends BaseService<File> {

    /**
     * 上传图片类型文件接口
     * @param file   文件
     * @param module 文件所属的模块名（如 "PICTURE_AVATAR" 头像、"PICTURE_PRODUCT" 商品图）
     * @param userId  用户ID
     */
    String uploadImage(MultipartFile file, OssModuleEnum module, Long userId);

    /**
     * 批量上传图片类型文件接口
     * @param files  文件列表
     * @param module 文件所属的模块名
     * @param userId  用户ID
     */
    List<String> batchUploadImages(List<MultipartFile> files, OssModuleEnum module, Long userId);

    /**
     * 上传文件通用接口
     * @param file  文件
     * @param module 文件所属的模块名
     * @param userId  用户ID
     */
    String uploadFile(MultipartFile file, OssModuleEnum module, Long userId);

    /**
     * 批量上传文件通用接口
     * @param files  文件列表
     * @param module 文件所属的模块名
     * @param userId  用户ID
     */
    List<String> batchUploadFiles(List<MultipartFile> files, OssModuleEnum module, Long userId);

    /**
     * 文件浏览器下载接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     * @param response   HttpServletResponse（浏览器下载）
     */
    void downloadToResponse(String objectName, HttpServletResponse response);

    /**
     * 文件预览接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     */
    String generatePresignedUrl(String objectName);

    /**
     * 文件删除接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     * @param userId  用户ID
     */
    void deleteFile(String objectName, Long userId);

    /**
     * 获取文件列表接口
     * @param bucketName 存储桶名称
     * @param userId  用户ID
     */
    List<String> listAllFiles(String bucketName, Long userId);
}
