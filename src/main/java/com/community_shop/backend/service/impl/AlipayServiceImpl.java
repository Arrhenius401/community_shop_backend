package com.community_shop.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community_shop.backend.config.AlipayConfig;
import com.community_shop.backend.dao.mapper.OrderMapper;
import com.community_shop.backend.dao.mapper.PaymentMapper;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.entity.Payment;
import com.community_shop.backend.enums.code.OrderStatusEnum;
import com.community_shop.backend.enums.code.PayStatusEnum;
import com.community_shop.backend.enums.code.PayTypeEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.service.base.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付服务
 */
@Slf4j
@Service
public class AlipayServiceImpl extends BaseServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    /**
     * 生成支付宝支付链接（电脑网站支付）
     * @param orderId 订单ID
     * @return 支付链接（前端跳转至该链接完成支付）
     */
    @Override
    public String generatePaymentUrl(Long orderId) {
        // 1. 查询订单（校验订单状态：必须是「待支付」）
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
        }
        if (!OrderStatusEnum.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "订单状态异常，仅待支付订单可发起支付");
        }

        // 2. 创建支付记录（关联订单，状态为「待支付」）
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setOrderNo(order.getOrderNo());
        payment.setPayType(PayTypeEnum.ALIPAY); // 支付宝
        payment.setPayAmount(order.getTotalAmount()); // 支付金额=订单总金额
        payment.setPayStatus(PayStatusEnum.PENDING); // 待支付
        paymentMapper.insert(payment);

        // 3. 构造支付宝支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl()); // 异步回调地址
        request.setReturnUrl(alipayConfig.getReturnUrl()); // 同步跳转地址

        // 组装支付参数（JSON格式）
        String bizContent = JSON.toJSONString(new HashMap<String, Object>() {{
            put("out_trade_no", order.getOrderNo()); // 商户订单号（与支付记录一致）
            put("total_amount", order.getTotalAmount().toString()); // 支付金额（BigDecimal转String）
            put("subject", "graygoo401 的社区交易-订单" + order.getOrderNo()); // 订单标题（展示给用户）
            put("product_code", "FAST_INSTANT_TRADE_PAY"); // 电脑网站支付固定产品码
        }});
        request.setBizContent(bizContent);

        // 4. 调用支付宝API生成支付链接
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                return response.getBody(); // 返回支付表单HTML（前端直接渲染即可跳转）
            } else {
                throw new BusinessException(ErrorCode.PAYMENT_GENERATE_URL_FAILS, "生成支付链接失败：" + response.getMsg());
            }
        } catch (BusinessException e) {
          throw e;
        } catch (AlipayApiException e) {
            log.error("支付宝API调用异常：" + e.getErrMsg());
            throw new BusinessException(ErrorCode.PAYMENT_ALIPAY_FAILS, "支付宝API调用异常");
        } catch (Exception e) {
            log.error("支付异常：" + e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_ALIPAY_FAILS, "支付异常");
        }
    }

    /**
     * 处理支付宝异步回调（核心：验证签名+更新订单/支付状态）
     * @param params 支付宝回调参数（request.getParameterMap）
     * @return "success"（支付宝收到后停止重试，否则会重试24小时）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handlePayCallback(Map<String, String> params) {
        try {
            // 1. 验证回调签名（防止伪造请求）
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );
            if (!signVerified) {
                throw new BusinessException(ErrorCode.PAYMENT_CALLBACK_VALIDATE_FAILS);
            }

            // 2. 解析回调参数（核心参数：订单号、支付状态、第三方流水号）
            String outTradeNo = params.get("out_trade_no"); // 商户订单号
            String tradeStatus = params.get("trade_status"); // 支付状态（TRADE_SUCCESS=支付成功）
            String tradeNo = params.get("trade_no"); // 支付宝流水号
            String gmtPayment = params.get("gmt_payment"); // 支付时间（格式：yyyy-MM-dd HH:mm:ss）

            // 3. 校验支付状态（仅处理「支付成功」的回调）
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                return false; // 非成功状态无需处理，返回 false 避免重试
            }

            // 4. 查询支付记录（避免重复处理回调）
            // 调用paymentMapper的selectOne方法，传入查询条件，返回单个Payment对象
            Payment payment = paymentMapper.selectOne(
                    // 创建LambdaQueryWrapper，指定查询的实体是Payment
                    new LambdaQueryWrapper<Payment>()
                            // 3. 拼接第一个条件：payment表的order_no字段 = outTradeNo（支付宝回调的商户订单号）
                            .eq(Payment::getOrderNo, outTradeNo)
                            // 拼接第二个条件：payment表的pay_platform字段 = ALIPAY
                            .eq(Payment::getPayType, PayTypeEnum.ALIPAY)
            );
            if (payment == null) {
                throw new BusinessException(ErrorCode.PAYMENT_NOT_EXISTS, "支付记录不存在，订单号：" + outTradeNo);
            }
            if (PayStatusEnum.SUCCESS == payment.getPayStatus()) { // 已处理过支付成功
                return false;
            }

            // 5. 更新支付记录状态
            paymentMapper.update(null,
                    new LambdaUpdateWrapper<Payment>()
                            .eq(Payment::getPaymentId, payment.getPaymentId())
                            .set(Payment::getPayStatus, PayStatusEnum.SUCCESS)
                            .set(Payment::getPlatformTradeNo, tradeNo)
                            .set(Payment::getPayTime, LocalDateTime.parse(gmtPayment, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .set(Payment::getCallbackTime, LocalDateTime.now())
                            .set(Payment::getCallbackContent, JSON.toJSONString(params))
            );

            // 6. 更新订单状态（关联现有订单流程：待支付→已支付）
            orderMapper.update(null,
                    new LambdaUpdateWrapper<Order>()
                            .eq(Order::getOrderNo, outTradeNo)
                            .set(Order::getStatus, "PAID") // 假设已支付状态为PAID
                            .set(Order::getPayTime, LocalDateTime.parse(gmtPayment, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            );

            // 7. 后续业务：发送支付成功通知、解锁库存等（可调用现有Service）
            // notificationService.sendPaySuccessMsg(order.getBuyerId(), orderId);

            return true; // 回调处理成功，返回支付宝停止重试
        } catch (Exception e) {
            // 记录错误日志（便于排查问题）
            log.error("支付宝回调处理失败：", e);
            throw new BusinessException(ErrorCode.PAYMENT_CALLBACK_FAILS);
        }
    }
}
