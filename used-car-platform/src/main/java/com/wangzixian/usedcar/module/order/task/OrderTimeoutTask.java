package com.wangzixian.usedcar.module.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wangzixian.usedcar.module.order.entity.Order;
import com.wangzixian.usedcar.module.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消任务
 * 模拟大厂的“延时队列”功能
 */
@Component
public class OrderTimeoutTask {

    @Autowired
    private OrderService orderService;

    // 每分钟执行一次 (cron表达式：秒 分 时 日 月 周)
    @Scheduled(cron = "0 * * * * ?")
    public void checkTimeoutOrders() {
        // 1. 定义超时时间：30分钟前
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);

        // 2. 查询所有：未支付 (status=0) 且 创建时间早于 expireTime 的订单
        List<Order> timeoutOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, expireTime));

        if (timeoutOrders.isEmpty()) {
            return;
        }

        // 3. 遍历处理
        for (Order order : timeoutOrders) {
            try {
                // 调用 Service 的关单方法（那里有事务控制）
                orderService.closeOvertimeOrder(order.getId());
            } catch (Exception e) {
                // 捕获异常，防止单条失败影响整个任务
                System.err.println("处理超时订单失败：" + order.getOrderNo());
                e.printStackTrace();
            }
        }
    }
}