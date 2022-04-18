package com.mengxinya.ys.social.wechatplatform;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class WechatPlatform {

    public boolean checkConnect(WechatConnectVo vo) {
        String Token = "修改为你的Token！！！";
        // 字典序排序
        String[] getStr = new String[]{Token, vo.timestamp(), vo.nonce()};
        Arrays.sort(getStr);

        //sha1加密
        String strConn = getStr[0] + getStr[1] + getStr[2];
        //引入一个char[] 已知为16进制
        char[] changeChar = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("sha1");
            byte[] digest = messageDigest.digest(strConn.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(changeChar[(b >> 4) & 15]);
                sb.append(changeChar[(b) & 15]);
            }
            String s = sb.toString();
            String signature = vo.signature();
            return s.equals(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("wechat check connect error", e);
        }
    }
}
