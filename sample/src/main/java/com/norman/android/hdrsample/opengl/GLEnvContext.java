package com.norman.android.hdrsample.opengl;


import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;


/**
 * EGLContext的封装
 */
public interface GLEnvContext {


    EGLContext getEGLContext();

    GLEnvDisplay getEnvDisplay();

    GLEnvConfig getEnvConfig();

    /**
     * 把Surface设置为当前环境的渲染目标
     * @param envSurface
     */

    void makeCurrent(GLEnvSurface envSurface);

    /**
     * 把Surface设置为当前环境的渲染目标
     * @param eglSurface
     */

    void makeCurrent(EGLSurface eglSurface);

    /**
     * 退出OpenGL当前执行环境
     */

    void makeNoCurrent();

    void release();

    boolean isRelease();

    static GLEnvContext create() {
        GLEnvContext.Builder builder = new GLEnvContext.Builder();
        return builder.build();
    }

    static GLEnvContext create(@GLEnvVersion int version) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder();
        builder.setClientVersion(version);
        return builder.build();
    }

    static GLEnvContext create(GLEnvConfigChooser configChooser) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder(configChooser);
        return builder.build();
    }

    static GLEnvContext create(GLEnvDisplay envDisplay,GLEnvConfig envConfig) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder(envDisplay,envConfig);
        return builder.build();
    }

    static GLEnvContext create(GLEnvDisplay envDisplay,GLEnvConfigChooser configChooser) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder(envDisplay,configChooser);
        return builder.build();
    }

    static GLEnvContext create(@GLEnvVersion int version, GLEnvConfigChooser configChooser) {
        GLEnvContext.Builder builder = new GLEnvContext.Builder(configChooser);
        builder.setClientVersion(version);
        return builder.build();
    }


    class Builder {

        GLEnvDisplay envDisplay;
        GLEnvConfig envConfig;
        EGLContext shareContext;

        final EnvContextImpl.AttrListImpl contextAttribArray = new EnvContextImpl.AttrListImpl();

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
            setClientVersion(GLEnvVersion.VERSION_3);
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfigChooser configChooser) {
            this(envDisplay,envDisplay.chooseConfig(configChooser));
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig) {
            this(envDisplay,envConfig,EGL14.EGL_NO_CONTEXT);
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, EGLContext eglContext) {
            this.envDisplay = envDisplay;
            this.envConfig = envConfig;
            this.shareContext = eglContext;
            setClientVersion(GLEnvVersion.VERSION_3);
        }


        /**
         * OpenGL版本号
         * @param version
         */
        public void setClientVersion(@GLEnvVersion int version) {
            contextAttribArray.setClientVersion(version);
        }

        /**
         * 设置Context属性
         * @param key
         * @param value
         */

        public void setContextAttrib(int key, int value) {
            contextAttribArray.setAttrib(key, value);
        }

        public GLEnvContext build() {
            return new EnvContextImpl(envDisplay, envConfig,contextAttribArray.clone(), shareContext);
        }
    }

    /**
     * EGLContext的属性列表
     */
    interface AttrList extends GLEnvAttrList {
        void setClientVersion(@GLEnvVersion int version);
    }
}
