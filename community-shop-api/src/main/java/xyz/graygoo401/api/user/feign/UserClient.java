package xyz.graygoo401.api.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.vo.ResultVO;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", contextId = "userClient")
public interface UserClient {

    /**
     * 单个查询：获取买家/卖家脱敏信息
     */
    @GetMapping("/api/v1/users/inner/{userId}")
    ResultVO<UserDTO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 批量查询：查询列表页（如帖子列表）的发布者信息，极其重要（性能优化点）
     */
    @GetMapping("/api/v1/users/inner/batch")
    ResultVO<Map<Long, UserDTO>> getUserMapByIds(@RequestParam("userIds") List<Long> userIds);

    /**
     * 积分变更：交易评价后远程加/减分
     */
    @PostMapping("/api/v1/users/inner/credit/update")
    ResultVO<Boolean> updateCreditScore(@RequestParam("userId") Long userId,
                                        @RequestParam("scoreChange") Integer scoreChange,
                                        @RequestParam("reason") String reason);

    /**
     * 角色验证：用户角色验证
     */
    @GetMapping("/api/v1/users/inner/verify/role")
    ResultVO<Boolean> verifyRole(@RequestParam("userId") Long userId,
                                 @RequestParam("role") UserRoleEnum role);
}
