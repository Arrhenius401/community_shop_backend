package com.community_shop.backend.service;

import com.community_shop.backend.dto.user.LoginResultDTO;
import com.community_shop.backend.utils.TokenUtil;
import com.community_shop.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckService {

    private static final String ADMIN_STRING = "ROLE_ADMIN";

    @Autowired
    private TokenUtil tokenUtil;

    //验证令牌有效性
    public boolean checkToken(LoginResultDTO loginResultDTO){
        User tokenUser = loginResultDTO.getUser();
        String token = loginResultDTO.getToken();

        boolean isValid = false;

        try {
            isValid = tokenUtil.validateToken(token, tokenUser.getUserId());
            //输出日志
            tokenUtil.printToken(token);
        }catch (Exception e){
            System.out.println("解析异常: " + e.getMessage());
        }
        System.out.println("解析前端本地令牌结果: " + isValid);

        return isValid;
    }

    //从令牌中验证管理员身份有效性
    public boolean checkAdmin(LoginResultDTO loginResultDTO){
        //判断令牌有效性
        boolean isTokenValid = checkToken(loginResultDTO);
        boolean isAdmin = false;

        if(isTokenValid){
            String token = loginResultDTO.getToken();
            String role = tokenUtil.getUserRoleFromToken(token);
            isAdmin =  role.equals(ADMIN_STRING);
        }else {
            isAdmin = false;
        }

        //生成日志
        System.out.println("检验管理员权限结果: " + isAdmin);

        return isAdmin;
    }
}
