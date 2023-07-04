package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLSurface;

class EnvPbufferSurfaceImpl implements GLEnvPbufferSurface {

    private final EGLSurface eglSurface;

    private final GLEnvDisplay envDisplay;

    private final GLEnvConfig envConfig;

    private final GLEnvPbufferSurfaceAttrib surfaceAttrib;

    private boolean release;


    public EnvPbufferSurfaceImpl(GLEnvDisplay envDisplay, GLEnvConfig envConfig, GLEnvPbufferSurfaceAttrib surfaceAttrib) {
        this.envDisplay = envDisplay;
        this.envConfig = envConfig;
        this.surfaceAttrib = surfaceAttrib;
        eglSurface = EGL14.eglCreatePbufferSurface(
                envDisplay.getEGLDisplay(),
                envConfig.getEGLConfig(),
                surfaceAttrib.getAttribArray(), 0);
        if (eglSurface == null || eglSurface == EGL14.EGL_NO_SURFACE) {
            GLEnvException.checkAndThrow();
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
        return surfaceAttrib.getHeight();
    }

    @Override
    public int getWidth() {
        return surfaceAttrib.getWidth();
    }

    @Override
    public void release() {
        if (release) return;
        release = true;
        boolean destroySurface = EGL14.eglDestroySurface(envDisplay.getEGLDisplay(), eglSurface);
        if (!destroySurface) {
            GLEnvException.checkAndThrow();
        }
    }

    @Override
    public boolean isRelease() {
        return release;
    }


}
