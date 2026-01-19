package com.community_shop.backend.controller;

import com.community_shop.backend.dto.payment.AlipayPayDTO;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.service.impl.AlipayServiceImpl;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务接口
 */
@RestController
@RequestMapping("/api/v1/payment")
@Tag(
        name = "支付服务接口",
        description = "包含支付功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
public class PaymentController {

    @Autowired
    private AlipayServiceImpl alipayService;

    /**
     * 发起支付宝支付（前端调用，获取支付链接）
     */
    @PostMapping("/alipay/pay")
    public ResultVO<String> generatePayUrl(@Valid @RequestBody AlipayPayDTO payDTO) {
        try {
            String payUrl = alipayService.generatePaymentUrl(payDTO.getOrderId());
            return ResultVO.success(payUrl);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }

    }

    /**
     * 接收支付宝异步回调（支付宝服务器主动调用）
     */
    @PostMapping("/alipay/callback")
    public ResultVO<Boolean> handleNotify(HttpServletRequest request) {
        try {
            // 解析支付宝回调参数（request.getParameterMap转Map<String, String>）
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                params.put(key, values.length > 0 ? values[0] : "");
            });
            // 调用Service处理回调
            boolean result = alipayService.handlePayCallback(params);

            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.fail(ErrorCode.FAILURE);
        }

    }

    /**
     * 支付成功同步跳转（用户支付后跳转，仅做页面展示，不处理业务逻辑）
     */
    @GetMapping("/success")
    public String paySuccess(HttpServletRequest request) {
        // 可解析request中的订单号，展示“支付成功”页面
        String orderNo = request.getParameter("out_trade_no");
        return "<h1>订单" + orderNo + "支付成功！</h1>";
    }
}
