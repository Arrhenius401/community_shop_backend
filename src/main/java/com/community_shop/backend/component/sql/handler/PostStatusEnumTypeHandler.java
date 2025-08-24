package com.community_shop.backend.component.sql.handler;

import com.community_shop.backend.component.statusEnum.PostStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 帖子状态枚举类型处理器，实现PostStatusEnum与varchar的转换
 */
public class PostStatusEnumTypeHandler extends BaseTypeHandler<PostStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PostStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为code存入数据库
        ps.setString(i, parameter.getCode());
    }

    @Override
    public PostStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? PostStatusEnum.getByCode(code) : null;
    }

    @Override
    public PostStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? PostStatusEnum.getByCode(code) : null;
    }

    @Override
    public PostStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? PostStatusEnum.getByCode(code) : null;
    }
}