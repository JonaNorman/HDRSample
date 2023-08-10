package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLSurface;

class EnvContextManagerImpl implements GLEnvContextManager {

    static ThreadLocal<EnvContextManagerImpl> CONTEXT_MANAGER_THREAD_LOCAL = new ThreadLocal<>();
    final GLEnvContext envContext;
    final GLEnvDisplay envDisplay;
    final EGLSurface eglSurface;

    Thread attachThread;
    boolean release;

    public EnvContextManagerImpl(GLEnvContext envContext) {
        this.envContext = envContext;
        envDisplay = envContext.getEnvDisplay();
        if (envDisplay.isSupportSurfacelessContext()) {//不需要创建Surface的情况下也可以makeSurface
            eglSurface = EGL14.EGL_NO_SURFACE;
        } else {
            GLEnvSurface envSurface =GLEnvPbufferSurface.create(envContext,1,1);
            eglSurface = envSurface.getEGLSurface();
        }
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
    public synchronized boolean isCurrentThread() {
        return attachThread == Thread.currentThread();
    }

    @Override
    public synchronized boolean isAttach() {
        return attachThread != null;
    }

    @Override
    public synchronized boolean isRelease() {
        return release;
    }

    @Override
    public synchronized void attach() {
        if (isRelease()) {
            return;
        }
        EnvContextManagerImpl envContextManager = CONTEXT_MANAGER_THREAD_LOCAL.get();
        if (envContextManager == this) {
            return;
        }
        if (envContextManager != null) {
            synchronized (envContextManager) {
                envContextManager.attachThread = null;
            }
        }
        envContext.makeCurrent(eglSurface);
        CONTEXT_MANAGER_THREAD_LOCAL.set(this);
        attachThread = Thread.currentThread();
    }

    @Override
    public synchronized void detach() {
        if (isRelease() || attachThread == null) {
            return;
        }
        if (attachThread != Thread.currentThread()) {
            throw new IllegalThreadStateException("detach must in " + attachThread.getName());
        }
        envDisplay.releaseThread();
        CONTEXT_MANAGER_THREAD_LOCAL.set(null);
        attachThread = null;
    }

    @Override
    public synchronized void release() {
        if (release) return;
        detach();
        envContext.release();
        envDisplay.release();
        release = true;
    }


}
