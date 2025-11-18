package com.community_shop.backend.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliyunSmsSendUtil {

    // 从配置文件注入参数
    @Value("${aliyun.sms.access-key}")
    private String accessKey;
    @Value("${aliyun.sms.secret-key}")
    private String secretKey;
    @Value("${aliyun.sms.sign-name}")
    private String signName;
    @Value("${aliyun.sms.template-code}")
    private String templateCode;
    @Value("${aliyun.sms.endpoint}")
    private String endpoint;

    /**
     * 初始化短信服务客户端（处理签名、HTTP连接）
     */
    private Client createSmsClient() throws Exception {
        Config config = new Config()
                // 身份认证：AccessKey ID/Secret
                .setAccessKeyId(accessKey)
                .setAccessKeySecret(secretKey);
        // 短信服务固定Endpoint（无需修改）
        config.endpoint = endpoint;
        return new Client(config);
    }

    /**
     * 发送验证码短信
     * @param phone 目标手机号（如13800138000）
     * @param code 已生成的验证码（如123456）
     * @return 阿里云返回的请求ID（用于问题排查）
     * @throws Exception 发送失败时抛出异常
     */
    public String sendSmsCode(String phone, String code) throws Exception {
        Client client = createSmsClient();
        // 组装短信请求参数：模板变量需与模板中的变量名一致（如模板用${code}，这里key就为"code"）
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phone) // 接收手机号（支持多个，用逗号分隔）
                .setSignName(signName) // 短信签名（无需加【】）
                .setTemplateCode(templateCode) // 验证码模板ID
                .setTemplateParam("{\"code\":\"" + code + "\"}"); // 模板变量（JSON格式）

        try {
            // 调用SDK发送短信
            SendSmsResponse response = client.sendSms(sendSmsRequest);
            // 校验短信发送状态（"OK"表示发送成功，其他为失败）
            if (!"OK".equals(response.getBody().getCode())) {
                throw new RuntimeException("短信发送失败：" + response.getBody().getMessage());
            }
            return response.getBody().getRequestId(); // 返回请求ID，便于日志排查
        } catch (TeaException e) {
            // 捕获阿里云SDK异常（如参数错误、余额不足）
            throw new RuntimeException("阿里云短信服务异常：" + e.getMessage(), e);
        }
    }
}
