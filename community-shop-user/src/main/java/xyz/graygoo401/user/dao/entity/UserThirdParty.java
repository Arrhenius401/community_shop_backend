package xyz.graygoo401.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.user.enums.ThirdPartyTypeEnum;

import java.time.LocalDateTime;

/**
 * 第三方账号关联实体
 * 对应数据库表：user_third_party
 */
@NoArgsConstructor
@Data
@TableName("user_third_party")
public class UserThirdParty {

    /** 自增主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联平台用户ID（外键关联user.user_id） */
    @TableField("user_id")
    private Long userId;

    /** 第三方平台类型（WECHAT/QQ/ALIPAY） */
    @TableField("third_type")
    private ThirdPartyTypeEnum thirdType;

    /** 第三方平台唯一标识（如微信openid） */
    @TableField("openid")
    private String openid;

    /** 第三方临时凭证（加密存储） */
    @TableField("access_token")
    private String accessToken;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 绑定时间 */
    @TableField("bind_time")
    private LocalDateTime bindTime;

    /** 绑定状态（1-有效，0-已解绑） */
    @TableField("is_valid")
    private Integer isValid;

    public UserThirdParty(Long userId, ThirdPartyTypeEnum thirdType, String openid, String accessToken) {
        this.userId = userId;
        this.thirdType = thirdType;
        this.openid = openid;
        this.accessToken = accessToken;
        this.bindTime = LocalDateTime.now();
        this.isValid = 1;
    }

}
