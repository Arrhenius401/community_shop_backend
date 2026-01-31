package xyz.graygoo401.trade.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.graygoo401.trade.dao.entity.Payment;

/**
 * 支付信息Mapper接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
