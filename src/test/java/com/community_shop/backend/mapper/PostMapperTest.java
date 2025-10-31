package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.enums.CodeEnum.PostStatusEnum;
import com.community_shop.backend.dto.post.PostQueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostMapper单元测试
 * 适配文档：
 * 1. 《代码文档1 Mapper层设计.docx》2.2节 PostMapper接口规范
 * 2. 《代码文档0 实体类设计.docx》2.9节 Post实体属性与枚举依赖
 * 3. 《中间件文档3 自定义枚举类设计.docx》枚举TypeHandler自动转换
 * 4. 《测试文档1 基础SQL脚本设计.docx》POST模块初始化数据
 */
@MybatisPlusTest  // 仅加载MyBatis相关Bean，轻量化测试
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 禁用默认数据库替换，使用H2配置
@ActiveProfiles("test")  // 启用test环境配置（加载application-test.properties）
public class PostMapperTest {

    @Autowired
    private PostMapper postMapper;  // 注入待测试的PostMapper

    // 测试复用的基础数据（从data-post.sql初始化数据中获取）
    private Post topEssencePost;    // 置顶精华帖（postId=1，userId=3，status=NORMAL）
    private Post normalUserPost;    // 普通用户帖子（postId=2，userId=1，status=NORMAL）
    private Post offShelfPost;      // 下架帖子（postId=3，userId=1，status=HIDDEN）

    /**
     * 测试前初始化：从数据库查询基础测试帖子，确保与data-post.sql数据一致
     * 适配《代码文档0》中Post实体的枚举属性（status）与业务属性（isTop/isEssence/isHot）
     */
    @BeforeEach
    void setUp() {
        // 按postId查询（基于BaseMapper的selectById方法）
        topEssencePost = postMapper.selectById(1L);
        normalUserPost = postMapper.selectById(2L);
        offShelfPost = postMapper.selectById(3L);

        // 断言初始化成功（确保POST模块SQL脚本已正确执行）
        assertNotNull(topEssencePost, "初始化失败：未查询到置顶精华帖（data-post.sql中postId=1）");
        assertNotNull(normalUserPost, "初始化失败：未查询到普通用户帖子（data-post.sql中postId=2）");
        assertNotNull(offShelfPost, "初始化失败：未查询到下架帖子（data-post.sql中postId=3）");
    }

    /**
     * 测试selectTopPosts：查询置顶帖子（正常场景）
     * 适配《代码文档1》2.2.2节 多维度列表查询 - selectTopPosts方法
     */
    @Test
    void selectTopPosts_validLimit_returnsTopPostList() {
        // 1. 执行测试方法（查询前10条置顶帖子）
        List<Post> topPostList = postMapper.selectTopPosts(10);

        // 2. 断言结果（匹配data-post.sql中置顶帖数据）
        assertNotNull(topPostList);
        assertEquals(1, topPostList.size(), "置顶帖子数量应为1（data-post.sql中仅postId=1为置顶）");

        Post resultPost = topPostList.get(0);
        assertEquals(topEssencePost.getPostId(), resultPost.getPostId());
        assertTrue(resultPost.getIsTop(), "查询结果应为置顶帖（isTop=true）");
        assertTrue(resultPost.getIsEssence(), "置顶帖同时为精华帖（isEssence=true）");
        assertEquals(PostStatusEnum.NORMAL, resultPost.getStatus(), "置顶帖状态应为NORMAL");
    }

    /**
     * 测试selectByUserId：按用户ID查询帖子（正常场景）
     * 适配《代码文档1》2.2.2节 多维度列表查询 - selectByUserId方法
     */
    @Test
    void selectByUserId_existUserId_returnsUserPostList() {
        // 1. 执行测试方法（查询test_buyer（userId=1）的帖子，分页：offset=0，limit=10）
        List<Post> userPostList = postMapper.selectByUserId(1L, 0, 10);

        // 2. 断言结果（data-post.sql中userId=1有2个帖子：postId=2和postId=3）
        assertNotNull(userPostList);
        assertEquals(2, userPostList.size(), "用户userId=1应拥有2个帖子");

        // 验证帖子ID匹配
        boolean hasNormalPost = userPostList.stream().anyMatch(p -> p.getPostId().equals(2L));
        boolean hasOffShelfPost = userPostList.stream().anyMatch(p -> p.getPostId().equals(3L));
        assertTrue(hasNormalPost, "查询结果应包含普通帖子（postId=2）");
        assertTrue(hasOffShelfPost, "查询结果应包含下架帖子（postId=3）");
    }

    /**
     * 测试selectByUserId：查询无帖子的用户（异常场景）
     */
    @Test
    void selectByUserId_nonPostUserId_returnsEmptyList() {
        // 1. 执行测试方法（查询test_seller（userId=2）的帖子，该用户无发帖记录）
        List<Post> emptyPostList = postMapper.selectByUserId(2L, 0, 10);

        // 2. 断言结果（无帖子用户应返回空列表）
        assertNotNull(emptyPostList);
        assertTrue(emptyPostList.isEmpty(), "无发帖记录的用户应返回空列表");
    }

