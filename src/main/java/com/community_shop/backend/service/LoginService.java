package com.community_shop.backend.service;

import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.LocalToken;
import com.community_shop.backend.mapper.UserMapper;
import com.community_shop.backend.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginService {
    //接入数据库中user图表
    @Autowired
    private UserMapper userMapper;

    //JWT令牌相关
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    //数据库测试方法
    public List<User> getUser(){
        return userMapper.getAllUsers();
    }

    //使用邮箱地址登录
    public LocalToken loginByEmail(String email, String password){
        LocalToken response = new LocalToken();
        String token = "";
        String status = "";

        User user = getUserByEmail(email);
        if(user == null){
            //用户不存在，提示注册
            token = generateToken(user);
            status = "wrong credential";
        }else {
            String db_password = user.getPassword();
            if(password.equals(db_password)){
                //用户名和密码匹配
                status = "ok";
            }else{
                status =  "wrong password";
            }
        }

        response.setSubject("LOGIN");
        response.setStatus(status);
        response.setToken(token);
        response.setUser(user);

        return response;
    }

    //使用电话号码登录
    public LocalToken loginByPhoneNumber(String phoneNumber, String password){
        LocalToken response = new LocalToken();
        String token = "";
        String status = "";

        User user = getUserByPhone(phoneNumber);
        if(user == null){
            //用户不存在，提示注册
            status = "wrong credential";
        }else {
            String db_password = user.getPassword();
            if(password.equals(db_password)){
                //用户名和密码匹配
                token = generateToken(user);
                status = "ok";
            }else{
                status = "wrong password";
            }
        }

        response.setSubject("LOGIN");
        response.setStatus(status);
        response.setToken(token);
        response.setUser(user);

        return response;
    }

    //使用电话号码得到User
    public User getUserByPhone(String phoneNumber){
        Long dbID = userMapper.getIDByPhoneNumber(phoneNumber);
        if(dbID == null){
            return null;
        }
        User user = userMapper.getUserByID(dbID);
        return user;
    }

    //使用邮箱号码得到User
    public User getUserByEmail(String email){
        Long dbID = userMapper.getIDByEmail(email);
        if(dbID == null){
            return null;
        }
        User user = userMapper.getUserByID(dbID);
        return user;
    }

    //创建token
    public String generateToken(User user){
        String token = jwtTokenUtil.generateToken("LOGIN", user.getUserID().toString(), user.getUsername(), user.getRole().toString(), user.getStatus().toString());
        return token;
    }
}
