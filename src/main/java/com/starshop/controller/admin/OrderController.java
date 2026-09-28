package com.starshop.controller.admin;

import com.starshop.pojo.dto.OrderDTO;
import com.starshop.result.Result;
import com.starshop.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
