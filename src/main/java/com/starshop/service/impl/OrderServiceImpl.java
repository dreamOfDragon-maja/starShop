package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.utils.DateUtils;
import com.starshop.mapper.OrderMapper;
import com.starshop.pojo.entity.Order;
import com.starshop.pojo.enums.OrderStatusEnum;
import com.starshop.service.OrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService{

    /**
     * 根据订单将订单更新为已取消
     * @param orderNo
     * @param cancelReason
     * @return
     */
    public boolean cancelOrderCommon(String orderNo, String cancelReason) {
        String now = DateUtils.formatLocalDateTime(LocalDateTime.now());
        return lambdaUpdate().eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, OrderStatusEnum.CANCELLED.getCode())
                .set(Order::getCancelTime, now)
                .set(Order::getCancelReason, cancelReason).update();
    }
}
