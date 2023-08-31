package com.norman.android.hdrsample.opengl;

import android.opengl.EGLConfig;

/**
 * EGL的Config配置
 */
public interface GLEnvConfig {

    static GLEnvConfig create(GLEnvDisplay display, EGLConfig config) {
        return new EnvConfigImpl(display.getEGLDisplay(), config);
    }

    int getBufferSize();

    /**
     * alpha通道数
     * @return
     */
    int getAlphaSize();

    /**
     * 蓝色通道数
     * @return
     */
    int getBlueSize();

    /**
     * 绿色通道数
     * @return
     */
    int getGreenSize();

    /**
     * 红色通道数
     * @return
     */
    int getRedSize();

    int getDepthSize();

    int getStencilSize();

    boolean isSlow();

    int getConfigId();

    int getLevel();

    int getMaxPBufferHeight();

    int getMaxPBufferPixels();

    int getMaxPBufferWidth();

    boolean isNativeRenderable();

    int getNativeVisualId();

    int getNativeVisualType();

    int getSamples();

    int getSampleBuffers();

    /**
     * 是否可以创建WindowSurface
     * @return
     */
    boolean isWindowSurface();

    /**
     * 是否可以创建PBufferSurface
     * @return
     */

    boolean isPBufferSurface();

    boolean isTransparent();

    int getTransparentRedValue();

    int getTransparentGreenValue();

    int getTransparentBlueValue();

    boolean isBindTextureRgb();

    boolean isBindToTextureRgba();

    int getMinSwapInterval();

    int getMaxSwapInterval();

    int getLuminanceSize();

    int getAlphaMaskSize();

    boolean isRgbColor();

    boolean isLuminanceColor();

    /**
     * 是否支持OpenGL3.0 
     * @return
     */
    boolean isRenderGL30();
    /**
     * 是否支持OpenGL2.0
     * @return
     */
    boolean isRenderGL20();
    /**
     * 是否支持OpenGL1.0
     * @return
     */
    boolean isRenderGL10();

    boolean isConformantGL30();

    boolean isConformantGL20();

    boolean isConformantGL10();

    /**
     * 是否可以录制，在编码时候要是true才行
     * @return
     */

    boolean isRecordable();

    /**
     * 颜色组件是整数
     * @return
     */
    boolean isColorFixed();

    /**
     * 颜色组件是浮点数
     * @return
     */
    boolean isColorFloat();

    EGLConfig getEGLConfig();
}
