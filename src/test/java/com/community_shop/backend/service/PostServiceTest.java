package com.community_shop.backend.service;

import com.community_shop.backend.convert.PostConvert;
import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.enums.code.PostStatusEnum;
import com.community_shop.backend.enums.code.UserRoleEnum;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.enums.sort.PostSortFieldEnum;
import com.community_shop.backend.enums.sort.SortDirectionEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserPostLike;
import com.community_shop.backend.dao.mapper.PostMapper;
import com.community_shop.backend.service.base.UserPostLikeService;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PostServiceTest {

    // 模拟依赖组件
    @Mock
    private PostMapper postMapper;
    @Mock
    private UserService userService;
    @Mock
    private UserPostLikeService userPostLikeService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private PostConvert postConvert;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    // 注入测试目标服务
    @InjectMocks
    private PostServiceImpl postService;

    // 测试数据
    private User testAdminUser;
    private User testNormalUser;
    private User testNewUser;
    private Post testPost;
    private PostPublishDTO testPublishDTO;
    private PostUpdateDTO testUpdateDTO;
    private PostLikeDTO testLikeDTO;
    private PostEssenceTopDTO testEssenceTopDTO;
    private PostQueryDTO testQueryDTO;
    private PageParam testPageParam;

    @BeforeEach
    void setUp() {
        // 初始化测试用户数据
        initTestUsers();
        // 初始化测试帖子数据
        initTestPost();
        // 初始化测试DTO数据
        initTestDTOs();
        // 注入MyBatis-Plus父类baseMapper
        injectBaseMapper();
        // 模拟Redis依赖行为（修复核心错误）
        mockRedisBehavior();
    }

    /**
     * 初始化测试用户数据
     */
    private void initTestUsers() {
        // 管理员用户
        testAdminUser = new User();
        testAdminUser.setUserId(1L);
        testAdminUser.setUsername("admin");
        testAdminUser.setCreditScore(100);
        testAdminUser.setRole(UserRoleEnum.ADMIN);
        testAdminUser.setCreateTime(LocalDateTime.now().minusMonths(1));
        testAdminUser.setAvatarUrl("https://admin-avatar.jpg");

        // 普通用户
        testNormalUser = new User();
        testNormalUser.setUserId(2L);
        testNormalUser.setUsername("normalUser");
        testNormalUser.setCreditScore(80);
        testNormalUser.setRole(UserRoleEnum.USER);
        testNormalUser.setCreateTime(LocalDateTime.now().minusMonths(1));
        testNormalUser.setAvatarUrl("https://normal-avatar.jpg");

        // 新用户（注册时间≤7天）
        testNewUser = new User();
        testNewUser.setUserId(3L);
        testNewUser.setUsername("newUser");
        testNewUser.setCreditScore(70);
        testNewUser.setRole(UserRoleEnum.USER);
        testNewUser.setCreateTime(LocalDateTime.now().minusDays(3));
        testNewUser.setAvatarUrl("https://new-avatar.jpg");
    }

    /**
     * 初始化测试帖子数据
     */
    private void initTestPost() {
        testPost = new Post();
        testPost.setPostId(1001L);
        testPost.setUserId(2L);
        testPost.setTitle("测试帖子标题");
        testPost.setContent("测试帖子内容，符合长度要求");
        testPost.setStatus(PostStatusEnum.NORMAL);
        testPost.setLikeCount(10);
        testPost.setPostFollowCount(5);
        testPost.setIsEssence(false);
        testPost.setIsTop(false);
        testPost.setCreateTime(LocalDateTime.now().minusDays(1));
        testPost.setUpdateTime(LocalDateTime.now().minusDays(1));
    }

    /**
     * 初始化测试DTO数据
     */
    private void initTestDTOs() {
        // 帖子发布DTO
        testPublishDTO = new PostPublishDTO();
        testPublishDTO.setTitle("新发布帖子标题");
        testPublishDTO.setContent("新发布帖子内容，长度符合要求，用于测试发布功能");
        testPublishDTO.setImageUrls("[\"https://test-image1.jpg\",\"https://test-image2.jpg\"]");

        // 帖子更新DTO
        testUpdateDTO = new PostUpdateDTO();
        testUpdateDTO.setPostId(1001L);
        testUpdateDTO.setTitle("更新后的帖子标题");
        testUpdateDTO.setContent("更新后的帖子内容，长度符合编辑要求");

        // 帖子点赞DTO
        testLikeDTO = new PostLikeDTO();
        testLikeDTO.setPostId(1001L);
        testLikeDTO.setUserId(2L);
        testLikeDTO.setIsLike(true);

        // 帖子置顶/加精DTO
        testEssenceTopDTO = new PostEssenceTopDTO();
        testEssenceTopDTO.setPostId(1001L);
        testEssenceTopDTO.setIsEssence(true);
        testEssenceTopDTO.setIsTop(true);

        // 帖子查询DTO
        testQueryDTO = new PostQueryDTO();
        testQueryDTO.setKeyword("测试");
        testQueryDTO.setStatus(PostStatusEnum.NORMAL);
        testQueryDTO.setSortField(PostSortFieldEnum.CREATE_TIME);
        testQueryDTO.setSortDir(SortDirectionEnum.DESC);
        testQueryDTO.setPageNum(1);
        testQueryDTO.setPageSize(10);

        // 分页参数DTO
        testPageParam = new PageParam();
        testPageParam.setPageNum(1);
        testPageParam.setPageSize(10);
    }

    /**
     * 注入MyBatis-Plus父类的baseMapper字段
     */
    private void injectBaseMapper() {
        try {
            // 获取ServiceImpl父类的baseMapper字段
            Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            // 为测试服务注入mock的mapper
            baseMapperField.set(postService, postMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("初始化PostService baseMapper失败", e);
        }
    }

    /**
     * 模拟Redis相关行为（修复：删除decrement的doNothing()，改用when().thenReturn()）
     */
    private void mockRedisBehavior() {
        // 1. 模拟RedisTemplate的opsForValue()返回ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 2. 模拟Redis的set操作（void方法，可用doNothing()）
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // 3. 模拟Redis的increment操作（返回Long，用when().thenReturn()）
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // 4. 模拟Redis的decrement操作（返回Long，用when().thenReturn()，修复核心错误）
        when(valueOperations.decrement(anyString())).thenReturn(1L);

        // 5. 模拟Redis的delete操作（void方法，可用doNothing()）
        doReturn( true).when(redisTemplate).delete(anyString());
        doReturn(1L).when(redisTemplate).delete(anyCollection());

        // 6. 模拟Redis的get操作（默认返回null，可在具体测试方法中覆盖）
        when(valueOperations.get(anyString())).thenReturn(null);
    }

    // ==================== 测试用例 ====================

    /**
     * 测试帖子发布功能 - 成功场景（普通用户、信用分达标）
     */
    @Test
    void testPublishPost_Success() {
        // 1. 模拟依赖行为
        when(userService.getById(2L)).thenReturn(testNormalUser);
        when(postConvert.postToPostDetailDTO(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            PostDetailDTO dto = new PostDetailDTO();
            dto.setPostId(post.getPostId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            dto.setStatus(post.getStatus());
            return dto;
        });
        when(postMapper.insert(any(Post.class))).thenReturn(1);

        // 2. 执行测试方法
        PostDetailDTO result = postService.publishPost(2L, testPublishDTO);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(testPublishDTO.getTitle(), result.getTitle());
        assertEquals(testPublishDTO.getContent(), result.getContent());
        assertEquals(PostStatusEnum.NORMAL, result.getStatus());

        // 4. 验证依赖调用
        verify(userService, times(1)).getById(2L);
        verify(postMapper, times(1)).insert(any(Post.class));
        verify(redisTemplate, times(1)).opsForValue();
    }

    /**
     * 测试帖子发布功能 - 失败场景（信用分不足）
     */
    @Test
    void testPublishPost_CreditTooLow() {
        // 1. 准备低信用分用户（信用分50 < 最低要求60）
        User lowCreditUser = new User();
        lowCreditUser.setUserId(4L);
        lowCreditUser.setUsername("lowCreditUser");
        lowCreditUser.setCreditScore(50);
        lowCreditUser.setRole(UserRoleEnum.USER);

        // 2. 模拟依赖行为
        when(userService.getById(4L)).thenReturn(lowCreditUser);

        // 3. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.publishPost(4L, testPublishDTO);
        });

        // 4. 验证结果
        assertEquals(ErrorCode.CREDIT_TOO_LOW.getCode(), exception.getCode());
        verify(postMapper, never()).insert(any(Post.class));
    }

    /**
     * 测试帖子编辑功能 - 成功场景（作者编辑自己的帖子）
     */
    @Test
    void testUpdatePost_Success_Author() {
        // 1. 准备更新后的帖子对象
        Post updatedPost = new Post();
        BeanUtils.copyProperties(testPost, updatedPost); // 复制原始属性
        updatedPost.setTitle(testUpdateDTO.getTitle()); // 更新标题
        updatedPost.setContent(testUpdateDTO.getContent()); // 更新内容
        updatedPost.setUpdateTime(LocalDateTime.now()); // 更新时间

        // 2. 模拟依赖行为（第一次返回原始帖子，第二次返回更新后帖子）
        when(postMapper.selectById(1001L)).thenReturn(testPost, updatedPost);
        when(userService.getById(2L)).thenReturn(testNormalUser);
        when(userPostLikeService.isLiked(2L, 1001L)).thenReturn(false);
        when(postConvert.postToPostDetailDTO(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            PostDetailDTO dto = new PostDetailDTO();
            dto.setPostId(post.getPostId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            return dto;
        });
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        // 3. 执行测试方法
        PostDetailDTO result = postService.updatePost(1001L, 2L, testUpdateDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(testUpdateDTO.getTitle(), result.getTitle());
        assertEquals(testUpdateDTO.getContent(), result.getContent());

        // 5. 验证依赖调用
        verify(postMapper, times(2)).selectById(1001L);
        verify(postMapper, times(1)).updateById(any(Post.class));
        verify(redisTemplate, times(1)).delete(anyString());
    }

    /**
     * 测试帖子点赞功能 - 成功场景（首次点赞）
     */
    @Test
    void testUpdateLikeStatus_Success_FirstLike() {
        // 1. 模拟依赖行为
        when(postMapper.selectById(1001L)).thenReturn(testPost);
        when(userService.getById(2L)).thenReturn(testNormalUser);
        when(userPostLikeService.isLiked(2L, 1001L)).thenReturn(null); // 未点赞
        when(valueOperations.get(anyString())).thenReturn(5); // 当日已点赞5次（未超限）
        when(postMapper.updateLikeCount(1001L, 1)).thenReturn(1);

        // 2. 执行测试方法
        Integer newLikeCount = postService.updateLikeStatus(testLikeDTO);

        // 3. 验证结果
        assertNotNull(newLikeCount);
        assertEquals(11, newLikeCount); // 原10次 + 1次

        // 4. 验证依赖调用
        verify(userPostLikeService, times(1)).save(any(UserPostLike.class));
        verify(valueOperations, times(1)).increment(anyString());
        verify(postMapper, times(1)).updateLikeCount(1001L, 1);
    }

    /**
     * 测试帖子置顶/加精功能 - 成功场景（管理员操作）
     */
    @Test
    void testSetEssenceOrTop_Success_Admin() {
        // 1. 模拟依赖行为
        when(userService.getById(1L)).thenReturn(testAdminUser);
        when(postMapper.selectById(1001L)).thenReturn(testPost);
        when(postMapper.countTopPosts()).thenReturn(4); // 当前置顶4篇（未达上限5篇）
        when(postMapper.updatePostEssenceAndTop(1001L, true, true)).thenReturn(1);

        // 2. 执行测试方法
        Boolean result = postService.setEssenceOrTop(1L, testEssenceTopDTO);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(postMapper, times(1)).countTopPosts();
        verify(postMapper, times(1)).updatePostEssenceAndTop(1001L, true, true);
        verify(redisTemplate, times(2)).delete(anyString()); // 清除帖子详情+置顶列表缓存
    }

    /**
     * 测试帖子列表查询功能 - 成功场景（从数据库查询）
     */
    @Test
    void testQueryPosts_Success_FromDb() {
        // 1. 准备测试数据
        List<Post> postList = Arrays.asList(testPost);
        PostListItemDTO listItemDTO = new PostListItemDTO();
        listItemDTO.setPostId(1001L);
        listItemDTO.setTitle(testPost.getTitle());
        listItemDTO.setLikeCount(testPost.getLikeCount());

        // 2. 模拟依赖行为
        when(postMapper.countByQuery(testQueryDTO)).thenReturn(1);
        when(postMapper.selectByQuery(testQueryDTO)).thenReturn(postList);
        when(postConvert.postToPostListItemDTO(any(Post.class))).thenReturn(listItemDTO);
        when(userService.getByIds(anyList())).thenReturn(Arrays.asList(testNormalUser));

        // 3. 执行测试方法
        PageResult<PostListItemDTO> result = postService.queryPosts(testQueryDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(1001L, result.getList().get(0).getPostId());

        // 5. 验证依赖调用
        verify(postMapper, times(1)).countByQuery(testQueryDTO);
        verify(postMapper, times(1)).selectByQuery(testQueryDTO);
    }

    /**
     * 测试按ID查询帖子 - 成功场景（修复缓存Key格式）
     */
    @Test
    void testSelectPostById_Success() {
        // 1. 模拟依赖行为（修复缓存Key：用正确的字符串拼接，而非String.format）
        String cacheKey = "post:detail:" + 1001L;
        when(valueOperations.get(cacheKey)).thenReturn(null); // 缓存未命中
        when(postMapper.selectById(1001L)).thenReturn(testPost);
        when(userService.getById(2L)).thenReturn(testNormalUser);
        when(userPostLikeService.isLiked(2L, 1001L)).thenReturn(false);
        when(postConvert.postToPostDetailDTO(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            PostDetailDTO dto = new PostDetailDTO();
            dto.setPostId(post.getPostId());
            dto.setTitle(post.getTitle());
            dto.setPublisher(new PostDetailDTO.PublisherDTO(
                    testNormalUser.getUserId(),
                    testNormalUser.getUsername(),
                    testNormalUser.getAvatarUrl(),
                    testNormalUser.getCreditScore()
            ));
            return dto;
        });

        // 2. 执行测试方法
        PostDetailDTO result = postService.selectPostById(2L, 1001L);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(1001L, result.getPostId());
        assertNotNull(result.getPublisher());
        assertEquals(2L, result.getPublisher().getUserId());

        // 4. 验证依赖调用
        verify(postMapper, times(1)).selectById(1001L);
        verify(valueOperations, times(1)).set(eq(cacheKey), any(), anyLong(), any(TimeUnit.class));
    }
}