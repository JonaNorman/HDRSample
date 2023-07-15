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

        final GLEnvContext.Builder envContextBuilder;

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
            envContextBuilder = new GLEnvContext.Builder(configChooser, shareContext);
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, EGLContext shareContext) {
            envContextBuilder = new GLEnvContext.Builder(envDisplay, envConfig, shareContext);
        }


        public void setClientVersion(@GLEnvContext.OpenGLESVersion int version) {
            envContextBuilder.setClientVersion(version);
        }
        public void setContextAttrib(int key, int value) {
            envContextBuilder.setContextAttrib(key,value);
        }

        public GLEnvContextManager build() {
            return new EnvContextManagerImpl(envContextBuilder.build());
        }
    }
}
