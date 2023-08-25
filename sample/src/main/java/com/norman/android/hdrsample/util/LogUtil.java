package com.norman.android.hdrsample.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * LOG工具类统一设置TAG为HdrSampleLog，方便后续直接搜这个TAG排除其他日志，而方法中的name就替代原先的tag作为其他搜索条件
 */
public class LogUtil {

    private final static String LOG_NAME = "HdrSampleLog";

    public static void v(String name, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append(message);
        Log.v(LOG_NAME, stringBuilder.toString());
    }

    public static void v(String message) {
        v(null, message);
    }

    public static void d(String message) {
        d(null,message);
    }

    public static void d(String name, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append(message);
        Log.d(LOG_NAME, stringBuilder.toString());
    }

    public static void w(String name, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append(message);
        Log.w(LOG_NAME, stringBuilder.toString());
    }

    public static void w(String message) {
        w(null,message);
    }

    public static void e(String name, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append(message);
        Log.e(LOG_NAME, stringBuilder.toString());
    }
    public static void e(String message) {
        e(null,message);
    }


}
