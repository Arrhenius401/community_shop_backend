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
@Tag(name = "消息管理接口", description = "包含消息发送、查询、状态更新、未读统计及系统公告发布等功能")
@Validated
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发送消息接口（支持私信、系统通知等类型）
     * @param messageSendDTO 消息发送请求参数（接收者ID、内容、消息类型等）
     * @return 包含消息ID的统一响应
     */
    @PostMapping
    @LoginRequired
    @Operation(
            summary = "发送消息接口",
            description = "发送私信或系统通知（系统通知需管理员权限），支持携带附件，登录后访问"
    )
    public ResultVO<Long> sendMessage(@Valid @RequestBody MessageSendDTO messageSendDTO) {
        Long currentUserId = parseUserIdFromToken();
        Long messageId = messageService.sendMessage(currentUserId, messageSendDTO);
        return ResultVO.success(messageId);
    }

    /**
     * 发送卖家通知接口（系统触发，管理员调用）
     * @param sellerId 目标卖家ID
     * @param content 通知内容
     * @param orderId 关联订单ID
     * @return 发送结果的统一响应
     */
    @PostMapping("/sellers/{sellerId}/notices")
    @AdminRequired
    @Operation(
            summary = "发送卖家通知接口",
            description = "向指定卖家发送系统通知（如订单创建通知），仅管理员可访问"
    )
    public ResultVO<Boolean> sendSellerNotice(
            @PathVariable Long sellerId,
            @RequestParam @Valid String content,
            @RequestParam Long orderId
    ) {
        Boolean sendResult = messageService.sendSellerNotice(sellerId, content, orderId);
        return ResultVO.success(sendResult);
    }

    /**
     * 发送买家通知接口（系统触发，管理员调用）
     * @param buyerId 目标买家ID
     * @param content 通知内容
     * @param orderId 关联订单ID
     * @return 发送结果的统一响应
     */
    @PostMapping("/buyers/{buyerId}/notices")
    @AdminRequired
    @Operation(
            summary = "发送买家通知接口",
            description = "向指定买家发送系统通知（如支付成功通知），仅管理员可访问"
    )
    public ResultVO<Boolean> sendBuyerNotice(
            @PathVariable Long buyerId,
            @RequestParam @Valid String content,
            @RequestParam Long orderId
    ) {
        Boolean sendResult = messageService.sendBuyerNotice(buyerId, content, orderId);
        return ResultVO.success(sendResult);
    }

    /**
     * 获取消息详情接口
     * @param msgId 目标消息ID
     * @return 包含消息完整信息的统一响应
     */
    @GetMapping("/{msgId}")
    @LoginRequired
    @Operation(
            summary = "获取消息详情接口",
            description = "查询指定消息的完整内容及关联业务信息，需校验消息归属权，登录后访问"
    )
    public ResultVO<MessageDetailDTO> getMessageDetail(@PathVariable Long msgId) {
        Long currentUserId = parseUserIdFromToken();
        MessageDetailDTO messageDetail = messageService.getMessageDetail(currentUserId, msgId);
        return ResultVO.success(messageDetail);
    }

    /**
     * 更新消息状态接口（标记已读/删除）
     * @param statusUpdateDTO 消息状态更新请求参数（消息ID、目标状态）
     * @return 包含影响行数的统一响应
     */
    @PatchMapping("/status")
    @LoginRequired
    @Operation(
            summary = "更新消息状态接口",
            description = "标记消息为已读或逻辑删除，需校验消息归属权，登录后访问"
    )
    public ResultVO<Boolean> updateMessageStatus(@Valid @RequestBody MessageStatusUpdateDTO statusUpdateDTO) {
        Long currentUserId = parseUserIdFromToken();
        Boolean isAffected = messageService.updateMessageStatus(currentUserId, statusUpdateDTO);
        return ResultVO.success(isAffected);
    }

    /**
     * 批量标记消息已读接口
     * @param msgIds 待标记的消息ID列表
     * @return 包含成功标记数量的统一响应
     */
    @PatchMapping("/batch/read")
    @LoginRequired
    @Operation(
            summary = "批量标记消息已读接口",
            description = "批量将多个消息标记为已读，需登录后访问"
    )
    public ResultVO<Void> batchUpdateReadStatus(@RequestBody List<Long> msgIds) {
        Long currentUserId = parseUserIdFromToken();
        for(Long msgId : msgIds){
            messageService.markAsRead(currentUserId, msgId);
        }
        return ResultVO.success();
    }

    /**
     * 分页查询消息列表接口
     * @param messageQueryDTO 消息查询参数（类型、状态、关键词、分页信息等）
     * @return 包含分页消息列表的统一响应
     */
    @GetMapping
    @LoginRequired
    @Operation(
            summary = "分页查询消息列表接口",
            description = "按条件查询当前登录用户的消息列表，支持类型筛选与分页，登录后访问"
    )
    public ResultVO<PageResult<MessageListItemDTO>> searchMessagesByQuery(@Valid @ModelAttribute MessageQueryDTO messageQueryDTO) {
        Long currentUserId = parseUserIdFromToken();
        PageResult<MessageListItemDTO> messagePage = messageService.searchMessagesByQuery(currentUserId, messageQueryDTO);
        return ResultVO.success(messagePage);
    }

    /**
     * 统计未读消息数接口
     * @return 包含未读消息数量的统一响应
     */
    @GetMapping("/unread/count")
    @LoginRequired
    @Operation(
            summary = "统计未读消息数接口",
            description = "统计当前登录用户的未读消息总数，登录后访问"
    )
    public ResultVO<Integer> countUnreadMessages() {
        Long currentUserId = parseUserIdFromToken();
        Integer unreadCount = messageService.countUnreadMessages(currentUserId);
        return ResultVO.success(unreadCount);
    }

    /**
     * 获取最近未读消息预览接口
     * @return 包含未读消息预览列表的统一响应
     */
    @GetMapping("/unread/preview")
    @LoginRequired
    @Operation(
            summary = "获取最近未读消息预览接口",
            description = "获取当前登录用户最近未读消息的精简预览，登录后访问"
    )
    public ResultVO<List<MessagePreviewDTO>> getRecentUnreadPreviews() {
        Long currentUserId = parseUserIdFromToken();
        List<MessagePreviewDTO> previews = messageService.getRecentUnreadPreviews(currentUserId);
        return ResultVO.success(previews);
    }

    /**
     * 发布系统公告接口
     * @param messageSendDTO 公告发送参数（标题、内容、目标群体等）
     * @return 包含公告ID的统一响应
     */
    @PostMapping("/system-announce")
    @AdminRequired
    @Operation(
            summary = "发布系统公告接口",
            description = "管理员发布系统公告，可指定目标用户群体，仅管理员可访问"
    )
    public ResultVO<Long> publishSystemAnnouncement(@Valid @RequestBody MessageSendDTO messageSendDTO) {
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