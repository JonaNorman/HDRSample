package com.norman.android.hdrsample.player.opengl;


import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public interface GLEnvContext {
    int OPENGL_ES_VERSION_1 = 1;
    int OPENGL_ES_VERSION_2 = 2;
    int OPENGL_ES_VERSION_3 = 3;

    @IntDef({OPENGL_ES_VERSION_1, OPENGL_ES_VERSION_2, OPENGL_ES_VERSION_3})
    @Retention(RetentionPolicy.SOURCE)
    @interface OpenGLESVersion {
    }


    EGLContext getEGLContext();

    GLEnvDisplay getEnvDisplay();

    GLEnvConfig getEnvConfig();

    void makeCurrent(GLEnvSurface envSurface);

    void makeCurrent(EGLSurface eglSurface);

    void makeNoCurrent();

    void release();

    boolean isRelease();

    static GLEnvContext create() {
        GLEnvContext.Builder builder = new GLEnvContext.Builder();
        return builder.build();
    }

    static GLEnvContext create(@OpenGLESVersion int version) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvContext create(GLEnvConfigChooser configChooser) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder(configChooser);
        return builder.build();
    }

    static GLEnvContext create(@OpenGLESVersion int version,GLEnvConfigChooser configChooser) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder(configChooser);
        builder.setClientVersion(version);
        return builder.build();
    }


    class Builder {

        GLEnvDisplay envDisplay;
        GLEnvConfig envConfig;
        EGLContext shareContext;

        @OpenGLESVersion
        int version = OPENGL_ES_VERSION_2;

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


        public void setClientVersion(@OpenGLESVersion int version) {
            this.version = version;
        }

        public GLEnvContext build() {
            GLEnvContextAttrs attrs = new EnvContextAttrsImpl();
            attrs.setClientVersion(version);
            GLEnvContext envContext = new EnvContextImpl(envDisplay, envConfig, attrs, shareContext);
            return envContext;
        }
    }

}
