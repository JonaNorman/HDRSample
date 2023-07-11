package com.norman.android.hdrsample.player;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.opengl.GLEnvContext;
import com.norman.android.hdrsample.opengl.GLEnvContextManager;
import com.norman.android.hdrsample.opengl.GLEnvWindowSurface;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class GLVideoOutput extends VideoOutput {



    private int textureId;
    private GLTextureSurface textureSurface;

    private GLEnvContextManager envContextManager;
    private GLEnvContext envContext;

    private PlayerSurface playerSurface = new PlayerSurface();


    private GLExternalTextureRenderer externalTextureRenderer = new GLExternalTextureRenderer() ;
    private GLTextureRenderer screenRenderer = new GLTextureRenderer() ;


    private List<GLVideoTransform> transformList = new ArrayList<>();


    private GLRenderScreenTarget screenTarget = new GLRenderScreenTarget();


    private GLRenderTextureTarget frontTarget = new GLRenderTextureTarget();

    private GLRenderTextureTarget backTarget = new GLRenderTextureTarget();





    public static GLVideoOutput create(){
        return new GLVideoOutput();
    }

    @Override
    protected void onPrepare() {
        envContextManager = GLEnvContextManager.create();
        envContextManager.attach();
        envContext = envContextManager.getEnvContext();
        textureId = GLESUtil.createExternalTextureId();
        textureSurface = new GLTextureSurface(textureId);
        externalTextureRenderer.setTextureId(textureId);
    }

    @Override
    protected void onRelease() {
        playerSurface.clean();
        envContextManager.detach();
        textureSurface.release();
    }


    @Override
    public void setOutputSurface(Surface surface) {
        this.playerSurface.setOutputSurface(surface);
    }

    @Override
    protected void onDecoderPrepare(VideoDecoder decoder, MediaFormat inputFormat) {
        decoder.setOutputMode(VideoDecoder.SURFACE_MODE);
        decoder.setOutputSurface(textureSurface);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }

    @Override
    protected void onDecoderStop() {

    }

    public void addVideoTransform(GLVideoTransform videoTransform){
        transformList.add(videoTransform);
    }


    @Override
    protected void onVideoSizeChange(int width, int height) {
        super.onVideoSizeChange(width, height);

    }

    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        super.onOutputFormatChanged(outputFormat);
    }


    @Override
    protected void onOutputBufferRelease(long presentationTimeUs) {
        GLEnvWindowSurface windowSurface = playerSurface.getWindowSurface();
        if (windowSurface == null) {
            return;
        }
        textureSurface.updateTexImage();

        textureSurface.getTransformMatrix(externalTextureRenderer.getTextureMatrix().get());
        envContext.makeCurrent(windowSurface);
        screenTarget.setRenderSize(windowSurface.getWidth(),windowSurface.getHeight());
        if (transformList.isEmpty()){
            externalTextureRenderer.renderToTarget(screenTarget);
        }else {
            frontTarget.setRenderSize(getWidth(),getHeight());
            backTarget.setRenderSize(getWidth(),getHeight());
            externalTextureRenderer.renderToTarget(frontTarget);
            GLRenderTextureTarget inputTarget  = frontTarget;
            GLRenderTextureTarget outputTarget = backTarget;
            for (GLVideoTransform videoTransform : transformList) {
                videoTransform.renderToTarget(inputTarget,outputTarget);
                if (videoTransform.transformSuccess){
                    GLRenderTextureTarget temp = inputTarget;
                    inputTarget = outputTarget;
                    outputTarget = temp;
                }
            }
            screenRenderer.setTextureId(outputTarget.textureId);
            screenRenderer.renderToTarget(screenTarget);
        }
        windowSurface.swapBuffers();
    }

    class PlayerSurface {
        private GLEnvWindowSurface windowSurface;

        private Surface outputSurface;

        public synchronized void setOutputSurface(Surface surface) {
            outputSurface = surface;
            if (surface == null || !surface.isValid()) {
                if (windowSurface != null) {
                    windowSurface.release();
                    windowSurface = null;
                }
            }
        }


        public synchronized void clean() {
            if (windowSurface == null) {
                return;
            }
            windowSurface.release();
            windowSurface = null;
        }

        public synchronized GLEnvWindowSurface getWindowSurface() {
            if (outputSurface == null) {
                if (windowSurface != null) {
                    windowSurface.release();
                    windowSurface = null;
                }
                return null;
            }
            if (windowSurface == null) {
                windowSurface = GLEnvWindowSurface.create(envContext,outputSurface);
            } else if (outputSurface != windowSurface.getSurface()) {
                windowSurface.release();
                windowSurface = GLEnvWindowSurface.create(envContext,outputSurface);
            }
            if (!windowSurface.isValid()) {
                windowSurface.release();
                windowSurface = null;
            }
            return windowSurface;
        }
    }




}
