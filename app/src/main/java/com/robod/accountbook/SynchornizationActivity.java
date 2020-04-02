package com.robod.accountbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.x;

/**
 * @author Robod Lee
 */
@ContentView(R.layout.activity_synchornization)
public class SynchornizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
    }

    @Event(value = {R.id.upload_to_cloud, R.id.down_to_local})
    private void myClick(View view) {
        Intent intent = new Intent(SynchornizationActivity.this , LoadingActivity.class);
        switch (view.getId()) {
            case R.id.upload_to_cloud:
                intent.putExtra("upload",true);
                startActivity(intent);
                break;
            case R.id.down_to_local:
                intent.putExtra("upload",false);
                startActivity(intent);
                break;
            default:
        }
    }
}
