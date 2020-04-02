package com.robod.accountbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.alibaba.fastjson.JSONArray;
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
@ContentView(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity{

    @ViewInject(R.id.phone_number)
    private EditText etPhoneNumber;
    private String phoneNumber;

    @ViewInject(R.id.password)
    private EditText etPassword;
    private String password;

    @ViewInject(R.id.remember_password)
    private CheckBox rememberPassword;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String ip;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        init();
    }

    private void init() {
        preferences = getSharedPreferences("login_info",MODE_PRIVATE);
        phoneNumber = preferences.getString("phone_number" , "");
        ip = preferences.getString("ip","");
        etPhoneNumber.setText(phoneNumber);
        boolean rememberPasswordBool = preferences.getBoolean("remember_password" , false);
        if (rememberPasswordBool) {
            password = preferences.getString("password" , "");
            etPassword.setText(password);
            rememberPassword.setChecked(true);
        }
    }

    @Event(value = {R.id.register,R.id.login,R.id.find_password})
    private void myClick(View view) {
        switch (view.getId()) {
            case R.id.register:
                startActivity(new Intent(LoginActivity.this , RegisterActivity.class));
                break;
            case R.id.login:
                phoneNumber = etPhoneNumber.getText().toString();
                password = etPassword.getText().toString();
                if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.isEmpty(password)) {
                    String address = ip + Constant.USER_LOGIN + "phoneNumber=" + phoneNumber + "&password=" + password;
                    LogUtil.d(TAG , address);
                    HttpUtil.sendOkHttpGetRequest(address, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.Pop("服务器异常");
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseString = response.body().string();
                            final ResultInfo resultInfo = JSONObject.parseObject(responseString,ResultInfo.class);
                            if (resultInfo.isFlag()) {
                                editor = preferences.edit();
                                if (rememberPassword.isChecked()) {
                                    editor.putBoolean("remember_password" , true);
                                }
                                editor.putBoolean("already_login" , true);
                                String loginUserStr = JSONArray.toJSONString(resultInfo.getData());
                                editor.putString("login_user" , loginUserStr);
                                editor.apply();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.Pop("登录成功");
                                        startActivity(new Intent(LoginActivity.this , MainActivity.class));
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
                } else {
                    ToastUtil.Pop("用户名或密码不能为空");
                }
                break;
            case R.id.find_password:
                ToastUtil.Pop("未完成的功能");
                break;
            default: break;
        }
    }

}
