package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.AdminRequired;
import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.config.MinioConfig;
import com.community_shop.backend.enums.code.OssModuleEnum;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.utils.MinioUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/v1/oss")
public class OssController {

    private final MinioUtil minioUtil;
    private final MinioConfig minioConfig;

    @Autowired
    public OssController(MinioUtil minioUtil, MinioConfig minioConfig) {
        this.minioUtil = minioUtil;
        this.minioConfig = minioConfig;
    }

    /**
     * 上传图片类型文件接口
     * @param file   文件
     * @param module 文件所属的模块名（如 "PICTURE_AVATAR" 头像、"PICTURE_PRODUCT" 商品图）
     */
    @PostMapping("/upload/image")
    @LoginRequired
    @Operation(
            summary = "单一图片上传接口",
            description = "上传单一图片文件，并返回图片的逻辑路径（存储桶内文件的「逻辑路径」，相对于存储桶根目录）"
    )
    public ResultVO<?> uploadImage(
            @RequestParam @Parameter(description = "上传的文件")
            MultipartFile file,
            @RequestParam @Parameter(description = "文件所属的模块名")
            OssModuleEnum module
            ) {
        try {
            // 0. 可检验用户是否有正常操作的权限
            // 1. 上传图片
            String filePath = minioUtil.uploadImage(file, module);
            return ResultVO.success(filePath);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    /**
     * 批量上传图片类型文件接口
     * @param files  文件列表
     * @param module 文件所属的模块名
     */
    @PostMapping("/upload/images")
    @LoginRequired
    @Operation(
            summary = "批量图片上传接口",
            description = "上传图片文件列表，并返回图片的逻辑路径列表（存储桶内文件的「逻辑路径」，相对于存储桶根目录）"
    )
    public ResultVO<?> batchUploadImages(
            @RequestParam @Parameter(description = "上传的文件列表")
            List<MultipartFile> files,
            @RequestParam @Parameter(description = "文件所属的模块名")
            OssModuleEnum module
    ) {
        try {
            // 0. 可检验用户是否有正常操作的权限
            // 1. 批量上传图片
            List<String> filePaths = minioUtil.uploadImages(files, module);
            return ResultVO.success(filePaths);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    /**
     * 上传文件通用接口
     * @param file  文件
     * @param module 文件所属的模块名
     */
    @PostMapping("/upload")
    @LoginRequired
    @Operation(
            summary = "单一文件上传接口",
            description = "上传文件，并返回文件的逻辑路径（存储桶内文件的「逻辑路径」，相对于存储桶根目录）"
    )
    public ResultVO<String> uploadFile(
            @RequestParam @Parameter(description = "上传的文件")
            MultipartFile file,
            @RequestParam @Parameter(description = "文件所属的模块名")
            OssModuleEnum module) {
        try {
            // 0. 可检验用户是否有正常操作的权限
            String filePath = minioUtil.uploadFile(file, module);
            return ResultVO.success(filePath);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    /**
     * 批量上传文件通用接口
     * @param files  文件列表
     * @param module 文件所属的模块名
     */
    @PostMapping("/upload/batch")
    @LoginRequired
    @Operation(
            summary = "批量文件上传接口",
            description = "上传文件列表，并返回文件的逻辑路径列表（存储桶内文件的「逻辑路径」，相对于存储桶根目录）"
    )
    public ResultVO<List<String>> batchUploadFiles(
            @RequestParam @Parameter(description = "上传的文件列表")
            List<MultipartFile> files,
            @RequestParam @Parameter(description = "文件所属的模块名")
            OssModuleEnum module) {
        try {
            // 0. 可检验用户是否有正常操作的权限
            // 1. 批量上传文件
            List<String> filePaths = minioUtil.uploadFiles(files, module);
            return ResultVO.success(filePaths);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    /**
     * 文件浏览器下载接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     * @param response   HttpServletResponse（浏览器下载）
     */
    @GetMapping("/download/{objectName}")
    @LoginRequired
    @Operation(
            summary ="文件浏览器下载接口",
            description = "下载默认存储桶内指定文件，并返回文件内容（浏览器下载）"
    )
    public void downloadFile(
            @PathVariable @Parameter(description = "存储对象名称")
            String objectName,
            HttpServletResponse response) {
        try {
            minioUtil.downloadToResponse(objectName, response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * 文件预览接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     */
    @GetMapping("/preview/{objectName}")
    @LoginRequired
    @Operation(
            summary = "文件预览接口",
            description = "预览默认存储桶内指定文件，并返回文件预览地址"
    )
    public ResultVO<String> previewFile(
            @PathVariable @Parameter(description = "存储对象名称")
            String objectName) {
        try {
            String url = minioUtil.generatePresignedUrl(objectName);
            return ResultVO.success(url);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE, e.getMessage());
        }
    }

    /**
     * 文件删除接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     */
    @DeleteMapping("/delete/{objectName}")
    @AdminRequired
    @Operation(
            summary = "文件删除接口",
            description = "删除默认存储桶内指定文件"
    )
    public ResultVO<Void> deleteFile(
            @PathVariable @Parameter(description = "存储对象名称")
            String objectName) {
        try {
            minioUtil.deleteFile(objectName);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    /**
     * 获取文件列表接口
     * @param bucketName 存储桶名称
     */
    @GetMapping("/list")
    @AdminRequired
    @Operation(
            summary ="获取文件列表接口",
            description = "获取指定存储桶内的所有文件列表，并返回文件列表（存储桶内文件的「逻辑路径」，相对于存储桶根目录）"
    )
    public ResultVO<List<String>> listFiles(
            @RequestParam(required = false) @Parameter(description = "存储桶名称")
            String bucketName) {
        try {
            String targetBucket = (bucketName != null && !bucketName.isEmpty())
                    ? bucketName : minioConfig.getBucketName();
            List<String> files = minioUtil.listAllFiles(targetBucket);
            return ResultVO.success(files);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

}
