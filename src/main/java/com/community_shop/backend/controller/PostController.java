package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.AdminRequired;
import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.service.base.PostService;
import com.community_shop.backend.service.base.UserPostLikeService;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子管理模块Controller，负责帖子发布、详情查询、编辑、点赞及运营管理等接口实现
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "帖子管理接口", description = "包含帖子发布、详情查询、编辑、点赞及管理员运营操作等功能")
@Validated
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserPostLikeService userPostLikeService;

    /**
     * 发布帖子接口
     * @param postPublishDTO 帖子发布参数（标题、内容、图片URL列表）
     * @return 包含帖子详情的统一响应
     */
    @PostMapping("/publish")
    @LoginRequired
    @Operation(
            summary = "发布帖子接口",
            description = "登录用户发布社区帖子，需校验信用分≥60，支持富文本内容与图片上传（最多9张）"
    )
    public ResultVO<PostDetailDTO> publishPost(
            @Valid @RequestBody
            @Parameter(description = "帖子发布参数，包含标题、内容及图片URL列表", required = true)
            PostPublishDTO postPublishDTO
    ) {
        // 从令牌解析当前登录用户ID（实际项目需结合JWT工具实现）
        Long currentUserId = parseUserIdFromToken();
        PostDetailDTO postDetail = postService.publishPost(currentUserId, postPublishDTO);
        return ResultVO.success(postDetail);
    }

    /**
     * 获取帖子详情接口
     * @param postId 帖子ID
     * @return 包含帖子完整信息的统一响应
     */
    @GetMapping("/{postId}")
    @Operation(
            summary = "获取帖子详情接口",
            description = "查询指定帖子的完整信息，包含发布者脱敏信息、点赞数及跟帖数"
    )
    public ResultVO<PostDetailDTO> getPostDetail(
            @PathVariable
            @Parameter(description = "帖子唯一标识", required = true, example = "2001")
            Long postId
    ) {
        // 未登录时currentUserId为null，Service层适配未登录场景的点赞状态判断
        Long currentUserId = parseUserIdFromToken();
        PostDetailDTO postDetail = postService.selectPostById(currentUserId, postId);
        return ResultVO.success(postDetail);
    }

    /**
     * 编辑帖子接口
     * @param postId 帖子ID
     * @param postUpdateDTO 帖子更新参数（新标题、新内容）
     * @return 包含更新后帖子详情的统一响应
     */
    @PutMapping("/{postId}")
    @LoginRequired
    @Operation(
            summary = "编辑帖子接口",
            description = "帖子作者可修改标题与内容，需校验操作用户身份与帖子状态"
    )
    public ResultVO<PostDetailDTO> updatePost(
            @PathVariable
            @Parameter(description = "待编辑帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "帖子更新参数，包含新标题与新内容", required = true)
            PostUpdateDTO postUpdateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        PostDetailDTO updatedPost = postService.updatePost(postId, currentUserId, postUpdateDTO);
        return ResultVO.success(updatedPost);
    }

    /**
     * 帖子点赞/取消点赞接口
     * @param postId 帖子ID
     * @param postLikeDTO 点赞操作参数（操作类型：点赞/取消点赞）
     * @return 包含最新点赞数的统一响应
     */
    @PatchMapping("/{postId}/like")
    @LoginRequired
    @Operation(
            summary = "帖子点赞/取消点赞接口",
            description = "登录用户对帖子进行点赞或取消点赞操作，实时返回最新点赞数"
    )
    public ResultVO<Integer> updateLikeStatus(
            @PathVariable
            @Parameter(description = "帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "点赞操作参数，指定操作类型（true-点赞，false-取消点赞）", required = true)
            PostLikeDTO postLikeDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        // 补充用户ID与帖子ID到DTO中，便于Service层处理
        postLikeDTO.setUserId(currentUserId);
        postLikeDTO.setPostId(postId);
        Integer latestLikeCount = postService.updateLikeStatus(postLikeDTO);
        return ResultVO.success(latestLikeCount);
    }

    /**
     * 帖子置顶/加精接口
     * @param postId 帖子ID
     * @param postEssenceTopDTO 运营操作参数（置顶/加精状态）
     * @return 操作结果的统一响应
     */
    @PatchMapping("/{postId}/essence-top")
    @AdminRequired
    @Operation(
            summary = "帖子置顶/加精接口",
            description = "管理员专属接口，可设置帖子为精华帖或置顶帖，置顶数量限制≤5"
    )
    public ResultVO<Boolean> setEssenceOrTop(
            @PathVariable
            @Parameter(description = "帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "运营操作参数，指定精华与置顶状态", required = true)
            PostEssenceTopDTO postEssenceTopDTO
    ) {
        Long adminId = parseUserIdFromToken();
        // 补充帖子ID到DTO中
        postEssenceTopDTO.setPostId(postId);
        Boolean result = postService.setEssenceOrTop(adminId, postEssenceTopDTO);
        return ResultVO.success(result);
    }

    /**
     * 分页查询帖子列表接口
     * @param postQueryDTO 列表查询参数（关键词、排序方式、分页信息）
     * @return 包含分页帖子列表的统一响应
     */
    @GetMapping("/list")
    @Operation(
            summary = "分页查询帖子列表接口",
            description = "支持按关键词搜索、按发布时间/点赞数排序，默认按发布时间降序分页返回"
    )
    public ResultVO<PageResult<PostListItemDTO>> queryPostList(
            @Valid @ModelAttribute
            @Parameter(description = "帖子列表查询参数，包含关键词、排序规则及分页信息")
            PostQueryDTO postQueryDTO
    ) {
        PageResult<PostListItemDTO> postPage = postService.queryPosts(postQueryDTO);
        return ResultVO.success(postPage);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID（未登录时返回null）
     */
    private Long parseUserIdFromToken() {
        // 此处为简化实现，实际应通过HttpServletRequest获取Authorization头并解析
        return 1001L;
    }
}
