package com.community_shop.backend.controller;

import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.service.impl.PostServiceImplOld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProfileController {
    @Autowired
    PostServiceImplOld postService;

    //获得指定用户所发布的帖子
    @RequestMapping("/api/post/byUserID")
    public List<Post> getMyPost(@RequestBody User user){
        //生成日志
        System.out.println("收到传输指定用户创建帖子的请求");

        Long userID = user.getUserId();

        if(userID == null){
            System.out.println("传输指定用户创建帖子的请求: 失败");
            return null;
        }
        System.out.println("传输指定用户创建帖子的请求状态: 成功");
        return postService.getAllPostByUserID(userID);
    }


}
