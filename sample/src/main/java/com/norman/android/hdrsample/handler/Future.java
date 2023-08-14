package com.norman.android.hdrsample.handler;

import java.util.concurrent.TimeUnit;

//忽略异常的Future，异常会在MessageHandler的onHandlerError回调中
public interface Future<V> extends java.util.concurrent.Future<V> {
    @Override
    V get();
    @Override
    V get(long timeout, TimeUnit unit);
    
}
