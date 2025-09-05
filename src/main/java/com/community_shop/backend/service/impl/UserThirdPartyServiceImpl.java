package com.community_shop.backend.service.impl;

import com.community_shop.backend.component.enums.ThirdPartyTypeEnum;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserThirdParty;
import com.community_shop.backend.mapper.UserMapper;
import com.community_shop.backend.mapper.UserThirdPartyMapper;
import com.community_shop.backend.service.base.UserThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 第三方账号Service实现类
 * 匹配《代码文档2 Service层设计.docx》的实现规范（事务控制、Mapper依赖、业务逻辑）
 */
@Service
public class UserThirdPartyServiceImpl implements UserThirdPartyService {
    // 依赖Mapper层，符合《代码文档2》模块依赖关系（Service→Mapper）
    @Autowired
    private UserThirdPartyMapper userThirdPartyMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 实现第三方登录逻辑：事务控制确保"注册+绑定"原子性
     * 匹配《代码文档2》OrderService.createOrder的事务管理规范
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User login(ThirdPartyTypeEnum thirdType, String openid, String accessToken) {
        // 1. 查询第三方账号是否已绑定平台用户（参考《代码文档1》UserPostLikeMapper.selectIsLiked逻辑）
        UserThirdParty boundRecord = userThirdPartyMapper.selectByThirdTypeAndOpenid(thirdType, openid);

        if (boundRecord != null) {
            // 2. 已绑定：返回用户信息，同步更新access_token
            User user = userMapper.selectById(boundRecord.getUserId());
            if (user == null) {
                throw new RuntimeException("绑定的平台用户已注销");
            }
            // 若凭证变更，更新access_token（适配《文档2》第三方API调用需求）
            if (!accessToken.equals(boundRecord.getAccessToken())) {
                userThirdPartyMapper.updateAccessToken(thirdType, openid, accessToken);
            }
            return user;
        } else {
            // 3. 未绑定：自动创建平台用户+绑定（符合《文档1》第三方注册需求）
            // 自动生成用户名（参考《文档4_数据库工作（新）.docx》user表字段规范）
            String username = "用户_" + thirdType + "_" + openid.substring(0, 6);
            User newUser = new User();
            userMapper.insert(newUser);

            // 绑定第三方账号（参考《代码文档1》UserThirdPartyMapper.insert逻辑）
            // 返回自增的userId（MyBatis-Plus会自动回填自增ID）
            UserThirdParty newBinding = new UserThirdParty(newUser.getUserId(), thirdType, openid, accessToken);
            userThirdPartyMapper.insert(newBinding);

            return newUser;
        }
    }

    /**
     * 实现绑定逻辑：校验重复绑定，符合《文档1》账号安全需求
     */
    @Override
    public Boolean bind(Long userId, ThirdPartyTypeEnum thirdType, String openid, String accessToken) {
        // 1. 校验平台用户是否存在（参考《代码文档2》UserService.selectUserById逻辑）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("平台用户不存在");
        }

        // 2. 校验第三方账号是否已绑定其他用户（参考《代码文档1》UserThirdPartyMapper.selectByThirdTypeAndOpenid）
        UserThirdParty existingBinding = userThirdPartyMapper.selectByThirdTypeAndOpenid(thirdType, openid);
        if (existingBinding != null) {
            throw new RuntimeException("该" + thirdType + "账号已绑定其他平台用户");
        }

        // 3. 校验用户是否已绑定同类型第三方账号（避免重复绑定）
        List<UserThirdParty> userBindings = userThirdPartyMapper.selectValidByUserId(userId);
        boolean hasSameType = userBindings.stream().anyMatch(binding -> thirdType.equals(binding.getThirdType()));
        if (hasSameType) {
            throw new RuntimeException("您已绑定过" + thirdType + "账号，不可重复绑定");
        }

        // 4. 执行绑定操作
        UserThirdParty binding = new UserThirdParty(userId, thirdType, openid, accessToken);
        userThirdPartyMapper.insert(binding);

        return true;
    }

    /**
     * 实现解绑逻辑：逻辑删除（参考《文档4》post_follow表is_deleted设计）
     */
    @Override
    public Boolean unbind(Long userId, Long bindingId) {
        // 执行逻辑解绑（更新is_valid=0，避免物理删除数据）
        int affectedRows = userThirdPartyMapper.updateInvalidById(bindingId, userId);
        if (affectedRows <= 0) {
            throw new RuntimeException("解绑失败：绑定记录不存在或无权限");
        }
        return true;
    }

    /**
     * 实现绑定列表查询：仅返回有效绑定记录
     */
    @Override
    public List<UserThirdParty> listBindings(Long userId) {
        // 查询用户所有有效绑定记录（参考《代码文档1》分页查询逻辑，此处简化）
        return userThirdPartyMapper.selectValidByUserId(userId);
    }

    /**
     * 实现access_token更新：适配第三方凭证过期场景
     */
    @Override
    public Boolean updateAccessToken(ThirdPartyTypeEnum thirdType, String openid, String newToken) {
        int affectedRows = userThirdPartyMapper.updateAccessToken(thirdType, openid, newToken);
        if (affectedRows <= 0) {
            throw new RuntimeException("更新失败：第三方绑定记录不存在");
        }
        return true;
    }
}