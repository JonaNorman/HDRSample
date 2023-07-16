//================================================================================================================================
//
// Copyright (c) 2015-2022 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.norman.android.hdrsample.player;


import android.opengl.GLES20;
import android.opengl.GLES30;


class GLRenderScreenTarget extends GLRenderTarget {

    @Override
    void onRenderSizeChange(int renderWidth, int renderHeight) {

    }

    @Override
    void onRenderStart() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, renderWidth, renderHeight);
    }


    @Override
    void onRenderFinish() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    void onRenderClearColor() {
        GLES30.glClearColor(0.0f, 0.f, 0.f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }
}
