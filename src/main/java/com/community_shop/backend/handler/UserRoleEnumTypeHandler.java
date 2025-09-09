package com.community_shop.backend.handler;

import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户角色枚举类型处理器，实现UserRoleEnum与varchar的转换
 */
@MappedTypes(UserRoleEnum.class)    // 声明该处理器用于处理UserRoleEnum
public class UserRoleEnumTypeHandler extends BaseTypeHandler<UserRoleEnum> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserRoleEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为code存入数据库
        ps.setString(i, parameter.getCode());
    }

    @Override
    public UserRoleEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? UserRoleEnum.getByCode(code) : null;
    }

    @Override
    public UserRoleEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? UserRoleEnum.getByCode(code) : null;
    }

    @Override
    public UserRoleEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? UserRoleEnum.getByCode(code) : null;
    }
}
