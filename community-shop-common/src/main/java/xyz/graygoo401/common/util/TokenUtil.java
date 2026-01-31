package xyz.graygoo401.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * 令牌工具类
 * 在分布式系统中，拦截器不应该去查数据库。所有的权限信息（角色、状态）都应该直接存在 JWT Token 的 Payload（载荷）里
 */
@Slf4j
@Component
public class TokenUtil {

    /**
     * "::" 是 Java 8 引入的 方法引用（Method Reference）语法，用于简化 Lambda 表达式
     * 方法引用允许你直接引用现有方法，而不必显式编写 Lambda 表达式的完整形式。
     * Lambda 表达式的基本语法结构为: (参数列表) -> { 函数体 }
     */

    /** 令牌过期时间 */
    @Value("${jwt.expiration}")
    private long EXPIRATION;

    /** 密钥 */
    @Value("${jwt.secret}")
    private String KEY;

    /**
     * 生成令牌（依靠用户ID生成，现已废弃）
     * @param userID 用户ID
     * @return 令牌
     */
    @Deprecated
    public String generateToken(Long userID){
        JwtBuilder jwtBuilder = Jwts.builder();
        return jwtBuilder
                // 首部（header）
                .setHeaderParam("typ", "JWT")   //类型
                .setHeaderParam("alg", "HS256") //算法
                // 负载（payload）
                .claim("userId", userID.toString())    //自定义参数1
                .setSubject("USER")    //主题
                .setIssuedAt(new Date())    //签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .setId(UUID.randomUUID().toString())
                //  签名（signature）
                .signWith(SignatureAlgorithm.HS256, KEY)  //加密算法及其密钥
                // 三部分拼装
                .compact();
    }

    /**
     * 生成令牌
     * @param userID 用户ID
     * @return 令牌
     */
    public String generateToken(Long userID, String role, String status){
        JwtBuilder jwtBuilder = Jwts.builder();
        return jwtBuilder
                // 首部（header）
                .setHeaderParam("typ", "JWT")   //类型
                .setHeaderParam("alg", "HS256") //算法
                // 负载（payload）
                .claim("userId", userID.toString())    //自定义参数1，用户ID
                .claim("role", role)                   //自定义参数2，用户角色
                .claim("status", status)               //自定义参数3，用户状态
                .setSubject("USER")    //主题
                .setIssuedAt(new Date())    //签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .setId(UUID.randomUUID().toString())
                //  签名（signature）
                .signWith(SignatureAlgorithm.HS256, KEY)  //加密算法及其密钥
                // 三部分拼装
                .compact();
    }

    /**
     * 获取令牌中的用户ID
     * @param token 令牌字符串
     * @return 用户ID
     */
    public Long getUserIdByToken(String token){
        Long userID = null;
        Claims claims = getAllClaimsFromToken(token);
        String userIDString = claims.get("userId").toString();
        try{
            userID = Long.valueOf(userIDString);
        }catch (Exception e){
            log.debug("数据转换失败: " + e.getMessage());
        }

        return userID;
    }

    /**
     * 获取令牌中的角色
     * @param token 令牌字符串
     * @return 角色
     */
    public String getUserRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 获取令牌中的过期时间（Date）
     * @param token 令牌字符串
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 获取令牌中的过期时间（LocalDateTime）
     * @param token 令牌字符串
     * @return 过期时间
     */
    public LocalDateTime getExpirationTimeFromToken(String token){
        Date expiration = getExpirationFromToken(token);
        return expiration.toInstant()
                .atZone(java.time.ZoneId.systemDefault())   //  转换为系统默认时区的ZonedDateTime
                .toLocalDateTime(); // 转换为LocalDateTime
    }

    /**
     * 获取令牌中的签发时间
     * @param token 令牌字符串
     * @return 令牌中的某个字段
     */
    public Date getIssuedAtFromToken(String token){
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 获取令牌中的某个字段
     * @param token 令牌字符串
     * @param claimResolver 获取字段的函数
     * @return 令牌中的某个字段
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimResolver){
        final Claims claims = getAllClaimsFromToken(token);
        return claimResolver.apply(claims);
    }

    /**
     * 获取令牌中的所有字段
     * @param token 令牌字符串
     * @return 令牌中的所有字段
     */
    private Claims getAllClaimsFromToken(String token){
        return Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证令牌
     * @param token 令牌字符串
     * @return true: 验证通过
     */
    public Boolean validateToken(String token){
        // 1. 空令牌直接无效
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            // 2. 验证令牌是否过期，以及签发时间是否合理（如防止未来签发的令牌）
            log.info("验证令牌: " + token);
            if (isTokenExpired(token) || !isTokenIssuedAtReasonable(token)) {
                return false; // 令牌已过期
            }

            // 3. 获取用户ID
            Long userID = getUserIdByToken(token);
            if (userID == null) {
                return false;
            }

            // 所有验证通过
            return true;

        } catch (Exception e) {
            // 捕获所有JWT相关异常，任何异常都表示令牌无效
            log.warn("令牌验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取令牌过期时间（毫秒 ）
     * @return 令牌过期时间
     */
    public Long getExpiration(){
        return EXPIRATION;
    }


    /**
     * 打印令牌
     * @param token 令牌字符串
     */
    public void printToken(String token){
        Claims claims = getAllClaimsFromToken(token);
        log.info("解析token: " + "ID = " + claims.getId() + "; subject: " + claims.getSubject() + "; expiration = " + claims.getExpiration()
                + "; userId = "+ claims.get("userId") + "; username = " + claims.get("username") + "; role = " + claims.get("role")
                + "; status = " + claims.get("status"));

    }

    /**
     * 判断令牌是否过期
     * @param token 令牌字符串
     * @return true: 过期
     */
    private Boolean isTokenExpired(String token){
        final Date expiration = getExpirationFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 判断令牌签发时间是否合理
     * @param token 令牌字符串
     * @return 新的令牌
     */
    private Boolean isTokenIssuedAtReasonable(String token){
        final Date issuedAt = getIssuedAtFromToken(token);
        return issuedAt.before(new Date());
    }
}
