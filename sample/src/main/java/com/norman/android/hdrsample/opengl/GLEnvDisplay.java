package com.norman.android.hdrsample.opengl;

import android.opengl.EGLDisplay;

public interface GLEnvDisplay {

    static GLEnvDisplay createDisplay() {
        return new EnvDisplayImpl();
    }

    static GLEnvDisplay createDisplay(int displayId) {
        return new EnvDisplayImpl(displayId);
    }

    EGLDisplay getEGLDisplay();

    int getDisplayId();

    GLEnvConfig chooseConfig(GLEnvConfigChooser configChooser);

    void release();

    void releaseThread();
    /**
     * 是否支持BT2020 PQ
     * @return
     */
    boolean isSupportBT2020PQ();

    /**
     * 是否支持BT2020 HLG
     * @return
     */
    boolean isSupportBT2020HLG();

    boolean isSupportSurfacelessContext();

    String getEGLExtensions();

    boolean isRelease();

}
