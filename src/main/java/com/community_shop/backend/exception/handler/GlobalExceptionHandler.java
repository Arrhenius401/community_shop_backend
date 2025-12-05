package com.community_shop.backend.exception.handler;

import com.community_shop.backend.exception.AiException;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.exception.NoPermissionException;
import com.community_shop.backend.exception.UnLoginException;
import com.community_shop.backend.vo.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一捕获并处理Controller层抛出的异常
 * 遵循文档3.2节规范：按异常类型分类处理，返回标准化ResultVO
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常处理（Service层抛出的自定义异常，如信用分不足、库存不足）
     * 核心异常场景
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}（错误码：{}）", e.getMessage(), e.getCode());
        return ResultVO.fail(e.getStandardCode().toString(), e.getMessage());
    }

    /**
     * 参数校验异常处理（JSR-303校验失败，如@NotBlank/@Pattern触发）
     * 参数校验规范的配套处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<?> handleValidException(MethodArgumentNotValidException e) {
        // 获取校验失败的字段及默认提示
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验异常：{}", message);
        return ResultVO.fail("400", message);
    }

    /**
     * 未登录异常处理（权限拦截器抛出，适配@LoginRequired注解）
     * 权限拦截逻辑的配套异常处理
     */
    @ExceptionHandler(UnLoginException.class)
    public ResultVO<?> handleUnLoginException(UnLoginException e) {
        log.warn("未登录访问拦截：{}", e.getMessage());
        return ResultVO.fail("401", "请先登录");
    }

    /**
     * 权限不足异常处理（权限拦截器抛出，适配@AdminRequired注解）
     * 管理员权限校验的配套异常处理
     */
    @ExceptionHandler(NoPermissionException.class)
    public ResultVO<?> handleNoPermissionException(NoPermissionException e) {
        log.warn("权限不足拦截：{}", e.getMessage());
        return ResultVO.fail("403", "权限不足，无法操作");
    }

    /**
     * AI服务异常处理（AI服务抛出的异常，如调用OpenAI服务失败）
     * AI服务的配套异常处理，返回友好提示
     */
    public ResultVO<?> handleAiException(AiException e) {
        log.warn("AI 服务异常：{}", e.getMessage());
        return ResultVO.fail("500", e.getMessage());
    }

    /**
     * 请求方法不支持异常处理（适配@RequestMapping注解）
     * 请求方法不支持的配套异常处理，返回友好提示
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultVO<?> handleMethodNotSupported(HttpServletRequest request, Exception e) {
        String requestUrl = request.getRequestURI(); // 获取请求路径
        log.warn("接口[{}]请求方法不支持，错误信息：{}", requestUrl, e.getMessage());
        return ResultVO.fail("405", "请求方法不支持");
    }

    /**
     * 系统异常处理（兜底，捕获所有未定义异常）
     * 打印完整堆栈，返回友好提示
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<?> handleSystemException(Exception e) {
        log.error("系统异常：", e); // 打印完整堆栈便于排查
        return ResultVO.fail("500", "系统繁忙，请稍后再试");
    }
}
