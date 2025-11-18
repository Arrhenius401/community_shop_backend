package com.community_shop.backend.service.impl;

import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.service.base.VerificationCodeService;
import com.community_shop.backend.utils.CodeCacheUtil;
import com.community_shop.backend.utils.CodeGenerateUtil;
import com.community_shop.backend.utils.EmailSendUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码服务实现类
 */
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
            throw new BusinessException(ErrorCode.VERIFY_CODE_SEND_FAILED);
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
                throw new BusinessException(ErrorCode.VERIFY_CODE_NOT_EXISTS);
            }
            if (!cachedCode.equals(code)) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_NOT_MATCH);
            }
            codeCacheUtil.deleteCachedEmailCode(email);
            return true;
        } catch (Exception e) {
            return false;
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
