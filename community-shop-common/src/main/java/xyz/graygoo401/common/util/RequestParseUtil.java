package xyz.graygoo401.common.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.graygoo401.common.exception.UnLoginException;

/**
 * 请求解析工具类（提取用户ID等信息）
 */
@Component
public class RequestParseUtil {

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 从HttpServletRequest中解析用户ID
     * @param request 请求对象
     * @return 用户ID
     * @throws UnLoginException 当令牌无效或解析失败时抛出
     */
    public Long parseUserIdFromRequest(HttpServletRequest request) {
        // 1. 从请求头获取Authorization令牌
        String token = request.getHeader("Authorization");

        // 2. 校验令牌是否为空或无效
        if (StringUtils.isBlank(token) || !tokenUtil.validateToken(token.trim())) {
            throw new UnLoginException("登录已失效，请重新登录");
        }

        // 3. 处理Bearer前缀（符合OAuth 2.0规范）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim(); // 去掉前缀并trim避免空格问题
        }

        // 4. 调用TokenUtil解析用户ID
        Long userId = tokenUtil.getUserIdByToken(token);
        if (userId == null) {
            throw new UnLoginException("令牌解析失败，无法获取用户信息");
        }

        return userId;
    }

    /**
     * 无参获取解析登录用户的ID
     * @return 用户ID
     * @throws UnLoginException 当用户未登录时抛出
     */
    public Long parseUserIdFromRequest() {
        // RequestContextHolder 是 Spring 提供的工具类，用于在非 Web 层代码中获取当前请求的相关信息。通过 RequestContextHolder.getRequestAttributes() 可以获取到当前请求的 ServletRequestAttributes，进而获取 HttpServletRequest。
        // 这样就不需要在 Controller 的方法参数中显式声明 HttpServletRequest，也能获取到请求相关信息，符合代码的简洁性和封装性要求。
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new UnLoginException("无法获取请求上下文");
        }
        HttpServletRequest request = requestAttributes.getRequest();

        // 从请求头中获取 Authorization 信息
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new UnLoginException("请求头中缺少 Authorization 信息");
        }

        // 处理 Bearer 格式的 token（假设 token 格式为 Bearer <tokenValue>）
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7).trim() : authorization;

        // 校验 token 并获取用户 ID
        if (!tokenUtil.validateToken(token)) {
            throw new UnLoginException("token 无效或已过期");
        }
        Long userId = tokenUtil.getUserIdByToken(token);
        if (userId == null) {
            throw new UnLoginException("无法从 token 中获取用户 ID");
        }
        return userId;
    }

    /**
     * 配合gateway模块，获取当前登录用户的ID
     * @return 用户ID
     * @throws UnLoginException 当用户未登录时抛出
     */
    public Long getUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 直接从网关传过来的 Header 中获取
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            throw new UnLoginException("用户未登录");
        }
        return Long.valueOf(userIdStr);
    }
}