package com.robod.accountbook.util;

import android.widget.Toast;

/**
 * @author Robod Lee
 * @date 2020/2/5 14:09
 */
public class ToastUtil {
    public static void Pop(String content) {
        Toast.makeText(MyApplication.getContext(),content,Toast.LENGTH_SHORT).show();
    }
}
