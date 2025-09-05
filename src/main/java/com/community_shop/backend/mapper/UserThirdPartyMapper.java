package com.community_shop.backend.mapper;

import com.community_shop.backend.component.enums.simple.ThirdPartyTypeEnum;
import com.community_shop.backend.entity.UserThirdParty;
import org.apache.ibatis.annotations.*;

import java.util.List;

//XML文件可帮助处理复杂动态SQL场景，实现SQL与代码分离和满足高级映射需求（即多表查询）三方面
//因而辅助mapper功能的XML文件是必要的
/**
 * 第三方账号关联Mapper（注解式，无需XML）
 * 匹配《代码文档1 Mapper层设计.docx》的接口设计规范
 */
@Mapper
public interface UserThirdPartyMapper {
    /**
     * 新增第三方绑定记录（对应《代码文档1》的insert方法规范）
     */
    @Insert("INSERT INTO user_third_party (user_id, third_type, openid, access_token, bind_time, is_valid) " +
            "VALUES (#{userId}, #{thirdType}, #{openid}, #{accessToken}, #{bindTime}, #{isValid})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 自增主键返回
    int insert(UserThirdParty userThirdParty);

    /**
     * 按第三方类型+openid查询绑定记录（用于登录校验）
     * 参考《代码文档1》UserPostLikeMapper.selectIsLiked的条件查询风格
     */
    @Select("SELECT id, user_id, third_type, openid, access_token, bind_time, is_valid " +
            "FROM user_third_party " +
            "WHERE third_type = #{thirdType} AND openid = #{openid} AND isValid = 1")
    UserThirdParty selectByThirdTypeAndOpenid(@Param("thirdType") ThirdPartyTypeEnum thirdType, @Param("openid") String openid);

    /**
     * 按用户ID查询有效绑定列表（用于个人中心展示）
     * 参考《代码文档1》PostMapper.selectByUserId的分页查询逻辑
     */
    @Select("SELECT id, user_id, third_type, openid, access_token, bind_time, is_valid " +
            "FROM user_third_party " +
            "WHERE user_id = #{userId} AND isValid = 1 " +
            "ORDER BY bind_time DESC")
    List<UserThirdParty> selectValidByUserId(@Param("userId") Long userId);

    /**
     * 解绑第三方账号（逻辑删除，参考《文档4》post_follow表的is_deleted设计）
     */
    @Update("UPDATE user_third_party SET is_valid = 0 " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int updateInvalidById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 更新第三方access_token（用于凭证刷新）
     */
    @Update("UPDATE user_third_party SET access_token = #{newToken} " +
            "WHERE third_type = #{thirdType} AND openid = #{openid} AND isValid = 1")
    int updateAccessToken(@Param("thirdType") ThirdPartyTypeEnum thirdType, @Param("openid") String openid, @Param("newToken") String newToken);
}
