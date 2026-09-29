package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starshop.pojo.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    IPage<Order> getOrderList(Page<Object> objectPage, String userId, int status);

    List<Order> getUserAllOrder(String userId);

    Order getOrderDesc(String orderNo);

    Order getOrderLogistics(String orderNo);

    List<Order> searchOrderByCondition(String orderNo, String logisticsNo, String productName, String userId);

}
