package com.norman.android.hdrsample.opengl;


import android.opengl.EGLSurface;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * EGLSurface的状态
 */
public interface GLEnvSurface {


    int EGL_COLOR_SPACE_SRGB = 1;
    int EGL_COLOR_SPACE_LINEAR = 2;
    int EGL_COLOR_SPACE_BT2020_PQ = 3;
    int EGL_COLOR_SPACE_BT2020_HLG = 4;
    int EGL_COLOR_SPACE_BT2020_LINEAR = 5;
    int EGL_COLOR_SPACE_DISPLAY_P3 = 6;
    int EGL_COLOR_SPACE_DISPLAY_P3_PASSTHROUGH = 7;

    /**
     * EGLSurface的颜色空间
     */

    @IntDef({EGL_COLOR_SPACE_SRGB, EGL_COLOR_SPACE_LINEAR, EGL_COLOR_SPACE_BT2020_PQ, EGL_COLOR_SPACE_BT2020_HLG,EGL_COLOR_SPACE_BT2020_LINEAR, EGL_COLOR_SPACE_DISPLAY_P3, EGL_COLOR_SPACE_DISPLAY_P3_PASSTHROUGH})
    @Retention(RetentionPolicy.SOURCE)
    @interface EGLColorSpace {
    }

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


        void setColorSpace(@EGLColorSpace int colorSpace);
    }
}
