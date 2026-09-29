package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.OrderDTO;
import com.starshop.pojo.entity.Order;
import com.starshop.common.result.Result;
import com.starshop.pojo.enums.OrderStatusEnum;
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

    /**
     * 支付成功订单
     * @param orderNo
     * @return
     */
    Result paySuccessOrder(@NotBlank String orderNo);

    /**
     * 确认收货
     * @param orderNo
     * @return
     */
    Result confirmOrderReceipt(@NotBlank String orderNo);

    /**
     * 逻辑删除订单
     * @param orderNo
     * @return
     */
    Result deleteOrder(@NotBlank String orderNo);

    /**
     * 计算运费
     * @param productIds
     * @param addressId
     * @return
     */
    Result getOrderFreight(@NotBlank String productIds, @NotBlank String addressId);

    /**
     * 获取物流信息
     * @param orderNo
     * @return
     */
    Result getOrderLogistics(@NotBlank String orderNo);

    /**
     * 滚动分页查询订单(全部页面)
     * @param beginId
     * @return
     */
    Result getOrderByScrollQuery(@NotNull Long beginId);

    /**
     * 条件搜索订单,前端传字符串,后端判断类型
     * @param searchCondition
     * @return
     */
    Result searchOrderByCondition(@NotBlank String searchCondition);

    Boolean updateOrderStatus(Long orderId, String orderNo, @NotNull OrderStatusEnum orderStatusEnum);
}
