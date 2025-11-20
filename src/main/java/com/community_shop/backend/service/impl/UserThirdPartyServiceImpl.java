package com.community_shop.backend.service.impl;

import com.community_shop.backend.dto.user.ThirdPartyBindDTO;
import com.community_shop.backend.dto.user.ThirdPartyBindingListDTO;
import com.community_shop.backend.enums.SimpleEnum.ThirdPartyTypeEnum;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserThirdParty;
import com.community_shop.backend.mapper.UserMapper;
import com.community_shop.backend.mapper.UserThirdPartyMapper;
import com.community_shop.backend.service.base.UserThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 第三方账号Service实现类
 */
@Service
public class UserThirdPartyServiceImpl implements UserThirdPartyService {
    // 依赖Mapper层，符合《代码文档2》模块依赖关系（Service→Mapper）
    @Autowired
    private UserThirdPartyMapper userThirdPartyMapper;

    @Autowired
    private UserMapper userMapper;


    /**
     * 实现绑定逻辑：校验重复绑定，符合《文档1》账号安全需求
     */
    @Override
    public Boolean bind(Long userId, ThirdPartyBindDTO thirdPartyBindDTO) {
        // 1. 校验平台用户是否存在（参考《代码文档2》UserService.selectUserById逻辑）
        ThirdPartyTypeEnum thirdType = thirdPartyBindDTO.getThirdType();
        String openid = thirdPartyBindDTO.getOpenid();
        String accessToken = thirdPartyBindDTO.getAccessToken();

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
    public ThirdPartyBindingListDTO listBindings(Long userId) {
        // 1.查询用户所有有效绑定记录（参考《代码文档1》分页查询逻辑，此处简化）
        List<UserThirdParty> bindings = userThirdPartyMapper.selectValidByUserId(userId);

        // 2.转换为DTO
        ThirdPartyBindingListDTO resultDTO = new ThirdPartyBindingListDTO();
        resultDTO.setUserId(userId);
        List<ThirdPartyBindingListDTO.BindingItemDTO> bindingItems = new ArrayList<>();
        for (UserThirdParty binding : bindings) {
            ThirdPartyBindingListDTO.BindingItemDTO itemDTO = new ThirdPartyBindingListDTO.BindingItemDTO();
            itemDTO.setBindingId(binding.getId());
            itemDTO.setThirdType(binding.getThirdType());
            // OpenID脱敏（匹配文档“微信用户 ****1234”格式）
            itemDTO.setOpenidDesensitized(binding.getThirdType().name() + "用户 ****" + binding.getOpenid().substring(binding.getOpenid().length() - 4));
            itemDTO.setBindTime(binding.getBindTime());
            bindingItems.add(itemDTO);
        }

        return resultDTO;
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