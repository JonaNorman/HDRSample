package com.norman.android.hdrsample.opengl;

import android.util.SparseArray;

abstract class EnvSurfaceAttrsImpl extends EnvAttrListImpl implements GLEnvSurface.AttrList {

    /**
     * 颜色空间扩展
     */
    private static final int EGL_GL_COLORSPACE_KHR = 0x309D;
    private static final int EGL_GL_COLORSPACE_SRGB_KHR = 0x3089;
    private static final int EGL_GL_COLORSPACE_LINEAR_KHR = 0x308A;
    private static final int EGL_GL_COLORSPACE_BT2020_PQ_EXT = 0x3340;
    private static final int EGL_GL_COLORSPACE_BT2020_HLG_EXT = 0x3540;

    private static final int EGL_GL_COLORSPACE_DISPLAY_P3_EXT = 0x3363;
    private static final int EGL_GL_COLORSPACE_DISPLAY_P3_PASSTHROUGH_EXT = 0x3490;

    private static final SparseArray<Integer> COLOR_SPACE_MAP = new SparseArray<>();

    static {
        COLOR_SPACE_MAP.put(GLEnvSurface.EGL_COLOR_SPACE_SRGB, EGL_GL_COLORSPACE_SRGB_KHR);
        COLOR_SPACE_MAP.put(GLEnvSurface.EGL_COLOR_SPACE_LINEAR, EGL_GL_COLORSPACE_LINEAR_KHR);
        COLOR_SPACE_MAP.put(GLEnvSurface.EGL_COLOR_SPACE_BT2020_PQ, EGL_GL_COLORSPACE_BT2020_PQ_EXT);
        COLOR_SPACE_MAP.put(GLEnvSurface.EGL_COLOR_SPACE_BT2020_HLG, EGL_GL_COLORSPACE_BT2020_HLG_EXT);
        COLOR_SPACE_MAP.put(GLEnvSurface.EGL_COLOR_SPACE_DISPLAY_P3, EGL_GL_COLORSPACE_DISPLAY_P3_EXT);
        COLOR_SPACE_MAP.put(GLEnvSurface.EGL_COLOR_SPACE_DISPLAY_P3_PASSTHROUGH, EGL_GL_COLORSPACE_DISPLAY_P3_PASSTHROUGH_EXT);
    }

    @Override
    public void setColorSpace(@GLEnvSurface.EGLColorSpace int colorSpace) {
        setAttrib(EGL_GL_COLORSPACE_KHR, COLOR_SPACE_MAP.get(colorSpace));
    }
}
