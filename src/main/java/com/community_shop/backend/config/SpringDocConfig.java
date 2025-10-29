package com.community_shop.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc 配置类
 */
@Configuration
public class SpringDocConfig {

    /**
     * Swagger UI 页面（主要交互界面）。这是最常用的可视化接口文档页面，以交互式方式展示所有接口信息
     * http://localhost:端口号/项目上下文路径/swagger-ui/index.html
     * ----------------------------------------------------------
     * OpenAPI 规范 JSON 页面（接口元数据）。这是 Swagger UI 页面的数据来源，以 JSON 格式返回项目所有接口的元数据（符合 OpenAPI 3.0 规范）
     * http://localhost:端口号/项目上下文路径/v3/api-docs
     * ----------------------------------------------------------
     * OpenAPI 规范 YAML 页面（可选格式）。与 JSON 格式的 v3/api-docs 内容一致，但以 YAML 格式展示，更适合人类阅读
     * http://localhost:端口号/项目上下文路径/v3/api-docs.yaml
     * ----------------------------------------------------------
     * Swagger UI 快捷访问页（可选）。部分版本的 SpringDoc 会自动重定向到 swagger-ui/index.html，作为快捷访问入口（实际内容与主 UI 页面一致）。
     * http://localhost:端口号/项目上下文路径/swagger-ui.html
     * ----------------------------------------------------------
     * 分组接口文档页面（多分组场景）。仅展示该分组下的接口，方便按模块（如 “用户模块”“订单模块”）拆分文档，避免接口过多导致混乱。
     * http://localhost:端口号/项目上下文路径/swagger-ui/index.html?group=分组名称
     */

    /**
     * 显式配置 OpenAPI 文档信息，同时触发 SpringDoc 接口注册
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Graygoo401 的个人网站 API 文档")
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
