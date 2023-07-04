package com.norman.android.hdrsample.handler;

import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MessageHandler {

    private final Object lock = new Object();
    private final MessageThreadPool threadPool;
    private final MessageThread thread;
    private final LifeCycleCallback lifeCycleCallback;
    private final Handler handler;

    private final MessageThread.ErrorCallback errorCallback = new MessageThread.ErrorCallback() {
        @Override
        public void onThreadError(Exception exception) {
            if (lifeCycleCallback != null) {
                lifeCycleCallback.onHandlerError(exception);
            }
            finish();
        }
    };
    private boolean hasFinish = false;
    private boolean hasStart = false;


    private Runnable finishCallbackRunnable = new Runnable() {
        @Override
        public void run() {
            if (!hasStart) {
                return;
            }
            if (lifeCycleCallback != null) {
                lifeCycleCallback.onHandlerFinish();
            }
        }
    };

    MessageHandler(MessageThreadPool threadPool, String threadName, Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        super();
        this.threadPool = threadPool;
        this.lifeCycleCallback = lifeCycleCallback;
        thread = threadPool.obtain(threadName);
        thread.addErrorCallback(errorCallback);
        handler = new Handler(thread.getLooper(), callback) {
            @Override
            public void dispatchMessage(@NonNull Message msg) {
                if (!hasStart) {
                    hasStart = true;
                    if (lifeCycleCallback != null) {
                        lifeCycleCallback.onHandlerStart();
                    }
                }
                super.dispatchMessage(msg);
            }
        };

    }


    public final boolean post(Runnable r) {
        if (isFinish()) {
            return false;
        }
        return handler.post(r);
    }


    public final boolean postAtTime(Runnable r, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postAtTime(r, uptimeMillis);
    }


    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postAtTime(r, token, uptimeMillis);
    }


    public final boolean postDelayed(Runnable r, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postDelayed(r, delayMillis);
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    public final boolean postDelayed(Runnable r, Object token, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postDelayed(r, token, delayMillis);
    }


    public final boolean postAtFrontOfQueue(Runnable r) {
        if (isFinish()) {
            return false;
        }
        return handler.postAtFrontOfQueue(r);
    }


    public final void removeCallbacks(Runnable r) {
        if (isFinish()) {
            return;
        }
        handler.removeCallbacks(r);
    }


    public final void removeCallbacks(Runnable r, Object token) {
        if (isFinish()) {
            return;
        }
        handler.removeCallbacks(r, token);
    }

    public final boolean sendMessage(Message msg) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessage(msg);
    }


    public final boolean sendEmptyMessage(int what) {
        if (isFinish()) {
            return false;
        }
        return handler.sendEmptyMessage(what);
    }


    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendEmptyMessageDelayed(what, delayMillis);
    }


    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendEmptyMessageAtTime(what, uptimeMillis);
    }

    public final boolean sendMessageDelayed(Message msg, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessageDelayed(msg, delayMillis);
    }


    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessageAtTime(msg, uptimeMillis);
    }


    public final boolean sendMessageAtFrontOfQueue(Message msg) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessageAtFrontOfQueue(msg);
    }


    public final void removeMessages(int what) {
        if (isFinish()) {
            return;
        }
        handler.removeMessages(what);
    }

    public final void removeMessages(int what, Object object) {
        if (isFinish()) {
            return;
        }
        handler.removeMessages(what, object);
    }


    public final void removeCallbacksAndMessages(Object token) {
        if (isFinish()) {
            return;
        }
        handler.removeCallbacksAndMessages(token);
    }


    public final boolean hasMessages(int what) {
        if (isFinish()) {
            return false;
        }
        return handler.hasMessages(what);
    }


    public final boolean hasMessages(int what, Object object) {
        if (isFinish()) {
            return false;
        }
        return handler.hasMessages(what, object);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public final boolean hasCallbacks(Runnable r) {
        if (isFinish()) {
            return false;
        }
        return handler.hasCallbacks(r);
    }

    public boolean postSync(Runnable runnable) {
        return postSync(runnable, 0);
    }

    public boolean postSync(Runnable runnable, long timeout) {
        if (isFinish()) {
            return false;
        }
        RunnableFuture<Boolean> runnableFuture = thread.createFuture(runnable);
        boolean success = post(runnableFuture);
        if (!success) {
            return false;
        }
        Boolean result;
        if (timeout <= 0) {
            result = runnableFuture.get();
        } else {
            result = runnableFuture.get(timeout, TimeUnit.MILLISECONDS);
        }
        return result != null;
    }

    public boolean isCurrentThread() {
        return handler.getLooper().isCurrentThread();
    }


    public <T> Future<T> submit(Callable<T> callable) {
        RunnableFuture<T> runnableFuture = thread.createFuture(callable);
        post(runnableFuture);
        return runnableFuture;
    }

    public <T> T submitSync(Callable<T> callable) {
        RunnableFuture<T> runnableFuture = thread.createFuture(callable);
        post(runnableFuture);
        return runnableFuture.get();
    }

    public Future<Boolean> submit(Runnable runnable) {
        RunnableFuture<Boolean> runnableFuture = thread.createFuture(runnable);
        post(runnableFuture);
        return runnableFuture;
    }


    public boolean waitAllMessage() {
        return postSync(() -> {
        });
    }


    public void removeAllMessage() {
        removeCallbacksAndMessages(null);
    }


    public boolean finishSafe() {
        synchronized (lock) {
            if (hasFinish) {
                return false;
            }
            if (isCurrentThread()) {
                finishCallbackRunnable.run();
            } else {
                post(finishCallbackRunnable);
            }
            hasFinish = true;
            thread.removeErrorCallback(errorCallback);
            this.threadPool.recycle(thread);
            return true;
        }
    }

    public boolean finishSafeSync() {
        finishSafe();
        return waitAllMessage();
    }

    public boolean finish() {
        removeAllMessage();
        return finishSafe();
    }

    public boolean finishSync() {
        finish();
        return waitAllMessage();
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        finish();
    }

    public boolean isFinish() {
        synchronized (lock) {
            return hasFinish || thread.isTerminated();
        }
    }

    public static MessageHandler obtain() {
        return obtain((String) null);
    }

    public static MessageHandler obtain(String threadName) {
        return obtain(threadName, (Handler.Callback) null);
    }

    public static MessageHandler obtain(Handler.Callback callback) {
        return obtain((String) null, callback);
    }

    public static MessageHandler obtain(LifeCycleCallback lifeCycleCallback) {
        return obtain((String) null, null, lifeCycleCallback);
    }

    public static MessageHandler obtain(Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        return obtain((String) null, callback, lifeCycleCallback);
    }

    public static MessageHandler obtain(String threadName, Handler.Callback callback) {
        return obtain(threadName, callback, null);
    }

    public static MessageHandler obtain(String threadName, LifeCycleCallback lifeCycleCallback) {
        return obtain(threadName, null, lifeCycleCallback);
    }

    public static MessageHandler obtain(String threadName, Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        MessageThreadPool pool = MessageThreadPool.get();
        return obtain(pool, threadName, callback, lifeCycleCallback);
    }

    public static MessageHandler obtain(MessageThreadPool pool) {
        return obtain(pool, (String) null);
    }

    public static MessageHandler obtain(MessageThreadPool pool, String threadName) {
        return obtain(pool, threadName, null);
    }

    public static MessageHandler obtain(MessageThreadPool pool, Handler.Callback callback) {
        return obtain(pool, null, callback);
    }

    public static MessageHandler obtain(MessageThreadPool pool, Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        return obtain(pool, null, callback, lifeCycleCallback);
    }


    public static MessageHandler obtain(MessageThreadPool pool, String threadName, Handler.Callback callback) {
        return obtain(pool, threadName, callback, null);
    }

    public static MessageHandler obtain(MessageThreadPool pool, String threadName, Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
        return new MessageHandler(pool, threadName, callback, lifeCycleCallback);
    }


    public interface LifeCycleCallback {

        default void onHandlerStart() {

        }

        default void onHandlerFinish() {

        }

        default void onHandlerError(Exception exception) {

        }
    }


}
