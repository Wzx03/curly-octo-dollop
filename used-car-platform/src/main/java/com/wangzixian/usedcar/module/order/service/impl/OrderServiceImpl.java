package com.wangzixian.usedcar.module.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzixian.usedcar.common.RedisUtil;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.service.CarService;
import com.wangzixian.usedcar.module.chat.server.ChatServer;
import com.wangzixian.usedcar.module.order.entity.Order;
import com.wangzixian.usedcar.module.order.mapper.OrderMapper;
import com.wangzixian.usedcar.module.order.service.OrderService;
import com.wangzixian.usedcar.module.order.vo.OrderVO;
import com.wangzixian.usedcar.module.user.entity.TransactionLog;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.TransactionLogMapper;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private CarService carService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionLogMapper transactionLogMapper;

    private static final String CACHE_KEY_PREFIX = "car:detail:";
    private static final String LOCK_KEY_PREFIX = "lock:order:car:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long carId, Long buyerId) {
        String lockKey = LOCK_KEY_PREFIX + carId;
        String requestId = IdUtil.fastSimpleUUID();
        
        // 1. 获取分布式锁 (防止超卖/并发下单)
        boolean locked = redisUtil.setIfAbsent(lockKey, requestId, 10); // 10秒锁过期
        if (!locked) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        try {
            Car car = carService.getById(carId);
            if (car == null) throw new RuntimeException("车辆不存在");
            if (car.getStatus() != 1) throw new RuntimeException("手慢了！该车已售出或未上架");

            boolean updateSuccess = carService.update(new LambdaUpdateWrapper<Car>()
                    .eq(Car::getId, carId)
                    .eq(Car::getStatus, 1)
                    .set(Car::getStatus, 2));

            if (!updateSuccess) {
                throw new RuntimeException("抢购失败，请重试");
            }

            redisUtil.delete(CACHE_KEY_PREFIX + carId);
            // 清除首页列表缓存 (因为车辆状态变了，不再显示在首页)
            redisUtil.delete("car:list:home");

            Order order = new Order();
            order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
            order.setCarId(carId);
            order.setBuyerId(buyerId);
            order.setSellerId(car.getUserId());
            order.setTotalPrice(car.getPrice());
            order.setStatus(0);
            order.setCreateTime(LocalDateTime.now());
            this.save(order);

            return order.getOrderNo();
        } finally {
            // 释放锁 (简单判断值是否一致，严谨做法应用Lua脚本，但这里简单处理)
            String currentVal = redisUtil.get(lockKey);
            if (requestId.equals(currentVal)) {
                redisUtil.delete(lockKey);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 0) throw new RuntimeException("订单已支付或已取消");

        // 1. 扣减买家余额
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer == null) throw new RuntimeException("买家用户不存在");
        
        double price = order.getTotalPrice().doubleValue();
        if (buyer.getBalance() < price) {
            throw new RuntimeException("余额不足，请先充值");
        }
        buyer.setBalance(buyer.getBalance() - price);
        userMapper.updateById(buyer);

        // 记录买家流水
        TransactionLog buyerLog = new TransactionLog();
        buyerLog.setUserId(buyer.getId());
        buyerLog.setType(2); // 消费
        buyerLog.setAmount(order.getTotalPrice().negate()); // 负数
        buyerLog.setBalanceAfter(BigDecimal.valueOf(buyer.getBalance()));
        buyerLog.setRemark("购买车辆: " + order.getCarId());
        buyerLog.setCreateTime(LocalDateTime.now());
        transactionLogMapper.insert(buyerLog);

        // 2. 增加卖家余额
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            seller.setBalance(seller.getBalance() + price);
            userMapper.updateById(seller);

            // 记录卖家流水
            TransactionLog sellerLog = new TransactionLog();
            sellerLog.setUserId(seller.getId());
            sellerLog.setType(3); // 收入
            sellerLog.setAmount(order.getTotalPrice());
            sellerLog.setBalanceAfter(BigDecimal.valueOf(seller.getBalance()));
            sellerLog.setRemark("出售车辆: " + order.getCarId());
            sellerLog.setCreateTime(LocalDateTime.now());
            transactionLogMapper.insert(sellerLog);
        } else {
            System.out.println("Warning: Seller not found for order " + orderNo + ", ID: " + order.getSellerId());
        }

        // 3. 更新订单状态
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);

        // 4. 更新车辆状态
        Car car = carService.getById(order.getCarId());
        car.setStatus(3);
        carService.updateById(car);
        
        ChatServer.sendInfoToAdmins("新订单支付成功：" + orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOvertimeOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) return;

        order.setStatus(2);
        this.updateById(order);

        carService.update(new LambdaUpdateWrapper<Car>()
                .eq(Car::getId, order.getCarId())
                .eq(Car::getStatus, 2)
                .set(Car::getStatus, 1));

        redisUtil.delete(CACHE_KEY_PREFIX + order.getCarId());
        // 恢复上架，清除首页缓存
        redisUtil.delete("car:list:home");
        
        System.out.println("订单超时自动关闭：" + order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrdersByCarId(Long carId) {
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getCarId, carId)
               .eq(Order::getStatus, 0)
               .set(Order::getStatus, 2);
        
        this.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 0) throw new RuntimeException("订单状态已改变，无法取消");

        order.setStatus(2);
        this.updateById(order);

        carService.update(new LambdaUpdateWrapper<Car>()
                .eq(Car::getId, order.getCarId())
                .eq(Car::getStatus, 2)
                .set(Car::getStatus, 1));

        redisUtil.delete(CACHE_KEY_PREFIX + order.getCarId());
        // 恢复上架，清除首页缓存
        redisUtil.delete("car:list:home");
    }

    @Override
    public List<OrderVO> getMyOrders(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, userId);
        wrapper.orderByDesc(Order::getCreateTime);
        
        List<Order> orders = this.list(wrapper);
        List<OrderVO> vos = new ArrayList<>();
        
        for (Order order : orders) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            
            Car car = carService.getById(order.getCarId());
            if (car != null) {
                vo.setCarBrand(car.getBrand());
                vo.setCarModel(car.getModel());
                vo.setCarImage(car.getImage());
                vo.setCarYear(car.getBuyYear());
            }
            vos.add(vo);
        }
        return vos;
    }
}