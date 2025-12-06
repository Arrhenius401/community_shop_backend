package com.community_shop.backend.enums.handler;

import com.community_shop.backend.enums.code.EvaluationStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 评价状态枚举类型处理器，实现EvaluationStatusEnum与varchar的转换
 */
@MappedTypes(EvaluationStatusEnum.class)
public class EvaluationStatusEnumTypeHandler extends BaseTypeHandler<EvaluationStatusEnum> {

    /**
     * 写入数据库：将枚举转换为String（code）
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EvaluationStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为code存入数据库
        ps.setString(i, parameter.getCode());
    }

    /**
     * 读取数据库：从ResultSet中根据列名获取String，转换为枚举
     */
    @Override
    public EvaluationStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? EvaluationStatusEnum.getByCode(code) : null;
    }

    /**
     * 读取数据库：从ResultSet中根据列索引获取String，转换为枚举
     */
    @Override
    public EvaluationStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? EvaluationStatusEnum.getByCode(code) : null;
    }

    /**
     * 读取数据库：从CallableStatement中获取String，转换为枚举
     */
    @Override
    public EvaluationStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? EvaluationStatusEnum.getByCode(code) : null;
    }
}
