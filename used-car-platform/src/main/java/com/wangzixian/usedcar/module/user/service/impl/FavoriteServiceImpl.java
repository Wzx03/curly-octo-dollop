package com.wangzixian.usedcar.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.mapper.CarMapper;
import com.wangzixian.usedcar.module.user.entity.Favorite;
import com.wangzixian.usedcar.module.user.mapper.FavoriteMapper;
import com.wangzixian.usedcar.module.user.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private CarMapper carMapper;

    @Override
    public boolean toggleFavorite(Long userId, Long carId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getCarId, carId);
        
        Favorite exist = this.getOne(wrapper);
        if (exist != null) {
            // 已存在，则删除 (取消收藏)
            this.removeById(exist.getId());
            return false;
        } else {
            // 不存在，则添加 (收藏)
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setCarId(carId);
            favorite.setCreateTime(LocalDateTime.now());
            this.save(favorite);
            return true;
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long carId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getCarId, carId);
        return this.count(wrapper) > 0;
    }

    @Override
    public List<Long> getFavoriteCarIds(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.select(Favorite::getCarId);
        
        return this.list(wrapper).stream()
                .map(Favorite::getCarId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getMyFavoriteCars(Long userId) {
        List<Long> carIds = getFavoriteCarIds(userId);
        if (carIds.isEmpty()) {
            return new ArrayList<>();
        }
        return carMapper.selectBatchIds(carIds);
    }
}