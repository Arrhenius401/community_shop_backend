package xyz.graygoo401.api.user.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.api.user.feign.UserClient;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.exception.error.SystemErrorCode;

import java.util.List;
import java.util.Map;

/**
 * user 模块工具类
 */
@Component
public class UserUtil {

    @Autowired
    private UserClient userClient;

    /**
     * 根据用户id获取用户信息
     */
    public UserDTO getUserById(Long userId) {
        UserDTO user = userClient.getUserById(userId).getData();
        if (user == null) {
            throw new BusinessException(SystemErrorCode.USER_NOT_EXISTS);
        }
        return user;
    }

    /**
     * 批量获取用户信息
     */
    public Map<Long, UserDTO> getUserMapByIds(List<Long> userIds) {
        Map<Long, UserDTO> userMap = userClient.getUserMapByIds(userIds).getData();
        if (userMap == null) {
            throw new BusinessException(SystemErrorCode.USER_NOT_EXISTS);
        }
        return userMap;
    }

    /**
     * 积分变更
     */
    public boolean updateCreditScore(Long userId, Integer scoreChange, String reason) {
        Boolean result = userClient.updateCreditScore(userId, scoreChange, reason).getData();

        // 处理空值
        if(result == null){
            return false;
        }

        return result;
    }

    /**
     * 验证用户角色
     */
    public boolean verifyRole(Long userId, UserRoleEnum role) {
        Boolean result = userClient.verifyRole(userId, role).getData();

        // 处理空值
        if(result == null){
            return false;
        }

        return result;
    }

}
