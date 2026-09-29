package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.OrderDTO;
import com.starshop.pojo.entity.Order;
import com.starshop.result.Result;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface OrderService extends IService<Order> {
    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    Result insertOrder(@NotNull OrderDTO orderDTO);

    /**
     * 获取订单列表
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    Result getOrderList(Integer pageNum, Integer pageSize, String status);

    /**
     * 查询指定页面订单列表
     * @param pageName
     * @return
     */
    Result getOrderListByPage(@NotBlank String pageName);

    /**
     * 查看订单详情
     * @param orderNo
     * @return
     */
    Result getOrderDesc(@NotBlank String orderNo);

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    Result cancelOrder(@NotBlank String orderNo, String cancelReason);
}
