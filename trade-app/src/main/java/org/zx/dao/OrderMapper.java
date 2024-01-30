package org.zx.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.zx.dto.Order;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
