package com.community_shop.backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息Mapper接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
