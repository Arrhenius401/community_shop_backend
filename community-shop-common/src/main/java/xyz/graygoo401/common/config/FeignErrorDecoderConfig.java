package xyz.graygoo401.common.config;

import com.alibaba.fastjson.JSON;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.exception.error.SystemErrorCode;
import xyz.graygoo401.common.vo.ResultVO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign 错误处理配置类
 * 如果 User 服务报了一个业务错（如“用户积分不足”，400错误），Feign 默认会把它封装成一个冰冷的 FeignException: [500] during ...
 * 我们要让调用方能够原封不动地抓到 User 服务的 BusinessException。
 */
@Slf4j
@Configuration
public class FeignErrorDecoderConfig implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // 1. 获取远程服务返回的 JSON 字符串
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

            // 2. 解析成 ResultVO（这是我们全员通用的格式）
            ResultVO<?> result = JSON.parseObject(body, ResultVO.class);

            // 3. 如果结果是失败的，封装回 BusinessException 抛出
            if (result != null && !result.getCode().equals("200")) {
                log.error("Feign 远程调用业务异常: {}", result.getMessage());
                // 这里需要你根据 result.getCode() 找到对应的 IErrorCode
                // 暂时可以用一个通用的处理方式，或者根据 code 重新抛出异常
                return new BusinessException(SystemErrorCode.FAILURE, result.getMessage());
            }
        } catch (IOException e) {
            log.error("解析 Feign 异常响应失败", e);
        }

        // 如果是网络层错误，使用默认处理
        return new ErrorDecoder.Default().decode(methodKey, response);
    }
}
