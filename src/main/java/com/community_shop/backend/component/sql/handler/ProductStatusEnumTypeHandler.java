package com.community_shop.backend.component.sql.handler;

import com.community_shop.backend.component.enums.codeEnum.ProductStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 商品状态枚举类型处理器，实现ProductStatusEnum与varchar的转换
 */
public class ProductStatusEnumTypeHandler extends BaseTypeHandler<ProductStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProductStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为code存入数据库
        ps.setString(i, parameter.getCode());
    }

    @Override
    public ProductStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? ProductStatusEnum.getByCode(code) : null;
    }

    @Override
    public ProductStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? ProductStatusEnum.getByCode(code) : null;
    }

    @Override
    public ProductStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? ProductStatusEnum.getByCode(code) : null;
    }
}