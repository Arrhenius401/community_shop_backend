package com.community_shop.backend.service.impl;

import com.community_shop.backend.dao.mapper.FileMapper;
import com.community_shop.backend.entity.File;
import com.community_shop.backend.enums.code.OssModuleEnum;
import com.community_shop.backend.enums.code.UserRoleEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.service.base.FileService;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.utils.MinioUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
public class FileServiceImpl extends BaseServiceImpl<FileMapper, File> implements FileService {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private UserService userService;

    /**
     * 上传图片类型文件接口
     * @param file   文件
     * @param module 文件所属的模块名（如 "PICTURE_AVATAR" 头像、"PICTURE_PRODUCT" 商品图）
     * @param userId  用户ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String uploadImage(MultipartFile file, OssModuleEnum module, Long userId) {
        try {
            // 1. 检验用户是否有正常操作的权限
            if (userService.getById(userId) == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 2. 上传图片
            String filePath = minioUtil.uploadImage(file, module);
            File fileEntity = createFile(file, filePath, userId);

            // 3. 保存文件信息至数据库
            int insertCount = fileMapper.insert(fileEntity);
            if (insertCount <= 0) {
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            return filePath;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传文件失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_MULTIPART_FILE_UPLOAD_FAILS);
        }
    }

    /**
     * 批量上传图片类型文件接口
     * @param files  文件列表
     * @param module 文件所属的模块名
     * @param userId  用户ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<String> batchUploadImages(List<MultipartFile> files, OssModuleEnum module, Long userId) {
        try {
            // 1. 检验用户是否有正常操作的权限
            if (userService.getById(userId) == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 2. 上传图片
            List<String> filePaths = minioUtil.uploadImages(files, module);
            List<File> fileEntities = createFileList(files, filePaths, userId);

            // 3. 保存文件信息至数据库
            for (File fileEntity : fileEntities) {
                int insertCount = fileMapper.insert(fileEntity);
                if (insertCount <= 0) {
                    throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
                }
            }

            return filePaths;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传文件失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_MULTIPART_FILE_UPLOAD_FAILS);
        }
    }

    /**
     * 上传文件通用接口
     * @param file  文件
     * @param module 文件所属的模块名
     * @param userId  用户ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file, OssModuleEnum module, Long userId) {
        try {
            // 1. 检验用户是否有正常操作的权限
            if (userService.getById(userId) == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 2. 上传图片
            String filePath = minioUtil.uploadFile(file, module);
            File fileEntity = createFile(file, filePath, userId);

            // 3. 保存文件信息至数据库
            int insertCount = fileMapper.insert(fileEntity);
            if (insertCount <= 0) {
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            return filePath;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传文件失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_MULTIPART_FILE_UPLOAD_FAILS);
        }
    }

    /**
     * 批量上传文件通用接口
     * @param files  文件列表
     * @param module 文件所属的模块名
     * @param userId  用户ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<String> batchUploadFiles(List<MultipartFile> files, OssModuleEnum module, Long userId) {
        try {
            // 1. 检验用户是否有正常操作的权限
            if (userService.getById(userId) == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 2. 上传图片
            List<String> filePaths = minioUtil.uploadFiles(files, module);
            List<File> fileEntities = createFileList(files, filePaths, userId);

            // 3. 保存文件信息至数据库
            for (File fileEntity : fileEntities) {
                int insertCount = fileMapper.insert(fileEntity);
                if (insertCount <= 0) {
                    throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
                }
            }

            return filePaths;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传文件失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_MULTIPART_FILE_UPLOAD_FAILS);
        }
    }

    /**
     * 文件浏览器下载接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     * @param response   HttpServletResponse（浏览器下载）
     */
    @Override
    public void downloadToResponse(String objectName, HttpServletResponse response) {
        try {
            minioUtil.downloadToResponse(objectName, response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件下载失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_FILE_DOWNLOAD_FAILS);
        }

    }

    /**
     * 文件预览接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     */
    @Override
    public String generatePresignedUrl(String objectName) {
        try {
            return minioUtil.generatePresignedUrl(objectName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件预览失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_PRESIGNED_URL_GENERATE_FAILS);
        }
    }

    /**
     * 文件删除接口
     * @param objectName 存储对象名称（存储桶内文件的「逻辑路径」，相对于存储桶根目录）
     * @param userId  用户ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteFile(String objectName, Long userId) {
        try {
            // 1. 检验用户是否有正常操作的权限
            if (userService.getById(userId) == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 2. 删除文件
            minioUtil.deleteFile(objectName);

            // 3. 删除文件信息
            // 清理文件路径
            if (objectName.startsWith("/")) {
                objectName = objectName.substring(1);
            }

            int deleteCount = fileMapper.deleteByFilePath(objectName);
            if (deleteCount <= 0) {
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件删除失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_FILE_DELETE_FAILS);
        }
    }

    /**
     * 获取文件列表接口
     * @param bucketName 存储桶名称
     */
    @Override
    public List<String> listAllFiles(String bucketName, Long userId) {
        try {
            // 1. 检验用户是否有正常操作的权限
            if (!userService.verifyRole(userId, UserRoleEnum.ADMIN)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 2. 获取文件列表
            return minioUtil.listAllFiles(bucketName);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取文件列表失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_SERVICE_FAILS);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 安全获取文件后缀名（统一转为小写，无后缀返回空字符串）
     * @param originalFilename 原始文件名
     * @return 小写的文件后缀（无后缀返回""）
     */
    private String getFileSuffix(String originalFilename) {
        // 1. 非空校验
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        // 2. 找到最后一个点的位置
        int lastDotIndex = originalFilename.lastIndexOf(".");
        // 3. 边界判断：点不能是最后一个字符，且不能是第一个字符（避免".gitignore"被误判）
        if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
            return "";
        }
        // 4. 截取后缀并转为小写，统一格式
        String suffix = originalFilename.substring(lastDotIndex + 1);
        return suffix.toLowerCase();
    }

    /**
     * 创建系统原生文件信息
     * @param file 文件
     * @param filePath 文件路径
     * @param userId 用户ID
     * @return 系统原生文件信息
     */
    private File createFile(MultipartFile file, String filePath, Long userId) {
        File fileEntity = new File();

        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFilePath(filePath);
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileType(file.getContentType());

        // 完善后缀名获取逻辑：安全获取+统一小写
        String suffix = getFileSuffix(file.getOriginalFilename());
        fileEntity.setSuffix(suffix);

        fileEntity.setUserId(userId);
        fileEntity.setCreateTime(LocalDateTime.now());
        fileEntity.setUpdateTime(LocalDateTime.now());
        fileEntity.setIsDelete(false);

        return fileEntity;
    }

    /**
     * 创建系统原生文件列表
     * @param files 文件列表
     * @param filePaths 文件路径列表
     * @param userId 用户ID
     * @return 系统原生文件列表
     */
    private List<File> createFileList(List<MultipartFile> files, List<String> filePaths, Long userId) {
        List<File> fileList = new ArrayList<>();

        // 1. 检验参数
        if (files == null || files.size() <= 0) {
            return fileList;
        }

        if (files.size() != filePaths.size()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 2. 创建文件列表
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String filePath = filePaths.get(i);

            File fileEntity = createFile(file, filePath, userId);
            fileList.add(fileEntity);
        }

        return fileList;
    }
}
