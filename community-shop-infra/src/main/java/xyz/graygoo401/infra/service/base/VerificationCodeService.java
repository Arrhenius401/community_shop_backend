package xyz.graygoo401.infra.service.base;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param credentials 登录凭证（手机号/邮箱）
     * @return 验证码
     */
    String sendCode(String credentials);

    /**
     * 验证验证码
     * @param credentials 登录凭证（手机号/邮箱）
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyCode(String credentials, String code);

    /**
     * 删除验证码
     * @param credentials 登录凭证（手机号/邮箱）
     */
    void deleteCode(String credentials);
}
