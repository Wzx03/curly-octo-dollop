
package com.wangzixian.usedcar.common;

import lombok.Data;

/**
 * 【大厂规范】统一响应结果封装类
 * 整个项目所有的 Controller 接口，返回类型必须是这个 Result<T>
 * T 代表 data 里的具体数据类型（比如 User, Car, List<Car> 等）
 */
@Data // 1. 这是 Lombok 注解，自动帮你生成 get/set/toString 方法，代码更清爽
public class Result<T> {

    // 状态码：200成功，500失败
    private Integer code;

    // 提示信息："操作成功" 或 "系统异常"
    private String message;

    // 返回给前端的数据（泛型 T）
    private T data;

    // --- 下面是两个“工厂方法”，方便你快速创建对象 ---

    /**
     * 成功时候调用的方法
     * @param data 你要传给前端的数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    /**
     * 失败时候调用的方法
     * @param msg 错误提示信息
     */
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.message = msg;
        return r;
    }
}