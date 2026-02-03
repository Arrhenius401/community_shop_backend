package xyz.graygoo401.user.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.graygoo401.api.common.dto.mq.OrderEventDTO;
import xyz.graygoo401.user.service.base.UserService;

/**
 * 消费者
 * 用户服务监听订单服务发送的支付消息
 */
@Component
@Slf4j
public class UserCreditListener {

    @Autowired
    private UserService userService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "user.credit.queue"),
            exchange = @Exchange(name = "order.topic", type = ExchangeTypes.TOPIC),
            key = "order.pay.#" // 监听所有以 order.pay 开头的消息
    ))
    public void handleOrderPaidForCredit(OrderEventDTO event) {
        log.info("【用户服务】收到支付消息，为用户 {} 增加信用分", event.getBuyerId());
        userService.updateCreditScore(event.getBuyerId(), 5, "订单消费奖励");
    }

}
