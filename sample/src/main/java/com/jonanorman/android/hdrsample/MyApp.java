package com.jonanorman.android.hdrsample;

import android.app.Application;

import com.jonanorman.android.hdrsample.util.AppUtil;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.init(this);
    }
}
