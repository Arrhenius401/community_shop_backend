package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.user.LoginResultDTO;
import org.springframework.stereotype.Service;

/**
 * 本地token服务
 */
@Service
public interface LocalTokenService {
    //检验token有效性
    boolean checkToken(LoginResultDTO loginResultDTO);

    //检验admin身份
    boolean checkAdmin(LoginResultDTO loginResultDTO);

    //检验teacher身份
    boolean checkTeacher(LoginResultDTO loginResultDTO);

    //检验normal状态
    boolean checkNormal(LoginResultDTO loginResultDTO);
}
