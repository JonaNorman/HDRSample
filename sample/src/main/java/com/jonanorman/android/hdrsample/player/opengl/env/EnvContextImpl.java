package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;


class EnvContextImpl implements GLEnvContext {
    EGLContext eglContext;
    GLEnvDisplay envDisplay;
    GLEnvConfig envConfig;
    boolean release;

    public EnvContextImpl(GLEnvDisplay display, GLEnvConfig config, GLEnvContextAttrib contextAttrib, EGLContext context) {
        envDisplay = display;
        envConfig = config;
        eglContext = EGL14.eglCreateContext(
                envDisplay.getEGLDisplay(),
                envConfig.getEGLConfig(), context,
                contextAttrib.getAttrib(), 0);
        if (eglContext == null || eglContext == EGL14.EGL_NO_CONTEXT) {
            GLEnvException.checkAndThrow();
        }
    }


    @Override
    public EGLContext getEGLContext() {
        return eglContext;
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
    public void makeCurrent(GLEnvSurface envSurface) {
        if (isRelease()) return;
        if (!EGL14.eglMakeCurrent(envDisplay.getEGLDisplay(), envSurface.getEGLSurface(), envSurface.getEGLSurface(), eglContext)) {
            GLEnvException.checkAndThrow();
        }
    }

    @Override
    public void makeCurrent(EGLSurface eglSurface) {
        if (isRelease()) return;
        if (!EGL14.eglMakeCurrent(envDisplay.getEGLDisplay(), eglSurface, eglSurface, eglContext)) {
            GLEnvException.checkAndThrow();
        }
    }


    @Override
    public void makeNoCurrent() {
        if (isRelease()) return;
        if (!EGL14.eglMakeCurrent(envDisplay.getEGLDisplay(), EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
            GLEnvException.checkAndThrow();
        }
    }

    @Override
    public void releaseThread() {
        if (isRelease()) {
            return;
        }
        if (!EGL14.eglReleaseThread()) {
            GLEnvException.checkAndThrow();
        }
    }


    @Override
    public final void release() {
        if (release) {
            return;
        }
        release = true;
        if (!EGL14.eglDestroyContext(envDisplay.getEGLDisplay(), eglContext)) {
            GLEnvException.checkAndThrow();
        }

    }

    @Override
    public boolean isRelease() {
        return release;
    }


}
