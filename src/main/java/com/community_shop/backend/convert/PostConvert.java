package com.community_shop.backend.convert;

import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.entity.PostFollow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Post 模块对象转换器
 * 处理 Post、PostFollow 实体与 DTO 之间的映射
 */
@Mapper(componentModel = "spring", uses = ObjectMapper.class)
public interface PostConvert {

    // 单例实例（非 Spring 环境使用）
    PostConvert INSTANCE = Mappers.getMapper(PostConvert.class);

    /**
     * Post 实体 -> PostDetailDTO（帖子详情响应）
     * 映射说明：
     * 1. 实体中 postFollowCount 对应 DTO 中 commentCount
     * 2. JSON 格式图片列表转为 String 数组
     */
    @Mappings({
            @Mapping(target = "commentCount", source = "postFollowCount"),
            @Mapping(target = "imageUrls", expression = "java(jsonToArray(post.getImageUrls()))"),
            @Mapping(target = "isLiked", ignore = true) // 需业务逻辑判断，单独赋值
    })
    PostDetailDTO postToPostDetailDTO(Post post);

    /**
     * Post 实体 -> PostListItemDTO（帖子列表项）
     * 映射说明：取首图作为封面，简化发布者信息
     */
    @Mappings({
            @Mapping(target = "commentCount", source = "postFollowCount"),
            @Mapping(target = "coverImage", expression = "java(getFirstImage(post.getImageUrls()))"),
            @Mapping(target = "publisher.userId", source = "userId"),
            @Mapping(target = "publisher.username", ignore = true), // 需关联 User 实体查询后赋值
            @Mapping(source = "content", target = "summary",
                    expression = "java(post.getContent() != null ? " +
                            "(post.getContent().length() <= 20 ? " +
                            "post.getContent() : post.getContent().substring(0, 20)) : null)"
            )
    })
    PostListItemDTO postToPostListItemDTO(Post post);

    /**
     * PostPublishDTO（帖子发布请求）-> Post 实体
     * 映射说明：
     * 1. 发布时默认初始化点赞数、跟帖数为 0，状态为正常
     * 2. 图片列表以 JSON 字符串存储
     */
    @Mappings({
            @Mapping(target = "postId", ignore = true),
            @Mapping(target = "likeCount", constant = "0"),
            @Mapping(target = "postFollowCount", constant = "0"),
            @Mapping(target = "isHot", constant = "false"),
            @Mapping(target = "isEssence", constant = "false"),
            @Mapping(target = "isTop", constant = "false"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.PostStatusEnum.NORMAL)"),
            @Mapping(target = "imageUrls", source = "imageUrls") // 直接接收 JSON 字符串
    })
    Post postPublishDtoToPost(PostPublishDTO dto);

    /**
     * PostFollowPublishDTO（跟帖发布请求）-> PostFollow 实体
     * 映射说明：初始化点赞数为 0，状态为正常，父 ID 默认为 null（非嵌套回复）
     */
    @Mappings({
            @Mapping(target = "postFollowId", ignore = true),
            @Mapping(target = "parentId", defaultValue = "null"),
            @Mapping(target = "likeCount", constant = "0"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.PostFollowStatusEnum.NORMAL)")
    })
    PostFollow postFollowPublishDtoToPostFollow(PostFollowPublishDTO dto);

    /**
     * PostFollow 实体 -> PostFollowDetailDTO（跟帖详情响应）
     * 映射说明：关联跟帖人信息，初始化 isLiked 为 false
     */
    @Mappings({
            @Mapping(target = "follower.userId", source = "userId"),
            @Mapping(target = "follower.username", ignore = true), // 需关联 User 实体查询
            @Mapping(target = "follower.avatarUrl", ignore = true), // 需关联 User 实体查询
            @Mapping(target = "isLiked", constant = "false")
    })
    PostFollowDetailDTO postFollowToPostFollowDetailDTO(PostFollow postFollow);

    /**
     * 批量转换 Post 列表 -> PostListItemDTO 列表
     */
    List<PostListItemDTO> postListToPostListItemList(List<Post> posts);

    /**
     * 批量转换 PostFollow 列表 -> PostFollowDetailDTO 列表
     */
    List<PostFollowDetailDTO> postFollowListToPostFollowDetailList(List<PostFollow> postFollows);

    // ------------------------------ 辅助方法 ------------------------------
    /**
     * JSON 字符串转 String 数组
     */
    default String[] jsonToArray(String json) {
        if (json == null || json.isEmpty()) {
            return new String[0];
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, String[].class);
        } catch (JsonProcessingException e) {
            return new String[0];
        }
    }

    /**
     * 获取首张图片 URL 作为封面
     */
    default String getFirstImage(String json) {
        String[] urls = jsonToArray(json);
        return urls.length > 0 ? urls[0] : "default_cover.png"; // 默认封面图
    }
}
