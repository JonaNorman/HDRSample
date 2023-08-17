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

    private final GLEnvWindowSurfaceAttribArray windowSurfaceAttribArray;

    private final int[] surfaceSize = new int[2];
    private boolean release;

    public EnvWindowSurfaceImpl(GLEnvDisplay envDisplay, GLEnvConfig envConfig, Surface surface, GLEnvWindowSurfaceAttribArray windowSurfaceAttrib) {
        this.envDisplay = envDisplay;
        this.envConfig = envConfig;
        this.surface = surface;
        this.windowSurfaceAttribArray = windowSurfaceAttrib;
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
            checkValid();
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
            checkValid();
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
        EGLSurface currentSurface =  EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        boolean destroySurface = EGL14.eglDestroySurface(envDisplay.getEGLDisplay(), eglSurface);
        if (!destroySurface) {
            GLEnvException.checkError();
        }else {
            //部分机型如果eglDestroySurface后不releaseThread或makeNoCurrent在重新创建EGLSurface会报EGL_BAD_ALLOC
            if (eglSurface.equals(currentSurface)){
                envDisplay.releaseThread();
            }
        }
    }

    @Override
    public boolean isValid() {
        if (release) return false;
        return  surface.isValid();
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
        checkValid();
        boolean presentationTimeANDROID = EGLExt.eglPresentationTimeANDROID(envDisplay.getEGLDisplay(), eglSurface, presentationNs);
        if (!presentationTimeANDROID) {
            GLEnvException.checkError();
        }
    }


    @Override
    public void swapBuffers() {
        if (isRelease()) return;
        checkValid();
        boolean swapBuffers = EGL14.eglSwapBuffers(envDisplay.getEGLDisplay(), eglSurface);
        if (!swapBuffers) {
            GLEnvException.checkError();
        }
    }

    @Override
    public Surface getSurface() {
        return surface;
    }


    private void checkValid(){
        if (isRelease() ||isValid()){
            return;
        }
        throw new IllegalStateException("Surface is no longer available, please check Surface");
    }
}
