package com.community_shop.backend.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.community_shop.backend.annotation.AdminRequired;
import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.exception.NoPermissionException;
import com.community_shop.backend.exception.UnLoginException;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限拦截器（校验登录状态和管理员权限）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenUtil tokenUtil;;

    @Autowired
    private UserService userService; // 依赖Service层接口，无需实现具体逻辑

    //在当前代码中，我们通过抛异常的方式处理错误（由全局异常处理器统一返回响应），因此不需要直接操作 response。
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 非Controller方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 2. 登录校验：如果方法标注@LoginRequired，校验token
        if (handlerMethod.hasMethodAnnotation(LoginRequired.class)) {
            String token = request.getHeader("Authorization");

            // 调用Service层校验token有效性（Service层已实现）
            if (StringUtils.isBlank(token) || !tokenUtil.validateToken(token)) {
                throw new UnLoginException("登录已失效，请重新登录");
            }

            // 3. 验证token格式 (Bearer token)
            // 在实际项目中，Token 的格式格式通常遵循一定的规范，"Bearer "前缀符合 OAuth 2.0 协议中推荐的 Token 格式规范。，大多数后端框架（如 Spring Security）默认支持这种格式
            // 除了 Bearer Token，还有一些场景会用到其他格式，但远不如前者普遍
            if (token.startsWith("Bearer ")) {
                // 4. 提取实际token (去掉Bearer前缀)
                token = token.substring(7);
            }

            // 4. 管理员校验：如果方法标注@AdminRequired，进一步校验管理员身份
            if (handlerMethod.hasMethodAnnotation(AdminRequired.class)) {
                Long userId = tokenUtil.getUserIdByToken(token); // 通过token获取用户ID
                if (!userService.verifyRole(userId, UserRoleEnum.ADMIN)) { // Service层已实现：判断是否为管理员
                    throw new NoPermissionException("非管理员，无操作权限");
                }
            }
        }

        // 校验通过，放行请求
        return true;
    }
}
