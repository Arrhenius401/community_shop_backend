package com.community_shop.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    /**
     * 显式配置 OpenAPI 文档信息，同时触发 SpringDoc 接口注册
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Arrhenius401 的个人网站 API 文档")
                        .version("v1")
                        .description("包含用户管理、社区互动、商品交易、消息通知等模块接口")
                        // 联系人信息（可选）
                        .contact(new Contact()
                        .name("Arrhenius401")
                        .email("17268287727@163.com"))
                        // 许可证信息（可选）
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
