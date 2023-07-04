package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLContext;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface GLEnvThreadManager {


    void release();

    boolean isRelease();


    boolean post(Runnable runnable);

    boolean postDelayed(Runnable r, long delayMillis);

    boolean postWait(Runnable runnable);

    boolean postWait(Runnable runnable, long timeout);

    <T> Future<T> submit(Callable<T> callable);

    GLEnvContext getEnvContext();

    <T> T submitWait(Callable<T> callable);

    Future<Boolean> submit(Runnable runnable);

    void setErrorCallback(ErrorCallback errorCallback);

    interface ErrorCallback {
        void onEnvThreadError(Exception exception);
    }

    static GLEnvThreadManager create() {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder();
        return builder.build();
    }

    static GLEnvThreadManager create(@GLEnvContext.OpenGLESVersion int version) {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvThreadManager create(GLEnvConfigChooser configChooser) {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder(configChooser);
        return builder.build();
    }

    static GLEnvThreadManager create(@GLEnvContext.OpenGLESVersion int version, GLEnvConfigChooser configChooser) {
        GLEnvThreadManager.Builder builder = new GLEnvThreadManager.Builder(configChooser);
        builder.setClientVersion(version);
        return builder.build();
    }

    class Builder {

        GLEnvDisplay envDisplay;
        GLEnvConfig envConfig;
        EGLContext shareContext;

        @GLEnvContext.OpenGLESVersion
        int version = GLEnvContext.OPENGL_ES_VERSION_3;

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
            this.envDisplay = GLEnvDisplay.createDisplay();
            this.envConfig = envDisplay.chooseConfig(configChooser);
            this.shareContext = shareContext;
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, EGLContext eglContext) {
            this.envDisplay = envDisplay;
            this.envConfig = envConfig;
            this.shareContext = eglContext;
        }


        public void setClientVersion(@GLEnvContext.OpenGLESVersion int version) {
            this.version = version;
        }

        public GLEnvThreadManager build() {
            return new EnvThreadManagerImpl(envDisplay,envConfig,shareContext,version);
        }
    }


}
