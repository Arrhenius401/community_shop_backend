package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.AdminRequired;
import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.message.*;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import com.community_shop.backend.service.base.MessageService;
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

import java.util.List;

/**
 * 消息管理模块Controller，负责消息发送、查询、状态更新及系统公告发布等接口实现
 */
@RestController
@RequestMapping("/api/v1/messages")
@Tag(
        name = "消息管理接口",
        description = "包含消息发送（私信/系统通知）、详情查询、状态更新、未读统计及管理员专属通知发送等功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
@Validated
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发送消息接口
     * 对应Service层：MessageServiceImpl.sendMessage()，校验接收人存在、内容长度≤1000字、消息类型非空
     */
    @PostMapping
    @LoginRequired
    @Operation(
            summary = "发送消息接口（私信/系统通知）",
            description = "登录用户发送消息，业务规则：1.私信：所有登录用户可发送，接收人需存在，内容≤1000字；2.系统通知：仅管理员可发送，需指定目标用户；3.订单类消息需关联业务ID（如订单ID）；4.发送后更新接收人未读消息缓存（+1）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功，返回消息ID",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（接收人ID为空=MSG_004、内容为空=MSG_003、内容超1000字=MSG_007、类型为空=MSG_006、订单消息无业务ID=ORDER_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限发送系统通知（非管理员，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "接收人不存在（MSG_011）/发送人不存在（MSG_012）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Long> sendMessage(
            @Valid @RequestBody
            @Parameter(description = "消息发送参数，receiverId/content/type为必填，businessId（订单消息）可选", required = true)
            MessageSendDTO messageSendDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        Long messageId = messageService.sendMessage(currentUserId, messageSendDTO);
        return ResultVO.success(messageId);
    }

    /**
     * 发送卖家通知接口
     * 对应Service层：MessageServiceImpl.sendSellerNotice()，仅管理员可操作，校验卖家存在
     */
    @PostMapping("/sellers/{sellerId}/notices")
    @AdminRequired
    @Operation(
            summary = "发送卖家通知接口（管理员专属）",
            description = "管理员向指定卖家发送系统通知（如订单创建/支付通知），业务规则：1.仅管理员可操作；2.卖家需存在；3.通知内容非空；4.关联订单ID非空；5.消息类型固定为ORDER（订单相关）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（卖家ID为空=USER_015、内容为空=MSG_003、订单ID为空=ORDER_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无管理员权限（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "卖家不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> sendSellerNotice(
            @PathVariable
            @Parameter(description = "目标卖家ID", required = true, example = "1001")
            Long sellerId,
            @RequestParam @Valid
            @Parameter(description = "通知内容，非空且≤1000字", required = true)
            String content,
            @RequestParam
            @Parameter(description = "关联订单ID，非空", required = true, example = "5001")
            Long orderId
    ) {
        Boolean sendResult = messageService.sendSellerNotice(sellerId, content, orderId);
        return ResultVO.success(sendResult);
    }

    /**
     * 发送买家通知接口
     * 对应Service层：MessageServiceImpl.sendBuyerNotice()，仅管理员可操作，校验买家存在
     */
    @PostMapping("/buyers/{buyerId}/notices")
    @AdminRequired
    @Operation(
            summary = "发送买家通知接口（管理员专属）",
            description = "管理员向指定买家发送系统通知（如支付成功/发货通知），业务规则：1.仅管理员可操作；2.买家需存在；3.通知内容非空；4.关联订单ID非空；5.消息类型固定为ORDER（订单相关）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（买家ID为空=USER_015、内容为空=MSG_003、订单ID为空=ORDER_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无管理员权限（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "买家不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> sendBuyerNotice(
            @PathVariable
            @Parameter(description = "目标买家ID", required = true, example = "1002")
            Long buyerId,
            @RequestParam @Valid
            @Parameter(description = "通知内容，非空且≤1000字", required = true)
            String content,
            @RequestParam
            @Parameter(description = "关联订单ID，非空", required = true, example = "5001")
            Long orderId
    ) {
        Boolean sendResult = messageService.sendBuyerNotice(buyerId, content, orderId);
        return ResultVO.success(sendResult);
    }

    /**
     * 获取消息详情接口
     * 对应Service层：MessageServiceImpl.getMessageDetail()，校验消息归属权，返回发送者脱敏信息
     */
    @GetMapping("/{msgId}")
    @LoginRequired
    @Operation(
            summary = "获取消息详情接口",
            description = "查询指定消息的完整内容，业务规则：1.仅消息接收人可查看；2.返回信息含发送者脱敏信息（ID/用户名/头像）、消息内容、创建时间；3.系统消息发送者固定为“系统通知”",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回消息完整详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（消息ID为空=MSG_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限查看（非消息接收人，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "消息不存在（MSG_001）/用户不存在（USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<MessageDetailDTO> getMessageDetail(
            @PathVariable
            @Parameter(description = "目标消息ID", required = true, example = "6001")
            Long msgId
    ) {
        Long currentUserId = parseUserIdFromToken();
        MessageDetailDTO messageDetail = messageService.getMessageDetail(currentUserId, msgId);
        return ResultVO.success(messageDetail);
    }

    /**
     * 更新消息状态接口
     * 对应Service层：MessageServiceImpl.updateMessageStatus()，校验消息归属权，支持标记已读/删除
     */
    @PatchMapping("/status")
    @LoginRequired
    @Operation(
            summary = "更新消息状态接口（标记已读/删除）",
            description = "更新消息状态，业务规则：1.仅消息接收人可操作；2.支持状态：已读（READ）、删除（DELETED）；3.未读消息标记已读后，更新未读缓存（-1）；4.删除为逻辑删除，不物理删除数据",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "状态更新成功，返回是否影响数据（true=成功）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（消息ID为空=MSG_002、状态为空=MSG_006、状态转换非法=MSG_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限操作（非消息接收人，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "消息不存在（对应错误码：MSG_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updateMessageStatus(
            @Valid @RequestBody
            @Parameter(description = "消息状态更新参数，messageId/targetStatus为必填，targetStatus仅支持READ/DELETED", required = true)
            MessageStatusUpdateDTO statusUpdateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        Boolean isAffected = messageService.updateMessageStatus(currentUserId, statusUpdateDTO);
        return ResultVO.success(isAffected);
    }

    /**
     * 批量标记消息已读接口
     * 对应Service层：MessageServiceImpl.markAsRead()，循环标记每条消息，校验归属权
     */
    @PatchMapping("/batch/read")
    @LoginRequired
    @Operation(
            summary = "批量标记消息已读接口",
            description = "批量将多条消息标记为已读，业务规则：1.仅消息接收人可操作；2.消息需存在且未删除；3.每条消息标记后更新未读缓存（-1）；4.部分消息标记失败不影响整体操作（返回成功，日志记录失败信息）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "批量标记成功（无返回数据）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（消息ID列表为空=SYSTEM_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限操作（部分消息非当前用户所有，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "部分消息不存在（对应错误码：MSG_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "部分消息更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Void> batchUpdateReadStatus(
            @RequestBody
            @Parameter(description = "待标记已读的消息ID列表，非空且元素为有效消息ID", required = true)
            List<Long> msgIds
    ) {
        Long currentUserId = parseUserIdFromToken();
        for (Long msgId : msgIds) {
            messageService.markAsRead(currentUserId, msgId);
        }
        return ResultVO.success();
    }

    /**
     * 分页查询消息列表接口
     * 对应Service层：MessageServiceImpl.searchMessagesByQuery()，仅查询当前用户消息，支持类型/状态筛选
     */
    @GetMapping
    @LoginRequired
    @Operation(
            summary = "分页查询消息列表接口",
            description = "查询当前登录用户的消息列表，业务规则：1.仅返回当前用户作为接收人的消息；2.支持按消息类型（私信/系统通知/订单通知）、状态（未读/已读/已删除）筛选；3.分页默认pageNum=1、pageSize=10；4.默认按创建时间降序排序；5.系统消息发送者显示为“系统通知”",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回分页消息列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（分页参数为负数=SYSTEM_002、类型非法=MSG_005、状态非法=MSG_006）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<MessageListItemDTO>> searchMessagesByQuery(
            @Valid @ModelAttribute
            @Parameter(description = "消息查询参数，支持类型/状态筛选及分页，receiverId自动从Token解析")
            MessageQueryDTO messageQueryDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        PageResult<MessageListItemDTO> messagePage = messageService.searchMessagesByQuery(currentUserId, messageQueryDTO);
        return ResultVO.success(messagePage);
    }

    /**
     * 统计未读消息数接口
     * 对应Service层：MessageServiceImpl.countUnreadMessages()，优先从缓存获取，有效期5分钟
     */
    @GetMapping("/unread/count")
    @LoginRequired
    @Operation(
            summary = "统计未读消息数接口",
            description = "统计当前登录用户的未读消息总数，业务规则：1.仅统计未删除且未读的消息；2.优先从缓存获取（有效期5分钟），缓存未命中则查库并更新缓存；3.返回总数（含所有类型未读消息）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "统计成功，返回未读消息总数（无未读时返回0）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Integer> countUnreadMessages() {
        Long currentUserId = parseUserIdFromToken();
        Integer unreadCount = messageService.countUnreadMessages(currentUserId);
        return ResultVO.success(unreadCount);
    }

    /**
     * 获取最近未读消息预览接口
     * 对应Service层：MessageServiceImpl.getRecentUnreadPreviews()，返回前3条未读消息，内容截取30字
     */
    @GetMapping("/unread/preview")
    @LoginRequired
    @Operation(
            summary = "获取最近未读消息预览接口",
            description = "获取当前登录用户最近未读消息的精简预览，业务规则：1.最多返回3条未读消息（按创建时间倒序）；2.内容摘要截取前30字（超30字加“...”）；3.优先从缓存获取（有效期3分钟）；4.系统消息发送者显示为“系统通知”",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回未读消息预览列表（无未读时返回空列表）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<List<MessagePreviewDTO>> getRecentUnreadPreviews() {
        Long currentUserId = parseUserIdFromToken();
        List<MessagePreviewDTO> previews = messageService.getRecentUnreadPreviews(currentUserId);
        return ResultVO.success(previews);
    }

    /**
     * 发布系统公告接口
     * 对应Service层：MessageServiceImpl.sendMessage()，仅管理员可操作，类型固定为SYSTEM
     */
    @PostMapping("/system-announce")
    @AdminRequired
    @Operation(
            summary = "发布系统公告接口（管理员专属）",
            description = "管理员发布系统公告（如平台规则更新、活动通知），业务规则：1.仅管理员可操作；2.标题≤50字、内容≤1000字；3.可指定目标用户群体（全体/特定角色）；4.消息类型固定为SYSTEM（系统公告）；5.发布后所有目标用户收到通知",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发布成功，返回公告ID",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（标题超50字=MSG_007、内容超1000字=MSG_007、目标群体为空=SYSTEM_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无管理员权限（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Long> publishSystemAnnouncement(
            @Valid @RequestBody
            @Parameter(description = "系统公告发布参数，title/content/targetUserGroup为必填，类型自动设为SYSTEM", required = true)
            MessageSendDTO messageSendDTO
    ) {
        Long adminId = parseUserIdFromToken();
        messageSendDTO.setType(MessageTypeEnum.SYSTEM);
        Long announcementId = messageService.sendMessage(adminId, messageSendDTO);
        return ResultVO.success(announcementId);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（复用系统JWT解析逻辑）
     * @return 当前登录用户ID
     */
    private Long parseUserIdFromToken() {
        return requestParseUtil.parseUserIdFromRequest();
    }
}