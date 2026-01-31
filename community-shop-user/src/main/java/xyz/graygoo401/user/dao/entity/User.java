package xyz.graygoo401.user.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import xyz.graygoo401.api.user.enums.GenderEnum;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.enums.UserStatusEnum;

import java.time.LocalDateTime;


/**
 * 用户实体类
 */
@NoArgsConstructor
@Data
@TableName("`user`")
public class User{

    /**
     * 无参构造方法（如public User(){}）的作用是创建一个 "空对象"（所有字段为默认值）
     * 校验注解（@NotBlank等）本身不会阻止创建对象，只会在触发校验时（如 Controller 层接收请求参数时）生效
     * 直接通过new User()创建对象时，即使字段为空也不会触发校验，只有在校验器执行时才会检查
     * 当使用校验框架（如 Spring Validation）对通过无参构造创建的对象进行校验时，会触发注解规则，导致校验失败
     */

    // ===================== 1. 基础主键与账号信息（严格匹配数据库表） =====================
    /**
     * 用户唯一标识（数据库主键，自动递增）
     * 对应文档4 user表：user_id（bigint AI PK）
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 登录密码（BCrypt加密存储）
     * 对应文档4 user表：password（varchar(45)）；文档3要求加密
     */
    @TableField("password")
    private String password;

    /**
     * 用户名（显示用，非登录账号）
     * 对应文档4 user表：user_name（varchar(45)）
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱（登录/找回密码用）
     * 对应文档4 user表：email（varchar(45)）；文档1注册需求
     */
    @TableField("email")
    private String email;

    /**
     * 手机号（登录/验证码用）
     * 对应文档4 user表：phone_number（bigint）；文档1手机号注册需求
     */
    @TableField("phone_number")
    private String phoneNumber;

    // ===================== 2. 个人资料信息（文档1个人中心需求） =====================
    /**
     * 头像路径（阿里云OSS存储URL）
     * 对应文档4 user表：avatar_url（varchar(512)）；文档2云服务集成
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 个人简介
     * 对应文档5原字段：bio（补充长度限制，避免过长）
     */
    @TableField("bio")
    @Length(max = 200, message = "个人简介不超过200字")
    private String bio;

    /**
     * 性别（可扩展为枚举，此处保留字符串兼容灵活性）
     * 对应文档5原字段：gender
     */
    @TableField("gender")
    private GenderEnum gender;

    // ===================== 3. 社区与交易统计信息（文档4业务关联字段） =====================
    //对象创建阶段：当 ORM 框架（如 MyBatis、Hibernate）从数据库查询数据时，会先通过无参构造方法创建一个实体对象，此时 postCount 会被初始化为 0。
    //数据映射阶段：框架会根据查询结果，将数据库中 post_count 字段的值（假设有值 5）通过反射或 setter 方法赋值给 postCount 字段，覆盖初始值 0。
    /**
     * 信用分（基础100分，影响发帖/商品发布权限）
     * 对应文档4 user表：credit_score（int）；文档2信用体系规则（≥80可发商品，≥60可发帖）
     */
    @TableField("credit_score")
    private int creditScore = 100; // 初始化100分，文档3明确要求

    // ===================== 4. 状态与时间信息（文档2系统设计要求） =====================
    /**
     * 注册时间
     * 对应文档5原字段：createTime；记录用户创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 最后活跃时间（登录/发帖/交易时更新）
     * 对应文档5原字段：updateTime；用于活跃度统计
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 账号状态（激活/禁用/封禁）
     * 对应文档5原字段：status（枚举类型）；文档1账号安全需求
     */
    @TableField("status")
    private UserStatusEnum status;

    /**
     * 用户角色（普通用户/管理员/吧主）
     * 对应文档5原字段：role（枚举类型）；文档2权限管理需求
     */
    @TableField("role")
    private UserRoleEnum role;


    // ===================== 5. 构造方法（适配不同业务场景） =====================

    /**
     * 基本注册专用构造（最常用场景）
     * 自动初始化时间与默认状态，减少Service层重复代码
     */
    public User(String password, String username) {
        this.password = password;
        this.username = username;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.creditScore = 100;
        this.status = UserStatusEnum.NORMAL;
        this.role = UserRoleEnum.USER;
        this.gender = GenderEnum.UNKNOWN;
    }

    // ===================== 6. 业务辅助方法（简化Service层逻辑） =====================
    /**
     * 判断是否为管理员（文档2权限校验需求）
     */
    public boolean isAdmin() {
        return UserRoleEnum.ADMIN.equals(this.role);
    }

    /**
     * 判断账号是否可用（文档1账号安全需求）
     */
    public boolean isActive() {
        return UserStatusEnum.NORMAL.equals(this.status);
    }

    /**
     * 判断是否有权限发布商品（文档2信用分规则：≥80分）
     */
    public boolean canPublishProduct() {
        return this.creditScore >= 80 && isActive();
    }

    /**
     * 判断是否有权限发帖（文档2信用分规则：≥60分）
     */
    public boolean canPublishPost() {
        return this.creditScore >= 60 && isActive();
    }
}
