package com.norman.android.hdrsample.handler;

import android.os.Looper;
import android.os.MessageQueue;

import com.norman.android.hdrsample.util.ExceptionUtil;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class MessageThread extends android.os.HandlerThread {

    final List<Future> futureList = new CopyOnWriteArrayList<>();
    final List<ErrorCallback> errorCallbackList = new CopyOnWriteArrayList<>();
    long cacheTime;

    MessageThread(String name) {
        super(name);
    }


    public void addErrorCallback(ErrorCallback errorCallback) {
        errorCallbackList.add(errorCallback);
    }

    public void removeErrorCallback(ErrorCallback errorCallback) {
        errorCallbackList.remove(errorCallback);
    }

    public boolean isTerminated() {
        return getState() == State.TERMINATED;
    }

    public boolean isIdle() {
        Looper looper = getLooper();
        if (looper == null){
            return false;
        }
        MessageQueue messageQueue = looper.getQueue();
        return !isTerminated() && messageQueue.isIdle();
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Exception e) {
            for (ErrorCallback errorCallback : errorCallbackList) {
                errorCallback.onThreadError(e);
            }
            errorCallbackList.clear();
        } finally {
            cancelAll();
        }
    }


    private void cancelAll() {
        for (Future future : futureList) {
            future.cancel(true);
        }
        futureList.clear();
    }

    public FutureTask createFutureTask(Runnable runnable) {
        return createFutureTask(runnable, true);
    }

    public <T> FutureTask<T> createFutureTask(Runnable runnable, T value) {
        FutureTaskImpl<T> futureTask = new FutureTaskImpl<T>(runnable, value);
        return futureTask;
    }

    public <T> FutureTask<T> createFutureTask(Callable<T> callable) {
        return new FutureTaskImpl(callable);
    }

    class FutureTaskImpl<T> extends FutureTask<T> {

        public FutureTaskImpl(Callable callable) {
            super(callable);
            init();
        }

        public FutureTaskImpl(Runnable runnable, T result) {
            super(runnable, result);
            init();
        }

        private void init() {
            if (isTerminated()) {
                cancel(true);
            } else {
                futureList.add(this);
            }
        }

        @Override
        protected void setException(Throwable t) {
            set(null);
            ExceptionUtil.throwRuntime(t);
        }

        @Override
        public void run() {
            try {
                super.run();
            } finally {
                futureList.remove(this);
            }
        }
    }

    public interface ErrorCallback {
        void onThreadError(Exception exception);
    }
}
