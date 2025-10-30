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
import org.springframework.lang.Nullable;
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

    /**
     * 拦截处理方法
     * 在当前代码中，我们通过抛异常的方式处理错误（由全局异常处理器统一返回响应），因此不需要直接操作 response
     * Spring Security 拦截器优先于自定义的 AuthInterceptor 执行，因此放行一个接口还需要再 SecurityConfig 中配置放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 0.放行无需认证的接口
        // 放行 Swagger 相关请求，直接返回 true
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/swagger-ui")
                || requestUri.contains("/v3/api-docs")
                || requestUri.contains("/webjars/")
                || requestUri.contains("/swagger-resources/")) {
            return true;
        }

        // 放行注册和登录接口
        if (requestUri.contains("/api/v1/users/register")
                || requestUri.contains("/api/v1/users/login")) {
            return true;
        }

        // 1. 非Controller方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 2. 登录校验：如果方法标注@LoginRequired，校验token
        if (handlerMethod.hasMethodAnnotation(LoginRequired.class)) {
            // 从请求头获取Authorization令牌
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

    // 后置处理与完成处理方法默认空实现（无需业务逻辑）
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
