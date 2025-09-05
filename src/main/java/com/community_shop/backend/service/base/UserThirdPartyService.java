package com.community_shop.backend.service.base;

import com.community_shop.backend.component.enums.simple.ThirdPartyTypeEnum;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserThirdParty;

import java.util.List;

/**
 * 第三方账号Service接口
 * 匹配《代码文档2 Service层设计.docx》的接口定义规范
 */
public interface UserThirdPartyService {
    /**
     * 业务方法：第三方登录（含自动注册）
     * 对应《文档1_需求分析.docx》第三方登录需求
     * @param thirdType 第三方平台类型（WECHAT/QQ/ALIPAY）
     * @param openid 第三方平台用户唯一标识
     * @param accessToken 第三方登录临时凭证
     * @return 登录结果（含用户信息）
     */
    User login(ThirdPartyTypeEnum thirdType, String openid, String accessToken);

    /**
     * 业务方法：已注册用户绑定第三方账号
     * 对应《文档1_需求分析.docx》账号安全-第三方绑定需求
     * @param userId 平台用户ID（当前登录用户）
     * @param thirdType 第三方平台类型
     * @param openid 第三方平台用户唯一标识
     * @param accessToken 第三方登录临时凭证
     * @return 绑定结果
     */
    Boolean bind(Long userId, ThirdPartyTypeEnum thirdType, String openid, String accessToken);

    /**
     * 基础方法：解绑第三方账号
     * 参考《代码文档2》基础CRUD方法设计规范
     * @param userId 平台用户ID（当前登录用户）
     * @param bindingId 第三方绑定记录ID
     * @return 解绑结果
     */
    Boolean unbind(Long userId, Long bindingId);

    /**
     * 基础方法：查询用户已绑定的第三方账号列表
     * 参考《代码文档2》基础查询方法设计规范
     * @param userId 平台用户ID
     * @return 绑定列表结果
     */
    List<UserThirdParty> listBindings(Long userId);

    /**
     * 基础方法：更新第三方账号access_token
     * 适配《文档2_系统设计.docx》第三方登录凭证刷新需求
     * @param thirdType 第三方平台类型
     * @param openid 第三方平台用户唯一标识
     * @param newToken 新的access_token
     * @return 更新结果
     */
    Boolean updateAccessToken(ThirdPartyTypeEnum thirdType, String openid, String newToken);
}
