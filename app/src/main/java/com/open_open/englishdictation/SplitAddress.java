package com.open_open.englishdictation;

import java.net.URLEncoder;
import java.util.Random;

/**
 * Created by dellpc on 2017/6/15.
 */

public class SplitAddress {

    //百度翻译参数
    private static final String UTF8 = "utf-8";
    //申请者开发者id，实际使用时请修改成开发者自己的appid
    private static final String APP_ID = "20170610000056942";
    //申请成功后的证书token，实际使用时请修改成开发者自己的token (密钥)
    private static final String SECRET_KEY = "StrAVpVn_Y8qkZy8KTaz";
    //翻译API HTTP地址：
    private static final String baseURL = "http://api.fanyi.baidu.com/api/trans/vip/translate";
    //随机数，用于生成md5值，开发者使用时请激活下边第四行代码
    private static final Random random = new Random();

    public static String getAddress(final String word, final String from, final String to) throws Exception {

        String salt = String.valueOf(random.nextInt(10000));
        String md5String = APP_ID + new String(word.getBytes(), "utf-8") + salt + SECRET_KEY;
        final String sign = MD5Encoder.encode(md5String);

        //注意在生成签名拼接 appid+needToTransString+salt+密钥 字符串时，needToTransString不需要做URL encode，
        // 在生成签名之后，发送HTTP请求之前才需要对要发送的待翻译文本字段needToTransString做URL encode。
        String address = baseURL + "?q=" + URLEncoder.encode(word, UTF8) +
                "&from=" + from + "&to=" + to + "&appid=" + APP_ID + "&salt=" + salt + "&sign=" + sign;
        return address;
    }
}
