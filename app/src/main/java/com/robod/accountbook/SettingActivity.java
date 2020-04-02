package com.robod.accountbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.robod.accountbook.util.ToastUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * @author Robod Lee
 */
@ContentView(R.layout.activity_setting)
public class SettingActivity extends AppCompatActivity {

    @ViewInject(R.id.server_ip)
    private EditText serverIPEt;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        preferences = getSharedPreferences("login_info",MODE_PRIVATE);
        editor = preferences.edit();
        ip = preferences.getString("ip","");
        if (!TextUtils.isEmpty(ip)) {
            ip = ip.substring(7);
            ip = ip.split(":")[0];
            serverIPEt.setText(ip);
        }

    }

    @Event(R.id.save_setting)
    private void myClick(View view) {
        switch (view.getId()) {
            case R.id.save_setting:
                ip = serverIPEt.getText().toString();
                if (!TextUtils.isEmpty(ip)) {
                    editor.putString("ip","http://"+ip+":8080/accountbook");
                    editor.apply();
                    ToastUtil.Pop("保存成功");
                    finish();
                }
                break;
            default:
        }
    }
}
