package xyz.graygoo401.user.controller.inner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.vo.ResultVO;
import xyz.graygoo401.user.convert.UserConvert;
import xyz.graygoo401.user.dao.entity.User;
import xyz.graygoo401.user.service.base.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/inner")
public class UserInnerController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserConvert userConvert;

    /**
     * 单个查询实现
     */
    @GetMapping("/{userId}")
    public ResultVO<UserDTO> getUserById(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) return ResultVO.success(null);
        return ResultVO.success(userConvert.userToUserDTO(user));
    }

    /**
     * 批量查询实现（极其重要：解决列表页 N+1 性能问题）
     */
    @GetMapping("/batch")
    public ResultVO<Map<Long, UserDTO>> getUserMapByIds(@RequestParam List<Long> userIds) {
        List<User> users = userService.getByIds(userIds);
        Map<Long, UserDTO> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> userConvert.userToUserDTO(user)
                ));
        return ResultVO.success(userMap);
    }

    /**
     * 信用分更新实现
     */
    @PostMapping("/credit/update")
    public ResultVO<Boolean> updateCreditScore(@RequestParam Long userId,
                                               @RequestParam Integer scoreChange,
                                               @RequestParam String reason) {
        boolean success = userService.updateCreditScore(userId, scoreChange, reason);
        return ResultVO.success(success);
    }

    /**
     * 角色验证实现
     */
    @GetMapping("/api/v1/users/inner/verify/role")
    ResultVO<Boolean> verifyRole(@RequestParam("userId") Long userId,
                                 @RequestParam("role") UserRoleEnum role) {
        return ResultVO.success(userService.verifyRole(userId, role));
    }
}
