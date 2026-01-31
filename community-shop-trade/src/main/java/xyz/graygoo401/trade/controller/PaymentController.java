package xyz.graygoo401.trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.graygoo401.api.trade.dto.payment.AlipayPayDTO;
import xyz.graygoo401.common.annotation.LoginRequired;
import xyz.graygoo401.common.util.RequestParseUtil;
import xyz.graygoo401.common.vo.ResultVO;
import xyz.graygoo401.trade.service.impl.AlipayServiceImpl;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@Tag(
        name = "支付服务接口",
        description = "包含支付功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
public class PaymentController {

    @Autowired
    private AlipayServiceImpl alipayService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 发起支付宝支付（前端调用，获取支付链接）
     */
    @PostMapping("/alipay/pay")
    @LoginRequired
    @Operation(
            summary = "发起支付宝支付接口",
            description = "用户发起支付宝支付，返回支付链接"
    )
    public ResultVO<String> generatePayUrl(@Valid @RequestBody AlipayPayDTO payDTO) {
        String payUrl = alipayService.generatePaymentUrl(payDTO.getOrderId(), parseUserIdFromToken());
        return ResultVO.success(payUrl);

    }

    /**
     * 接收支付宝异步回调（支付宝服务器主动调用）
     */
    @PostMapping("/alipay/callback")
    @Operation(
            summary = "接收支付宝异步回调接口",
            description = "支付宝服务器主动调用，处理回调参数"
    )
    public String handleNotify(HttpServletRequest request) {
        // 1. 解析支付宝回调参数（request.getParameterMap转Map<String, String>）
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            params.put(key, values.length > 0 ? values[0] : "");
        });
        // 2. 调用Service处理回调
        try {
            boolean result = alipayService.handlePayCallback(params);
            if (result) {
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            return "fail";
        }
    }

    /**
     * 支付成功同步跳转（用户支付后跳转，仅做页面展示，不处理业务逻辑）
     */
    @GetMapping("/success")
    @Operation(
            summary = "支付成功同步跳转接口",
            description = "用户支付成功后跳转的页面"
    )
    public String paySuccess(HttpServletRequest request) {
        // 可解析request中的订单号，展示“支付成功”页面
        String orderNo = request.getParameter("out_trade_no");
        return "<h1>订单" + orderNo + "支付成功！</h1>";
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID（未登录时返回null）
     */
    private Long parseUserIdFromToken() {
        return requestParseUtil.parseUserIdFromRequest();
    }
}
