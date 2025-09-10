package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.enums.SimpleEnum.ThirdPartyTypeEnum;
import com.community_shop.backend.entity.UserThirdParty;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 第三方账号关联模块Mapper接口，严格对应user_third_party表结构（文档4_数据库设计.docx）
 */
@Mapper
public interface UserThirdPartyMapper extends BaseMapper<UserThirdParty> {

    // ==================== 基础CRUD ====================
    /**
     * 新增第三方账号绑定记录
     * @param userThirdParty 第三方关联实体（含用户ID、openid、平台类型等核心字段）
     * @return 影响行数
     */
    int insert(UserThirdParty userThirdParty);

    /**
     * 通过绑定记录ID查询详情
     * @param id 自增主键ID
     * @return 第三方关联实体
     */
    UserThirdParty selectById(@Param("id") Long id);


    // ==================== 关联查询 ====================
    /**
     * 通过平台类型+openid查询有效绑定记录（第三方登录校验）
     * @param thirdType 第三方平台类型（枚举）
     * @param openid 第三方平台用户唯一标识
     * @return 第三方关联实体（null表示未绑定）
     */
    UserThirdParty selectByThirdTypeAndOpenid(
            @Param("thirdType") ThirdPartyTypeEnum thirdType,
            @Param("openid") String openid
    );

    /**
     * 查询用户所有有效绑定的第三方账号
     * @param userId 关联平台用户ID
     * @return 第三方关联列表
     */
    List<UserThirdParty> selectValidByUserId(@Param("userId") Long userId);

    /**
     * 统计用户某类第三方账号的绑定数量（限制同一平台仅绑定一个）
     * @param userId 用户ID
     * @param thirdType 第三方平台类型（枚举）
     * @return 绑定数量（0=未绑定，1=已绑定）
     */
    int countByUserIdAndThirdType(
            @Param("userId") Long userId,
            @Param("thirdType") ThirdPartyTypeEnum thirdType
    );


    // ==================== 状态与凭证更新 ====================
    /**
     * 解绑第三方账号（逻辑删除，更新绑定状态为无效）
     * @param id 绑定记录ID
     * @param userId 用户ID（身份校验）
     * @return 影响行数
     */
    int updateInvalidById(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    /**
     * 更新第三方登录临时凭证（access_token过期刷新）
     * @param thirdType 第三方平台类型（枚举）
     * @param openid 第三方平台用户唯一标识
     * @param newToken 新的access_token
     * @return 影响行数
     */
    int updateAccessToken(
            @Param("thirdType") ThirdPartyTypeEnum thirdType,
            @Param("openid") String openid,
            @Param("newToken") String newToken
    );

    /**
     * 批量解绑用户所有第三方账号（用户注销时联动清理）
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchUpdateInvalidByUserId(@Param("userId") Long userId);
}
