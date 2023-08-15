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

    int getAlphaSize();

    int getBlueSize();

    int getGreenSize();

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

    boolean isWindowSurface();

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

    boolean isRenderGL30();

    boolean isRenderGL20();

    boolean isRenderGL10();

    boolean isConformantGL30();

    boolean isConformantGL20();

    boolean isConformantGL10();

    boolean isRecordable();

    boolean isColorFixed();

    boolean isColorFloat();

    EGLConfig getEGLConfig();
}
