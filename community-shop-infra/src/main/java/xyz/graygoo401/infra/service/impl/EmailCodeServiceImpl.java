package xyz.graygoo401.infra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.util.CodeGenerateUtil;
import xyz.graygoo401.infra.exception.error.VerifyErrorCode;
import xyz.graygoo401.infra.service.base.VerificationCodeService;
import xyz.graygoo401.infra.util.CodeCacheUtil;
import xyz.graygoo401.infra.util.EmailSendUtil;

/**
 * 邮箱验证码服务实现类
 */
@Slf4j
@Service
public class EmailCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private EmailSendUtil emailSendUtil;

    @Autowired
    private CodeCacheUtil codeCacheUtil;

    @Autowired
    private CodeGenerateUtil codeGenerateUtil;

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @return 验证码
     */
    @Override
    public String sendCode(String email) {
        String code = codeGenerateUtil.generate6DigitCode();
        try {
            emailSendUtil.sendSimpleCodeEmail(email, code);
            codeCacheUtil.cacheEmailCode(email, code);
            return code;
        } catch (Exception e) {
            throw new BusinessException(VerifyErrorCode.VERIFY_CODE_SEND_FAILED, e.getMessage());
        }
    }

    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     * @return 验证结果
     */
    @Override
    public boolean verifyCode(String email, String code) {
        try {
            String cachedCode = codeCacheUtil.getCacheEmailCode(email);
            if (cachedCode == null) {
                throw new BusinessException(VerifyErrorCode.VERIFY_CODE_NOT_EXISTS);
            }
            if (!cachedCode.equals(code)) {
                throw new BusinessException(VerifyErrorCode.VERIFY_CODE_NOT_MATCH);
            }
            codeCacheUtil.deleteCachedEmailCode(email);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(VerifyErrorCode.VERIFY_CODE_VERIFY_FAILED, e.getMessage());
        }
    }

    /**
     * 删除邮箱验证码
     */
    @Override
    public void deleteCode(String email) {
        codeCacheUtil.deleteCachedEmailCode(email);
    }

}
