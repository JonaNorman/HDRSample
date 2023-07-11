package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;


class EnvContextImpl implements GLEnvContext {
    EGLContext eglContext;
    GLEnvDisplay envDisplay;
    GLEnvConfig envConfig;
    boolean release;

    public EnvContextImpl(GLEnvDisplay display, GLEnvConfig config, GLEnvContextAttribArray contextAttrib, EGLContext context) {
        envDisplay = display;
        envConfig = config;
        eglContext = EGL14.eglCreateContext(
                envDisplay.getEGLDisplay(),
                envConfig.getEGLConfig(), context,
                contextAttrib.getAttribArray(), 0);
        if (eglContext == null || eglContext == EGL14.EGL_NO_CONTEXT) {
            GLEnvException.checkError();
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
            GLEnvException.checkError();
        }
    }

    @Override
    public void makeCurrent(EGLSurface eglSurface) {
        if (isRelease()) return;
        if (!EGL14.eglMakeCurrent(envDisplay.getEGLDisplay(), eglSurface, eglSurface, eglContext)) {
            GLEnvException.checkError();
        }
    }


    @Override
    public void makeNoCurrent() {
        if (isRelease()) return;
        if (!EGL14.eglMakeCurrent(envDisplay.getEGLDisplay(), EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
            GLEnvException.checkError();
        }
    }




    @Override
    public final void release() {
        if (release) {
            return;
        }
        release = true;
        if (!EGL14.eglDestroyContext(envDisplay.getEGLDisplay(), eglContext)) {
            GLEnvException.checkError();
        }

    }

    @Override
    public boolean isRelease() {
        return release;
    }


}
