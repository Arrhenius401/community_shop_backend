package com.community_shop.backend.service.base;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付服务接口
 */
@Service
public interface PaymentService {

    /**
     * 生成支付链接（电脑网站支付）
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 支付链接（前端跳转至该链接完成支付）
     */
    String generatePaymentUrl(Long orderId, Long userId);

    /**
     * 处理第三方支付平台异步回调（核心：验证签名+更新订单/支付状态）
     * @param params 支付回调参数（request.getParameterMap）
     * @return true（支付宝收到后停止重试，否则会重试24小时）
     */
    Boolean handlePayCallback(Map<String, String> params);
}
