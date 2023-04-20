package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;
import android.opengl.EGLContext;

public interface GLEnvManager {
    void attachCurrentThread();

    void detachCurrentThread();

    Thread getAttachThread();

    boolean isCurrentThreadAttached();

    void release();

    GLEnvContext getEnvContext();

    static GLEnvManager create() {
        GLEnvManager.Builder builder = new GLEnvManager.Builder();
        return builder.build();
    }

    static GLEnvManager create(@GLEnvContext.OpenGLESVersion int version) {
        GLEnvManager.Builder builder = new GLEnvManager.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvManager create(GLEnvConfigChooser configChooser) {
        GLEnvManager.Builder builder = new GLEnvManager.Builder(configChooser);
        return builder.build();
    }

    static GLEnvManager create(@GLEnvContext.OpenGLESVersion int version, GLEnvConfigChooser configChooser) {
        GLEnvManager.Builder builder = new GLEnvManager.Builder(configChooser);
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

        public GLEnvManager build() {
            GLEnvContext.Builder builder = new GLEnvContext.Builder(envDisplay, envConfig, shareContext);
            builder.setClientVersion(version);
            return new EnvManagerImpl(builder.build());
        }
    }
}