package com.community_shop.backend.controller;


import com.community_shop.backend.entity.User;
import com.community_shop.backend.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class RegisterController {
    @Autowired
    private RegisterService registerService;

    @RequestMapping("api/register/default")
    public String insertDefaultUser(@RequestBody User user){
        //获取前端表格参数
        String username = user.getUsername();
        String email = user.getEmail();
        String phoneNumber = user.getPhoneNumber();
        String password = user.getPassword();

        //获取当前时间的标准字符表达式
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:dd:ss");   //'m'表示分钟，'h'表示12时制的小时
        String initDate = now.format(formatter);

        //添加日志
        System.out.printf("收到注册请求: username = %s; email = %s; phoneNumber = %d; password = %s; initDate = %s%n",
                username, email, phoneNumber, password, initDate);

        String status = registerService.insertDefaultUser(username, email, phoneNumber, password, initDate);

        //添加日志
        System.out.println("注册请求状态: " + status);

        return status;
    }
}
