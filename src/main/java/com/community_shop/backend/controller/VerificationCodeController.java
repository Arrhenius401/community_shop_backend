package com.community_shop.backend.controller;

import com.community_shop.backend.dto.verification.VerifyEmailDTO;
import com.community_shop.backend.dto.verification.VerifyPhoneDTO;
import com.community_shop.backend.service.impl.EmailCodeServiceImpl;
import com.community_shop.backend.service.impl.PhoneCodeServiceImpl;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/verify-code")
@Validated  // 开启参数校验
@Tag(
        name = "验证码管理接口",
        description = "包含邮箱验证码和手机验证码的发送与验证功能，支持用户注册、登录等场景的身份验证"
)
public class VerificationCodeController {

    @Autowired
    private EmailCodeServiceImpl emailCodeService;

    @Autowired
    private PhoneCodeServiceImpl phoneCodeService;

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @return 验证码
     */
    @PostMapping("/email/send")
    @Operation(
            summary = "发送邮箱验证码接口",
            description = "向指定邮箱发送6位数字验证码，业务规则：1.邮箱需符合标准格式；2.同一邮箱短时间内限制重复发送（防刷机制）；3.验证码有效期由配置决定"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功，返回验证码（实际生产环境建议不返回明文）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（邮箱格式不正确）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "429", description = "请求频繁（短时间内多次发送）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "发送失败（邮件服务异常）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<String> sendEmailCode(
            @Parameter(description = "目标邮箱地址，需符合标准格式", required = true, example = "1825270596@qq.com")
            @RequestParam
            @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式错误")
            String email) {
        String code = emailCodeService.sendCode(email);
        return ResultVO.success(code);
    }

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return 验证码
     */
    @PostMapping("/phone/send")
    @Operation(
            summary = "发送手机验证码接口",
            description = "向指定手机号发送6位数字验证码，业务规则：1.手机号需符合11位数字格式；2.同一手机号短时间内限制重复发送（防刷机制）；3.验证码有效期由配置决定"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功，返回验证码（实际生产环境建议不返回明文）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（手机号格式不正确）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "429", description = "请求频繁（短时间内多次发送）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "发送失败（短信服务异常）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<String> sendPhoneCode(
            @Parameter(description = "目标手机号，需为11位数字", required = true, example = "17268287727")
            @RequestParam
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误，需为11位有效数字")
            String phone
    ) {
        String code = phoneCodeService.sendCode(phone);
        return ResultVO.success(code);
    }

    /**
     * 验证邮箱验证码
     * @param verifyDTO 验证码DTO
     * @return 验证结果
     */
    @PostMapping("/email/verify")
    @Operation(
            summary = "验证邮箱验证码接口",
            description = "校验邮箱与验证码的匹配性，业务规则：1.验证成功后立即失效该验证码；2.验证码需在有效期内；3.邮箱需与发送时一致"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证成功（true）或失败（false）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（邮箱格式错误或验证码非6位数字）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "验证过程异常",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> verifyEmailCode(
            @Valid
            @Parameter(description = "邮箱验证参数，含邮箱和验证码", required = true)
            @RequestBody VerifyEmailDTO verifyDTO
    ) {
        boolean result = emailCodeService.verifyCode(verifyDTO.getEmail(), verifyDTO.getVerifyCode());
        emailCodeService.deleteCode(verifyDTO.getEmail());
        return ResultVO.success(result);
    }

    /**
     * 验证手机验证码
     * @param verifyDTO 验证码DTO
     * @return 验证结果
     */
    @PostMapping("/phone/verify")
    @Operation(
            summary = "验证手机验证码接口",
            description = "校验手机号与验证码的匹配性，业务规则：1.验证成功后立即失效该验证码；2.验证码需在有效期内；3.手机号需与发送时一致"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证成功（true）或失败（false）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（手机号格式错误或验证码非6位数字）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "验证过程异常",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> verifyPhoneCode(
            @Valid
            @Parameter(description = "手机验证参数，含手机号和验证码", required = true)
            @RequestBody VerifyPhoneDTO verifyDTO
    ) {
        boolean result = phoneCodeService.verifyCode(verifyDTO.getPhoneNumber(), verifyDTO.getVerifyCode());
        phoneCodeService.deleteCode(verifyDTO.getPhoneNumber());
        return ResultVO.success(result);
    }

}
