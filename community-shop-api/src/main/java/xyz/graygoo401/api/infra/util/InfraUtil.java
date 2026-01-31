package xyz.graygoo401.api.infra.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.graygoo401.api.infra.dto.message.MessageSendDTO;
import xyz.graygoo401.api.infra.dto.verification.VerifyEmailDTO;
import xyz.graygoo401.api.infra.dto.verification.VerifyPhoneDTO;
import xyz.graygoo401.api.infra.feign.InfraClient;

@Component
public class InfraUtil {

    @Autowired
    private InfraClient infraClient;

    /**
     * 验证电话验证码
     */
    public boolean checkPhoneVerifyCode(VerifyPhoneDTO checkDTO) {
        Boolean result = infraClient.checkPhoneVerifyCode(checkDTO).getData();

        // 处理空值
        if(result == null){
            return false;
        }

        return result;
    }

    /**
     * 验证邮箱验证码
     */
    public boolean checkEmailVerifyCode(VerifyEmailDTO checkDTO) {
        Boolean result = infraClient.checkEmailVerifyCode(checkDTO).getData();

        // 处理空值
        if(result == null){
            return false;
        }

        return result;
    }

    /**
     * 发送通知
     */
    public Long sendMessage(Long senderId, MessageSendDTO sendDTO) {
        return infraClient.sendMessage(senderId, sendDTO).getData();
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName, Long userId) {
        infraClient.deleteFile(objectName, userId).getData();
    }
}
