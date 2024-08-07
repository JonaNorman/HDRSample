package com.norman.android.hdrsample.handler;

import android.os.Looper;
import android.os.MessageQueue;

import com.norman.android.hdrsample.exception.RuntimeException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * MessageHandler的执行线程
 */
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

    /**
     * 线程是否已经结束
     * @return
     */
    public boolean isTerminated() {
        return getState() == State.TERMINATED;
    }

    /**
     * 空闲中，表示可以复用
     * @return
     */
    boolean isIdle() {
        Looper looper = getLooper();
        if (looper == null) {
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
            for (Future future : futureList) {//取消未执行完所有Future，防止一直等待
                future.cancel(true);
            }
            futureList.clear();
        }
    }


    public RunnableFuture<Boolean> createFuture(Runnable runnable) {
        return createFuture(runnable, true);
    }

    public <T> RunnableFuture<T> createFuture(Runnable runnable, T value) {
        return new FutureImpl<>(runnable, value);
    }

    public <T> RunnableFuture<T> createFuture(Callable<T> callable) {
        return new FutureImpl<>(callable);
    }

    class FutureImpl<T> implements Future<T>, RunnableFuture<T> {

        private final FutureTask<T> futureTask;

        public FutureImpl(Runnable runnable, T result) {
            this(Executors.callable(runnable, result));
        }

        public FutureImpl(Callable<T> callable) {
            futureTask = new FutureTask<T>(callable) {
                @Override
                protected void setException(Throwable t) {
                    set(null);//这样处理是为了不把错误抛在等待线程，而是抛在执行线程
                    throw new RuntimeException(t);
                }
            };
        }


        @Override
        public void run() {
            try {
                futureList.add(this);
                futureTask.run();
            } finally {
                futureList.remove(this);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return futureTask.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return futureTask.isCancelled();
        }

        @Override
        public boolean isDone() {
            return futureTask.isDone();
        }

        @Override
        public T get() {
            try {
                return futureTask.get();
            } catch (Exception ignored) {//错误已经抛在执行线程，不需要throw直接catch
            }
            return null;
        }

        @Override
        public T get(long timeout, TimeUnit unit) {
            try {
                return futureTask.get(timeout, unit);
            } catch (Exception ignored) {//错误已经抛在执行线程，不需要throw直接catch
            }
            return null;
        }
    }

    public interface ErrorCallback {
        void onThreadError(Exception exception);
    }
}
