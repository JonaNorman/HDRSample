package com.norman.android.hdrsample.opengl;

/**
 * EGLContext的属性列表
 */
interface GLEnvContextAttribArray extends GLEnvAttribArray {
    void setClientVersion(@GLEnvContext.OpenGLESVersion int version);
}
