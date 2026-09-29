package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.RemoveOrderDetailRedisCacheAnnotation;
import com.starshop.common.annotation.business.RemoveOrderSessionAnnotation;
import com.starshop.common.creation.SnowflakeIdGenerator;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.PageResult;
import com.starshop.common.result.ScrollQueryResult;
import com.starshop.common.utils.DateUtils;
import com.starshop.common.utils.SessionUtils;
import com.starshop.constant.DataConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.constant.RegexConstants;
import com.starshop.context.BaseContext;
import com.starshop.exception.PayException;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.job.delay.CancelUnpaidOrderDelayJob;
import com.starshop.mapper.OrderMapper;
import com.starshop.pojo.dto.OrderDTO;
import com.starshop.pojo.dto.OrderItemDTO;
import com.starshop.pojo.entity.Address;
import com.starshop.pojo.entity.Order;
import com.starshop.pojo.entity.OrderItem;
import com.starshop.pojo.enums.CommonStatus;
import com.starshop.pojo.enums.OrderPageEnum;
import com.starshop.pojo.enums.OrderStatusEnum;
import com.starshop.pojo.enums.PayTypeEnum;
import com.starshop.pojo.vo.OrderWithItemVO;
import com.starshop.pojo.vo.OrderWithTrackingVO;
import com.starshop.properties.RedisCacheTtlProperties;
import com.starshop.result.Result;
import com.starshop.service.AddressService;
import com.starshop.service.OrderItemService;
import com.starshop.service.OrderService;
import io.netty.util.internal.ThreadLocalRandom;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private RedisCacheTtlProperties redisCacheTtlProperties;

    @Resource
    private AddressService addressService;

    private static final Order emptyOrder = Order.builder().id(DataConstant.ZERO_LONG).build();
    private static final String IS_SUCCESS = "isSuccess";
    private static final String TRY_NUM = "tryNum";
    private static final String PRODUCT_IDS = "productIds";

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
     * 查看订单详情
     * @param orderNo
     * @return
     */
    @Override
    public Result getOrderDesc(String orderNo) {
        //先查询缓存
        String key = RedisKeyConstant.PREFIX_ORDER + RedisKeyConstant.DETAIL + RedisKeyConstant.ORDER_NO + orderNo;
        Order order = RedisConnector.getHashObject(key, Order.class);
        if (Objects.isNull(order)) {
            order = orderMapper.getOrderDesc(orderNo);
            //如果查询结果为null就缓存空对象
            order = Objects.isNull(order) ? emptyOrder : order;
            RedisConnector.setHashObject(key, order);
            RedisConnector.expire(key, redisCacheTtlProperties.getOrderTtl(), TimeUnit.SECONDS);
        }
        //过滤空对象
        if (order.equals(emptyOrder)) {
            return Result.error(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderWithItemVO orderWithItemVO = copyMapper.orderToOrderWithItemVO(order);
        return Result.success(orderWithItemVO);

    }

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    @Override
    @RemoveOrderSessionAnnotation
    @RemoveOrderDetailRedisCacheAnnotation
    public Result cancelOrder(String orderNo, String cancelReason) {
        boolean isSuccess = cancelOrderCommon(orderNo, cancelReason);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        HashMap<String, String> map = new HashMap<>(2);
        map.put(Order.Fields.orderNo, orderNo);
        map.put(Order.Fields.status, OrderStatusEnum.CANCELLED.getValue());
        return Result.success(map);
    }

    /**
     * 根据订单号将订单更新为已取消
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

    /**
     * 支付成功订单,如果五次失败,使用线程池异步进行更新
     * @param orderNo
     * @return
     */
    @Override
    @RemoveOrderSessionAnnotation
    @RemoveOrderDetailRedisCacheAnnotation
    public Result paySuccessOrder(String orderNo) {
        //在延迟队列中移除订单号
        cancelUnpaidOrderDelayJob.setPaidOrderNoToCancelDelayQueue(orderNo);

        int tryNum = 0;
        Map<String, Object> map = new HashMap<>(2);
        //更新订单状态
        updateOrderPayIsSuccess(map, orderNo, tryNum);
        boolean isSuccess = (boolean) map.get(IS_SUCCESS);
        tryNum = (int) map.get(TRY_NUM);
        //不成功继续更新
        while (!isSuccess) {
            Map<String, Object> nextMap = updateOrderPayIsSuccess(map, orderNo, tryNum);
            tryNum = (int) nextMap.get(TRY_NUM);
            isSuccess = (boolean) nextMap.get(IS_SUCCESS);

        }
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(Order.Fields.orderNo, orderNo);
        resultMap.put(Order.Fields.status, OrderStatusEnum.PENDING_SHIPMENT.getValue());
        return Result.success(resultMap);

    }

    /**
     * 更新订单支付状态，尝试次数超过5次，线程池异步进行更新
     * @param map
     * @param orderNo
     * @param tryNum
     * @return
     */
    private Map<String, Object> updateOrderPayIsSuccess(Map<String, Object> map, String orderNo, int tryNum) {
        if (tryNum == 5) {
            //异步更新
            throw new PayException(orderNo);

        }
        boolean isSuccess = lambdaUpdate().eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, OrderStatusEnum.PENDING_SHIPMENT)
                .set(Order::getPayType, PayTypeEnum.WECHAT_PAY)
                .set(Order::getPayTime, LocalDateTime.now())
                .update();
        //每次更新后尝试次数加一
        tryNum++;
        map.put(IS_SUCCESS, isSuccess);
        map.put(TRY_NUM, tryNum);
        return map;
    }

    /**
     * 确认收货
     * @param orderNo
     * @return
     */
    @Override
    @RemoveOrderSessionAnnotation
    @RemoveOrderDetailRedisCacheAnnotation
    public Result confirmOrderReceipt(String orderNo) {
        String userId = BaseContext.getUserId();
        String now = DateUtils.formatLocalDateTime(LocalDateTime.now());
        boolean isSuccess = lambdaUpdate().eq(Order::getUserId, userId)
                .eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                .set(Order::getReceiveTime, now).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>(3);
        map.put(Order.Fields.orderNo, orderNo);
        map.put(Order.Fields.status, OrderStatusEnum.COMPLETED.getValue());
        map.put(Order.Fields.receiveTime, now);
        return Result.success(map);

    }

    /**
     * 逻辑删除订单
     * @param orderNo
     * @return
     */
    @Override
    @RemoveOrderSessionAnnotation
    @RemoveOrderDetailRedisCacheAnnotation
    public Result deleteOrder(String orderNo) {
        boolean isSuccess = lambdaUpdate()
                .set(Order::getIs_deleted, CommonStatus.ACTIVE.getNumber())
                .eq(Order::getOrderNo, orderNo).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        return Result.success(orderNo);
    }

    /**
     * 计算运费
     * @param productIds
     * @param addressId
     * @return
     */
    @Override
    public Result getOrderFreight(String productIds, String addressId) {
        String[] productIdsArray = StringUtils.split(productIds, ",");
        Address address = addressService.getById(addressId);
        if (Objects.isNull(address)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //根据商品数量乘以一个 3~8 之间的随机小数
        BigDecimal originalFreight = BigDecimal.valueOf(productIdsArray.length * ThreadLocalRandom.current().nextDouble(3, 8));
        //保留两位小数，并且四舍五入
        double freight = originalFreight.setScale(2, RoundingMode.HALF_UP).doubleValue();

        HashMap<String, Object> map = new HashMap<>(2);
        map.put(Order.Fields.freight, freight);
        map.put(PRODUCT_IDS, productIdsArray);
        return Result.success(map);
    }

    /**
     * 获取物流信息
     * @param orderNo
     * @return
     */
    @Override
    public Result getOrderLogistics(String orderNo) {
        Order order = orderMapper.getOrderLogistics(orderNo);
        if (Objects.isNull(order)) {
            return Result.error(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderWithTrackingVO orderWithTrackingVO = copyMapper.orderToOrderWithTrackingVO(order);
        return Result.success(orderWithTrackingVO);
    }

    /**
     * 滚动分页查询订单(全部页面)
     * @param beginId
     * @return
     */
    @Override
    public Result getOrderByScrollQuery(Long beginId) {
        String userId = BaseContext.getUserId();
        List<Order> list;
        if (Objects.isNull(beginId)) {
            list = lambdaQuery().eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime)
                    .eq(Order::getIs_deleted, CommonStatus.INACTIVE.getNumber())
                    .last("LIMIT " + DataConstant.COMMON_SCROLL_QUERY_NUMBER)
                    .list();
        } else {
            list = lambdaQuery().eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime).lt(Order::getId, beginId)
                    .eq(Order::getIs_deleted, CommonStatus.INACTIVE.getNumber())
                    .last("LIMIT " + DataConstant.COMMON_SCROLL_QUERY_NUMBER)
                    .list();
        }
        if (list.isEmpty()) {
            return Result.success(ScrollQueryResult.builder().list(list).endId(beginId).build());
        }
        Long endId = list.get(list.size() - 1).getId();
        return Result.success(ScrollQueryResult.builder().list(list).endId(endId).build());
    }

    /**
     * 条件搜索订单,前端传字符串,后端判断类型
     * @param searchCondition
     * @return
     */
    @Override
    public Result searchOrderByCondition(String searchCondition) {
        String userId = BaseContext.getUserId();
        String orderNo = null;
        String logisticsNo = null;
        String productName = null;
        //判断是哪种类型的查询
        if (RegexConstants.isOrderNo(searchCondition)) {
            orderNo = searchCondition;
        } else if (RegexConstants.isLogisticsNo(searchCondition)) {
            logisticsNo = searchCondition;
        } else {
            productName = searchCondition;

        }
        List<Order> orderList = orderMapper.searchOrderByCondition(orderNo, logisticsNo, productName, userId);
        if (orderList.isEmpty()) {
            return Result.success();
        }

        List<OrderWithItemVO> orderWithItemVOs = orderList.stream()
                .map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
        return Result.success(orderWithItemVOs);
    }
}
