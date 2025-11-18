package com.community_shop.backend.utils;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云号码认证服务工具类（注：是需无企业资质的号码认证服务）
 */
@Component
public class AliyunPnsSendUtil {

    // 从配置文件注入参数
    @Value("${aliyun.pns.access-key}")
    private String accessKey;
    @Value("${aliyun.pns.secret-key}")
    private String secretKey;
    @Value("${aliyun.pns.sign-name}")
    private String signName;
    @Value("${aliyun.pns.template-code}")
    private String templateCode;
    @Value("${aliyun.pns.endpoint}")
    private String endpoint;

    // 验证码发送时间间隔（单位：秒）
    private static final Long INTERVAL = 60L;
    private static final int CACHE_TTL_CODE = 5;

    // 初始化客户端（单例，避免重复创建连接）
    public Client createPnsClient() throws Exception {
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
    public String senPnsCode(String phone, String code) throws Exception {
        Client client = createPnsClient();
        // 构建发送请求（验证码长度6位，与场景ID关联）
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"" + CACHE_TTL_CODE + "\"}")
                .setCodeLength(6L)
                .setInterval(INTERVAL);
        try {
            // 调用SDK发送短信
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request);
            // 校验短信发送状态（"OK"表示发送成功，其他为失败）
            if (!"OK".equals(response.getBody().getCode())) {
                throw new RuntimeException("发送失败：" + response.getBody().getMessage());
            }
            return response.getBody().getRequestId(); // 返回请求ID，便于日志排查
        } catch (TeaException e) {
            // 捕获阿里云SDK异常（如参数错误、余额不足）
            throw new RuntimeException("阿里云短信服务异常：" + e.getMessage(), e);
        }
    }
}
