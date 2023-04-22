package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;
import android.opengl.EGLSurface;

class EnvAttachManagerImpl implements GLEnvAttachManager {

    static ThreadLocal<EnvAttachManagerImpl> GLENVMANAGER_THREAD_LOCAL = new ThreadLocal<>();
    GLEnvContext envContext;
    GLEnvDisplay envDisplay;
    EGLSurface eglSurface;

    Thread attachThread;
    boolean release;

    public EnvAttachManagerImpl(GLEnvContext envContext) {
        this.envContext = envContext;
        envDisplay = envContext.getEnvDisplay();
        if (envDisplay.isSupportSurfacelessContext()) {
            eglSurface = EGL14.EGL_NO_SURFACE;
        } else {
            GLEnvSurface envSurface = new GLEnvPbufferSurface
                    .Builder(envContext, 1, 1)
                    .build();
            eglSurface = envSurface.getEGLSurface();
        }
    }

    @Override
    public synchronized void attachCurrentThread() {
        if (release) {
            return;
        }
        EnvAttachManagerImpl lastEnvManager = GLENVMANAGER_THREAD_LOCAL.get();
        if (lastEnvManager == this) {
            return;
        }
        if (lastEnvManager != null) {
            synchronized (lastEnvManager){
                lastEnvManager.attachThread = null;
            }
        }
        envContext.makeCurrent(eglSurface);
        GLENVMANAGER_THREAD_LOCAL.set(this);
        attachThread = Thread.currentThread();
    }

    @Override
    public synchronized void detachCurrentThread() {
        if (release) {
            return;
        }
        if (attachThread != null && attachThread != Thread.currentThread()) {
            throw new IllegalThreadStateException("detach must in " + attachThread.getName());
        }
        GLEnvAttachManager lastEnvManager = GLENVMANAGER_THREAD_LOCAL.get();
        if (lastEnvManager != this) {
            return;
        }
        envContext.releaseThread();
        GLENVMANAGER_THREAD_LOCAL.set(null);
        attachThread = null;
    }

    @Override
    public GLEnvContext getEnvContext() {
        return envContext;
    }

    @Override
    public synchronized Thread getAttachThread() {
        return attachThread;
    }

    @Override
    public synchronized boolean isCurrentThreadAttached() {
        return attachThread == Thread.currentThread();
    }

    @Override
    public synchronized void release() {
        if (release) return;
        release = true;
        GLEnvAttachManager lastEnvManager = GLENVMANAGER_THREAD_LOCAL.get();
        if (lastEnvManager == this) {
            GLENVMANAGER_THREAD_LOCAL.set(null);
            if (attachThread != null) {
                envContext.releaseThread();
                attachThread = null;
            }
        }
        envContext.release();
        envDisplay.release();
    }
}
