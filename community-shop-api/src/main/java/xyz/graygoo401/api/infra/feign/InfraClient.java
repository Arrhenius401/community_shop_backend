package xyz.graygoo401.api.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.graygoo401.api.infra.dto.message.MessageSendDTO;
import xyz.graygoo401.api.infra.dto.verification.VerifyEmailDTO;
import xyz.graygoo401.api.infra.dto.verification.VerifyPhoneDTO;
import xyz.graygoo401.common.vo.ResultVO;

@FeignClient(name = "infra-service", contextId = "infraClient")
public interface InfraClient {

    /**
     * 远程校验电话验证码（User服务注册时调用）
     */
    @PostMapping("/api/v1/infra/inner/verify-code/phone/check")
    ResultVO<Boolean> checkPhoneVerifyCode(@RequestBody VerifyPhoneDTO checkDTO);

    /**
     * 远程校验邮箱验证码（User服务注册时调用）
     */
    @PostMapping("/api/v1/infra/inner/verify-code/email/check")
    ResultVO<Boolean> checkEmailVerifyCode(@RequestBody VerifyEmailDTO checkDTO);

    /**
     * 发送通知（Trade/Community服务产生消息时调用）
     */
    @PostMapping("/api/v1/infra/inner/messages/send")
    ResultVO<Long> sendMessage(@RequestParam("senderId") Long senderId, @RequestBody MessageSendDTO sendDTO);

    /**
     * 远程删除文件（如更换头像时清理旧图）
     */
    @DeleteMapping("/api/v1/infra/inner/file/delete")
    ResultVO<Void> deleteFile(@RequestParam("objectName") String objectName, @RequestParam("userId") Long userId);
}