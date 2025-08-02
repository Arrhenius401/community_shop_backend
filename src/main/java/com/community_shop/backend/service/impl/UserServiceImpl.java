package com.community_shop.backend.service.impl;

import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserServiceImpl {
    //标准状态
    public static final String USER_STATUS_NORMAL = "NORMAL";
    public static final String USER_STATUS_BANNED = "BANNED";
    public static final List<String> ALLOWED_STATUS = Arrays.asList(USER_STATUS_NORMAL, USER_STATUS_BANNED);

    @Autowired
    UserMapper userMapper;

    //更新帖子状态
    public Boolean updateUserStatus(Long userID, String status){
        if(userID == null){
            return false;
        }

        User user = userMapper.getUserByID(userID);

        if(user == null || !ALLOWED_STATUS.contains(status)){
            return false;
        }

        userMapper.updateUserStatus(userID, status);
        return true;
    }

    //风险方法
    //获得所有用户
    public List<User> getAllUser(){
        //生成日志
        System.out.println("正在获取所有用户");
        List<User> users = null;

        try {
            users = userMapper.getAllUser();
        }catch (Exception e){
            System.out.println("获取用户失败: " + e.getMessage());
        }

        return users;
    }
}
