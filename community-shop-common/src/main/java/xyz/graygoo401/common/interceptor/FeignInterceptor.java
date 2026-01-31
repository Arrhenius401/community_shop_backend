package xyz.graygoo401.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * 前端请求 User 服务时带了 Authorization: Bearer xxx
 * 此时 User 要调 Infra 发邮件，Feign 会发起一个新的 HTTP 请求，如果不处理，这个 Token 就丢了
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // 1. 从当前线程中获取原始请求头
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");

            // 2. 将 Token 复制到 Feign 的请求头中
            if (authorization != null) {
                template.header("Authorization", authorization);
            }
        }
    }
}
