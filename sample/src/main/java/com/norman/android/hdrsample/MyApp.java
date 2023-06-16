package com.norman.android.hdrsample;

import android.app.Application;

import com.norman.android.hdrsample.util.AppUtil;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.init(this);
    }
}
