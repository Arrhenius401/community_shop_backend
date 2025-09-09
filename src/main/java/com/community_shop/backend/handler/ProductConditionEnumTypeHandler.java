package com.community_shop.backend.handler;

import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 商品状态枚举类型处理器，实现ProductConditionEnum与varchar的转换
 */
public class ProductConditionEnumTypeHandler extends BaseTypeHandler<ProductConditionEnum> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProductConditionEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为code存入数据库
        ps.setString(i, parameter.getCode());
    }

    @Override
    public ProductConditionEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? ProductConditionEnum.getByCode(code) : null;
    }

    @Override
    public ProductConditionEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? ProductConditionEnum.getByCode(code) : null;
    }

    @Override
    public ProductConditionEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? ProductConditionEnum.getByCode(code) : null;
    }
}
