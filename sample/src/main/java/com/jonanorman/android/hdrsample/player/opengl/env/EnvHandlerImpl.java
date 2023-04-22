package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGLContext;

import com.jonanorman.android.hdrsample.util.MessageHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

class EnvHandlerImpl implements GLEnvHandler, MessageHandler.LifeCycleCallback {
    MessageHandler messageHandler;
    GLEnvAttachManager manager;

    private GLEnvDisplay envDisplay;
    private GLEnvConfig envConfig;
    private EGLContext eglContext;

    private @GLEnvContext.OpenGLESVersion int version;

    private Future<GLEnvContext> envContextFuture;

    public EnvHandlerImpl(GLEnvDisplay envDisplay, GLEnvConfig envConfig, EGLContext eglContext, @GLEnvContext.OpenGLESVersion int version) {
        this.envDisplay = envDisplay;
        this.envConfig = envConfig;
        this.eglContext = eglContext;
        this.version = version;
        messageHandler = MessageHandler.obtain();
        messageHandler.addLifeCycleCallback(this);
        envContextFuture = submit(new Callable<GLEnvContext>() {
            @Override
            public GLEnvContext call() {
                return manager.getEnvContext();
            }
        });
    }


    @Override
    public void recycle() {
        messageHandler.recycle();
    }

    @Override
    public boolean isRecycle() {
        return messageHandler.isRecycle();
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
    public boolean postAndWait(Runnable runnable) {
        return messageHandler.postAndWait(runnable);
    }

    @Override
    public boolean postAndWait(Runnable runnable, long timeout) {
        return messageHandler.postAndWait(runnable, timeout);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return messageHandler.submit(callable);
    }

    @Override
    public GLEnvContext getEnvContext() {
        synchronized (this) {
            try {
                return envContextFuture.get();
            } catch (Exception e) {

            }
            return null;
        }
    }

    @Override
    public <T> T submitAndWait(Callable<T> callable) {
        return messageHandler.submitAndWait(callable);
    }

    @Override
    public Future<Boolean> submit(Runnable runnable) {
        return messageHandler.submit(runnable);
    }


    @Override
    public void onHandlerFinish() {
        manager.release();
    }

    @Override
    public void onHandlerError(Exception exception) {
    }

    @Override
    public void onHandlerStart() {
        GLEnvAttachManager.Builder builder = new GLEnvAttachManager.Builder(envDisplay, envConfig, eglContext);
        builder.setClientVersion(version);
        manager = builder.build();
        manager.attachCurrentThread();
    }
}
