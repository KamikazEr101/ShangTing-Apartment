package com.atguigu.lease.common.util;

import java.util.Random;

public class VerifyCodeUtil {
    public static String generateVerifyCode(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int code = random.nextInt(10);
            stringBuilder.append(code);
        }
        return stringBuilder.toString();
    }
}
