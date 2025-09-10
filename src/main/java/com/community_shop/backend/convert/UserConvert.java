package com.community_shop.backend.convert;

import com.community_shop.backend.dto.user.*;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserThirdParty;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * User 模块对象转换器
 * 基于 MapStruct 实现实体与 DTO 之间的映射
 */
@Mapper(componentModel = "spring")  // 声明为 Spring 组件，支持依赖注入
public interface UserConvert {

    // 单例实例（非 Spring 环境使用）
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    /**
     * User 实体 -> UserDetailDTO（用户详情响应）
     * 映射说明：直接匹配同名字段，枚举类型因类型一致可自动映射
     */
    UserDetailDTO userToUserDetailDTO(User user);

    /**
     * RegisterDTO（注册请求）-> User 实体
     * 映射说明：
     * 1. 注册时默认初始化信用分 100、状态为激活、角色为普通用户
     * 2. 忽略实体中自动生成的字段（userId、createTime 等）
     */
    @Mappings({
            @Mapping(target = "userId", ignore = true), // 主键自增，忽略
            @Mapping(target = "creditScore", constant = "100"), // 初始信用分 100
            @Mapping(target = "followerCount", constant = "0"), // 初始粉丝数 0
            @Mapping(target = "postCount", constant = "0"), // 初始发帖数 0
            @Mapping(target = "createTime", ignore = true), // 注册时间由系统生成
            @Mapping(target = "activityTime", ignore = true), // 活跃时间由系统更新
            @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.UserStatusEnum.ACTIVE)"), // 默认激活
            @Mapping(target = "role", expression = "java(com.community_shop.backend.enums.UserRoleEnum.USER)") // 默认普通用户
    })
    User registerDtoToUser(RegisterDTO registerDTO);

    /**
     * UserProfileUpdateDTO（资料更新请求）-> User 实体
     * 映射说明：仅更新请求中携带的非空字段，忽略实体中不可修改的字段
     */
    @Mappings({
            @Mapping(target = "userId", ignore = true),
            @Mapping(target = "password", ignore = true), // 密码需单独处理，不通过资料更新接口修改
            @Mapping(target = "phoneNumber", ignore = true), // 手机号修改需单独接口
            @Mapping(target = "email", ignore = true), // 邮箱修改需单独接口
            @Mapping(target = "creditScore", ignore = true), // 信用分由系统维护
            @Mapping(target = "followerCount", ignore = true),
            @Mapping(target = "postCount", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "activityTime", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "role", ignore = true)
    })
    void updateUserFromProfileDto(UserProfileUpdateDTO dto, @MappingTarget User user);

    /**
     * ThirdPartyBindDTO（第三方绑定请求）-> UserThirdParty 实体
     * 映射说明：绑定时间由系统生成，默认绑定状态为有效（1）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true), // 主键自增
            @Mapping(target = "bindTime", ignore = true), // 绑定时间由系统生成
            @Mapping(target = "isValid", constant = "1"), // 默认有效
            @Mapping(target = "remark", defaultValue = "用户主动绑定") // 默认备注
    })
    UserThirdParty thirdPartyBindDtoToUserThirdParty(ThirdPartyBindDTO dto);

    /**
     * UserThirdParty 实体 -> BindingItemDTO（第三方绑定列表项）
     * 映射说明：对 openid 进行脱敏处理（示例：微信用户 ****1234）
     */
    @Mappings({
            @Mapping(target = "bindingId", source = "id"),
            @Mapping(target = "openidDesensitized", expression = "java(desensitizeOpenid(userThirdParty.getThirdType(), userThirdParty.getOpenid()))")
    })
    ThirdPartyBindingListDTO.BindingItemDTO userThirdPartyToBindingItemDTO(UserThirdParty userThirdParty);

    /**
     * 批量转换 UserThirdParty 列表 -> BindingItemDTO 列表
     */
    List<ThirdPartyBindingListDTO.BindingItemDTO> userThirdPartyListToBindingItemList(List<UserThirdParty> userThirdParties);

    /**
     * 辅助方法：脱敏处理 OpenID
     * @param thirdType 第三方类型（WECHAT/QQ/ALIPAY）
     * @param openid 原始 OpenID
     * @return 脱敏后的 OpenID 描述
     */
    default String desensitizeOpenid(String thirdType, String openid) {
        if (openid == null || openid.length() < 4) {
            return thirdType + "用户 ****";
        }
        String prefix = switch (thirdType) {
            case "WECHAT" -> "微信";
            case "QQ" -> "QQ";
            case "ALIPAY" -> "支付宝";
            default -> "第三方";
        };
        String suffix = openid.substring(openid.length() - 4);
        return prefix + "用户 ****" + suffix;
    }
}
