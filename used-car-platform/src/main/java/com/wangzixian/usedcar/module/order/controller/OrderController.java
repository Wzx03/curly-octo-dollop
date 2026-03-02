package com.wangzixian.usedcar.module.order.controller;

import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.common.annotation.RateLimit;
import com.wangzixian.usedcar.module.order.dto.CreateOrderDTO;
import com.wangzixian.usedcar.module.order.entity.Order;
import com.wangzixian.usedcar.module.order.service.OrderService;
import com.wangzixian.usedcar.module.order.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    @RateLimit(time = 1, count = 1)
    public Result<String> create(@RequestBody CreateOrderDTO createOrderDTO, @RequestHeader("token") String token) {
        Long buyerId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
        String orderNo = orderService.createOrder(createOrderDTO.getCarId(), buyerId);
        return Result.success(orderNo);
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        orderService.pay(orderNo);
        return Result.success("支付成功，恭喜提车！");
    }

    @GetMapping("/my-orders")
    public Result<List<OrderVO>> myOrders(@RequestHeader("token") String token) {
        Long userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
        return Result.success(orderService.getMyOrders(userId));
    }

    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("订单已取消");
    }
}