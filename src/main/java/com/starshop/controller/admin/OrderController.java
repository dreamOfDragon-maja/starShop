package com.starshop.controller.admin;

import com.starshop.pojo.dto.OrderDTO;
import com.starshop.result.Result;
import com.starshop.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/create")
    public Result insertOrder(@RequestBody @NotNull OrderDTO orderDTO) {
        return orderService.insertOrder(orderDTO);
    }

    /**
     * 获取订单列表
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer pageNum
            , @RequestParam(defaultValue = "10") Integer pageSize
            , @RequestParam(defaultValue = "pendingPayment") String status) {
        return orderService.getOrderList(pageNum, pageSize, status);
    }

    /**
     * 查询指定页面订单列表
     * @param pageName
     * @return
     */
    @GetMapping("/page/list")
    public Result getOrderListByPage(@RequestParam @NotBlank String pageName) {
        return orderService.getOrderListByPage(pageName);
    }

    /**
     * 查看订单详情
     * @param orderNo
     * @return
     */
    @GetMapping("/detail")
    public Result getOrderDesc(@RequestParam @NotBlank String orderNo) {
        return orderService.getOrderDesc(orderNo);
    }

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestParam @NotBlank String orderNo, String cancelReason) {
        return orderService.cancelOrder(orderNo, cancelReason);
    }

    /**
     * 支付成功订单
     * @param orderNo
     * @return
     */
    @PutMapping("/pay/success")
    public Result paySuccessOrder(@RequestParam @NotBlank String orderNo){
        return orderService.paySuccessOrder(orderNo);
    }

    /**
     * 确认收货
     * @param orderNo
     * @return
     */
    @PutMapping("/confirmReceipt")
    public Result confirmOrderReceipt(@RequestParam @NotBlank String orderNo) {
        return orderService.confirmOrderReceipt(orderNo);
    }

    /**
     * 逻辑删除订单
     * @param orderNo
     * @return
     */
    @DeleteMapping("/delete")
    public Result deleteOrder(@RequestParam @NotBlank String orderNo){
        return orderService.deleteOrder(orderNo);
    }

    /**
     * 计算运费
     * @param productIds
     * @param addressId
     * @return
     */
    @GetMapping("/freight")
    public Result getOrderFreight(@RequestParam @NotBlank String productIds, @RequestParam @NotBlank String addressId) {
        return orderService.getOrderFreight(productIds, addressId);
    }

    /**
     * 获取物流信息
     * @param orderNo
     * @return
     */
    @GetMapping("/logistics")
    public Result getOrderLogistics(@RequestParam @NotBlank String orderNo) {
        return orderService.getOrderLogistics(orderNo);
    }

    /**
     * 滚动分页查询订单(全部页面)
     * @param beginId
     * @return
     */
    @GetMapping("/scroll/query/list")
    public Result getOrderByScrollQuery(@RequestParam @NotNull Long beginId) {
        return orderService.getOrderByScrollQuery(beginId);
    }
}
