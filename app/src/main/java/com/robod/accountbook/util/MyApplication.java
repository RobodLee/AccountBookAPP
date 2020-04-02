package com.robod.accountbook.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;
import org.xutils.x;

/**
 * @author Robod Lee
 * @date 2020/2/5 14:17
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        x.Ext.init(this);
        LitePal.initialize(this);
    }

    public static Context getContext() {
        return context;
    }

}
