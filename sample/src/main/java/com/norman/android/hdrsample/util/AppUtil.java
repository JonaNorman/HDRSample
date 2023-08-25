package com.norman.android.hdrsample.util;

import android.content.Context;

public class AppUtil {
    private static volatile Context APP_CONTEXT;

    public static Context getAppContext() {
        return APP_CONTEXT;
    }

    /**
     * 初始化App的Context，方便后续直接获取
     * @param context
     */
    public static void init(Context context) {
        if (APP_CONTEXT != null || context == null) {
            return;
        }
        synchronized (AppUtil.class) {
            if (APP_CONTEXT == null) {
                APP_CONTEXT = context.getApplicationContext();
            }
        }
    }
}
