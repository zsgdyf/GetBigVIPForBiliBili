package org.example;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Base64;

import java.util.Map;

import static java.util.Map.entry;


public class Main {

    public static Map<Integer, String> resMap = Map.of(
            78107, "兑换中，权益将在5分钟内发放，若因库存不足等原因兑换失败将自动退还",
            78113, "该手机号已被其他账号授权绑定，无法兑换，请切换账号重试",
            78122, "您要兑换权益的手机号与注册手机号不一致，请重新绑定",
            78125, "本月领取已达上限",
            78126, "手机号非2233卡",
            78127, "权益已发完，请明日再来",
            78104, "福利点不足",
            78117, "该手机号/账号本月已兑换福利点达上限"
    );

    public static void main(String[] args) {
        String encodedString = args[0];
        // 获取解码器
        Base64.Decoder decoder = Base64.getDecoder();
        // 解码 Base64 字符串
        byte[] decodedBytes = decoder.decode(encodedString);
        // 将解码后的字节数组转换为字符串
        String cookie = new String(decodedBytes);
        String url = "https://app.bilibili.com/x/wall/unicom/order/pack/receive";
        String body = "cross_domain=true&id=3&csrf=51f92d671aa194acc592d4dd52c1ff2b";
        JSONObject res;
        for (int i = 0; i < 6; i++) {
            res = post(url, body, cookie);
            System.out.println(res);
            int resCode = (Integer) res.get("code");
            String converMessage = resMap.get(resCode);
            System.out.println(converMessage);
            if (resCode == 0) {
                break;
            }
        }
    }

    public static JSONObject post(String url, String body, String cookie) {
        StringEntity entityBody = new StringEntity(body, "UTF-8");
        RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(defaultConfig).build();
        HttpPost httpPost = new HttpPost(url);
        Map<String, String> headers = Map.ofEntries(
                entry("Accept", "*/*"),
                entry("Accept-Language", "zh,zh-TW;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6"),
                entry("Content-Type", "application/x-www-form-urlencoded"),
                entry("Cookie", cookie),
                entry("Origin", "https://www.bilibili.com"),
                entry("Referer", "https://www.bilibili.com/blackboard/activity-new-freedata.html"),
                entry("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\""),
                entry("Sec-Ch-Ua-Mobile", "?1"),
                entry("Sec-Ch-Ua-Platform", "\"Android\""),
                entry("Sec-Fetch-Dest", "empty"),
                entry("Sec-Fetch-Mode", "cors"),
                entry("Sec-Fetch-Site", "same-site"),
                entry("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
        );

        // 批量设置请求头
        headers.forEach(httpPost::setHeader);

        // 设置请求体
        httpPost.setEntity(entityBody);
        HttpResponse resp;
        String respContent = null;
        try {
            resp = client.execute(httpPost);
            HttpEntity entity;
            entity = resp.getEntity();
            respContent = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            System.out.println("post请求错误 -- " + e);
        }
        return JSONObject.parseObject(respContent);
    }
}