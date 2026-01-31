package xyz.graygoo401.community.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import xyz.graygoo401.api.community.dto.follow.PostFollowDetailDTO;
import xyz.graygoo401.api.community.dto.follow.PostFollowPublishDTO;
import xyz.graygoo401.api.community.dto.follow.PostFollowQueryDTO;
import xyz.graygoo401.api.community.dto.follow.PostFollowStatusUpdateDTO;
import xyz.graygoo401.api.community.enums.PostFollowStatusEnum;
import xyz.graygoo401.api.community.enums.PostStatusEnum;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.api.user.util.UserUtil;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.community.convert.PostConvert;
import xyz.graygoo401.community.dao.entity.Post;
import xyz.graygoo401.community.dao.entity.PostFollow;
import xyz.graygoo401.community.dao.mapper.PostFollowMapper;
import xyz.graygoo401.community.service.base.PostService;
import xyz.graygoo401.community.service.impl.PostFollowServiceImpl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PostFollowServiceTest {

    // 模拟依赖组件
    @Mock
    private PostFollowMapper postFollowMapper;
    @Mock
    private PostService postService;
    @Mock
    private UserUtil userUtil;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private PostConvert postConvert;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    // 注入测试目标服务
    @InjectMocks
    private PostFollowServiceImpl postFollowService;

    // 测试数据
    private UserDTO testAdminUser;
    private UserDTO testNormalUser;
    private Post testPost;
    private PostFollow testPostFollow;
    private PostFollowPublishDTO testPublishDTO;
    private PostFollowStatusUpdateDTO testStatusUpdateDTO;
    private PostFollowQueryDTO testQueryDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        initTestUsers();
        initTestPost();
        initTestPostFollow();
        initTestDTOs();
        // 注入baseMapper
        injectBaseMapper();
        // 模拟Redis行为（同PostService，避免doNothing()错误）
        mockRedisBehavior();
    }

    /**
     * 初始化测试用户
     */
    private void initTestUsers() {
        testAdminUser = new UserDTO();
        testAdminUser.setUserId(1L);
        testAdminUser.setUsername("admin");
        testAdminUser.setRole(UserRoleEnum.ADMIN);
        testAdminUser.setAvatarUrl("https://admin-avatar.jpg");

        testNormalUser = new UserDTO();
        testNormalUser.setUserId(2L);
        testNormalUser.setUsername("normalUser");
        testNormalUser.setRole(UserRoleEnum.USER);
        testNormalUser.setAvatarUrl("https://normal-avatar.jpg");
    }

    /**
     * 初始化测试帖子
     */
    private void initTestPost() {
        testPost = new Post();
        testPost.setPostId(1001L);
        testPost.setUserId(2L);
        testPost.setStatus(PostStatusEnum.NORMAL);
    }

    /**
     * 初始化测试跟帖
     */
    private void initTestPostFollow() {
        testPostFollow = new PostFollow();
        testPostFollow.setPostFollowId(2001L);
        testPostFollow.setPostId(1001L);
        testPostFollow.setUserId(2L);
        testPostFollow.setContent("测试跟帖内容");
        testPostFollow.setStatus(PostFollowStatusEnum.NORMAL);
        testPostFollow.setCreateTime(LocalDateTime.now().minusHours(1));
    }

    /**
     * 初始化测试DTO
     */
    private void initTestDTOs() {
        testPublishDTO = new PostFollowPublishDTO();
        testPublishDTO.setPostId(1001L);
        testPublishDTO.setContent("合法的跟帖内容，长度符合要求");

        testStatusUpdateDTO = new PostFollowStatusUpdateDTO();
        testStatusUpdateDTO.setPostFollowId(2001L);
        testStatusUpdateDTO.setTargetStatus(PostFollowStatusEnum.HIDDEN);

        testQueryDTO = new PostFollowQueryDTO();
        testQueryDTO.setPostId(1001L);
        testQueryDTO.setPageNum(1);
        testQueryDTO.setPageSize(10);
    }

    /**
     * 注入MyBatis-Plus baseMapper
     */
    private void injectBaseMapper() {
        try {
            Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(postFollowService, postFollowMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("初始化PostFollowService baseMapper失败", e);
        }
    }

    /**
     * 模拟Redis行为（避免doNothing()错误）
     */
    private void mockRedisBehavior() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        doReturn(true).when(redisTemplate).delete(anyString());
        doReturn(1L).when(redisTemplate).delete(anyCollection());
        when(valueOperations.get(anyString())).thenReturn(null);
    }

    // ==================== 测试用例 ====================

    /**
     * 测试跟帖发布 - 成功场景
     */
    @Test
    void testPublishFollow_Success() {
        // 1. 模拟依赖行为
        when(postService.getById(1001L)).thenReturn(testPost);
        when(userUtil.getUserById(2L)).thenReturn(testNormalUser);
        when(postFollowMapper.insert(any(PostFollow.class))).thenReturn(1);
        when(postConvert.postFollowToPostFollowDetailDTO(any(PostFollow.class))).thenAnswer(invocation -> {
            PostFollow follow = invocation.getArgument(0);
            PostFollowDetailDTO dto = new PostFollowDetailDTO();
            dto.setPostFollowId(follow.getPostFollowId());
            dto.setContent(follow.getContent());
            dto.setStatus(follow.getStatus());
            return dto;
        });

        // 2. 执行测试
        PostFollowDetailDTO result = postFollowService.publishFollow(2L, testPublishDTO);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(testPublishDTO.getContent(), result.getContent());
        assertEquals(PostFollowStatusEnum.NORMAL, result.getStatus());
        assertNotNull(result.getFollower());
        assertEquals(2L, result.getFollower().getUserId());

        // 4. 验证依赖调用
        verify(postService, times(1)).getById(1001L);
        verify(postFollowMapper, times(1)).insert(any(PostFollow.class));
    }

    /**
     * 测试跟帖状态更新 - 成功场景（管理员操作）
     */
    @Test
    void testUpdateFollowStatus_Success_Admin() {
        // 1. 模拟依赖行为
        when(userUtil.getUserById(1L)).thenReturn(testAdminUser);
        when(postFollowMapper.selectById(2001L)).thenReturn(testPostFollow);
        when(postFollowMapper.updateById(any(PostFollow.class))).thenReturn(1);

        // 2. 执行测试
        Boolean result = postFollowService.updateFollowStatus(1L, testStatusUpdateDTO);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(userUtil, times(1)).getUserById(1L);
        verify(postFollowMapper, times(1)).updateById(any(PostFollow.class));
    }

    /**
     * 测试跟帖删除 - 成功场景（作者删除自己的跟帖）
     */
    @Test
    void testDeletePostFollowById_Success_Author() {
        // 1. 模拟依赖行为
        when(postFollowMapper.selectById(2001L)).thenReturn(testPostFollow);
        when(userUtil.verifyRole(2L, UserRoleEnum.ADMIN)).thenReturn(false); // 非管理员
        when(postFollowMapper.updateStatus(2001L, PostFollowStatusEnum.DELETED)).thenReturn(1);

        // 2. 执行测试
        Boolean result = postFollowService.deletePostFollowById(2L, 2001L);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(postFollowMapper, times(1)).updateStatus(2001L, PostFollowStatusEnum.DELETED);
        verify(redisTemplate, times(2)).delete(anyString()); // 清除跟帖+帖子跟帖列表缓存
    }
}