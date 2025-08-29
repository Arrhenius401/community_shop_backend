package com.community_shop.backend.service.impl;

import com.community_shop.backend.DTO.param.PageParam;
import com.community_shop.backend.DTO.result.PageResult;
import com.community_shop.backend.DTO.result.ResultDTO;
import com.community_shop.backend.component.enums.MessageTypeEnum;
import com.community_shop.backend.component.enums.UserRoleEnum;
import com.community_shop.backend.entity.Message;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.MessageMapper;
import com.community_shop.backend.service.base.MessageService;
import com.community_shop.backend.service.base.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内消息服务实现类，基于MyBatis操作message表，实现消息发送与管理功能
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultDTO<Boolean> sendSellerNotice(Long sellerId, String content, Long orderId) {
        // 校验卖家是否存在
        ResultDTO<User> userResult = userService.selectUserById(sellerId);
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            return ResultDTO.fail(404, "卖家不存在");
        }

        // 创建消息实体
        Message message = new Message();
        message.setSenderId(0L); // 0表示系统发送
        message.setReceiverId(sellerId);
        message.setContent(content);
        message.setType(MessageTypeEnum.ORDER); // 订单相关通知
        message.setOrderId(orderId);
        message.setIsRead(0); // 0=未读，1=已读
        message.setCreateTime(LocalDateTime.now());
        message.setIsDeleted(0); // 0=未删除

        // 插入消息
        int rows = messageMapper.insert(message);
        return rows > 0 ? ResultDTO.success(true) : ResultDTO.fail(500, "通知发送失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultDTO<Boolean> sendBuyerNotice(Long buyerId, String content, Long orderId) {
        // 校验买家是否存在
        ResultDTO<User> userResult = userService.selectUserById(buyerId);
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            return ResultDTO.fail(404, "买家不存在");
        }

        // 创建消息实体
        Message message = new Message();
        message.setSenderId(0L); // 系统发送
        message.setReceiverId(buyerId);
        message.setContent(content);
        message.setType(MessageTypeEnum.ORDER);
        message.setOrderId(orderId);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        message.setIsDeleted(0);

        // 插入消息
        int rows = messageMapper.insert(message);
        return rows > 0 ? ResultDTO.success(true) : ResultDTO.fail(500, "通知发送失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultDTO<Boolean> sendSystemAnnouncement(String content, Long operatorId) {
        // 校验操作员是否为管理员
        ResultDTO<User> userResult = userService.selectUserById(operatorId);
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            return ResultDTO.fail(404, "操作员不存在");
        }
        if (!UserRoleEnum.ADMIN.equals(userResult.getData().getRole())) {
            return ResultDTO.fail(403, "无管理员权限，无法发送系统公告");
        }

        // 创建系统公告（实际业务中可能需要向所有用户推送，这里简化为单条公告，用户查询时可见）
        Message message = new Message();
        message.setSenderId(0L);
        message.setReceiverId(-1L); // -1表示所有用户可见
        message.setContent(content);
        message.setType(MessageTypeEnum.SYSTEM);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        message.setIsDeleted(0);

        int rows = messageMapper.insert(message);
        return rows > 0 ? ResultDTO.success(true) : ResultDTO.fail(500, "公告发送失败");
    }

    @Override
    public ResultDTO<PageResult<Message>> selectUserMessages(Long userId, String msgType, PageParam pageParam) {
        // 计算分页参数
        int offset = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        int limit = pageParam.getPageSize();

        // 查询消息列表
        List<Message> messages = messageMapper.selectByReceiver(
                userId, msgType, offset, limit
        );

        // 查询总条数
        Long total = messageMapper.countByReceiver(userId, msgType);

        // 封装分页结果
        // Math.ceil() 是为了处理整除不尽的情况,结果向上取整（如 137 条不能被 10 整除，需要多一页显示剩余 7 条）
        PageResult<Message> pageResult = new PageResult<>(
                total,
                (int) Math.ceil((double) total / limit),
                messages,
                pageParam.getPageNum(),
                limit
        );
        return ResultDTO.success(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultDTO<Boolean> markAsRead(Long msgId, Long userId) {
        // 校验消息是否存在且属于当前用户
        Message message = messageMapper.selectById(msgId);
        if (message == null || message.getIsDeleted() == 1) {
            return ResultDTO.fail(404, "消息不存在或已删除");
        }
        if (!message.getReceiverId().equals(userId) && message.getReceiverId() != -1) {
            return ResultDTO.fail(403, "无权限操作此消息");
        }

        // 标记为已读
        int rows = messageMapper.updateReadStatus(msgId, 1); // 1=已读
        return rows > 0 ? ResultDTO.success(true) : ResultDTO.fail(500, "更新失败");
    }

    @Override
    public ResultDTO<Integer> countUnreadMessages(Long userId) {
        int count = messageMapper.countUnread(userId);
        return ResultDTO.success(count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultDTO<Boolean> deleteMessage(Long msgId, Long userId) {
        // 校验消息权限
        Message message = messageMapper.selectById(msgId);
        if (message == null || message.getIsDeleted() == 1) {
            return ResultDTO.fail(404, "消息不存在或已删除");
        }
        if (!message.getReceiverId().equals(userId) && message.getReceiverId() != -1) {
            return ResultDTO.fail(403, "无权限删除此消息");
        }

        // 逻辑删除（标记为已删除）
        int rows = messageMapper.updateDeleteStatus(msgId, 1); // 1=已删除
        return rows > 0 ? ResultDTO.success(true) : ResultDTO.fail(500, "删除失败");
    }
}