    /**
     * 测试updatePostStatus：更新帖子状态为下架（枚举参数，正常场景）
     * 适配《代码文档1》2.2.2节 基础操作与运营管理 - updatePostStatus方法
     * 适配《中间件文档3》枚举TypeHandler自动转换（枚举→数据库code）
     */
    @Test
    void updatePostStatus_changeToOffShelf_returnsAffectedRows1() {
        // 1. 准备参数（将普通帖子postId=2的状态从NORMAL改为HIDDEN）
        Long postId = 2L;
        PostStatusEnum newStatus = PostStatusEnum.HIDDEN;

        // 2. 执行更新方法（直接传递枚举对象，TypeHandler自动转换为"HIDDEN"）
        int affectedRows = postMapper.updatePostStatus(postId, newStatus.name());

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新帖子状态应影响1行数据");

        // 4. 验证状态已更新（查询结果自动转换为枚举）
        Post updatedPost = postMapper.selectById(postId);
        assertEquals(newStatus, updatedPost.getStatus(), "帖子状态未更新为HIDDEN");
    }

    /**
     * 测试updateLikeCount：更新帖子点赞数（正常场景，加分）
     * 适配《代码文档1》2.2.2节 互动数据更新与统计 - updateLikeCount方法
     */
    @Test
    void updateLikeCount_addCount_returnsAffectedRows1() {
        // 1. 准备参数（普通帖子postId=2当前点赞数30，更新为40）
        Long postId = 2L;
        Integer newLikeCount = 40;

        // 2. 执行更新方法
        int affectedRows = postMapper.updateLikeCount(postId, newLikeCount);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新点赞数应影响1行数据");

        // 4. 验证点赞数已更新
        Post updatedPost = postMapper.selectById(postId);
        assertEquals(newLikeCount, updatedPost.getLikeCount(), "帖子点赞数未更新为40");
    }

    /**
     * 测试updatePostFollowCount：更新帖子跟帖数（正常场景，加分）
     * 适配《代码文档1》2.2.2节 互动数据更新与统计 - updatePostFollowCount方法
     */
    @Test
    void updatePostFollowCount_addCount_returnsAffectedRows1() {
        // 1. 准备参数（置顶帖postId=1当前跟帖数20，更新为25）
        Long postId = 1L;
        Integer newFollowCount = 25;

        // 2. 执行更新方法
        int affectedRows = postMapper.updatePostFollowCount(postId, newFollowCount);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新跟帖数应影响1行数据");

        // 4. 验证跟帖数已更新
        Post updatedPost = postMapper.selectById(postId);
        assertEquals(newFollowCount, updatedPost.getPostFollowCount(), "帖子跟帖数未更新为25");
    }

    /**
     * 测试countByQuery：统计复杂条件下的帖子总数（正常场景）
     * 适配《代码文档1》2.2.2节 多维度列表查询 - countByQuery方法
     */
    @Test
    void countByQuery_complexCondition_returnsCorrectCount() {
        // 1. 构建查询DTO（筛选：isEssence=true，status=NORMAL）
        PostQueryDTO queryDTO = new PostQueryDTO();
        queryDTO.setIsEssence(true);
        queryDTO.setStatus(PostStatusEnum.NORMAL);

        // 2. 执行统计方法
        int postCount = postMapper.countByQuery(queryDTO);

        // 3. 断言结果（data-post.sql中仅postId=1为精华帖且状态为NORMAL，总数应为1）
        assertEquals(1, postCount, "精华且状态为NORMAL的帖子总数应为1");
    }

    /**
     * 测试batchUpdateStatus：批量更新帖子状态（正常场景）
     * 适配《代码文档1》2.2.2节 基础操作与运营管理 - batchUpdateStatus方法
     */
    @Test
    void batchUpdateStatus_validPostIds_returnsAffectedRows2() {
        // 1. 准备参数（批量将postId=2和postId=3的状态改为HIDDEN）
        List<Long> postIds = List.of(2L, 3L);
        PostStatusEnum targetStatus = PostStatusEnum.HIDDEN;

        // 2. 执行批量更新方法
        int affectedRows = postMapper.batchUpdateStatus(postIds, targetStatus);

        // 3. 断言更新行数
        assertEquals(2, affectedRows, "批量更新应影响2行数据（2个帖子）");

        // 4. 验证批量更新结果
        Post post2 = postMapper.selectById(2L);
        Post post3 = postMapper.selectById(3L);
        assertEquals(PostStatusEnum.HIDDEN, post2.getStatus(), "postId=2状态应为HIDDEN");
        assertEquals(PostStatusEnum.HIDDEN, post3.getStatus(), "postId=3状态应为HIDDEN");
    }
}