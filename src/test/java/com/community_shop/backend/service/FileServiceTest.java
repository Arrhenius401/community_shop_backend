package com.community_shop.backend.service;

import com.community_shop.backend.dao.mapper.FileMapper;
import com.community_shop.backend.entity.File;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.enums.code.OssModuleEnum;
import com.community_shop.backend.enums.code.UserRoleEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.service.impl.FileServiceImpl;
import com.community_shop.backend.utils.MinioUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FileServiceTest {

    @Mock
    private MinioUtil minioUtil;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private FileServiceImpl fileService;

    // 测试数据
    private User testUser;
    private MockMultipartFile mockImageFile;
    private MockMultipartFile mockDocFile;
    private String uploadedImagePath;
    private String uploadedDocPath;

    @BeforeEach
    void setUp() {
        // 初始化用户
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");
        testUser.setRole(UserRoleEnum.USER);

        // 初始化模拟文件
        mockImageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );
        mockDocFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "fake pdf content".getBytes()
        );

        // 模拟上传返回路径
        uploadedImagePath = "PICTURE_AVATAR/2026-01-20/abc123.jpg";
        uploadedDocPath = "DEFAULT/2026-01-20/def456.pdf";

        // 模拟依赖行为
        when(userService.getById(1L)).thenReturn(testUser);
        when(minioUtil.uploadImage(eq(mockImageFile), eq(OssModuleEnum.PICTURE_AVATAR))).thenReturn(uploadedImagePath);
        when(minioUtil.uploadFile(eq(mockDocFile), eq(OssModuleEnum.DEFAULT))).thenReturn(uploadedDocPath);
        when(fileMapper.insert(any(File.class))).thenReturn(1);
        doNothing().when(minioUtil).deleteFile(anyString());
        when(fileMapper.deleteById(anyString())).thenReturn(1);
        when(minioUtil.generatePresignedUrl(anyString())).thenReturn("https://minio.example.com/presigned-url");
        doNothing().when(minioUtil).downloadToResponse(anyString(), any(HttpServletResponse.class));
    }

    // ==================== 测试 getFileSuffix (私有方法) ====================
    @Test
    void testGetFileSuffix() throws Exception {
        Method method = FileServiceImpl.class.getDeclaredMethod("getFileSuffix", String.class);
        method.setAccessible(true);

        assertEquals("jpg", method.invoke(fileService, "photo.jpg"));
        assertEquals("jpeg", method.invoke(fileService, "photo.jpeg"));
        assertEquals("", method.invoke(fileService, "README"));       // 无后缀
        assertEquals("", method.invoke(fileService, "file."));        // 以点结尾
        assertEquals("gitignore", method.invoke(fileService, ".gitignore")); // 隐藏文件
        assertEquals("", method.invoke(fileService, ""));             // 空字符串
        assertEquals("", method.invoke(fileService, (String)null));           // null
    }

    // ==================== 测试 uploadImage ====================
    @Test
    void testUploadImage_Success() {
        String result = fileService.uploadImage(mockImageFile, OssModuleEnum.PICTURE_AVATAR, 1L);

        assertNotNull(result);
        assertEquals(uploadedImagePath, result);

        verify(userService, times(1)).getById(1L);
        verify(minioUtil, times(1)).uploadImage(mockImageFile, OssModuleEnum.PICTURE_AVATAR);
        verify(fileMapper, times(1)).insert(any(File.class));

        // 验证 File 实体内容
        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(fileMapper).insert(fileCaptor.capture());
        File captured = fileCaptor.getValue();
        assertEquals("test.jpg", captured.getFileName());
        assertEquals(uploadedImagePath, captured.getFilePath());
        assertEquals("jpg", captured.getSuffix());
        assertEquals("image/jpeg", captured.getFileType());
        assertEquals(Long.valueOf(18), captured.getFileSize()); // "fake image content".length()
        assertEquals(Long.valueOf(1L), captured.getUserId());
        assertNotNull(captured.getCreateTime());
    }

    @Test
    void testUploadImage_UserNotFound() {
        when(userService.getById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.uploadImage(mockImageFile, OssModuleEnum.PICTURE_AVATAR, 999L);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTS, exception.getErrorCode());
        verify(minioUtil, never()).uploadImage(any(), any());
        verify(fileMapper, never()).insert(any());
    }

    @Test
    void testUploadImage_InsertFailed() {
        when(fileMapper.insert(any(File.class))).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.uploadImage(mockImageFile, OssModuleEnum.PICTURE_AVATAR, 1L);
        });

        assertEquals(ErrorCode.DATA_INSERT_FAILED, exception.getErrorCode());
    }

    // ==================== 测试 batchUploadImages ====================
    @Test
    void testBatchUploadImages_Success() {
        List<MultipartFile> files = Arrays.asList(mockImageFile, mockImageFile);
        List<String> expectedPaths = Arrays.asList(uploadedImagePath, uploadedImagePath);

        when(minioUtil.uploadImages(eq(files), eq(OssModuleEnum.PICTURE_AVATAR))).thenReturn(expectedPaths);

        List<String> result = fileService.batchUploadImages(files, OssModuleEnum.PICTURE_AVATAR, 1L);

        assertEquals(expectedPaths, result);
        verify(fileMapper, times(2)).insert(any(File.class));
    }

    // ==================== 测试 uploadFile ====================
    @Test
    void testUploadFile_Success() {
        String result = fileService.uploadFile(mockDocFile, OssModuleEnum.DEFAULT, 1L);
        assertEquals(uploadedDocPath, result);
        verify(minioUtil).uploadFile(mockDocFile, OssModuleEnum.DEFAULT);
    }

    // ==================== 测试 deleteFile ====================
    @Test
    void testDeleteFile_Success() {
        fileService.deleteFile(uploadedImagePath, 1L);

        verify(minioUtil).deleteFile(uploadedImagePath);
        verify(fileMapper).deleteById(uploadedImagePath);
    }

    @Test
    void testDeleteFile_UserNotFound() {
        when(userService.getById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.deleteFile("any-path", 999L);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTS, exception.getErrorCode());
        verify(minioUtil, never()).deleteFile(anyString());
        verify(fileMapper, never()).deleteById(anyString());
    }

    @Test
    void testDeleteFile_DeleteDbFailed() {
        when(fileMapper.deleteById(anyString())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.deleteFile(uploadedImagePath, 1L);
        });

        assertEquals(ErrorCode.DATA_DELETE_FAILED, exception.getErrorCode());
        // 注意：MinIO 已删除，但 DB 回滚不了（实际生产中需考虑事务或补偿）
        verify(minioUtil).deleteFile(uploadedImagePath);
    }

    // ==================== 测试 generatePresignedUrl ====================
    @Test
    void testGeneratePresignedUrl() {
        String url = fileService.generatePresignedUrl("some/path/file.jpg");
        assertEquals("https://minio.example.com/presigned-url", url);
        verify(minioUtil).generatePresignedUrl("some/path/file.jpg");
    }

    // ==================== 测试 downloadToResponse ====================
    @Test
    void testDownloadToResponse() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        fileService.downloadToResponse("some/path/file.pdf", response);

        verify(minioUtil).downloadToResponse("some/path/file.pdf", response);
    }
}