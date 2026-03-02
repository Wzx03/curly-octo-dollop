package com.wangzixian.usedcar;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;
public class PasswordTest {

    public static void main(String[] args) {
        String password = "123456";
        String hash = BCrypt.hashpw(password);

        System.out.println("========================================");
        System.out.println("你的新密码密文是: " + hash);
        System.out.println("========================================");
    }
}
