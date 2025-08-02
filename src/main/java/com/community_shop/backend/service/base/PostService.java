package com.community_shop.backend.service.base;

import com.community_shop.backend.entity.Order;
import com.community_shop.backend.entity.Post;

import java.util.List;

public interface PostService {
    // 获取所有帖子
    List<Order> getAllPosts();

    // 获取帖子详情
    Post getPostById(int id);

    // 添加帖子
    int addPost(Post post);

    // 更新帖子信息
    int updatePost(Post post);

    // 删除帖子
    int deletePost(int id);
}
