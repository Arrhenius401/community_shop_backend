package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.AdminRequired;
import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.service.base.PostService;
import com.community_shop.backend.service.base.UserPostLikeService;
import com.community_shop.backend.utils.RequestParseUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(
        name = "帖子管理接口",
        description = "包含帖子发布、详情查询、编辑、点赞、管理员置顶/加精、状态管控等功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
@Validated
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserPostLikeService userPostLikeService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发布帖子接口
     * 对应Service层：PostServiceImpl.publishPost()，校验信用分≥60、标题/内容合法性、图片数量≤9张
     */
    @PostMapping("/publish")
    @LoginRequired
    @Operation(
            summary = "发布帖子接口",
            description = "登录用户发布社区帖子，业务规则：1.用户信用分需≥60分（低于则无法发布）；2.标题1-50字非空；3.内容1-2000字非空；4.图片最多9张（JSON格式URL列表）；5.新用户（注册≤7天）帖子需审核",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发布成功，返回帖子详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如标题为空=POST_002、内容过长=POST_023、图片超9张=POST_031）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "信用分不足（<60分，对应错误码：USER_081）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PostDetailDTO> publishPost(
            @Valid @RequestBody
            @Parameter(description = "帖子发布参数，标题/内容必填，图片URL列表最多9张", required = true)
            PostPublishDTO postPublishDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        PostDetailDTO postDetail = postService.publishPost(currentUserId, postPublishDTO);
        return ResultVO.success(postDetail);
    }

    /**
     * 获取帖子详情接口
     * 对应Service层：PostServiceImpl.selectPostById()，校验帖子存在性、权限（异常状态仅作者/管理员可见）
     */
    @GetMapping("/{postId}")
    @Operation(
            summary = "获取帖子详情接口",
            description = "查询指定帖子的完整信息，包含：1.发布者脱敏信息（ID/用户名/头像/信用分）；2.帖子内容与图片；3.点赞数/跟帖数；4.当前用户点赞状态；权限规则：异常状态帖子（如删除）仅作者或管理员可见"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回帖子完整详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（帖子ID为空，对应错误码：POST_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限查看异常状态帖子（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（对应错误码：POST_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PostDetailDTO> getPostDetail(
            @PathVariable
            @Parameter(description = "帖子ID，需为整数", required = true, example = "2001")
            Long postId
    ) {
        Long currentUserId = parseUserIdFromToken(); // 未登录时返回null，Service层适配未登录场景
        PostDetailDTO postDetail = postService.selectPostById(currentUserId, postId);
        return ResultVO.success(postDetail);
    }

    /**
     * 编辑帖子接口
     * 对应Service层：PostServiceImpl.updatePost()，校验作者身份、帖子未删除、内容合法性
     */
    @PutMapping("/{postId}")
    @LoginRequired
    @Operation(
            summary = "编辑帖子接口",
            description = "帖子作者修改标题与内容，业务规则：1.仅作者可编辑；2.帖子未删除（状态≠DELETED）；3.标题1-100字；4.内容1-5000字；编辑后清除旧缓存，更新新缓存",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "编辑成功，返回更新后详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（标题为空=POST_002、内容过长=POST_023）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非作者，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（对应错误码：POST_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "帖子已删除（对应错误码：POST_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PostDetailDTO> updatePost(
            @PathVariable
            @Parameter(description = "待编辑帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "帖子更新参数，需包含新标题与新内容", required = true)
            PostUpdateDTO postUpdateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        postUpdateDTO.setOperatorId(currentUserId);
        postUpdateDTO.setPostId(postId);
        PostDetailDTO updatedPost = postService.updatePost(postId, currentUserId, postUpdateDTO);
        return ResultVO.success(updatedPost);
    }

    /**
     * 帖子点赞/取消点赞接口
     * 对应Service层：PostServiceImpl.updateLikeStatus()，校验帖子状态、每日点赞次数≤50次
     */
    @PatchMapping("/{postId}/like")
    @LoginRequired
    @Operation(
            summary = "帖子点赞/取消点赞接口",
            description = "登录用户对帖子进行点赞/取消点赞，业务规则：1.帖子需正常状态（NORMAL）；2.每日点赞上限50次；3.点赞时新增记录，取消时删除记录；实时返回最新点赞数",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "操作成功，返回最新点赞数",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（操作类型为空=SYSTEM_003、每日点赞超限=POST_072）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（POST_001）/用户不存在（USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "帖子状态异常（非NORMAL，对应错误码：POST_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "点赞数更新失败（对应错误码：POST_074）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Integer> updateLikeStatus(
            @PathVariable
            @Parameter(description = "帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "点赞操作参数，isLike=true=点赞，false=取消点赞", required = true)
            PostLikeDTO postLikeDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        postLikeDTO.setUserId(currentUserId);
        postLikeDTO.setPostId(postId);
        Integer latestLikeCount = postService.updateLikeStatus(postLikeDTO);
        return ResultVO.success(latestLikeCount);
    }

    /**
     * 帖子置顶/加精接口
     * 对应Service层：PostServiceImpl.setEssenceOrTop()，仅管理员可操作，置顶数≤5
     */
    @PatchMapping("/{postId}/essence-top")
    @AdminRequired
    @Operation(
            summary = "帖子置顶/加精接口（管理员专属）",
            description = "管理员设置帖子为精华/置顶，业务规则：1.仅管理员可操作；2.帖子需正常状态（未删除）；3.置顶数上限5篇（超限时无法新增）；设置后清除置顶列表缓存",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "设置成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（帖子ID为空=POST_004、状态未指定=SYSTEM_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无管理员权限（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（对应错误码：POST_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "帖子已删除（POST_051）/置顶数超限（POST_071）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> setEssenceOrTop(
            @PathVariable
            @Parameter(description = "帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "运营操作参数，isEssence=true=精华，isTop=true=置顶", required = true)
            PostEssenceTopDTO postEssenceTopDTO
    ) {
        Long adminId = parseUserIdFromToken();
        postEssenceTopDTO.setPostId(postId);
        Boolean result = postService.setEssenceOrTop(adminId, postEssenceTopDTO);
        return ResultVO.success(result);
    }

    /**
     * 分页查询帖子列表接口
     * 对应Service层：PostServiceImpl.queryPosts()，支持多条件筛选、分页与排序
     */
    @GetMapping("/query/list")
    @Operation(
            summary = "分页查询帖子列表接口",
            description = "多条件查询帖子列表，支持：1.关键词模糊匹配标题/内容；2.按发布时间/点赞数/跟帖数排序；3.分页参数（pageNum默认1，pageSize默认10）；仅返回正常状态（NORMAL）帖子，结果优先从缓存获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回分页列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（pageNum/pageSize为负数，对应错误码：SYSTEM_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<PostListItemDTO>> queryPostList(
            @Valid @ModelAttribute
            @Parameter(description = "帖子列表查询参数，支持关键词、排序与分页")
            PostQueryDTO postQueryDTO
    ) {
        PageResult<PostListItemDTO> postPage = postService.queryPosts(postQueryDTO);
        return ResultVO.success(postPage);
    }

    /**
     * 查询帖子数量接口
     * 对应Service层：PostServiceImpl.countPosts()，统计符合条件的正常状态帖子总数
     */
    @GetMapping("/query/count")
    @Operation(
            summary = "查询帖子数量接口",
            description = "统计符合筛选条件的帖子总数，支持的查询条件与“分页查询帖子列表”一致（关键词、状态等），仅统计正常状态（NORMAL）帖子"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "统计成功（无数据时返回0）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（状态非法，对应错误码：POST_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Integer> queryPostCount(
            @Parameter(description = "帖子数量查询参数，支持关键词与状态筛选")
            PostQueryDTO postQueryDTO
    ) {
        return ResultVO.success(postService.countPosts(postQueryDTO));
    }

    /**
     * 帖子状态更新接口
     * 对应Service层：PostServiceImpl.updatePostStatus()，作者/管理员可操作，状态流转有约束
     */
    @PatchMapping("/update/status")
    @LoginRequired
    @Operation(
            summary = "帖子状态更新接口",
            description = "作者或管理员更新帖子状态，业务规则：1.作者权限：仅可更新自己的帖子为正常/隐藏（不可设置为删除/封禁）；2.管理员权限：可更新所有帖子为正常/删除/封禁（不可设置为隐藏）；3.已删除帖子不可变更状态；4.封禁帖子仅管理员可操作",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "状态更新成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（帖子ID为空=POST_004、状态非法=POST_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（如作者更新封禁状态，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（对应错误码：POST_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "帖子已删除（不可变更状态，对应错误码：POST_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updatePostStatus(
            @Valid @RequestBody
            @Parameter(description = "帖子状态更新参数，含帖子ID与目标状态（NORMAL/DELETED/HIDDEN/BLOCKED）", required = true)
            PostStatusUpdateDTO postStatusUpdateDTO
    ) {
        Long operatorId = parseUserIdFromToken();
        postService.updatePostStatus(operatorId, postStatusUpdateDTO.getPostId(), postStatusUpdateDTO.getStatus());
        return ResultVO.success(true);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID（未登录时返回null）
     */
    private Long parseUserIdFromToken() {
        return requestParseUtil.parseUserIdFromRequest();
    }
}