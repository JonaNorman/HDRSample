package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLSurface;

import androidx.annotation.NonNull;

class EnvPbufferSurfaceImpl implements GLEnvPbufferSurface {

    private final EGLSurface eglSurface;

    private final GLEnvDisplay envDisplay;

    private final GLEnvConfig envConfig;

    private final AttrList attrList;

    private boolean release;

    private int width;
    private int height;


    public EnvPbufferSurfaceImpl(GLEnvDisplay envDisplay, GLEnvConfig envConfig, AttrList surfaceAttrib) {
        this.envDisplay = envDisplay;
        this.envConfig = envConfig;
        this.attrList = surfaceAttrib;
        width = surfaceAttrib.getWidth();
        height = surfaceAttrib.getHeight();
        eglSurface = EGL14.eglCreatePbufferSurface(
                envDisplay.getEGLDisplay(),
                envConfig.getEGLConfig(),
                surfaceAttrib.getAttribArray(), 0);
        if (eglSurface == null || eglSurface == EGL14.EGL_NO_SURFACE) {
            GLEnvException.checkError();
        }

    }


    @Override
    public GLEnvDisplay getEnvDisplay() {
        return envDisplay;
    }

    @Override
    public GLEnvConfig getEnvConfig() {
        return envConfig;
    }


    @Override
    public EGLSurface getEGLSurface() {
        return eglSurface;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void release() {
        if (release) return;
        release = true;
        boolean destroySurface = EGL14.eglDestroySurface(envDisplay.getEGLDisplay(), eglSurface);
        if (!destroySurface) {
            GLEnvException.checkError();
        }
    }

    @Override
    public boolean isRelease() {
        return release;
    }


    static class AttrListImpl extends EnvSurfaceAttrsImpl implements AttrList {


        public void setWidth(int width) {
            setAttrib(EGL14.EGL_WIDTH, width);
        }

        public void setHeight(int height) {
            setAttrib(EGL14.EGL_HEIGHT, height);
        }

        @Override
        public int getWidth() {
            return getAttrib(EGL14.EGL_WIDTH);
        }

        @Override
        public int getHeight() {
            return getAttrib(EGL14.EGL_HEIGHT);
        }

        @NonNull
        @Override
        public AttrListImpl clone() {
            return (AttrListImpl) super.clone();
        }
    }
}
