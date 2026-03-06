package xyz.graygoo401.api.user.feign;

import org.springframework.stereotype.Component;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.vo.ResultVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这是 UserClient 的备胎类。
 * 当远程调用 user-service 失败、超时或被限流时，会自动执行这里的代码。
 */
@Component
public class UserClientFallback implements UserClient {

    /**
     * 单个查询：获取买家/卖家脱敏信息
     */
    @Override
    public ResultVO<UserDTO> getUserById(Long userId) {
        // 逻辑：如果用户服务挂了，我返回一个虚拟的“系统游客”对象，
        // 这样订单详情页依然能打开，只是名字显示“用户信息加载中”
        UserDTO guest = new UserDTO();
        guest.setUserId(userId);
        guest.setUsername("用户暂时不可见");
        guest.setAvatarUrl("/default_avatar.png");
        return ResultVO.success(guest);
    }

    /**
     * 批量查询：查询列表页（如帖子列表）的发布者信息，极其重要（性能优化点）
     */
    @Override
    public ResultVO<Map<Long, UserDTO>> getUserMapByIds(List<Long> userIds) {
        // 逻辑：如果用户服务挂了，我返回一个虚拟的“系统游客”对象，
        // 这样订单详情页依然能打开，只是名字显示“用户信息加载中”
        UserDTO guest = new UserDTO();
        guest.setUsername("用户暂时不可见");
        guest.setAvatarUrl("/default_avatar.png");
        Map<Long, UserDTO> map = new HashMap<>();

        for (Long userId : userIds) {
            map.put(userId, guest);
        }

        return ResultVO.success(map);
    }

    /**
     * 积分变更：交易评价后远程加/减分
     */
    @Override
    public ResultVO<Boolean> updateCreditScore(Long userId, Integer scoreChange, String reason) {
        // 逻辑：如果加积分失败了，返回 false。
        // 调用方（Trade）收到 false 后可以记录日志，或者稍后重试。
        return ResultVO.fail("503", "用户中心忙，积分稍后到账");
    }

    /**
     * 角色验证：用户角色验证
     */
    @Override
    public ResultVO<Boolean> verifyRole(Long userId, UserRoleEnum role) {
        return ResultVO.fail("503", "用户中心忙，稍后重试");
    }
}