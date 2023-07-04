package com.norman.android.hdrsample.opengl;


import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

class EnvWindowSurfaceImpl implements GLEnvWindowSurface {


    private final Surface surface;
    private final EGLSurface eglSurface;
    private final GLEnvDisplay envDisplay;
    private final GLEnvConfig envConfig;

    private final int[] surfaceSize = new int[2];
    private boolean release;

    public EnvWindowSurfaceImpl(GLEnvDisplay envDisplay, GLEnvConfig envConfig, Surface surface, GLEnvWindowSurfaceAttrs windowSurfaceAttrib) {
        this.envDisplay = envDisplay;
        this.envConfig = envConfig;
        this.surface = surface;
        eglSurface = EGL14.eglCreateWindowSurface(
                envDisplay.getEGLDisplay(),
                envConfig.getEGLConfig(),
                surface,
                windowSurfaceAttrib.getAttribArray(),
                0);
        if (eglSurface == null || eglSurface == EGL14.EGL_NO_SURFACE) {
            GLEnvException.checkError();
        }
    }


    @Override
    public final int getWidth() {
        if (!isRelease()) {
            boolean querySurface = EGL14.eglQuerySurface(envDisplay.getEGLDisplay(), eglSurface, EGL14.EGL_WIDTH, surfaceSize, 0);
            if (!querySurface) {
                GLEnvException.checkError();
            }
        }
        return surfaceSize[0];
    }

    @Override
    public final int getHeight() {
        if (!isRelease()) {
            boolean querySurface = EGL14.eglQuerySurface(envDisplay.getEGLDisplay(), eglSurface, EGL14.EGL_HEIGHT, surfaceSize, 1);
            if (!querySurface) {
                GLEnvException.checkError();
            }
        }
        return surfaceSize[1];
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
    public void setPresentationTime(long presentationNs) {
        if (isRelease()) return;
        boolean presentationTimeANDROID = EGLExt.eglPresentationTimeANDROID(envDisplay.getEGLDisplay(), eglSurface, presentationNs);
        if (!presentationTimeANDROID) {
            GLEnvException.checkError();
        }
    }


    @Override
    public void swapBuffers() {
        if (isRelease()) return;
        boolean swapBuffers = EGL14.eglSwapBuffers(envDisplay.getEGLDisplay(), eglSurface);
        if (!swapBuffers) {
            GLEnvException.checkError();
        }
    }

    @Override
    public Surface getSurface() {
        return surface;
    }
}
