package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.text.TextUtils;

import javax.microedition.khronos.egl.EGL10;

class EnvDisplayImpl implements GLEnvDisplay {

    private static final String EXTENSION_COLOR_SPACE_BT2020_PQ = "EGL_EXT_gl_colorspace_bt2020_pq";

    private static final String EXTENSION_YUV_TARGET = "GL_EXT_YUV_target";

    private static final String EGL_KHR_surfaceless_context = "EGL_KHR_surfaceless_context";



    private final EGLDisplay eglDisplay;
    private final int displayId;
    private final GLEnvConfig[] envConfigs;
    private  boolean supportSurfacelessContext;
    private boolean release;

    private String eglExtensions;

    private boolean supportBT2020PQ;

    private boolean supportYUVTarget;

    public EnvDisplayImpl() {
        this(EGL14.EGL_DEFAULT_DISPLAY);
    }

    public EnvDisplayImpl(int id) {
        displayId = id;
        eglDisplay = EGL14.eglGetDisplay(id);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY || eglDisplay == null) {
            GLEnvException.checkAndThrow();
        }
        int[] majorVersion = new int[1];
        int[] minorVersion = new int[1];
        if (!EGL14.eglInitialize(eglDisplay, majorVersion, 0, minorVersion, 0)) {
            GLEnvException.checkAndThrow();
        }
        int[] configNum = {1};
        while (true) {
            EGLConfig[] configs = new EGLConfig[configNum[0] * 2];
            if (!EGL14.eglGetConfigs(eglDisplay, configs, 0, configs.length, configNum, 0)) {
                GLEnvException.checkAndThrow();
            }
            if (configNum[0] < configs.length) {
                break;
            }
        }
        int maxConfigNum = configNum[0];
        EGLConfig[] eglConfigs = new EGLConfig[maxConfigNum];
        if (!EGL14.eglGetConfigs(eglDisplay, eglConfigs, 0, maxConfigNum, configNum, 0)) {
            GLEnvException.checkAndThrow();
        }
        envConfigs = new GLEnvConfig[maxConfigNum];
        for (int i = 0; i < maxConfigNum; i++) {
            envConfigs[i] = new EnvConfigImpl(eglDisplay, eglConfigs[i]);
        }
        eglExtensions = EGL14.eglQueryString(eglDisplay, EGL10.EGL_EXTENSIONS);
        eglExtensions = TextUtils.isEmpty(eglExtensions) ? "" : eglExtensions;
        supportBT2020PQ = eglExtensions.contains(EXTENSION_COLOR_SPACE_BT2020_PQ);
        supportYUVTarget =  eglExtensions.contains(EXTENSION_YUV_TARGET);
        supportSurfacelessContext = eglExtensions.contains(EGL_KHR_surfaceless_context);
    }

    @Override
    public EGLDisplay getEGLDisplay() {
        return eglDisplay;
    }

    @Override
    public int getDisplayId() {
        return displayId;
    }

    @Override
    public GLEnvConfig chooseConfig(GLEnvConfigChooser configChooser) {
        return configChooser.chooseConfig(envConfigs);
    }

    @Override
    public void release() {
        if (release) {
            return;
        }
        release = true;
        boolean terminate = EGL14.eglTerminate(eglDisplay);
        if (!terminate) {
            GLEnvException.checkAndThrow();
        }
    }

    @Override
    public boolean isSupportBT2020PQ() {
        return supportBT2020PQ;
    }

    @Override
    public boolean isSupportSurfacelessContext() {
        return supportSurfacelessContext;
    }

    @Override
    public boolean isSupportYUVTarget() {
        return supportYUVTarget;
    }

    @Override
    public String getEGLExtensions() {
        return eglExtensions;
    }

    @Override
    public boolean isRelease() {
        return release;
    }
}
