package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.RemoveOrderSessionAnnotation;
import com.starshop.common.creation.SnowflakeIdGenerator;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.utils.DateUtils;
import com.starshop.constant.MessageConstant;
import com.starshop.context.BaseContext;
import com.starshop.job.delay.CancelUnpaidOrderDelayJob;
import com.starshop.mapper.OrderMapper;
import com.starshop.pojo.dto.OrderDTO;
import com.starshop.pojo.dto.OrderItemDTO;
import com.starshop.pojo.entity.Order;
import com.starshop.pojo.entity.OrderItem;
import com.starshop.pojo.enums.OrderStatusEnum;
import com.starshop.pojo.vo.OrderWithItemVO;
import com.starshop.result.Result;
import com.starshop.service.OrderItemService;
import com.starshop.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService{

    @Resource
    private CopyMapper copyMapper;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private CancelUnpaidOrderDelayJob cancelUnpaidOrderDelayJob;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RemoveOrderSessionAnnotation
    public Result insertOrder(OrderDTO orderDTO) {
        List<OrderItemDTO> orderItems = orderDTO.getOrderItems();
        if (Objects.isNull(orderItems) || orderItems.isEmpty()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        Order order = copyMapper.orderDTOToOrder(orderDTO);
        String userId = BaseContext.getUserId();
        //雪花算法生成订单id
        String orderNo = snowflakeIdGenerator.generateOrderNo();
        order.setUserId(Long.valueOf(userId)).setOrderNo(orderNo);
        //保存
        save(order);

        List<OrderItem> orderItemList = orderItems.stream()
                .map(orderItemDTO -> copyMapper.orderItemDTOToOrderItem(orderItemDTO))
                .peek(orderItem -> orderItem.setOrderId(order.getId())).toList();
        //添加订单详细信息
        order.setOrderItems(orderItemList);
        orderItemService.saveBatch(orderItemList);

        //设置未支付订单到延迟队列中
        cancelUnpaidOrderDelayJob.setUnpaidOrderNoToDelayQueue(orderNo);

        OrderWithItemVO orderWithItemVO = copyMapper.orderToOrderWithItemVO(order);
        return Result.success(orderWithItemVO);
    }

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
