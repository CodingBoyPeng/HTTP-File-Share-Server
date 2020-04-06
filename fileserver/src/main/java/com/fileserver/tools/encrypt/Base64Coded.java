package com.fileserver.tools.encrypt;

import java.util.Base64;

/**
 * @Author: PengFeng
 * @Description:
 * @Date: Created in 13:34 2020/4/2
 */
public class Base64Coded {
    //base64 解码
    public static String decode(byte[] bytes) {
        return new String(Base64.getDecoder().decode(bytes));
    }

    //base64 编码
    public static String encode(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }
}


