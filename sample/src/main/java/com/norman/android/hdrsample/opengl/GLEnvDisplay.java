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
     * 是否支持不需要surface就可以执行OpenGL命令，  //不需要创建Surface下也可以makeSurface
     *             // 注意如果这个时候绑定FrameBuffer0会报错，只有makeCurrent其他Surface绑定FrameBuffer0才有用
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
