package com.robod.accountbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.robod.accountbook.entity.ResultInfo;
import com.robod.accountbook.util.Constant;
import com.robod.accountbook.util.HttpUtil;
import com.robod.accountbook.util.LogUtil;
import com.robod.accountbook.util.ToastUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Robod Lee
 */
@ContentView(R.layout.activity_register)
public class RegisterActivity extends AppCompatActivity {

    @ViewInject(R.id.register_phone_number)
    private EditText etPhoneNumber;
    @ViewInject(R.id.register_username)
    private EditText etUsername;
    @ViewInject(R.id.register_password)
    private EditText etPassword;
    @ViewInject(R.id.register_password_again)
    private EditText etPasswordAgain;

    private String phoneNumber;
    private String username;
    private String password;
    private String passwordAgain;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Event(R.id.register_now_btn)
    private void register(View view) {
        password = etPassword.getText().toString();
        passwordAgain = etPasswordAgain.getText().toString();
        if (password.equals(passwordAgain)) {   //判断两次密码是否一致
            phoneNumber = etPhoneNumber.getText().toString();
            username = etUsername.getText().toString();
            if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
                ToastUtil.Pop("用户名或密码不能为空");
            } else {
                String ip = getSharedPreferences("login_info",MODE_PRIVATE).getString("ip","");
                String address = ip + Constant.USER_REGISTER + "phoneNumber=" + phoneNumber
                        + "&password=" + password + "&username=" + username;
                LogUtil.d(TAG, address);
                HttpUtil.sendOkHttpGetRequest(address, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.Pop("网络异常");
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString = response.body().string();
                        final ResultInfo resultInfo = JSONObject.parseObject(responseString, ResultInfo.class);
                        Log.d(TAG, "onResponse: "+resultInfo.toString());
                        if (resultInfo.isFlag()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferences.Editor editor = getSharedPreferences("login_info", MODE_PRIVATE).edit();
                                    editor.putString("phone_number", phoneNumber);
                                    editor.apply();
                                    ToastUtil.Pop("注册成功");
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();

                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.Pop(resultInfo.getErrorMsg());
                                }
                            });
                        }
                    }
                });
            }
        } else {
            ToastUtil.Pop("两次密码不一致");
        }
    }

}
