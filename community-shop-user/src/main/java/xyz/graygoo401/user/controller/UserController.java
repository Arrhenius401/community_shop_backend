package xyz.graygoo401.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.graygoo401.api.infra.dto.verification.VerifyEmailDTO;
import xyz.graygoo401.api.infra.dto.verification.VerifyPhoneDTO;
import xyz.graygoo401.api.user.dto.third.ThirdPartyBindDTO;
import xyz.graygoo401.api.user.dto.third.ThirdPartyBindingListDTO;
import xyz.graygoo401.api.user.dto.third.ThirdPartyLoginDTO;
import xyz.graygoo401.api.user.dto.user.*;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.annotation.LoginRequired;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.util.RequestParseUtil;
import xyz.graygoo401.common.vo.ResultVO;
import xyz.graygoo401.user.service.base.UserService;
import xyz.graygoo401.user.service.base.UserThirdPartyService;

/**
 * 用户管理模块Controller，负责用户注册、登录、资料管理及第三方账号绑定等接口实现
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(
        name = "用户管理接口",
        description = "包含用户注册、账号登录（密码/第三方）、个人资料管理、第三方账号绑定、用户状态管控等核心功能，所有接口均返回统一ResultVO格式"
)
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserThirdPartyService userThirdPartyService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 用户注册接口
     * 对应Service层：UserServiceImpl.register()，校验手机号/邮箱唯一性、密码格式、验证码有效性
     */
    @PostMapping("/register")
    @Operation(
            summary = "用户注册接口",
            description = "通过手机号/邮箱注册新用户，业务规则：1.用户名1-20位字符；2.手机号需符合11位数字格式；3.邮箱需符合标准格式；4.密码需含字母+数字（6-20位）；5.验证码需为6位数字"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如手机号格式非法、密码不符合规则、验证码无效）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "用户名/手机号/邮箱已被注册（对应错误码：USER_001/USER_002/USER_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据插入失败，对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> register(
            @Valid @RequestBody
            @Parameter(description = "注册参数，含用户名、手机号/邮箱、密码、验证码", required = true)
            RegisterDTO registerDTO) {
        Boolean res = userService.register(registerDTO);
        return ResultVO.success(res);
    }

    /**
     * 邮箱注册接口
     * 对应Service层：UserServiceImpl.registerByEmail()，校验邮箱唯一性、密码格式、验证码有效性
     */
    @PostMapping("/register/email")
    @Operation(
            summary = "邮箱注册接口",
            description = "通过邮箱注册新用户，业务规则：1.用户名1-20位字符；2.手机号需符合11位数字格式；3.邮箱需符合标准格式；4.密码需含字母+数字（6-20位）；5.验证码需为6位数字"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如手机号格式非法、密码不符合规则、验证码无效）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "用户名/手机号/邮箱已被注册（对应错误码：USER_001/USER_002/USER_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据插入失败，对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> registerByEmail(
            @Valid @RequestBody
            @Parameter(description = "注册参数，含用户名、邮箱、密码、验证码", required = true)
            RegisterEmailDTO registerDTO) {
        Boolean res = userService.registerByEmail(registerDTO);
        return ResultVO.success(res);
    }

    /**
     * 手机号注册接口
     * 对应Service层：UserServiceImpl.registerByPhone()，校验手机号唯一性、密码格式、验证码有效性
     */
    @PostMapping("/register/phone")
    @Operation(
            summary = "手机号注册接口",
            description = "通过手机号注册新用户，业务规则：1.用户名1-20位字符；2.手机号需符合11位数字格式；3.邮箱需符合标准格式；4.密码需含字母+数字（6-20位）；5.验证码需为6位数字"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如手机号格式非法、密码不符合规则、验证码无效）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "用户名/手机号/邮箱已被注册（对应错误码：USER_001/USER_002/USER_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据插入失败，对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> registerByPhone(
            @Valid @RequestBody
            @Parameter(description = "注册参数，含用户名、手机号、密码、验证码", required = true)
            RegisterPhoneDTO registerDTO) {
        Boolean res = userService.registerByPhone(registerDTO);
        return ResultVO.success(res);
    }

    /**
     * 邮箱绑定接口
     * 对应Service层：UserServiceImpl.bindEmail()，校验邮箱唯一性、验证码有效性
     */
    @PostMapping("/bind/email")
    @LoginRequired
    @Operation(
            summary = "邮箱绑定接口",
            description = "绑定邮箱，业务规则：1.邮箱需符合标准格式；2.验证码需为6位数字"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "绑定成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如邮箱格式非法、验证码无效）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "邮箱已被绑定（对应错误码：USER_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据插入失败，对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> bindEmail(
            @Valid @RequestBody
            @Parameter(description = "绑定参数，含邮箱、验证码", required = true)
            VerifyEmailDTO bindDTO) {
        bindDTO.setUserId(parseUserIdFromToken());
        Boolean res = userService.bindEmail(bindDTO);
        return ResultVO.success(res);
    }

    /**
     * 手机号绑定接口
     * 对应Service层：UserServiceImpl.bindPhone()，校验手机号唯一性、验证码有效性
     */
    @PostMapping("/bind/phone")
    @LoginRequired
    @Operation(
            summary = "手机号绑定接口",
            description = "绑定手机号，业务规则：1.手机号需符合11位数字格式；2.验证码需为6位数字"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "绑定成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如手机号格式非法、验证码无效）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "手机号已被绑定（对应错误码：USER_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据插入失败，对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> bindPhone(
            @Valid @RequestBody
            @Parameter(description = "绑定参数，含手机号、验证码", required = true)
            VerifyPhoneDTO bindDTO) {
        bindDTO.setUserId(parseUserIdFromToken());
        Boolean res = userService.bindPhone(bindDTO);
        return ResultVO.success(res);
    }

    /**
     * 通过邮箱修改密码接口
     * 对应Service层：UserServiceImpl.updatePasswordByEmail()，校验邮箱、验证码有效性
     */
    @PostMapping("/update/password/email")
    @LoginRequired
    @Operation(
            summary = "邮箱修改密码接口",
            description = "修改邮箱绑定的密码，业务规则：1.邮箱需符合标准格式；2.验证码需为6位数字；3.新密码需含字母+数字（6-20位）"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如邮箱格式非法、验证码无效、新密码不符合规则）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据插入失败，对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updatePasswordByEmail(
            @Valid @RequestBody
            @Parameter(description = "修改密码参数，含邮箱、验证码、新密码", required = true)
            PasswordUpdateEmailDTO verifyDTO) {
        verifyDTO.setUserId(parseUserIdFromToken());
        Boolean res = userService.updatePasswordByEmail(verifyDTO);
        return ResultVO.success(res);
    }

    /**
     * 通过手机号修改密码接口
     * 对应Service层：UserServiceImpl.updatePasswordByPhone()，校验手机号、验证码有效性
     */
    @PostMapping("/update/password/phone")
    @LoginRequired
    @Operation(
            summary = "手机号修改密码接口",
            description = "修改手机号绑定的密码，业务规则：1.手机号需符合11位数字格式；2.验证码需为6位数字；3.新密码需含字母+数字（6-20位）"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如手机号格式非法、验证码无效、新密码不符合规则）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据插入失败，对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updatePasswordByPhone(
            @Valid @RequestBody
            @Parameter(description = "修改密码参数，含手机号、验证码、新密码", required = true)
            PasswordUpdatePhoneDTO verifyDTO) {
        verifyDTO.setUserId(parseUserIdFromToken());
        Boolean res = userService.updatePasswordByPhone(verifyDTO);
        return ResultVO.success(res);
    }

    /**
     * 账号密码登录接口
     * 对应Service层：UserServiceImpl.login()，支持邮箱/手机号登录，校验密码正确性、用户状态
     */
    @PostMapping("/login/password")
    @Operation(
            summary = "账号密码登录接口",
            description = "支持两种登录标识：1.邮箱+密码；2.手机号+密码，登录成功返回JWT令牌（有效期由配置决定）及用户基础信息（脱敏处理）"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功，返回令牌与用户信息",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如登录类型未指定，对应错误码：SYSTEM_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "账号不存在或密码错误（对应错误码：USER_051/USER_052）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "用户账号已禁用（需联系管理员，对应用户状态为DISABLED）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "系统异常（如数据查询失败，对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<LoginResultDTO> loginByPassword(
            @Valid @RequestBody
            @Parameter(description = "密码登录参数，含登录标识、密码、登录类型", required = true)
            LoginDTO loginDTO) {
        LoginResultDTO loginResult = userService.login(loginDTO);
        return ResultVO.success(loginResult);
    }

    /**
     * 第三方登录接口
     * 对应Service层：UserServiceImpl.loginByThirdParty()，支持微信/QQ，自动注册未绑定用户
     */
    @PostMapping("/login/third-party")
    @Operation(
            summary = "第三方账号登录接口",
            description = "支持微信、QQ等第三方平台授权登录，业务规则：1.需传入平台类型（WECHAT/QQ）及授权码；2.首次登录自动创建账号（用户名格式：平台_OpenID后4位）；3.已绑定用户直接返回令牌"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功，返回令牌与用户信息",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如平台类型非法、授权码为空，对应错误码：SYSTEM_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "第三方授权失败（如授权码已过期，对应错误码：USER_071）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "第三方系统异常（如接口调用失败，对应错误码：USER_072）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<LoginResultDTO> loginByThirdParty(
            @Valid @RequestBody
            @Parameter(description = "第三方登录参数，含平台类型、授权码", required = true)
            ThirdPartyLoginDTO thirdPartyLoginDTO) {
        LoginResultDTO loginResult = userService.loginByThirdParty(thirdPartyLoginDTO);
        return ResultVO.success(loginResult);
    }

    /**
     * 获取用户详情接口
     * 对应Service层：UserServiceImpl.selectUserById()，校验用户存在性，返回脱敏信息
     */
    @GetMapping("/{userId}")
    @LoginRequired
    @Operation(
            summary = "获取用户详情接口",
            description = "查询指定用户的基础信息（脱敏处理：手机号显示为138****5678，邮箱显示为xxx@xx.com），权限规则：1.管理员可查询所有用户；2.普通用户仅可查询自己",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回脱敏用户详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（未携带有效JWT令牌，对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（普通用户查询其他用户，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（userId未匹配到数据，对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<UserDetailDTO> getUserDetail(
            @PathVariable
            @Parameter(description = "目标用户ID，需为整数", required = true)
            Long userId) {
        UserDetailDTO userDetail = userService.selectUserById(userId);
        return ResultVO.success(userDetail);
    }

    /**
     * 查看自身个人资料接口
     * 对应Service层：UserServiceImpl.selectUserById()，从Token解析当前用户ID，返回完整信息
     */
    @GetMapping("/profile/private")
    @LoginRequired
    @Operation(
            summary = "查看自身个人资料接口",
            description = "当前登录用户查询自己的完整个人资料（含未脱敏的手机号/邮箱），无需额外参数，从JWT令牌自动解析用户ID",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回完整个人资料",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "当前用户数据不存在（异常场景，对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<UserDetailDTO> getUserProfile() {
        Long currentUserId = parseUserIdFromToken();
        UserDetailDTO userDetail = userService.selectUserById(currentUserId);
        return ResultVO.success(userDetail);
    }

    /**
     * 更新用户资料接口
     * 对应Service层：UserServiceImpl.updateProfile()，校验用户名长度、头像URL格式，仅允许修改自身资料
     */
    @PutMapping("/profile/update")
    @LoginRequired
    @Operation(
            summary = "更新自身个人资料接口",
            description = "当前登录用户更新个人资料，支持修改字段：1.昵称（1-10位字符）；2.头像URL（需符合HTTP/HTTPS格式）；3.个人简介（0-200位字符），业务规则：仅允许修改自身资料",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功，返回更新后资料",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如昵称超过10位、头像URL格式非法，对应错误码：USER_028/USER_029）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<UserDetailDTO> updateUserProfile(
            @Valid @RequestBody
            @Parameter(description = "资料更新参数，含昵称、头像、简介（至少填一个）", required = true)
            UserProfileUpdateDTO profileUpdateDTO) {
        Long currentUserId = parseUserIdFromToken();
        UserDetailDTO updatedDetail = userService.updateProfile(currentUserId, profileUpdateDTO);
        return ResultVO.success(updatedDetail);
    }

    /**
     * 绑定第三方账号接口
     * 对应Service层：UserThirdPartyServiceImpl.bind()，校验账号未绑定、同平台不重复绑定
     */
    @PostMapping("/third-party/bind")
    @LoginRequired
    @Operation(
            summary = "绑定第三方账号接口",
            description = "当前登录用户绑定第三方账号（微信/QQ），业务规则：1.每个平台仅可绑定1个账号；2.OpenID需与平台类型匹配；3.该OpenID未绑定其他用户；4.绑定成功后可用于第三方登录",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "绑定成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如平台类型非法、OpenID为空，对应错误码：SYSTEM_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "该第三方账号已绑定其他用户（对应错误码：SYSTEM_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "绑定失败（对应错误码：USER_076）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> bindThirdPartyAccount(
            @Valid @RequestBody
            @Parameter(description = "第三方绑定参数，含平台类型、OpenID、accessToken", required = true)
            ThirdPartyBindDTO thirdPartyBindDTO) {
        Long currentUserId = parseUserIdFromToken();
        Boolean bindResult = userThirdPartyService.bind(currentUserId, thirdPartyBindDTO);
        return ResultVO.success(bindResult);
    }

    /**
     * 查看绑定账号列表接口
     * 对应Service层：UserThirdPartyServiceImpl.listBindings()，仅返回有效绑定记录
     */
    @GetMapping("/third-party/list")
    @LoginRequired
    @Operation(
            summary = "查看已绑定第三方账号列表接口",
            description = "当前登录用户查询所有已绑定的第三方账号，返回信息：1.平台类型；2.脱敏OpenID（如微信用户****1234）；3.绑定时间；4.绑定记录ID",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功（无绑定账号时返回空列表）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<ThirdPartyBindingListDTO> getBindingList() {
        Long currentUserId = parseUserIdFromToken();
        ThirdPartyBindingListDTO bindingList = userThirdPartyService.listBindings(currentUserId);
        return ResultVO.success(bindingList);
    }

    /**
     * 检验用户是否是管理员
     * 对应Service层：UserServiceImpl.verifyRole()，校验用户角色是否为ADMIN
     */
    @GetMapping("/check/admin")
    @LoginRequired
    @Operation(
            summary = "校验当前用户是否为管理员接口",
            description = "从JWT令牌解析用户ID，校验用户角色是否为ADMIN（管理员），返回布尔值：true=管理员，false=普通用户",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "校验成功（返回true/false）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> checkIsAdmin() {
        Long currentUserId = parseUserIdFromToken();
        Boolean isAdmin = userService.verifyRole(currentUserId, UserRoleEnum.ADMIN);
        return ResultVO.success(isAdmin);
    }

    /**
     * 检验用户是否登录
     * 对应Service层：Token解析逻辑，校验令牌有效性
     */
    @GetMapping("/check/login")
    @LoginRequired
    @Operation(
            summary = "校验当前用户是否已登录接口",
            description = "校验请求头中JWT令牌的有效性：1.令牌未过期；2.令牌未被篡改；3.关联用户存在，校验通过返回true",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "已登录（令牌有效）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录或令牌无效（过期/篡改，对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> checkIsLogin() {
        parseUserIdFromToken();
        return ResultVO.success(true);
    }

    @PutMapping("/update/password")
    @LoginRequired
    @Operation(
            summary = "更新用户密码接口",
            description = "仅登录用户可操作，支持更新密码，业务规则：1.自身不可更新密码；2.密码长度在6-16位之间；3.密码需包含大小写字母、数字、特殊字符中的两种以上；4.目标用户需存在",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如密码长度不符合要求、目标用户ID为空，对应错误码：SYSTEM_003/USER_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "目标用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updatePassword(
            @Validated @RequestBody
            @Parameter(description = "用户密码更新参数，包括新旧密码和用户ID", required = true)
            PasswordUpdateDTO updateDTO){
        updateDTO.setUserId(parseUserIdFromToken());
        Boolean res = userService.updatePassword(updateDTO);
        return ResultVO.success(res);
    }


    /**
     * 更新用户状态接口
     * 对应Service层：UserServiceImpl.updateUserStatus()，仅管理员可操作，校验权限与用户存在性
     */
    @PutMapping("/update/status")
    @LoginRequired
    @Operation(
            summary = "更新用户状态接口（管理员专属）",
            description = "仅管理员可操作，支持更新用户状态：ENABLE（启用）/DISABLE（禁用）/DELETE（删除），业务规则：1.自身不可更新自身状态；2.需先校验管理员权限；3.目标用户需存在",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如状态值非法、目标用户ID为空，对应错误码：SYSTEM_003/USER_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无管理员权限（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "目标用户不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updateUserStatus(
            @Valid @RequestBody
            @Parameter(description = "用户状态更新参数，含目标用户ID、目标状态", required = true)
            UserStatusUpdateDTO userStatusUpdateDTO) {
        Long currentUserId = parseUserIdFromToken();
        userService.updateUserStatus(currentUserId, userStatusUpdateDTO.getUserId(), userStatusUpdateDTO.getStatus());
        return ResultVO.success(true);
    }

    /**
     * 用户查询接口（分页）
     * 对应Service层：UserServiceImpl.queryUsers()，支持多条件筛选，按权限控制查询范围
     */
    @GetMapping("/query/list")
    @LoginRequired
    @Operation(
            summary = "多条件查询用户列表（分页）",
            description = "支持多条件组合查询：1.精准匹配（用户ID、状态）；2.模糊匹配（昵称、手机号、邮箱）；3.分页参数（pageNum默认1，pageSize默认10），权限规则：1.管理员可查询所有用户；2.普通用户仅可查询自己",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功（无数据时返回空列表+分页信息）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（如pageNum/pageSize为负数，对应错误码：SYSTEM_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（普通用户查询其他用户，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<UserListItemDTO>> queryUserList(
            @Parameter(description = "用户查询参数（支持多条件组合）")
            UserQueryDTO userQueryDTO) {
        PageResult<UserListItemDTO> queryResult = userService.queryUsers(userQueryDTO);
        return ResultVO.success(queryResult);
    }

    /**
     * 用户查询接口（数量统计）
     * 对应Service层：UserServiceImpl.countUsers()，统计符合条件的用户总数
     */
    @GetMapping("/query/count")
    @LoginRequired
    @Operation(
            summary = "多条件查询用户数量",
            description = "查询符合条件的用户总数，支持的查询条件与“用户列表查询”一致，用于统计分析（如管理员统计禁用用户数量），权限规则同列表查询",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "统计成功（无数据时返回0）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Integer> queryUserCount(
            @Parameter(description = "用户查询参数（支持多条件组合）")
            UserQueryDTO userQueryDTO) {
        int queryCount = userService.countUsers(userQueryDTO);
        return ResultVO.success(queryCount);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（实际项目需结合JWT工具实现）
     * @return 当前登录用户ID
     */
    private Long parseUserIdFromToken() {
        // 通过HttpServletRequest获取Authorization头，解析JWT令牌得到用户ID
        return requestParseUtil.parseUserIdFromRequest();
    }
}