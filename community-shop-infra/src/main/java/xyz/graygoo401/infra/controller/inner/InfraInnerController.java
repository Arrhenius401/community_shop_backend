package xyz.graygoo401.infra.controller.inner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.graygoo401.api.infra.dto.message.MessageSendDTO;
import xyz.graygoo401.api.infra.dto.verification.VerifyPhoneDTO;
import xyz.graygoo401.api.user.enums.ThirdPartyTypeEnum;
import xyz.graygoo401.common.vo.ResultVO;
import xyz.graygoo401.infra.service.base.MessageService;
import xyz.graygoo401.infra.service.impl.EmailCodeServiceImpl;
import xyz.graygoo401.infra.service.impl.PhoneCodeServiceImpl;
import xyz.graygoo401.infra.util.ThirdPartyAuthUtil;

@RestController
@RequestMapping("/api/v1/infra/inner")
public class InfraInnerController {

    @Autowired
    private EmailCodeServiceImpl emailCodeService;

    @Autowired
    private PhoneCodeServiceImpl phoneCodeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ThirdPartyAuthUtil thirdPartyAuthUtil;

    /**
     * 电话验证码校验实现
     */
    @PostMapping("/verify-code/phone/check")
    public ResultVO<Boolean> checkPhoneVerifyCode(@RequestBody VerifyPhoneDTO checkDTO) {
        boolean isOk = phoneCodeService.verifyCode(checkDTO.getPhoneNumber(), checkDTO.getVerifyCode());
        return ResultVO.success(isOk);
    }

    /**
     * 邮箱验证码校验实现
     */
    @PostMapping("/verify-code/email/check")
    public ResultVO<Boolean> checkEmailVerifyCode(@RequestBody xyz.graygoo401.api.infra.dto.verification.VerifyEmailDTO checkDTO) {
        boolean isOk = emailCodeService.verifyCode(checkDTO.getEmail(), checkDTO.getVerifyCode());
        return ResultVO.success(isOk);
    }

    /**
     * 内部消息发送实现
     */
    @PostMapping("/messages/send")
    public ResultVO<Long> sendMessage(@RequestParam Long senderId, @RequestBody MessageSendDTO sendDTO) {
        Long msgId = messageService.sendMessage(senderId, sendDTO);
        return ResultVO.success(msgId);
    }

    /**
     * 三方平台授权码获取实现
     */
    @GetMapping("/third-party/openid")
    public ResultVO<String> getOpenId(@RequestParam ThirdPartyTypeEnum platform, @RequestParam String code) {
        // 这里调用 ThirdPartyAuthUtil，去问微信服务器要 openid
        String openid = thirdPartyAuthUtil.getOpenId(platform, code);
        return ResultVO.success(openid);
    }
}
