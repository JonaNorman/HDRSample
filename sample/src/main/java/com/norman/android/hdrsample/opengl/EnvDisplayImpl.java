package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.text.TextUtils;

import com.norman.android.hdrsample.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGL10;

class EnvDisplayImpl implements GLEnvDisplay {

    /**
     * BT2020PQ的扩展
     */
    private static final String EXTENSION_COLOR_SPACE_BT2020_PQ = "EGL_EXT_gl_colorspace_bt2020_pq";

    /**
     * BT2020HLG的扩展
     */
    private static final String EXTENSION_COLOR_SPACE_BT2020_HLG = "EGL_EXT_gl_colorspace_bt2020_hlg";


    /**
     * BT2020Linear的扩展
     */
    private static final String EXTENSION_COLOR_SPACE_BT2020_LINEAR = "EGL_EXT_gl_colorspace_bt2020_linear";

    /**
     * 不需要Surface也可以执行OpenGL命令
     */
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
        //初始化，majorVersion和minorVersion返回的就是1.4
        int[] majorVersion = new int[1];
        int[] minorVersion = new int[1];
        if (!EGL14.eglInitialize(eglDisplay, majorVersion, 0, minorVersion, 0)) {
            GLEnvException.checkError();
        }
        //获取Config最大数量
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
        //获取所有Config
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
        GLEnvConfig envConfig  = configChooser.chooseConfig(envConfigs);
        if (envConfig == null){
            LogUtil.w("chooseConfig is null");
        }else {
            LogUtil.d("chooseConfig is "+envConfig);
        }
        return envConfig;
    }

    @Override
    public boolean supportConfig(GLEnvConfigChooser configChooser) {
        GLEnvConfig envConfig =  configChooser.chooseConfig(envConfigs);
        return envConfig != null;
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

    /**
     * 是否支持BT2020 PQ
     * @return
     */
    @Override
    public boolean isSupportBT2020PQ() {
        return containEGLExtension(EXTENSION_COLOR_SPACE_BT2020_PQ);
    }

    /**
     * 是否支持BT2020 HLG
     * @return
     */
    @Override
    public boolean isSupportBT2020HLG() {
        return containEGLExtension(EXTENSION_COLOR_SPACE_BT2020_HLG);
    }

    /**
     * 是否支持BT2020 Linear
     * @return
     */
    @Override
    public boolean isSupportBT2020Linear() {
        return containEGLExtension(EXTENSION_COLOR_SPACE_BT2020_LINEAR);
    }

    /**
     * 是否不需要surface也可以makeCurrent
     * @return
     */
    @Override
    public boolean isSupportSurfacelessContext() {
        return containEGLExtension(EGL_KHR_surfaceless_context);
    }

    /**
     * 扩展信息
     * @return
     */

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
