package xyz.graygoo401.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "xyz.graygoo401.api")    // 扫描 Feign 客户端
@SpringBootApplication(scanBasePackages = {
        "xyz.graygoo401.common", // 扫描 common 模块的 Bean（如异常处理、拦截器）
        "xyz.graygoo401.api",    // 扫描 api 模块的 Bean
        "xyz.graygoo401.community"    // 扫描自己模块的 Bean
})
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
