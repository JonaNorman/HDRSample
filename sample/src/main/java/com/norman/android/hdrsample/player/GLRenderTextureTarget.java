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

    @VideoOutput.ColorSpace
    int colorSpace = VideoOutput.COLOR_SPACE_SDR;

    int maxContentLuminance;

    void setBitDepth(int bitDepth) {
        if (this.bitDepth != bitDepth && renderWidth >= 0 && renderHeight >= 0) {
            this.bitDepth = bitDepth;
            createFrameBuffer(renderWidth, renderHeight, bitDepth);
        }
    }

    void setColorSpace(@VideoOutput.ColorSpace int colorSpace) {
        this.colorSpace = colorSpace;
    }


    public void setMaxContentLuminance(int maxContentLuminance) {
        this.maxContentLuminance = maxContentLuminance;
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
        GLESUtil.attachTexture(frameBufferId,textureId);
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
        // 清空frameBuffer中的数据
        GLES30.glClearColor(0.0f, 0.f, 0.f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }
}
