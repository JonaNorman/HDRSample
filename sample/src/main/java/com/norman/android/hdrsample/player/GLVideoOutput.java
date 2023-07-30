package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.opengl.GLEnvContext;
import com.norman.android.hdrsample.opengl.GLEnvContextManager;
import com.norman.android.hdrsample.opengl.GLEnvWindowSurface;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.LogUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GLVideoOutput extends VideoOutput {


    private GLEnvContextManager envContextManager;
    private GLEnvContext envContext;

    private GLTextureSurface textureSurface;

    private final PlayerSurface playerSurface = new PlayerSurface();

    private final GLTextureRenderer externalTextureRenderer = new GLTextureRenderer(GLTextureRenderer.TYPE_TEXTURE_EXTERNAL_OES);

    private final GLTextureY2YRenderer y2YTextureRenderer = new GLTextureY2YRenderer();

    private final GLTextureRenderer outputTextureRenderer = new GLTextureRenderer(GLTextureRenderer.TYPE_TEXTURE_2D);

    private GLTextureRenderer textureSurfaceRenderer = externalTextureRenderer;

    private final List<GLVideoTransform> transformList = new ArrayList<>();


    private final GLRenderScreenTarget screenTarget = new GLRenderScreenTarget();


    private GLRenderTextureTarget frontTarget = new GLRenderTextureTarget();

    private GLRenderTextureTarget backTarget = new GLRenderTextureTarget();


    public static GLVideoOutput create() {
        return new GLVideoOutput();
    }

    @Override
    protected void onPrepare() {
        envContextManager = GLEnvContextManager.create();
        envContextManager.attach();
        envContext = envContextManager.getEnvContext();
        textureSurface = new GLTextureSurface(GLESUtil.createExternalTextureId());
        externalTextureRenderer.setTextureId(textureSurface.getTextureId());
        y2YTextureRenderer.setTextureId(textureSurface.getTextureId());

    }

    @Override
    protected void onRelease() {
        playerSurface.release();
        envContextManager.detach();
        textureSurface.release();
    }


    @Override
    public synchronized void setOutputSurface(Surface surface) {
        this.playerSurface.setOutputSurface(surface);
    }

    @Override
    protected void onDecoderPrepare(VideoDecoder decoder, MediaFormat inputFormat) {
//        decoder.setOutputMode(VideoDecoder.BUFFER_MODE);
        decoder.setOutputMode(VideoDecoder.SURFACE_MODE);
        decoder.setOutputSurface(textureSurface);

    }

    @Override
    protected void onDecoderStop() {

    }

    public synchronized void addVideoTransform(GLVideoTransform videoTransform) {
        transformList.add(videoTransform);
    }


    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        super.onOutputFormatChanged(outputFormat);
        int colorTransfer = getColorTransfer();
        int colorStandard = getColorStandard();

        boolean y2yEnable = false;

        if ((colorTransfer == MediaFormat.COLOR_TRANSFER_HLG || colorTransfer == MediaFormat.COLOR_TRANSFER_ST2084) &&
                colorStandard == MediaFormat.COLOR_STANDARD_BT2020) {
            if (GLTextureY2YRenderer.isContainY2YEXT()) {
                y2yEnable = true;
            }
        }
        if (y2yEnable) {
            textureSurfaceRenderer = y2YTextureRenderer;
            y2YTextureRenderer.setBitDepth(10);
            y2YTextureRenderer.setColorRange(getColorRange());
        } else {
            textureSurfaceRenderer = externalTextureRenderer;
        }
    }

    int count = 0;
    @Override
    protected void onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
        super.onOutputBufferAvailable(outputBuffer, presentationTimeUs);
        if (count<1){
            count++;
            LogUtil.d("buffer ratio"+outputBuffer.remaining()*1.0f/getWidth()/getHeight());
        }


    }

    @Override
    protected synchronized void onOutputBufferRelease(long presentationTimeUs) {
        GLEnvWindowSurface windowSurface = playerSurface.getWindowSurface();
        if (windowSurface == null) {
            return;
        }
        textureSurface.updateTexImage();
        textureSurface.getTransformMatrix(textureSurfaceRenderer.getTextureMatrixValue());
        envContext.makeCurrent(windowSurface);
        screenTarget.setRenderSize(windowSurface.getWidth(), windowSurface.getHeight());
        screenTarget.clearColor();
        if (transformList.isEmpty()) {
            textureSurfaceRenderer.renderToTarget(screenTarget);
        } else {
            frontTarget.setRenderSize(getWidth(), getHeight());
            backTarget.setRenderSize(getWidth(), getHeight());
            textureSurfaceRenderer.renderToTarget(frontTarget);
            for (GLVideoTransform videoTransform : transformList) {
                videoTransform.renderToTarget(frontTarget, backTarget);
                if (videoTransform.transformSuccess) {
                    GLRenderTextureTarget temp = frontTarget;
                    frontTarget = backTarget;
                    backTarget = temp;
                }
            }
            outputTextureRenderer.setTextureId(frontTarget.textureId);
            outputTextureRenderer.renderToTarget(screenTarget);
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


        public synchronized void release() {
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
                windowSurface = GLEnvWindowSurface.create(envContext, outputSurface);
            } else if (outputSurface != windowSurface.getSurface()) {
                windowSurface.release();
                windowSurface = GLEnvWindowSurface.create(envContext, outputSurface);
            }
            if (!windowSurface.isValid()) {
                windowSurface.release();
                windowSurface = null;
            }
            return windowSurface;
        }
    }
}
