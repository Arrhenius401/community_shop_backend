package xyz.graygoo401.community.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类（必须添加，否则分页等功能无法使用）
 */
@Configuration
@MapperScan(basePackages = "xyz.graygoo401.community.dao.mapper") // 扫描Mapper接口（与mybatis-config.xml的mappers配置二选一）
public class MyBatisConfig {

    /**
     * 注册分页插件（支持BaseMapper的selectPage方法）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加MySQL分页插件（根据数据库类型选择）
        interceptor.addInnerInterceptor(new com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor());
        return interceptor;
    }
}
