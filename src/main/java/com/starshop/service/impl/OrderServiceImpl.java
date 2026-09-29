package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.RemoveOrderSessionAnnotation;
import com.starshop.common.creation.SnowflakeIdGenerator;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.PageResult;
import com.starshop.common.utils.DateUtils;
import com.starshop.common.utils.SessionUtils;
import com.starshop.constant.MessageConstant;
import com.starshop.context.BaseContext;
import com.starshop.job.delay.CancelUnpaidOrderDelayJob;
import com.starshop.mapper.OrderMapper;
import com.starshop.pojo.dto.OrderDTO;
import com.starshop.pojo.dto.OrderItemDTO;
import com.starshop.pojo.entity.Order;
import com.starshop.pojo.entity.OrderItem;
import com.starshop.pojo.enums.OrderPageEnum;
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

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private SessionUtils sessionUtils;

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
     * 获取订单列表
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public Result getOrderList(Integer pageNum, Integer pageSize, String status) {
        String userId = BaseContext.getUserId();
        //获取订单状态
        int code = OrderStatusEnum.getByValue(status).getCode();
        IPage<Order> orderIPage = orderMapper.getOrderList(new Page<>(pageNum,pageSize),userId,code);
        List<OrderWithItemVO> orderWithItemVOS = orderIPage.getRecords().stream()
                .map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
        PageResult pageResult = PageResult.builder()
                .list(orderWithItemVOS)
                .total(orderIPage.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();

        return Result.success(pageResult);
    }

    /**
     * 查询指定页面订单列表
     * 查询后存入session,进行复用,一致性基于自定义注解
     * @param pageName
     * @return
     */
    @Override
    public Result getOrderListByPage(String pageName) {
        String userId = BaseContext.getUserId();
        //获取分页页面
        OrderPageEnum orderPageEnum = OrderPageEnum.getByPageKey(pageName);
        List<Order> userAllOrder = sessionUtils.getUserAllOrder(orderMapper::getUserAllOrder, userId);
        if (userAllOrder.isEmpty()) {
            return Result.success();
        }
        //全部订单
        if (orderPageEnum.equals(OrderPageEnum.ALL)) {
            List<OrderWithItemVO> orderWithItemVOs = userAllOrder.stream().
                    map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
            return Result.success(orderWithItemVOs);
        }
        //过滤指定的分页页面
        List<Order> list = userAllOrder.stream().
                filter(order -> order.getStatus().getPageCode() == orderPageEnum.getPageCode())
                .toList();

        List<OrderWithItemVO> orderWithItemVOs = list.stream()
                .map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
        return Result.success(orderWithItemVOs);
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
