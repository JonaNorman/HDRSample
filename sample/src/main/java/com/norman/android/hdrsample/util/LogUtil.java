package com.norman.android.hdrsample.util;

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {
    private final static String TAG = "HdrSampleLog";

    public static void v(String name, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append(message);
        Log.v(TAG, stringBuilder.toString());
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
        Log.d(TAG, stringBuilder.toString());
    }

    public static void w(String name, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append(message);
        Log.w(TAG, stringBuilder.toString());
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
        Log.e(TAG, stringBuilder.toString());
    }
    public static void e(String message) {
        e(null,message);
    }


}
