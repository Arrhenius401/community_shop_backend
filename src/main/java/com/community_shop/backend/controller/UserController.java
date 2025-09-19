package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.user.*;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.service.base.UserThirdPartyService;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理模块Controller，负责用户注册、登录、资料管理及第三方账号绑定等接口实现
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理接口", description = "包含用户注册、登录、资料管理及第三方账号绑定等功能")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserThirdPartyService userThirdPartyService;

    /**
     * 用户注册接口
     * @param registerDTO 注册请求参数（用户名、手机号、邮箱、密码、验证码）
     * @return 包含登录信息的统一响应
     */
    @PostMapping("/register")
    @Operation(
            summary = "用户注册接口",
            description = "通过手机号/邮箱注册新用户，需验证验证码有效性"
    ) // 替代@ApiOperation
    public ResultVO<Boolean> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Boolean res = userService.register(registerDTO);
        return ResultVO.success(res);
    }

    /**
     * 账号密码登录接口
     * @param loginDTO 登录请求参数（登录标识、密码、登录类型）
     * @return 包含登录结果（令牌、用户信息）的统一响应
     */
    @PostMapping("/login/password")
    @Operation(
            summary = "账号密码登录接口",
            description = "支持手机号/邮箱/用户名+密码登录，返回JWT令牌及用户基本信息"
    )
    public ResultVO<LoginResultDTO> loginByPassword(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResultDTO loginResult = userService.login(loginDTO);
        return ResultVO.success(loginResult);
    }

    /**
     * 第三方登录接口
     * @param thirdPartyLoginDTO 第三方登录参数（平台类型、授权码、设备信息）
     * @return 包含登录结果的统一响应
     */
    @PostMapping("/login/third-party")
    @Operation(
            summary = "第三方账号登录接口",
            description = "支持微信、QQ等第三方平台授权登录，需传入平台类型及授权码"
    )
    public ResultVO<LoginResultDTO> loginByThirdParty(@Valid @RequestBody ThirdPartyLoginDTO thirdPartyLoginDTO) {
        LoginResultDTO loginResult = userService.loginByThirdParty(thirdPartyLoginDTO);
        return ResultVO.success(loginResult);
    }

    /**
     * 获取用户详情接口
     * @param userId 目标用户ID
     * @return 包含脱敏后用户详情的统一响应
     */
    @GetMapping("/{userId}")
    @LoginRequired
    @Operation(
            summary = "获取用户详情接口",
            description = "查询指定用户的基本信息（脱敏处理），需登录后访问"
    )
    public ResultVO<UserDetailDTO> getUserDetail(@PathVariable Long userId) {
        UserDetailDTO userDetail = userService.selectUserById(userId);
        return ResultVO.success(userDetail);
    }

    /**
     * 更新用户资料接口
     * @param profileUpdateDTO 资料更新参数（昵称、头像、简介等）
     * @return 包含更新后用户详情的统一响应
     */
    @PutMapping("/profile")
    @LoginRequired
    @Operation(
            summary = "更新用户资料接口",
            description = "更新当前登录用户的资料（昵称、头像、简介等），需登录后访问"
    )
    public ResultVO<UserDetailDTO> updateUserProfile(@Valid @RequestBody UserProfileUpdateDTO profileUpdateDTO) {
        // 实际场景中需从令牌解析当前登录用户ID，此处简化为直接调用Service层（假设Service层已实现身份校验）
        Long currentUserId = parseUserIdFromToken();
        UserDetailDTO updatedDetail = userService.updateProfile(currentUserId, profileUpdateDTO);
        return ResultVO.success(updatedDetail);
    }

    /**
     * 绑定第三方账号接口
     * @param thirdPartyBindDTO 第三方绑定参数（平台类型、openid、授权凭证）
     * @return 绑定结果的统一响应
     */
    @PostMapping("/third-party/bind")
    @LoginRequired
    @Operation(
            summary = "绑定第三方账号接口",
            description = "绑定当前登录用户与第三方账号，需登录后访问"
    )
    public ResultVO<Boolean> bindThirdPartyAccount(@Valid @RequestBody ThirdPartyBindDTO thirdPartyBindDTO) {
        Long currentUserId = parseUserIdFromToken();
        Boolean bindResult = userThirdPartyService.bind(currentUserId, thirdPartyBindDTO);
        return ResultVO.success(bindResult);
    }

    /**
     * 查看绑定账号列表接口
     * @return 包含有效绑定记录的统一响应
     */
    @GetMapping("/third-party/list")
    @LoginRequired
    @Operation(
            summary = "查看绑定账号列表接口",
            description = "查看当前登录用户绑定的账号列表，需登录后访问"
    )
    public ResultVO<ThirdPartyBindingListDTO> getBindingList() {
        Long currentUserId = parseUserIdFromToken();
        ThirdPartyBindingListDTO bindingList = userThirdPartyService.listBindings(currentUserId);
        return ResultVO.success(bindingList);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID
     */
    private Long parseUserIdFromToken() {
        // 此处为简化实现，实际应通过HttpServletRequest获取Authorization头，解析JWT令牌得到用户ID
        return 1001L;
    }
}