package com.norman.android.hdrsample.handler;

import java.util.concurrent.TimeUnit;

/**
 * 忽略异常的Future，异常会在{@link MessageHandler.LifeCycleCallback#onHandlerError(Exception)}中回调
 * @param <V>
 */
public interface Future<V> extends java.util.concurrent.Future<V> {
    @Override
    V get();
    @Override
    V get(long timeout, TimeUnit unit);
    
}
