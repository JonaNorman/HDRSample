package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;
import android.opengl.EGLContext;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface GLEnvHandler {


    void recycle();

    boolean isRecycle();


    boolean post(Runnable runnable);

    boolean postDelayed(Runnable r, long delayMillis);

    boolean postAndWait(Runnable runnable);

    boolean postAndWait(Runnable runnable, long timeout);

    <T> Future<T> submit(Callable<T> callable);

    GLEnvContext getEnvContext();

    <T> T submitAndWait(Callable<T> callable);

    Future<Boolean> submit(Runnable runnable);

    static GLEnvHandler create() {
        GLEnvHandler.Builder builder = new GLEnvHandler.Builder();
        return builder.build();
    }

    static GLEnvHandler create(@GLEnvContext.OpenGLESVersion int version) {
        GLEnvHandler.Builder builder = new GLEnvHandler.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvHandler create(GLEnvConfigChooser configChooser) {
        GLEnvHandler.Builder builder = new GLEnvHandler.Builder(configChooser);
        return builder.build();
    }

    static GLEnvHandler create(@GLEnvContext.OpenGLESVersion int version, GLEnvConfigChooser configChooser) {
        GLEnvHandler.Builder builder = new GLEnvHandler.Builder(configChooser);
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
            this(GLEnvConfigSimpleChooser.createChooser(), shareContext);
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

        public GLEnvHandler build() {
            return new EnvHandlerImpl(envDisplay,envConfig,shareContext,version);

        }
    }


}
