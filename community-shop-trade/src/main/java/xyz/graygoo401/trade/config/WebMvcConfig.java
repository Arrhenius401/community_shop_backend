package xyz.graygoo401.trade.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.graygoo401.common.interceptor.AuthInterceptor;

/**
 * WebMvc配置类（注册拦截器）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册权限拦截器，拦截所有/api/v1/**接口
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/users/register", // 注册接口无需登录
                        "/api/v1/users/login/password", // 密码登录接口无需登录
                        "/api/v1/users/login/third-party" // 第三方登录接口无需登录
                );
    }
}
