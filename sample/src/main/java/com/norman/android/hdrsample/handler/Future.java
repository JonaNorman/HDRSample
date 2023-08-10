package com.norman.android.hdrsample.handler;

import java.util.concurrent.TimeUnit;

//继承Future为了不用throw异常
public interface Future<V> extends java.util.concurrent.Future<V> {
    @Override
    V get();
    @Override
    V get(long timeout, TimeUnit unit);
    
}
