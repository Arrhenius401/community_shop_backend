package com.community_shop.backend.config;

import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * CORS是Cross-Origin Resource Sharing（跨源资源共享）的缩写
 * 用于解决浏览器的同源策略限制，允许前端应用不同源（域名、端口、协议）访问后端资源
 * 除非前后端整合在一起，否则前端后端一般运行在不同端口上
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 创建CORS过滤器
     * @return CORS过滤器
     */
    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config = new CorsConfiguration();

        //1,设置允许跨域请求的域名
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://localhost");
        config.addAllowedOriginPattern("https://localhost:*");
        config.addAllowedOriginPattern("https://localhost");

        //2,允许任何请求头
        config.addAllowedHeader("*");
        //3,允许任何方法
        config.addAllowedMethod("*");
        //4,允许携带cookie
        config.setAllowCredentials(true);
        //5,预检请求的缓存时间（10分钟）
        config.setMaxAge(600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);

        return new CorsFilter(source);
    }
}
