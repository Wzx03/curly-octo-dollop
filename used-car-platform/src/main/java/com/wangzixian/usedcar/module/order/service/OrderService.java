package com.wangzixian.usedcar.module.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzixian.usedcar.module.order.entity.Order;
import com.wangzixian.usedcar.module.order.vo.OrderVO;
import java.util.List;

public interface OrderService extends IService<Order> {
    String createOrder(Long carId, Long buyerId);

    void pay(String orderNo);

    void closeOvertimeOrder(Long orderId);

    void cancelOrdersByCarId(Long carId);

    void cancelOrder(Long orderId);

    /**
     * 获取我的订单（带车辆信息）
     */
    List<OrderVO> getMyOrders(Long userId);
}