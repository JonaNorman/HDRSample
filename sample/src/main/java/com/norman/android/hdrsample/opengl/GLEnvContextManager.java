package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLContext;

public interface GLEnvContextManager {
    void attach();

    void detach();

    Thread getAttachThread();

    boolean isCurrentThread();

    boolean isAttach();

    void release();

    boolean isRelease();

    GLEnvContext getEnvContext();

    static GLEnvContextManager create() {
        GLEnvContextManager.Builder builder = new GLEnvContextManager.Builder();
        return builder.build();
    }

    static GLEnvContextManager create(@GLEnvContext.OpenGLESVersion int version) {
        GLEnvContextManager.Builder builder = new GLEnvContextManager.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvContextManager create(GLEnvConfigChooser configChooser) {
        GLEnvContextManager.Builder builder = new GLEnvContextManager.Builder(configChooser);
        return builder.build();
    }

    static GLEnvContextManager create(@GLEnvContext.OpenGLESVersion int version, GLEnvConfigChooser configChooser) {
        GLEnvContextManager.Builder builder = new GLEnvContextManager.Builder(configChooser);
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

        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, EGLContext shareContext) {
            this.envDisplay = envDisplay;
            this.envConfig = envConfig;
            this.shareContext = shareContext;
        }


        public void setClientVersion(@GLEnvContext.OpenGLESVersion int version) {
            this.version = version;
        }

        public GLEnvContextManager build() {
            GLEnvContext.Builder builder = new GLEnvContext.Builder(envDisplay, envConfig, shareContext);
            builder.setClientVersion(version);
            return new EnvContextManagerImpl(builder.build());
        }
    }
}
