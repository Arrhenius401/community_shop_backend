package com.community_shop.backend.convert;

import com.community_shop.backend.dto.user.RegisterDTO;
import com.community_shop.backend.dto.user.UserDetailDTO;
import com.community_shop.backend.dto.user.UserProfileUpdateDTO;
import com.community_shop.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User 模块 Entity 与 DTO 转换接口
 * componentModel = "spring"：生成 Spring 管理的 Bean，支持 @Autowired 注入
 */
@Mapper(componentModel = "spring")
public interface UserConvert {

    // 单例实例（非 Spring 环境使用）
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    // 密码加密器（MapStruct 支持注入 Spring Bean，此处简化为静态工具）
    BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    // 时间格式化器
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 1. RegisterDTO（注册请求） → UserEntity（数据库实体）
     * 核心逻辑：
     * - 密码 BCrypt 加密
     * - 初始化信用分 100、发帖数 0、粉丝数 0
     * - 性别枚举转换（DTO 枚举 → 数据库字符串）
     * - 生成注册时间
     */
    @Mapping(target = "userId", ignore = true) // 自增 ID，忽略
    @Mapping(target = "password", source = "password", qualifiedByName = "encryptPassword") // 密码加密
    @Mapping(target = "creditScore", constant = "100") // 初始信用分 100
    @Mapping(target = "postCount", constant = "0") // 初始发帖数 0
    @Mapping(target = "followerCount", constant = "0") // 初始粉丝数 0
    @Mapping(target = "gender", source = "gender", qualifiedByName = "enumToString") // 枚举转字符串
    @Mapping(target = "interestTags", source = "interestTags", qualifiedByName = "listToCommaString") // 列表转逗号分隔字符串
    @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now())") // 当前时间
    @Mapping(target = "activityTime", expression = "java(java.time.LocalDateTime.now())") // 最后活跃时间初始化
    User registerDtoToEntity(RegisterDTO registerDTO);


    /**
     * 2. UserEntity（数据库实体） → UserDetailDTO（详情响应）
     * 核心逻辑：
     * - 忽略密码字段（敏感信息不返回）
     * - 手机号/邮箱脱敏处理
     * - 逗号分隔的兴趣标签转为 List
     * - LocalDateTime 转为格式化字符串
     */
    @Mapping(target = "password", ignore = true) // 忽略密码
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "desensitizePhone") // 手机号脱敏
    @Mapping(target = "email", source = "email", qualifiedByName = "desensitizeEmail") // 邮箱脱敏
    @Mapping(target = "interestTags", source = "interestTags", qualifiedByName = "commaStringToList") // 逗号字符串转 List
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "formatLocalDateTime") // 时间格式化
    UserDetailDTO entityToDetailDto(User userEntity);


    /**
     * 3. UserProfileUpdateDTO（资料更新请求） → UserEntity（数据库实体）
     * 核心逻辑：仅映射可更新字段（昵称、头像、简介等），忽略不可修改字段
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "creditScore", ignore = true)
    @Mapping(target = "postCount", ignore = true)
    @Mapping(target = "followerCount", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "activityTime", expression = "java(java.time.LocalDateTime.now())") // 更新最后活跃时间
    @Mapping(target = "gender", source = "gender", qualifiedByName = "enumToString")
    @Mapping(target = "interestTags", source = "interestTags", qualifiedByName = "listToCommaString")
    User profileUpdateDtoToEntity(UserProfileUpdateDTO updateDTO);


    // ------------------------------ 自定义转换方法 ------------------------------
    /**
     * 密码 BCrypt 加密
     */
    @Named("encryptPassword")
    default String encryptPassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 手机号脱敏：13812345678 → 138****5678
     */
    @Named("desensitizePhone")
    default String desensitizePhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 邮箱脱敏：test123@163.com → t**t123@163.com
     */
    @Named("desensitizeEmail")
    default String desensitizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String prefix = parts[0];
        if (prefix.length() <= 2) {
            return prefix.replaceAll(".", "*") + "@" + parts[1];
        }
        return prefix.substring(0, 1) +
                prefix.substring(1, prefix.length() - 1).replaceAll(".", "*") +
                prefix.substring(prefix.length() - 1) + "@" + parts[1];
    }

    /**
     * LocalDateTime → 格式化字符串（yyyy-MM-dd HH:mm:ss）
     */
    @Named("formatLocalDateTime")
    default String formatLocalDateTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }

    /**
     * 枚举 → 字符串（如 GenderEnum.MALE → "MALE"）
     */
    @Named("enumToString")
    default String enumToString(Enum<?> enumObj) {
        return enumObj == null ? null : enumObj.name();
    }

    /**
     * List<String> → 逗号分隔字符串（如 ["Java","Python"] → "Java,Python"）
     */
    @Named("listToCommaString")
    default String listToCommaString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().collect(Collectors.joining(","));
    }

    /**
     * 逗号分隔字符串 → List<String>（如 "Java,Python" → ["Java","Python"]）
     */
    @Named("commaStringToList")
    default List<String> commaStringToList(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
