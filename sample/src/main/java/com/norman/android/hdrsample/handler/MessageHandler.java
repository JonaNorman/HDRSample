package com.norman.android.hdrsample.handler;

import android.os.Build;
import android.os.Message;

import androidx.annotation.RequiresApi;

import java.util.concurrent.Callable;
/**
 *  在Handler的基础上支持execute、submit及其同步方法
 */
public interface MessageHandler {
   
    //----------------------Handler已经支持的方法----------------
    boolean post(Runnable r);

    boolean postAtTime(Runnable r, long uptimeMillis);

    boolean postAtTime(Runnable r, Object token, long uptimeMillis);

    boolean postDelayed(Runnable r, long delayMillis);

    @RequiresApi(api = Build.VERSION_CODES.P)
    boolean postDelayed(Runnable r, Object token, long delayMillis);

    boolean postAtFrontOfQueue(Runnable r);

    void removeCallbacks(Runnable r);

    void removeCallbacks(Runnable r, Object token);

    boolean sendMessage(Message msg);

    boolean sendEmptyMessage(int what);

    boolean sendEmptyMessageDelayed(int what, long delayMillis);

    boolean sendEmptyMessageAtTime(int what, long uptimeMillis);

    boolean sendMessageDelayed(Message msg, long delayMillis);

    boolean sendMessageAtTime(Message msg, long uptimeMillis);

    boolean sendMessageAtFrontOfQueue(Message msg);

    void removeMessages(int what);

    void removeMessages(int what, Object object);

    void removeCallbacksAndMessages(Object token);

    boolean hasMessages(int what);

    boolean hasMessages(int what, Object object);

    @RequiresApi(api = Build.VERSION_CODES.Q)
    boolean hasCallbacks(Runnable r);


    //---------------------新增方法----------------

    /**
     * 同步执行方法
     * @param runnable
     * @return
     */
    boolean executeSync(Runnable runnable);

    /**
     * 同步执行方法
     * @param runnable
     * @param timeout
     * @return
     */
    boolean executeSync(Runnable runnable, long timeout);

    /**
     * 是否当前线程
     * @return
     */
    boolean isCurrentThread();

    /**
     * 异步提交callable
     * @param callable
     * @return
     * @param <T>
     */

    <T> Future<T> submit(Callable<T> callable);

    /**
     * 同步提交callable
     * @param callable
     * @return
     * @param <T>
     */
    <T> T submitSync(Callable<T> callable);

    /**
     * 异步提交runnable
     * @param runnable
     * @return
     */
    Future<Boolean> submit(Runnable runnable);

    /**
     * 等待所有消息执行完成
     * @return
     */
    boolean waitAllMessage();

    /**
     * 移除所有未执行消息
     */
    void removeAllMessage();

    /**
     * 取消后续所有消息，前面没有执行完的消息会异步执行完
     * @return
     */
    boolean finishSafe();
    /**
     * 取消后续所有消息，前面没有执行完的消息会同步执行完
     * @return
     */
    boolean finishSafeSync();

    /**
     * 取消后续所有消息，前面没有执行的消息不会执行，正在执行的会异步执行完
     * @return
     */
    boolean finish();
    /**
     * 取消后续所有消息，前面没有执行的消息不会执行，正在执行的会同步执行完
     * @return
     */
    boolean finishSync();

    /**
     * 是否已经关闭
     * @return
     */
    boolean isFinish();

    interface LifeCycleCallback {

        /**
         * 在第一个消息处理时被调用
         */
        default void onHandlerStart() {//
    
        }

        /**
         * 在finish和错误异常时被调用
         */
        default void onHandlerFinish() {//
    
        }


        /**
         * 在异常时被调用
         * @param exception
         */
        default void onHandlerError(Exception exception) {
    
        }
    }

    static MessageHandler obtain() {
        return obtain((String) null);
    }

    static MessageHandler obtain(String threadName) {
        return obtain(threadName, (android.os.Handler.Callback) null);
    }

    static MessageHandler obtain(android.os.Handler.Callback callback) {
        return obtain((String) null, callback);
    }

    static MessageHandler obtain(LifeCycleCallback lifeCycleCallback) {
        return obtain((String) null, null, lifeCycleCallback);
    }

    static MessageHandler obtain(android.os.Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        return obtain((String) null, callback, lifeCycleCallback);
    }

    static MessageHandler obtain(String threadName, android.os.Handler.Callback callback) {
        return obtain(threadName, callback, null);
    }

    static MessageHandler obtain(String threadName, LifeCycleCallback lifeCycleCallback) {
        return obtain(threadName, null, lifeCycleCallback);
    }

    static MessageHandler obtain(String threadName, android.os.Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        MessageThreadPool pool = MessageThreadPool.get();
        return obtain(pool, threadName, callback, lifeCycleCallback);
    }

    static MessageHandler obtain(MessageThreadPool pool) {
        return obtain(pool, (String) null);
    }

    static MessageHandler obtain(MessageThreadPool pool, String threadName) {
        return obtain(pool, threadName, null);
    }

    static MessageHandler obtain(MessageThreadPool pool, android.os.Handler.Callback callback) {
        return obtain(pool, null, callback);
    }

    static MessageHandler obtain(MessageThreadPool pool, android.os.Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        return obtain(pool, null, callback, lifeCycleCallback);
    }

    static MessageHandler obtain(MessageThreadPool pool, String threadName, android.os.Handler.Callback callback) {
        return obtain(pool, threadName, callback, null);
    }

    static MessageHandler obtain(MessageThreadPool pool, String threadName, android.os.Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        return new MessageHandlerImpl(pool, threadName, callback, lifeCycleCallback);
    }
}
