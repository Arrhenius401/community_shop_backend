package com.community_shop.backend.service.base;

import com.community_shop.backend.entity.LocalToken;
import org.springframework.stereotype.Service;

/**
 * 本地token服务
 */
@Service
public interface LocalTokenService {
    //检验token有效性
    boolean checkToken(LocalToken localToken);

    //检验admin身份
    boolean checkAdmin(LocalToken localToken);

    //检验teacher身份
    boolean checkTeacher(LocalToken localToken);

    //检验normal状态
    boolean checkNormal(LocalToken localToken);
}
