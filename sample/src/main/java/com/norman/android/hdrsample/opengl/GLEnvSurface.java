package com.norman.android.hdrsample.opengl;


import android.opengl.EGLSurface;

/**
 * EGLSurface的状态
 */
public interface GLEnvSurface {


    GLEnvDisplay getEnvDisplay();

    GLEnvConfig getEnvConfig();

    EGLSurface getEGLSurface();


    int getHeight();

    int getWidth();

    void release();

    boolean isRelease();

    /**
     * Surface的属性列表
     */
    interface AttrList extends GLEnvAttrList {


        void setColorSpace(@GLEnvColorSpace int colorSpace);
    }
}
