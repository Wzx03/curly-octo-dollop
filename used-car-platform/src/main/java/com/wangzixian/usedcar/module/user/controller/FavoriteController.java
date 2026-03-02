package com.wangzixian.usedcar.module.user.controller;

import cn.hutool.jwt.JWTUtil;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.user.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    private Long getUserIdFromToken(String token) {
        try {
            Object idObj = JWTUtil.parseToken(token).getPayload("id");
            return Long.valueOf(idObj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/toggle/{carId}")
    public Result<String> toggle(@PathVariable Long carId, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");

        boolean isFavorited = favoriteService.toggleFavorite(userId, carId);
        return Result.success(isFavorited ? "收藏成功" : "已取消收藏");
    }

    @GetMapping("/check/{carId}")
    public Result<Boolean> check(@PathVariable Long carId, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.success(false);

        return Result.success(favoriteService.isFavorite(userId, carId));
    }

    @GetMapping("/my-ids")
    public Result<List<Long>> getMyFavoriteIds(@RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.success(List.of());

        return Result.success(favoriteService.getFavoriteCarIds(userId));
    }

    @GetMapping("/my-list")
    public Result<List<Car>> getMyFavoriteCars(@RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");

        return Result.success(favoriteService.getMyFavoriteCars(userId));
    }
}