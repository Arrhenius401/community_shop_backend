package xyz.graygoo401.user.service.base;

import org.springframework.stereotype.Service;
import xyz.graygoo401.api.user.dto.third.ThirdPartyBindDTO;
import xyz.graygoo401.api.user.dto.third.ThirdPartyBindingListDTO;
import xyz.graygoo401.api.user.enums.ThirdPartyTypeEnum;

/**
 * 第三方账号Service接口
 * 匹配《代码文档2 Service层设计.docx》的接口定义规范
 */
@Service
public interface UserThirdPartyService {

    /**
     * 业务方法：已注册用户绑定第三方账号
     * 对应《文档1_需求分析.docx》账号安全-第三方绑定需求
     * @param userId 平台用户ID（当前登录用户）
     * @param thirdPartyBindDTO 第三方绑定信息
     * @return 绑定结果
     */
    Boolean bind(Long userId, ThirdPartyBindDTO thirdPartyBindDTO);

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
    ThirdPartyBindingListDTO listBindings(Long userId);

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
