package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGL10;

class EnvDisplayImpl implements GLEnvDisplay {

    private static final String EXTENSION_COLOR_SPACE_BT2020_PQ = "EGL_EXT_gl_colorspace_bt2020_pq";
    private static final String EGL_KHR_surfaceless_context = "EGL_KHR_surfaceless_context";


    private final Map<String,Boolean> extensionsContainMap = new HashMap<>();

    private final EGLDisplay eglDisplay;
    private final int displayId;
    private final GLEnvConfig[] envConfigs;
    private boolean release;

    private String eglExtensions;


    public EnvDisplayImpl() {
        this(EGL14.EGL_DEFAULT_DISPLAY);
    }

    public EnvDisplayImpl(int id) {
        displayId = id;
        eglDisplay = EGL14.eglGetDisplay(id);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY || eglDisplay == null) {
            GLEnvException.checkError();
        }
        int[] majorVersion = new int[1];
        int[] minorVersion = new int[1];
        if (!EGL14.eglInitialize(eglDisplay, majorVersion, 0, minorVersion, 0)) {
            GLEnvException.checkError();
        }
        int[] configNum = {1};
        while (true) {
            EGLConfig[] configs = new EGLConfig[configNum[0] * 2];
            if (!EGL14.eglGetConfigs(eglDisplay, configs, 0, configs.length, configNum, 0)) {
                GLEnvException.checkError();
            }
            if (configNum[0] < configs.length) {
                break;
            }
        }
        int maxConfigNum = configNum[0];
        EGLConfig[] eglConfigs = new EGLConfig[maxConfigNum];
        if (!EGL14.eglGetConfigs(eglDisplay, eglConfigs, 0, maxConfigNum, configNum, 0)) {
            GLEnvException.checkError();
        }
        envConfigs = new GLEnvConfig[maxConfigNum];
        for (int i = 0; i < maxConfigNum; i++) {
            envConfigs[i] = new EnvConfigImpl(eglDisplay, eglConfigs[i]);
        }
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
            GLEnvException.checkError();
        }
    }

    @Override
    public void releaseThread() {
        if (isRelease()) {
            return;
        }
        if (!EGL14.eglReleaseThread()) {
            GLEnvException.checkError();
        }
    }

    @Override
    public boolean isSupportBT2020PQ() {
        return containEGLExtension(EXTENSION_COLOR_SPACE_BT2020_PQ);
    }

    @Override
    public boolean isSupportSurfacelessContext() {
        return containEGLExtension(EGL_KHR_surfaceless_context);
    }

    @Override
    public String getEGLExtensions() {
        if (eglExtensions != null){
            return eglExtensions;
        }
        if (isRelease()){
            eglExtensions = "";
        }else {
            eglExtensions = EGL14.eglQueryString(eglDisplay, EGL10.EGL_EXTENSIONS);
            eglExtensions = TextUtils.isEmpty(eglExtensions) ? "" : eglExtensions;
        }
        return eglExtensions;
    }

    private boolean containEGLExtension(String key){
        Boolean value = extensionsContainMap.get(key);
        if (value != null){
            return value;
        }
        value =   getEGLExtensions().contains(key);
        extensionsContainMap.put(key,value);
        return  value;
    }

    @Override
    public boolean isRelease() {
        return release;
    }
}
