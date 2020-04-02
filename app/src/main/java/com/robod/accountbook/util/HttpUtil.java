package com.robod.accountbook.util;

import com.alibaba.fastjson.JSON;
import com.robod.accountbook.entity.Record;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author Robod Lee
 */
public class HttpUtil {

    private static final String TAG = "HttpUtil";

    public static void sendOkHttpGetRequest(String address , okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void uploadRecords(String address, String phoneNumber , List<Record> toUpgradeRecords , okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("phoneNumber",phoneNumber)
                .add("recordsJson" , JSON.toJSONString(toUpgradeRecords))
                .build();
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
