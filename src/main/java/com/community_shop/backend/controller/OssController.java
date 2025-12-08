package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.config.MinioConfig;
import com.community_shop.backend.enums.code.OssModuleEnum;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.utils.MinioUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
     * 上传商品图片（和原来的逻辑完全一致）
     */
    @PostMapping("/upload")
    @LoginRequired // 你的登录校验注解
    public ResultVO<?> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam OssModuleEnum module
            ) {
        // 1. 校验卖家信用分（你的业务逻辑）

        // 2. 批量上传图片
        List<String> filePaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String filePath = minioUtil.uploadImage(file, module); // 模块名传 "product"
                filePaths.add(filePath);
            }
        }
        return ResultVO.success(filePaths);
    }

    // ====================

//    @Operation(summary = "文件上传")
//    @PostMapping("/upload")
//    public ResultVO<String> uploadFile(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "objectName", required = false) String objectName,
//            @RequestParam String ) {
//        try {
//            if (file.isEmpty()) {
//                return ResultVO.fail(ErrorCode.OSS_FILE_NOT_EXISTS, "上传文件不能为空");
//            }
//            String url = minioUtil.uploadFile(file, objectName);
//            return ResultVO.success(url);
//        } catch (Exception e) {
//            return ResultVO.fail(ErrorCode.FAILURE);
//        }
//    }

    @Operation(summary ="文件下载")
    @GetMapping("/download/{objectName}")
    public void downloadFile(@PathVariable String objectName, HttpServletResponse response) {
        try {
            minioUtil.downloadToResponse(objectName, response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

//    @Operation(summary ="文件预览（7天有效期）")
//    @GetMapping("/preview/{objectName}")
//    public ResultVO<String> previewFile(@PathVariable String objectName) {
//        try {
//            String url = minioUtil.previewImage(objectName);
//            return ResultVO.success(url);
//        } catch (Exception e) {
//            return ResultVO.fail(ErrorCode.FAILURE, e.getMessage());
//        }
//    }

    @Operation(summary ="删除文件")
    @DeleteMapping("/delete/{objectName}")
    public ResultVO<Void> deleteFile(@PathVariable String objectName) {
        try {
            minioUtil.deleteFile(objectName);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    @Operation(summary ="获取文件列表")
    @GetMapping("/list")
    public ResultVO<List<String>> listFiles(@RequestParam(required = false) String bucketName) {
        try {
            String targetBucket = (bucketName != null && !bucketName.isEmpty())
                    ? bucketName : minioConfig.getBucketName();
            List<String> files = minioUtil.listAllFiles(targetBucket);
            return ResultVO.success(files);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    @Operation(summary ="生成临时访问URL")
    @GetMapping("/presigned-url")
    public ResultVO<String> getPresignedUrl(@RequestParam String objectName,
                                     @RequestParam(defaultValue = "3600") int expiry) {
        try {
            String url = minioUtil.generatePresignedUrl(objectName);
            return ResultVO.success(url);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    // 存储桶管理相关接口
//    @Operation(summary ="创建存储桶")
//    @PostMapping("/buckets/{bucketName}")
//    public ResultVO<Void> createBucket(@PathVariable String bucketName) {
//        try {
//            boolean created = minioUtil.createBucket(bucketName);
//            return created ? ResultVO.success() : ResultVO.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(),"存储桶已存在");
//        } catch (Exception e) {
//            return ResultVO.fail(ErrorCode.FAILURE);
//        }
//    }

    @Operation(summary ="删除存储桶")
    @DeleteMapping("/buckets/{bucketName}")
    public ResultVO<Void> deleteBucket(@PathVariable String bucketName) {
        try {
            minioUtil.removeBucket(bucketName);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

    @Operation(summary ="获取所有存储桶")
    @GetMapping("/buckets")
    public ResultVO<List<io.minio.messages.Bucket>> listBuckets() {
        try {
            List<io.minio.messages.Bucket> buckets = minioUtil.listAllBuckets();
            return ResultVO.success(buckets);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }
    }

}
