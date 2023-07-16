package com.norman.android.hdrsample.handler;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class MessageHandlerImpl implements MessageHandler {

    private final Object lock = new Object();
    private final MessageThreadPool threadPool;
    private final MessageThread thread;
    private final LifeCycleCallback lifeCycleCallback;
    private final Handler handler;

    private final MessageThread.ErrorCallback errorCallback = new MessageThread.ErrorCallback() {
        @Override
        public void onThreadError(Exception exception) {
            Log.e("aaaa",Log.getStackTraceString(exception));
            if (lifeCycleCallback != null) {
                lifeCycleCallback.onHandlerError(exception);
            }
            finish();
        }
    };
    private boolean hasFinish = false;
    private boolean hasStart = false;


    private final Runnable finishCallbackRunnable = new Runnable() {
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

    MessageHandlerImpl(MessageThreadPool threadPool, String threadName, Handler.Callback callback, LifeCycleCallback lifeCycleCallback) {
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


    @Override
    public final boolean post(Runnable r) {
        if (isFinish()) {
            return false;
        }
        return handler.post(r);
    }


    @Override
    public final boolean postAtTime(Runnable r, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postAtTime(r, uptimeMillis);
    }


    @Override
    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postAtTime(r, token, uptimeMillis);
    }


    @Override
    public final boolean postDelayed(Runnable r, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postDelayed(r, delayMillis);
    }


    @Override
    @RequiresApi(api = Build.VERSION_CODES.P)
    public final boolean postDelayed(Runnable r, Object token, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.postDelayed(r, token, delayMillis);
    }


    @Override
    public final boolean postAtFrontOfQueue(Runnable r) {
        if (isFinish()) {
            return false;
        }
        return handler.postAtFrontOfQueue(r);
    }


    @Override
    public final void removeCallbacks(Runnable r) {
        if (isFinish()) {
            return;
        }
        handler.removeCallbacks(r);
    }


    @Override
    public final void removeCallbacks(Runnable r, Object token) {
        if (isFinish()) {
            return;
        }
        handler.removeCallbacks(r, token);
    }

    @Override
    public final boolean sendMessage(Message msg) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessage(msg);
    }


    @Override
    public final boolean sendEmptyMessage(int what) {
        if (isFinish()) {
            return false;
        }
        return handler.sendEmptyMessage(what);
    }


    @Override
    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendEmptyMessageDelayed(what, delayMillis);
    }


    @Override
    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendEmptyMessageAtTime(what, uptimeMillis);
    }

    @Override
    public final boolean sendMessageDelayed(Message msg, long delayMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessageDelayed(msg, delayMillis);
    }


    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessageAtTime(msg, uptimeMillis);
    }


    @Override
    public final boolean sendMessageAtFrontOfQueue(Message msg) {
        if (isFinish()) {
            return false;
        }
        return handler.sendMessageAtFrontOfQueue(msg);
    }


    @Override
    public final void removeMessages(int what) {
        if (isFinish()) {
            return;
        }
        handler.removeMessages(what);
    }

    @Override
    public final void removeMessages(int what, Object object) {
        if (isFinish()) {
            return;
        }
        handler.removeMessages(what, object);
    }


    @Override
    public final void removeCallbacksAndMessages(Object token) {
        if (isFinish()) {
            return;
        }
        handler.removeCallbacksAndMessages(token);
    }


    @Override
    public final boolean hasMessages(int what) {
        if (isFinish()) {
            return false;
        }
        return handler.hasMessages(what);
    }


    @Override
    public final boolean hasMessages(int what, Object object) {
        if (isFinish()) {
            return false;
        }
        return handler.hasMessages(what, object);
    }


    @Override
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public final boolean hasCallbacks(Runnable r) {
        if (isFinish()) {
            return false;
        }
        return handler.hasCallbacks(r);
    }

    @Override
    public boolean postSync(Runnable runnable) {
        return postSync(runnable, 0);
    }

    @Override
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

    @Override
    public boolean isCurrentThread() {
        return handler.getLooper().isCurrentThread();
    }


    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        RunnableFuture<T> runnableFuture = thread.createFuture(callable);
        post(runnableFuture);
        return runnableFuture;
    }

    @Override
    public <T> T submitSync(Callable<T> callable) {
        RunnableFuture<T> runnableFuture = thread.createFuture(callable);
        post(runnableFuture);
        return runnableFuture.get();
    }

    @Override
    public Future<Boolean> submit(Runnable runnable) {
        RunnableFuture<Boolean> runnableFuture = thread.createFuture(runnable);
        post(runnableFuture);
        return runnableFuture;
    }


    @Override
    public boolean waitAllMessage() {
        return postSync(() -> {});
    }


    @Override
    public void removeAllMessage() {
        removeCallbacksAndMessages(null);
    }


    @Override
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

    @Override
    public boolean finishSafeSync() {
        finishSafe();
        return waitAllMessage();
    }

    @Override
    public boolean finish() {
        removeAllMessage();
        return finishSafe();
    }

    @Override
    public boolean finishSync() {
        finish();
        return waitAllMessage();
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        finish();
    }

    @Override
    public boolean isFinish() {
        synchronized (lock) {
            return hasFinish || thread.isTerminated();
        }
    }


}
