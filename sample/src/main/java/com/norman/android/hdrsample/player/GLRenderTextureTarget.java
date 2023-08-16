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

import com.norman.android.hdrsample.util.GLESUtil;


class GLRenderTextureTarget extends GLRenderTarget {

    int frameBufferId;
    int textureId;

    int bitDepth = 8;

    public void setBitDepth(int bitDepth) {
        if (this.bitDepth != bitDepth && renderWidth >= 0 && renderHeight >= 0) {
            this.bitDepth = bitDepth;
            createFrameBuffer(renderWidth, renderHeight, bitDepth);
        }
    }

    @Override
    void onRenderSizeChange(int renderWidth, int renderHeight) {
        createFrameBuffer(renderWidth, renderHeight, bitDepth);
    }

    void createFrameBuffer(int renderWidth, int renderHeight, int bitDepth) {
        GLESUtil.delTextureId(textureId);
        GLESUtil.deleteFrameBufferId(frameBufferId);
        frameBufferId = GLESUtil.createFrameBufferId();
        textureId = GLESUtil.createTextureId(renderWidth, renderHeight, bitDepth);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    void onRenderStart() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
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
