package xyz.graygoo401.community.controller;

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
import xyz.graygoo401.api.community.dto.follow.*;
import xyz.graygoo401.common.annotation.AdminRequired;
import xyz.graygoo401.common.annotation.LoginRequired;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.util.RequestParseUtil;
import xyz.graygoo401.common.vo.ResultVO;
import xyz.graygoo401.community.service.base.PostFollowService;

/**
 * 跟帖模块Controller，负责跟帖发布、编辑、状态管理及列表查询等接口实现
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/follows")
@Tag(
        name = "跟帖管理接口",
        description = "包含跟帖发布、编辑、管理员状态更新、分页查询等功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
@Validated
public class PostFollowController {

    @Autowired
    private PostFollowService postFollowService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发布跟帖接口
     * 对应Service层：PostFollowServiceImpl.publishFollow()，校验帖子存在、内容1-500字、用户存在
     */
    @PostMapping("/create")
    @LoginRequired
    @Operation(
            summary = "发布跟帖接口",
            description = "登录用户对指定帖子发布跟帖，业务规则：1.关联帖子需存在（状态正常）；2.跟帖内容1-500字非空；3.支持@其他用户（多个ID用逗号分隔）；4.初始状态为正常（NORMAL）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发布成功，返回跟帖详情（含跟帖人脱敏信息）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（帖子ID为空=POST_004、内容为空/超500字=POST_FOLLOW_CONTENT_ILLEGAL）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（POST_001）/用户不存在（USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PostFollowDetailDTO> publishFollow(
            @PathVariable
            @Parameter(description = "关联帖子ID，需为整数", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "跟帖发布参数，content为必填，atUserIds可选（逗号分隔多个用户ID）", required = true)
            PostFollowPublishDTO postFollowPublishDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        postFollowPublishDTO.setPostId(postId);
        PostFollowDetailDTO followDetail = postFollowService.publishFollow(currentUserId, postFollowPublishDTO);
        return ResultVO.success(followDetail);
    }

    /**
     * 编辑跟帖接口
     * 对应Service层：PostFollowServiceImpl.updateFollow()，校验跟帖存在、作者身份、内容合法性
     */
    @PutMapping("/{followId}")
    @LoginRequired
    @Operation(
            summary = "编辑跟帖接口",
            description = "跟帖作者修改跟帖内容，业务规则：1.仅跟帖作者可操作；2.跟帖需正常状态（NORMAL）；3.新内容1-500字非空；编辑后更新修改时间",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "编辑成功，返回更新后跟帖详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（跟帖ID为空=POST_FOLLOW_ID_NULL、内容为空/超500字=POST_FOLLOW_CONTENT_ILLEGAL）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非跟帖作者，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "跟帖不存在（POST_FOLLOW_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "跟帖状态异常（非NORMAL，对应错误码：FOLLOW_STATUS_ILLEGAL）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PostFollowDetailDTO> updateFollow(
            @PathVariable
            @Parameter(description = "待编辑跟帖ID", required = true, example = "3001")
            Long followId,
            @PathVariable
            @Parameter(description = "关联帖子ID（需与跟帖所属帖子一致）", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "跟帖更新参数，newContent为必填，atUserIds可选", required = true)
            PostFollowUpdateDTO postFollowUpdateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        postFollowUpdateDTO.setPostFollowId(followId);
        PostFollowDetailDTO updatedFollow = postFollowService.updateFollow(currentUserId, postFollowUpdateDTO);
        return ResultVO.success(updatedFollow);
    }

    /**
     * 更新跟帖状态接口
     * 对应Service层：PostFollowServiceImpl.updateFollowStatus()，仅管理员可操作，校验跟帖存在
     */
    @PatchMapping("/update/status")
    @AdminRequired
    @Operation(
            summary = "更新跟帖状态接口（管理员专属）",
            description = "管理员变更跟帖状态，业务规则：1.仅管理员可操作；2.跟帖需存在；3.目标状态仅支持NORMAL（正常）/HIDDEN（隐藏）；更新后记录操作时间",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "状态更新成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（跟帖ID为空=POST_FOLLOW_ID_NULL、目标状态非法=POST_FOLLOW_STATUS_ERROR）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无管理员权限（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "跟帖不存在（对应错误码：POST_FOLLOW_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updateFollowStatus(
            @Valid @RequestBody
            @Parameter(description = "跟帖状态更新参数，postFollowId必填，targetStatus仅支持NORMAL/HIDDEN", required = true)
            PostFollowStatusUpdateDTO postFollowStatusUpdateDTO
    ) {
        Long adminId = parseUserIdFromToken();
        Boolean result = postFollowService.updateFollowStatus(adminId, postFollowStatusUpdateDTO);
        return ResultVO.success(result);
    }

    /**
     * 分页查询跟帖列表接口
     * 对应Service层：PostFollowServiceImpl.queryFollowsByPostId()，校验帖子存在、支持状态筛选与分页
     */
    @GetMapping("/query/list")
    @Operation(
            summary = "分页查询跟帖列表接口",
            description = "查询指定帖子的跟帖列表，业务规则：1.关联帖子需存在；2.默认查询正常状态（NORMAL）跟帖；3.分页参数默认pageNum=1、pageSize=10；4.默认按发布时间降序排序"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回分页跟帖列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（帖子ID为空=POST_004、分页参数为负数=SYSTEM_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（对应错误码：POST_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<PostFollowDetailDTO>> queryFollowList(
            @PathVariable
            @Parameter(description = "关联帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @ModelAttribute
            @Parameter(description = "跟帖查询参数，支持状态筛选、分页与排序")
            PostFollowQueryDTO postFollowQueryDTO
    ) {
        postFollowQueryDTO.setPostId(postId);
        PageResult<PostFollowDetailDTO> followPage = postFollowService.queryFollows(postFollowQueryDTO);
        return ResultVO.success(followPage);
    }

    /**
     * 查询跟帖数量接口
     * 对应Service层：PostFollowServiceImpl.countFollows()，统计符合条件的正常状态跟帖总数
     */
    @GetMapping("/query/count")
    @Operation(
            summary = "查询跟帖数量接口",
            description = "查询指定帖子的正常状态跟帖数量，业务规则：1.关联帖子需存在；2.默认查询正常状态（NORMAL）跟帖"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回跟帖数量",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（帖子ID为空=POST_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "帖子不存在（对应错误码：POST_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Integer> queryFollowCount(
            @PathVariable
            @Parameter(description = "关联帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @ModelAttribute
            @Parameter(description = "跟帖查询参数，支持状态筛选、分页与排序")
            PostFollowQueryDTO postFollowQueryDTO
    ) {
        postFollowQueryDTO.setPostId(postId);
        int followCount = postFollowService.countFollows(postFollowQueryDTO);
        return ResultVO.success(followCount);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID（未登录时返回null）
     */
    private Long parseUserIdFromToken() {
        return requestParseUtil.parseUserIdFromRequest();
    }
}