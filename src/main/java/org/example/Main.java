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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

import static java.lang.Thread.sleep;
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

    public static void main(String[] args) throws InterruptedException {
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
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDateTime = now.format(formatter);
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 获取当前年份
        int year = today.getYear();
        // 获取当前月份
        int month = today.getMonthValue();
        // 获取当前日期（日）
        int day = today.getDayOfMonth();
        LocalDateTime specifiedTime = LocalDateTime.of(year, month, day, 15, 59, 59);
        // 计算两个时间之间的差异（秒）
        long secondsBetween = ChronoUnit.SECONDS.between(now, specifiedTime);
        if (secondsBetween < 0) {
            return;
        }
        System.out.println("The time is: " + formattedDateTime);
        System.out.println("sleep: " + secondsBetween +" s");
        sleep(secondsBetween * 1000);
        for (int i = 0; i < 10; i++) {
            now = LocalDateTime.now();
            formattedDateTime = now.format(formatter);
            System.out.println("The time is: " + formattedDateTime);
            res = post(url, body, cookie);
            System.out.println(res);
            int resCode = (Integer) res.get("code");
            String convertMessage = resMap.get(resCode);
            System.out.println(convertMessage);
            if (resCode == 0) {
                break;
            }
            sleep(90);
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
