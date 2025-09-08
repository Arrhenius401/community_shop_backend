package com.community_shop.backend.enums.TypeHandler;

import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户状态枚举类型处理器，实现UserStatusEnum与varchar的转换
 */
public class UserStatusEnumTypeHandler extends BaseTypeHandler<UserStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为code存入数据库
        ps.setString(i, parameter.getCode());
    }

    @Override
    public UserStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? UserStatusEnum.getByCode(code) : null;
    }

    @Override
    public UserStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? UserStatusEnum.getByCode(code) : null;
    }

    @Override
    public UserStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? UserStatusEnum.getByCode(code) : null;
    }
}
