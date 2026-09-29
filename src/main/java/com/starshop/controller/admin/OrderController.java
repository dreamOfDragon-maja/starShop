package com.starshop.controller.admin;

import com.starshop.pojo.dto.OrderDTO;
import com.starshop.result.Result;
import com.starshop.service.OrderService;
import jakarta.annotation.Resource;
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
}
