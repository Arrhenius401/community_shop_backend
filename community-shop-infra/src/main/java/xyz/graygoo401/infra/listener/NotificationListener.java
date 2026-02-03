package xyz.graygoo401.infra.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.graygoo401.api.common.dto.mq.CommunityEventDTO;
import xyz.graygoo401.api.common.dto.mq.OrderEventDTO;
import xyz.graygoo401.api.infra.dto.message.MessageSendDTO;
import xyz.graygoo401.api.infra.enums.MessageTypeEnum;
import xyz.graygoo401.infra.service.base.MessageService;

/**
 * 通知服务监听器
 */
@Component
@Slf4j
public class NotificationListener {

    @Autowired
    private MessageService messageService;

    /**
     * 监听订单类消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "infra.notice.order"),
            exchange = @Exchange(name = "order.topic", type = ExchangeTypes.TOPIC),
            key = "order.#"
    ))
    public void onOrderMessage(OrderEventDTO event) {
        log.info("【通知服务】准备为订单 {} 发送通知", event.getOrderNo());

        // 内部逻辑：构造 MessageSendDTO 并存入数据库
        // 1. 构造 MessageSendDTO
        MessageSendDTO sendDTO = new MessageSendDTO();
        sendDTO.setReceiverId(event.getBuyerId());
        sendDTO.setTitle("【通知服务】准备为订单 {" + event.getOrderNo() + "} 发送通知");
        sendDTO.setContent("订单状态变更：" + event.getType());
        sendDTO.setBusinessId(event.getOrderId());
        sendDTO.setType(MessageTypeEnum.ORDER);

        // 2. 向买家发送通知
        messageService.sendNotice(sendDTO);

        // 3. 向卖家发送通知
        sendDTO.setReceiverId(event.getSellerId());
        messageService.sendNotice(sendDTO);

    }

    // 监听社区类消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "infra.notice.community"),
            exchange = @Exchange(name = "community.topic", type = ExchangeTypes.TOPIC),
            key = "community.#"
    ))
    public void onCommunityMessage(CommunityEventDTO event) {
        log.info("【通知服务】用户 {} 给你点赞了", event.getAuthorId());
        // 内部逻辑：给作者发送站内信

        // 内部逻辑：构造 MessageSendDTO 并存入数据库
        MessageSendDTO sendDTO = new MessageSendDTO();
        sendDTO.setReceiverId(event.getAuthorId());
        sendDTO.setTitle("【通知服务】用户 " + event.getOperatorId() + " 给你点赞了");
        sendDTO.setContent("用户 " + event.getOperatorId() + " 赞了你的帖子 " + event.getPostId());
        sendDTO.setBusinessId(event.getPostId());
        sendDTO.setType(MessageTypeEnum.PRIVATE);

        messageService.sendNotice(sendDTO);
    }
}
