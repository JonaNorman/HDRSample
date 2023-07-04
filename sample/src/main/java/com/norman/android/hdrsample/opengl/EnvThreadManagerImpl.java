package com.norman.android.hdrsample.opengl;

import android.opengl.EGLContext;

import com.norman.android.hdrsample.handler.Future;
import com.norman.android.hdrsample.handler.MessageHandler;

import java.util.concurrent.Callable;

class EnvThreadManagerImpl implements GLEnvThreadManager, MessageHandler.LifeCycleCallback {
    private MessageHandler messageHandler;
    private GLEnvContextManager contextManager;

    private GLEnvDisplay envDisplay;
    private GLEnvConfig envConfig;
    private EGLContext eglContext;

    private @GLEnvContext.OpenGLESVersion int version;

    private Future<GLEnvContext> envContextFuture;

    private volatile ErrorCallback errorCallback;

    public EnvThreadManagerImpl(GLEnvDisplay envDisplay, GLEnvConfig envConfig, EGLContext eglContext, @GLEnvContext.OpenGLESVersion int version) {
        this.envDisplay = envDisplay;
        this.envConfig = envConfig;
        this.eglContext = eglContext;
        this.version = version;
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
        GLEnvContextManager.Builder builder = new GLEnvContextManager.Builder(envDisplay, envConfig, eglContext);
        builder.setClientVersion(version);
        contextManager = builder.build();
        contextManager.attach();
    }
}
