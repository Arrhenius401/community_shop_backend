package xyz.graygoo401.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.graygoo401.common.util.TokenUtil;

/**
 * 全局过滤器：
 * 1. 登录校验
 * 2. 提取用户信息并传递
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行（注册、登录等）
        if (path.contains("/login") || path.contains("/register") || path.contains("/v3/api-docs")) {
            return chain.filter(exchange);
        }

        // 2. 提取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 3. 校验 Token
        if (token == null || !tokenUtil.validateToken(token)) {
            // Token 无效，直接返回 401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 4. 【核心】提取用户信息并传递
        Long userId = tokenUtil.getUserIdByToken(token);
        String role = tokenUtil.getUserRoleFromToken(token);

        // 关键：将 userId 存入 Header，后端服务直接从 Header 拿，不再需要解析 Token
        ServerHttpRequest newRequest = request.mutate()
                .header("X-User-Id", userId.toString())
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    @Override
    public int getOrder() {
        return -1; // 优先级最高
    }
}
