package com.norman.android.hdrsample.opengl;

import android.opengl.EGLDisplay;

/**
 * EGLDisPlay的封装
 */
public interface GLEnvDisplay {

    static GLEnvDisplay createDisplay() {
        return new EnvDisplayImpl();
    }

    static GLEnvDisplay createDisplay(int displayId) {
        return new EnvDisplayImpl(displayId);
    }

    EGLDisplay getEGLDisplay();

    int getDisplayId();

    /**
     * 查找config
     * @param configChooser
     * @return
     */
    GLEnvConfig chooseConfig(GLEnvConfigChooser configChooser);

    /**
     * 是否存在configChooser要求的配置
     * @param configChooser
     * @return
     */

    boolean supportConfig(GLEnvConfigChooser configChooser);

    void release();

    /**
     * 清空当前线程绑定的OpenGL状态，包含了{@link GLEnvContext#makeNoCurrent()}的作用
     */
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

    /**
     * 是否支持BT2020 Linear
     * @return
     */
    boolean isSupportBT2020Linear();

    /**
     * 不需要surface就可以执行OpenGL命令
     * @return
     */

    boolean isSupportSurfacelessContext();

    /**
     * EGL扩展列表
     * @return
     */

    String getEGLExtensions();

    boolean isRelease();

}
