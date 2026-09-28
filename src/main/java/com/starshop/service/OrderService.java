package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.OrderDTO;
import com.starshop.pojo.entity.Order;
import com.starshop.result.Result;
import jakarta.validation.constraints.NotNull;

public interface OrderService extends IService<Order> {
    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    Result insertOrder(@NotNull OrderDTO orderDTO);
}
