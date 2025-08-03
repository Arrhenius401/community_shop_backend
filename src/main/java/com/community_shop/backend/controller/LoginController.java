package com.community_shop.backend.controller;

import com.community_shop.backend.entity.DTO.request.LoginRequest;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.LocalToken;
import com.community_shop.backend.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LoginController {

    //接入数据库中user图表
    @Autowired
    private LoginService loginService;

    //联网测试方法
    @RequestMapping("/api/hello")
    public String hello(){
        return  "hello Sping boot 333";
    }

    //数据库测试方法
    @RequestMapping("/api/getUser")
    public List<User> getUser(){
        return loginService.getUser();
    }

    @RequestMapping({"/api/login/email"})
    public LocalToken loginByEmail(@RequestBody LoginRequest request){
        String email = request.getEmail();
        String password = request.getPassword();

        System.out.println("收到登录请求: " + "email = " + email + "; password = " + password); // 添加日志
        LocalToken response = loginService.loginByEmail(email, password);
        System.out.println("登录请求状态: " + response.getStatus());  //添加日志

        return response;
    }

    @RequestMapping({"/api/login/phoneNumber"})
    public LocalToken loginByPhoneNumber(@RequestBody LoginRequest request){
        String phoneNumber = request.getPhoneNumber();
        String password = request.getPassword();

        System.out.println("收到登录请求: " + "phoneNumber = " + phoneNumber + "; password = " + password); // 添加日志
        LocalToken response = loginService.loginByPhoneNumber(phoneNumber, password);
        System.out.println("登录请求状态: " + response.getStatus());  //添加日志

        return response;
    }

}
