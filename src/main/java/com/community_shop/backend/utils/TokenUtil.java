package com.community_shop.backend.utils;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * 令牌工具类
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
    private static String KEY;


    /**
     * 生成令牌
     * @param userID 用户ID
     * @return 令牌
     */
    public String generateToken(Long userID){
        JwtBuilder jwtBuilder = Jwts.builder();
        String token = jwtBuilder
                // 首部（header）
                .setHeaderParam("typ", "JWT")   //类型
                .setHeaderParam("alg", "HS256") //算法
                // 负载（payload）
                .claim("userId", userID.toString())    //自定义参数1
                .setSubject("USER")    //主题
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .setId(UUID.randomUUID().toString())
                //  签名（signature）
                .signWith(SignatureAlgorithm.HS256, KEY)  //加密算法及其密钥
                // 三部分拼装
                .compact();

        return token;
    }

    /**
     * 获取令牌中的用户ID
     * @param token
     * @return 用户ID
     */
    public Long getUserIdByToken(String token){
        Long userID = null;
        Claims claims = getAllClaimsFromToken(token);
        String userIDString = claims.get("userId").toString();
        try{
            userID = Long.valueOf(userIDString);
        }catch (Exception e){
            System.out.println("数据转换失败: " + e.getMessage());
        }

        return userID;
    }

    /**
     * 获取令牌中的过期时间（Date）
     * @param token
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 获取令牌中的过期时间（LocalDateTime）
     * @param token
     * @return 过期时间
     */
    public LocalDateTime getExpirationTimeFromToken(String token){
        Date expiration = getExpirationFromToken(token);
        LocalDateTime expirationTime = expiration.toInstant()
                .atZone(java.time.ZoneId.systemDefault())   //  转换为系统默认时区的ZonedDateTime
                .toLocalDateTime(); // 转换为LocalDateTime
        return expirationTime;
    }

    /**
     * 获取令牌中的签发时间
     * @param token
     * @return 令牌中的某个字段
     */
    public Date getIssuedAtFromToken(String token){
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 获取令牌中的某个字段
     * @param token
     * @param claimResolver
     * @return 令牌中的某个字段
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimResolver){
        final Claims claims = getAllClaimsFromToken(token);
        return claimResolver.apply(claims);
    }

    /**
     * 获取令牌中的所有字段
     * @param token
     * @return 令牌中的所有字段
     */
    private Claims getAllClaimsFromToken(String token){
        Claims claims =  Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    /**
     * 验证令牌
     * @param token
     * @return true: 验证通过
     */
    public Boolean validateToken(String token){
        // 1. 空令牌直接无效
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            // 3. 验证令牌是否过期，以及签发时间是否合理（如防止未来签发的令牌）
            if (isTokenExpired(token) || !isTokenIssuedAtReasonable(token)) {
                return false; // 令牌已过期
            }

            // 4. 可选：业务层面验证（如检查令牌是否被注销）
            // 例如：从Redis黑名单中检查是否存在该token
            // if (redisTemplate.hasKey("blacklist:" + token)) {
            //     return false; // 令牌已被注销
            // }

            // 所有验证通过
            return true;

        } catch (Exception e) {
            // 捕获所有JWT相关异常，任何异常都表示令牌无效
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
     * @param token
     */
    public void printToken(String token){
        Claims claims = getAllClaimsFromToken(token);
        System.out.println("解析token: " + "ID = " + claims.getId() + "; subject: " + claims.getSubject() + "; expiration = " + claims.getExpiration()
                + "; userId = "+ claims.get("userId") + "; username = " + claims.get("username") + "; role = " + claims.get("role")
                + "; status = " + claims.get("status"));

    }

    /**
     * 判断令牌是否过期
     * @param token
     * @return true: 过期
     */
    private Boolean isTokenExpired(String token){
        final Date expiration = getExpirationFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 判断令牌签发时间是否合理
     * @param token
     * @return 新的令牌
     */
    private Boolean isTokenIssuedAtReasonable(String token){
        final Date issuedAt = getIssuedAtFromToken(token);
        return issuedAt.before(new Date());
    }
}
