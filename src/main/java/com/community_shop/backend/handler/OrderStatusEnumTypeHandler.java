package com.community_shop.backend.handler;

import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 订单状态枚举类型处理器，实现OrderStatusEnum与varchar的转换
 */
public class OrderStatusEnumTypeHandler extends BaseTypeHandler<OrderStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OrderStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为code存入数据库
        ps.setString(i, parameter.getCode());
    }

    @Override
    public OrderStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? OrderStatusEnum.getByCode(code) : null;
    }

    @Override
    public OrderStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? OrderStatusEnum.getByCode(code) : null;
    }

    @Override
    public OrderStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? OrderStatusEnum.getByCode(code) : null;
    }
}
