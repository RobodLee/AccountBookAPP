package com.robod.accountbook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.robod.accountbook.entity.Record;
import com.robod.accountbook.entity.ResultInfo;
import com.robod.accountbook.entity.User;
import com.robod.accountbook.util.Constant;
import com.robod.accountbook.util.ConvertUtils;
import com.robod.accountbook.util.HttpUtil;
import com.robod.accountbook.util.ToastUtil;

import org.litepal.LitePal;
import org.xutils.view.annotation.ContentView;
import org.xutils.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Robod Lee
 */
@ContentView(R.layout.activity_loading)
public class LoadingActivity extends AppCompatActivity {

    private boolean isUpload;   //是否是上传，不是上传就是下载
    private SharedPreferences preferences;
    private String phoneNumber;
    private static final String TAG = "LoadingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        ToastUtil.Pop("同步中...");
        isUpload = getIntent().getBooleanExtra("upload", false);
        preferences = getSharedPreferences("login_info", MODE_PRIVATE);

        boolean alreadyLogin = preferences.getBoolean("already_login", false);
        if (alreadyLogin) {
            String loginUserStr = preferences.getString("login_user", "");
            if (!TextUtils.isEmpty(loginUserStr)) {
                User loginUser = JSONObject.parseObject(loginUserStr, User.class);
                phoneNumber = loginUser.getPhoneNumber();
                if (!TextUtils.isEmpty(phoneNumber)) {
                    syncData();
                }
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoadingActivity.this);
            builder.setMessage("您尚未登陆，请先登录");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                }
            });
            builder.show();
        }

    }

    //同步数据
    private void syncData() {
        String ip = preferences.getString("ip","");

        if (isUpload) {     //上传
            final List<Record> toUpgradeRecords = LitePal.where("status > ?", "0").find(Record.class);
            HttpUtil.uploadRecords(ip+Constant.UPLOAD_RECORDS, phoneNumber, toUpgradeRecords, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    showToast("服务器异常");
                    finish();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    ResultInfo resultInfo = JSONObject.parseObject(responseBody, ResultInfo.class);
                    if (resultInfo.isFlag()) {
                        for (Record record : toUpgradeRecords) {
                            if (record.getStatus() != 2) {
                                record.setStatus(0);
                                record.save();
                            } else {
                                record.delete();
                            }
                        }
                        showToast("同步成功");
                        finish();
                    } else {
                        showToast("同步失败");
                        finish();
                    }
                }
            });
        } else {            //下载
            String address = ip+Constant.DOWNLOAD_RECORDS + phoneNumber;
            HttpUtil.sendOkHttpGetRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    showToast("服务器异常");
                    finish();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    ResultInfo resultInfo = JSONObject.parseObject(responseBody, ResultInfo.class);
                    Log.d(TAG, "onResponse: "+resultInfo.getData().toString());
                    List<Record> recordsDownloadFormServer = JSONArray.parseArray(resultInfo.getData().toString(),Record.class);
                    if (resultInfo.isFlag()) {
                        for (Record record : recordsDownloadFormServer) {
                            try {
                                LitePal.where("uuid = ? ", record.getUuid()).find(Record.class).get(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                record.setDateString(ConvertUtils.dateToString(record.getDate()));
                                record.save();
                            }
                        }
                        showToast("下载成功");
                    }
                    finish();
                }
            });
        }
    }

    private void showToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.Pop(content);
            }
        });
    }

}
