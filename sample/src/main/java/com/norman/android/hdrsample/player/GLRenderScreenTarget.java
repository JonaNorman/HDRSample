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
    void onCreate() {

    }

    @Override
    void onDestroy() {

    }

    @Override
    void onStart() {
        //渲染到屏幕的frameBuffer是0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, width, height);
    }


    @Override
    void onFinish() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    void onClearColor() {
        GLES30.glClearColor(0.0f, 0.f, 0.f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }
}
