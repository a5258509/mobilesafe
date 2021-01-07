package com.asao.mobilesafe.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密
 *
 *2016-10-10  上午9:38:52
 */
public class MD5Util {
    /**
     * MD5加密操作
     *
     * @param string 2016-10-10 上午9:39:26
     */
    public static String msgToMD5(String string) {
        StringBuilder sb = new StringBuilder();
        //MD5加密：数据摘要加密       123    13
        try {
            //1.获取数据摘要器
            //参数：加密方式
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            //2.摘要数据
            //参数：明文转化成的byte数组
            byte[] digest = messageDigest.digest(string.getBytes());
            //3.加密操作
            for (int i = 0; i < digest.length; i++) {
                //byte:-128-127
                //MD5加密的原理：将一个byte值通过与int类型的255进行与运算，得到一个int类型的正整数
                int result = digest[i] & 0xff;
                //int类型的取值范围比较大，所以有可能会得到一个非常大的int类型的值，会造成md5密码比较长，所以一般会将int类型的值转化成16进制字符串
                String hexString = Integer.toHexString(result);
                if (hexString.length() < 2) {
                    //System.out.print("0");
                    sb.append("0");
                }
                //System.out.println(hexString);
                sb.append(hexString);
            }
            //System.out.println(sb.toString());
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            //加密方式找不到的异常
            e.printStackTrace();
        }
        return string;
    }
}
