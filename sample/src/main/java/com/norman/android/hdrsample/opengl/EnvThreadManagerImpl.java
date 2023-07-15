package com.norman.android.hdrsample.opengl;

import com.norman.android.hdrsample.handler.Future;
import com.norman.android.hdrsample.handler.MessageHandler;

import java.util.concurrent.Callable;

class EnvThreadManagerImpl implements GLEnvThreadManager, MessageHandler.LifeCycleCallback {
    private MessageHandler messageHandler;
    private GLEnvContextManager contextManager;


    private Future<GLEnvContext> envContextFuture;

    private volatile ErrorCallback errorCallback;

    public EnvThreadManagerImpl(GLEnvContextManager envContextManager) {
        this.contextManager = envContextManager;
        messageHandler = MessageHandler.obtain(this);
        envContextFuture = submit(() -> contextManager.getEnvContext());
    }


    @Override
    public void release() {
        messageHandler.finish();
    }

    @Override
    public boolean isRelease() {
        return messageHandler.isFinish();
    }

    @Override
    public boolean post(Runnable runnable) {
        return messageHandler.post(runnable);
    }

    @Override
    public boolean postDelayed(Runnable r, long delayMillis) {
        return messageHandler.postDelayed(r, delayMillis);
    }

    @Override
    public boolean postSync(Runnable runnable) {
        return messageHandler.postSync(runnable);
    }

    @Override
    public boolean postSync(Runnable runnable, long timeout) {
        return messageHandler.postSync(runnable, timeout);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return messageHandler.submit(callable);
    }

    @Override
    public GLEnvContext getEnvContext() {
        return  envContextFuture.get();
    }

    @Override
    public <T> T submitSync(Callable<T> callable) {
        return messageHandler.submitSync(callable);
    }

    @Override
    public Future<Boolean> submit(Runnable runnable) {
        return messageHandler.submit(runnable);
    }

    @Override
    public void setErrorCallback(ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }


    @Override
    public void onHandlerFinish() {
        contextManager.release();
    }

    @Override
    public void onHandlerError(Exception exception) {
        ErrorCallback callback = this.errorCallback;
        if (callback != null) {
            callback.onEnvThreadError(exception);
        }
    }

    @Override
    public void onHandlerStart() {
        contextManager.attach();
    }
}
