package com.norman.android.hdrsample.opengl;

/**
 * EGL的config配置选择器
 */
public interface GLEnvConfigChooser {
    GLEnvConfig chooseConfig(GLEnvConfig[] configs);
}
