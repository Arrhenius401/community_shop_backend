package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.AdminRequired;
import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.service.base.PostFollowService;
import com.community_shop.backend.utils.RequestParseUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 跟帖模块Controller，负责跟帖发布、编辑、状态管理及列表查询等接口实现
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/follows")
@Tag(name = "跟帖管理接口", description = "包含跟帖发布、编辑、状态更新及列表查询等功能")
@Validated
public class PostFollowController {

    @Autowired
    private PostFollowService postFollowService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发布跟帖接口
     * @param postId 关联帖子ID
     * @param postFollowPublishDTO 跟帖发布参数（内容、@用户ID列表）
     * @return 包含跟帖详情的统一响应
     */
    @PostMapping("")
    @LoginRequired
    @Operation(
            summary = "发布跟帖接口",
            description = "登录用户对指定帖子发布跟帖，支持@其他用户，内容限制1-500字"
    )
    public ResultVO<PostFollowDetailDTO> publishFollow(
            @PathVariable
            @Parameter(description = "关联帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @RequestBody
            @Parameter(description = "跟帖发布参数，包含跟帖内容及@用户ID列表", required = true)
            PostFollowPublishDTO postFollowPublishDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        // 补充关联帖子ID到DTO中
        postFollowPublishDTO.setPostId(postId);
        PostFollowDetailDTO followDetail = postFollowService.publishFollow(currentUserId, postFollowPublishDTO);
        return ResultVO.success(followDetail);
    }

    /**
     * 编辑跟帖接口
     * @param followId 跟帖ID
     * @param postFollowUpdateDTO 跟帖更新参数（新内容）
     * @return 包含更新后跟帖详情的统一响应
     */
    @PutMapping("/{followId}")
    @LoginRequired
    @Operation(
            summary = "编辑跟帖接口",
            description = "跟帖作者可修改跟帖内容，需校验操作用户身份与跟帖状态"
    )
    public ResultVO<PostFollowDetailDTO> updateFollow(
            @PathVariable
            @Parameter(description = "待编辑跟帖ID", required = true, example = "3001")
            Long followId,
            @Valid @RequestBody
            @Parameter(description = "跟帖更新参数，包含新的跟帖内容", required = true)
            PostFollowUpdateDTO postFollowUpdateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        // 补充跟帖ID到DTO中
        postFollowUpdateDTO.setPostFollowId(followId);
        PostFollowDetailDTO updatedFollow = postFollowService.updateFollow(currentUserId, postFollowUpdateDTO);
        return ResultVO.success(updatedFollow);
    }

    /**
     * 更新跟帖状态接口
     * @param followId 跟帖ID
     * @param postFollowStatusUpdateDTO 状态更新参数（目标状态：正常/隐藏）
     * @return 操作结果的统一响应
     */
    @PatchMapping("/{followId}/status")
    @AdminRequired
    @Operation(
            summary = "更新跟帖状态接口",
            description = "管理员专属接口，可将违规跟帖设置为隐藏状态或恢复正常显示"
    )
    public ResultVO<Boolean> updateFollowStatus(
            @PathVariable
            @Parameter(description = "跟帖ID", required = true, example = "3001")
            Long followId,
            @Valid @RequestBody
            @Parameter(description = "状态更新参数，指定跟帖目标状态", required = true)
            PostFollowStatusUpdateDTO postFollowStatusUpdateDTO
    ) {
        Long adminId = parseUserIdFromToken();
        // 补充跟帖ID到DTO中
        postFollowStatusUpdateDTO.setPostFollowId(followId);
        Boolean result = postFollowService.updateFollowStatus(adminId, postFollowStatusUpdateDTO);
        return ResultVO.success(result);
    }

    /**
     * 分页查询跟帖列表接口
     * @param postId 关联帖子ID
     * @param postFollowQueryDTO 跟帖查询参数（状态、排序、分页信息）
     * @return 包含分页跟帖列表的统一响应
     */
    @GetMapping("")
    @Operation(
            summary = "分页查询跟帖列表接口",
            description = "查询指定帖子的跟帖列表，默认按发布时间降序返回正常状态的跟帖"
    )
    public ResultVO<PageResult<PostFollowDetailDTO>> queryFollowList(
            @PathVariable
            @Parameter(description = "关联帖子ID", required = true, example = "2001")
            Long postId,
            @Valid @ModelAttribute
            @Parameter(description = "跟帖查询参数，包含状态、排序规则及分页信息")
            PostFollowQueryDTO postFollowQueryDTO
    ) {
        // 补充关联帖子ID到DTO中
        postFollowQueryDTO.setPostId(postId);
        PageResult<PostFollowDetailDTO> followPage = postFollowService.queryFollowsByPostId(postFollowQueryDTO);
        return ResultVO.success(followPage);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID（未登录时返回null）
     */
    private Long parseUserIdFromToken() {
        // 通过HttpServletRequest获取Authorization头，解析JWT令牌得到用户ID
        return requestParseUtil.parseUserIdFromRequest();
    }
}