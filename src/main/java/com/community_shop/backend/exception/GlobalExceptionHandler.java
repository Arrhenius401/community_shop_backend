package com.community_shop.backend.exception;

import com.community_shop.backend.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
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
     * 文档3.2节明确的核心异常场景
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}（错误码：{}）", e.getMessage(), e.getCode());
        return ResultVO.fail(e.getStandardCode().toString(), e.getMessage());
    }

    /**
     * 参数校验异常处理（JSR-303校验失败，如@NotBlank/@Pattern触发）
     * 文档3.2节与2.2.2节参数校验规范的配套处理
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
     * 文档4.2节权限拦截逻辑的配套异常处理
     */
    @ExceptionHandler(UnLoginException.class)
    public ResultVO<?> handleUnLoginException(UnLoginException e) {
        log.warn("未登录访问拦截：{}", e.getMessage());
        return ResultVO.fail("401", "请先登录");
    }

    /**
     * 权限不足异常处理（权限拦截器抛出，适配@AdminRequired注解）
     * 文档4.2节管理员权限校验的配套异常处理
     */
    @ExceptionHandler(NoPermissionException.class)
    public ResultVO<?> handleNoPermissionException(NoPermissionException e) {
        log.warn("权限不足拦截：{}", e.getMessage());
        return ResultVO.fail("403", "权限不足，无法操作");
    }

    /**
     * 系统异常处理（兜底，捕获所有未定义异常）
     * 文档3.2节要求：打印完整堆栈，返回友好提示
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<?> handleSystemException(Exception e) {
        log.error("系统异常：", e); // 打印完整堆栈便于排查
        return ResultVO.fail("500", "系统繁忙，请稍后再试");
    }
}
