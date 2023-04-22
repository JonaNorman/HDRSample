package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;
import android.opengl.EGLContext;

public interface GLEnvAttachManager {
    void attachCurrentThread();

    void detachCurrentThread();

    Thread getAttachThread();

    boolean isCurrentThreadAttached();

    void release();

    GLEnvContext getEnvContext();

    static GLEnvAttachManager create() {
        GLEnvAttachManager.Builder builder = new GLEnvAttachManager.Builder();
        return builder.build();
    }

    static GLEnvAttachManager create(@GLEnvContext.OpenGLESVersion int version) {
        GLEnvAttachManager.Builder builder = new GLEnvAttachManager.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvAttachManager create(GLEnvConfigChooser configChooser) {
        GLEnvAttachManager.Builder builder = new GLEnvAttachManager.Builder(configChooser);
        return builder.build();
    }

    static GLEnvAttachManager create(@GLEnvContext.OpenGLESVersion int version, GLEnvConfigChooser configChooser) {
        GLEnvAttachManager.Builder builder = new GLEnvAttachManager.Builder(configChooser);
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

        public GLEnvAttachManager build() {
            GLEnvContext.Builder builder = new GLEnvContext.Builder(envDisplay, envConfig, shareContext);
            builder.setClientVersion(version);
            return new EnvAttachManagerImpl(builder.build());
        }
    }
}
