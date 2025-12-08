package com.community_shop.backend.service.impl;

import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.service.base.VerificationCodeService;
import com.community_shop.backend.utils.AliyunPnsSendUtil;
import com.community_shop.backend.utils.CodeCacheUtil;
import com.community_shop.backend.utils.CodeGenerateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 手机号验证码服务实现类
 */
@Slf4j
@Service
public class PhoneCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private AliyunPnsSendUtil pnsSendUtil;

    @Autowired
    private CodeCacheUtil codeCacheUtil;

    @Autowired
    private CodeGenerateUtil codeGenerateUtil;

    /**
     * 发送验证码
     * @param phone 手机号
     * @return 验证码
     */
    @Override
    public String sendCode(String phone) {
        String code = codeGenerateUtil.generate6DigitCode();
        try {
            String result = pnsSendUtil.senPnsCode(phone, code);
            codeCacheUtil.cachePhoneCode(phone, code);
            return result;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_SEND_FAILED, e.getMessage());
        }
    }

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    @Override
    public boolean verifyCode(String phone, String code) {
        try {
            String cachedCode = codeCacheUtil.getCachePhoneCode(phone);
            if (cachedCode == null) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_NOT_EXISTS);
            }
            if (!cachedCode.equals(code)) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_NOT_MATCH);
            }
            codeCacheUtil.deleteCachedPhoneCode(phone);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_VERIFY_FAILED, e.getMessage());
        }
    }

    /**
     * 删除验证码
     * @param phone 手机号
     */
    @Override
    public void deleteCode(String phone) {
        codeCacheUtil.deleteCachedPhoneCode(phone);
    }
}
