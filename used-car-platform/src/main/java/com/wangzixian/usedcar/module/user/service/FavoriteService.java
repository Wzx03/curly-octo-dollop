package com.wangzixian.usedcar.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.user.entity.Favorite;

import java.util.List;

public interface FavoriteService extends IService<Favorite> {
    /**
     * 切换收藏状态 (已收藏则取消，未收藏则添加)
     * @param userId 用户ID
     * @param carId 车辆ID
     * @return true=收藏成功, false=取消收藏
     */
    boolean toggleFavorite(Long userId, Long carId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorite(Long userId, Long carId);

    /**
     * 获取用户收藏的车辆ID列表
     */
    List<Long> getFavoriteCarIds(Long userId);

    /**
     * 获取用户收藏的车辆详情列表
     */
    List<Car> getMyFavoriteCars(Long userId);
}