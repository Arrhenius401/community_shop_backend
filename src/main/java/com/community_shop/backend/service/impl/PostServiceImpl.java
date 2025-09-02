package com.community_shop.backend.service.impl;

import com.community_shop.backend.entity.Post;
import com.community_shop.backend.mapper.PostMapper;
import com.community_shop.backend.mapper.UserMapper;
import com.community_shop.backend.service.base.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class PostServiceImpl {
    public static final int HOME_POST_LIMIT = 50;
    public static final int POST_DEFAULT_OFFSET = 0;

    //标准状态
    public static final String POST_STATUS_NORMAL = "NORMAL";
    public static final String POST_STATUS_DELETED = "DELETED";
    public static final String POST_STATUS_HIDDEN = "HIDDEN";
    public static final List<String> ALLOWED_STATUS = Arrays.asList(POST_STATUS_NORMAL, POST_STATUS_HIDDEN, POST_STATUS_DELETED);

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    //home界面获取帖子
    public List<Post> getHomePost(){
        //生成日志
        System.out.println("正在获取主页帖子");
        List<Post> posts = null;
        try{
            posts = postMapper.getAllPostAddLimit(POST_STATUS_NORMAL, HOME_POST_LIMIT);
            posts = fillDbPost(posts);
        }catch (Exception e){
            System.out.println("获取帖子失败: " + e.getMessage());
        }

        return posts;
    }

    //对数据库原生Post进行填充
    public List<Post> fillDbPost(List<Post> posts){
        for(Post post : posts){
            String username = userMapper.getUsernameByID(post.getUserID());
//            post.setUsername(username);
        }
        return posts;
    }

    //更新帖子状态
    public Boolean updatePostStatus(Long postID, String status){
        //测试
        System.out.println("postID: " + postID + "; status: " + status);
        if(postID == null){
            return false;
        }

        Post post = postMapper.selectById(postID);

        if(post == null || !ALLOWED_STATUS.contains(status)){
            return false;
        }

        postMapper.updatePostStatus(postID, status);
        return true;
    }

    //获得所有帖子
    
    public List<Post> getAllPosts(){
        //生成日志
        System.out.println("正在获取所有帖子");
        List<Post> posts = null;

        try {
            posts = postMapper.getAllPost();
            posts = fillDbPost(posts);
        }catch (Exception e){
            System.out.println("获取帖子失败: " + e.getMessage());
        }

        return posts;
    }

    // 获取帖子详情

    public Post getPostById(Long id){
        return postMapper.selectById(id);
    }

    // 添加帖子

    public int addPost(Post post){
        return postMapper.addPost(post);
    }

    // 更新帖子信息

    public int updatePost(Post post){
        return postMapper.updatePost(post);
    }

    // 删除帖子

    public int deletePost(Long id){
        return postMapper.deletePost(id);
    }

    //通过userID获得用户发布的所有帖子
    public List<Post> getAllPostByUserID(Long userID){
        return postMapper.getAllPostByUserID(userID);
    }
}
