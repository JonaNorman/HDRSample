package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLContext;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface GLEnvThreadManager {


    void release();

    boolean isRelease();


    /**
     * 在当前OpenGL环境提交一个请求
     * @param runnable
     * @return
     */
    boolean post(Runnable runnable);


    boolean postDelayed(Runnable r, long delayMillis);

    /**
     * 同步执行
     * @param runnable
     * @return
     */
    boolean executeSync(Runnable runnable);

    boolean executeSync(Runnable runnable, long timeout);

    /**
     * 异步提交
     * @param callable
     * @return
     * @param <T>
     */
    <T> Future<T> submit(Callable<T> callable);

    GLEnvContext getEnvContext();

    /**
     * 同步提交
     * @param callable
     * @return
     * @param <T>
     */
    <T> T submitSync(Callable<T> callable);

    Future<Boolean> submit(Runnable runnable);

    void setErrorCallback(ErrorCallback errorCallback);

    interface ErrorCallback {
        void onEnvThreadError(Exception exception);
    }

    static GLEnvThreadManager create() {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder();
        return builder.build();
    }

    static GLEnvThreadManager create(@GLEnvVersion int version) {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvThreadManager create(GLEnvConfigChooser configChooser) {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder(configChooser);
        return builder.build();
    }

    static GLEnvThreadManager create(@GLEnvVersion int version, GLEnvConfigChooser configChooser) {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder(configChooser);
        builder.setClientVersion(version);
        return builder.build();
    }

    class Builder {

        final GLEnvContextManager.Builder builder;
        public Builder() {
            this(EGL14.EGL_NO_CONTEXT);
        }

        public Builder(EGLContext shareContext) {
            this(GLEnvConfigSimpleChooser.create(), shareContext);
        }

        public Builder(GLEnvConfigChooser configChooser) {
            this(configChooser, EGL14.EGL_NO_CONTEXT);
        }

        public Builder(GLEnvConfigChooser configChooser, EGLContext shareContext) {
            builder = new GLEnvContextManager.Builder(configChooser,shareContext);
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, EGLContext shareContext) {
            builder = new GLEnvContextManager.Builder(envDisplay,envConfig,shareContext);
        }


        public void setClientVersion(@GLEnvVersion int version) {
            builder.setClientVersion(version);
        }

        public void setContextAttrib(int key, int value) {
            builder.setContextAttrib(key,value);
        }

        public GLEnvThreadManager build() {
            return new EnvThreadManagerImpl(builder.build());
        }
    }


}
