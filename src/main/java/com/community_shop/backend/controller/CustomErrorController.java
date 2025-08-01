package com.community_shop.backend.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    // 获取错误属性
    protected Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options){
        return  new DefaultErrorAttributes().getErrorAttributes(webRequest, options);
    }

    // 获取错误属性选项（可根据需要自定义）
    protected ErrorAttributeOptions getErrorAttributeOptions(){
        return  ErrorAttributeOptions.defaults()
                .including(ErrorAttributeOptions.Include.MESSAGE,
                        ErrorAttributeOptions.Include.EXCEPTION);
    }

    // 获取HTTP状态码（从请求中提取）
    protected HttpStatus getStatus(WebRequest webRequest){
        Integer statusCode = (Integer) webRequest.getAttribute(
                "javax.servlet.error.status_code",
                WebRequest.SCOPE_REQUEST
        );
        if(statusCode != null){
            try{
                return  HttpStatus.valueOf(statusCode);
            }catch(Exception ex){
                // 无效状态码时返回500
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    // 处理所有类型的错误请求
    @RequestMapping(path = "/error", produces = {MediaType.APPLICATION_JSON_VALUE})
    public  ResponseEntity<Map<String, Object>> handelError(WebRequest webRequest){
        // 获取默认的错误属性
        Map<String, Object> errorAttributes = getErrorAttributes(webRequest, getErrorAttributeOptions());

        // 设置自定义的错误消息
        errorAttributes.put("message", "Custom error message");

        // 从错误属性中获取HTTP状态码
        HttpStatus status = getStatus(webRequest);

        return new ResponseEntity<>(errorAttributes, status);
    }

}
