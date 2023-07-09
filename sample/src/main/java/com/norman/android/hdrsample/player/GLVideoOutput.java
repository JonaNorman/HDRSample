package com.norman.android.hdrsample.player;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.opengl.GLEnvContext;
import com.norman.android.hdrsample.opengl.GLEnvContextManager;
import com.norman.android.hdrsample.opengl.GLEnvWindowSurface;
import com.norman.android.hdrsample.opengl.GLMatrix;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

public class GLVideoOutput extends VideoOutput {


    private final SurfaceInfo surfaceInfo = new SurfaceInfo();

    private final TextureInfo textureInfo = new TextureInfo();

    private int textureId;
    private GLTextureSurface textureSurface;

    private GLEnvContextManager envContextManager;
    private GLEnvContext envContext;

    private PlayerSurface playerSurface = new PlayerSurface();

    private PlayRenderer playRenderer = new PlayRenderer();

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
    }

    @Override
    protected void onRelease() {
        playerSurface.clean();
        playRenderer.clean();
        envContextManager.detach();
        textureSurface.release();
    }


    public synchronized void setTextureRenderer(TextureRenderer renderer) {
        playRenderer.setOutputRenderer(renderer);
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
        textureInfo.width = MediaFormatUtil.getInteger(inputFormat, MediaFormat.KEY_WIDTH);
        textureInfo.height = MediaFormatUtil.getInteger(inputFormat, MediaFormat.KEY_HEIGHT);
        textureInfo.textureId = textureId;
    }

    @Override
    protected void onDecoderStop() {

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
        textureSurface.getTransformMatrix(textureInfo.textureMatrix.get());
        envContext.makeCurrent(windowSurface);
        TextureRenderer renderer = playRenderer.getCurrentRenderer();
        surfaceInfo.width = windowSurface.getWidth();
        surfaceInfo.height = windowSurface.getHeight();
        renderer.render(textureInfo,surfaceInfo);
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

    class  PlayRenderer{

        private TextureRenderer pendRenderer;


        private TextureRenderer  defaultRenderer = new GLVideoTextureRenderer();


        private TextureRenderer outputRenderer;


        public synchronized void setOutputRenderer(TextureRenderer textureRenderer){
            pendRenderer = textureRenderer;
        }

        public synchronized void clean(){
            if (outputRenderer != null){
                outputRenderer.clean();
            }
            if (defaultRenderer != null){
                defaultRenderer.clean();
            }
            if (pendRenderer != null){
                pendRenderer.clean();
            }
        }


        public synchronized TextureRenderer getCurrentRenderer(){
            if (pendRenderer != outputRenderer){
                if (outputRenderer != null){
                    outputRenderer.clean();
                }
                outputRenderer = pendRenderer;
            }
            if (outputRenderer == null){
                return defaultRenderer;
            }else {
                return outputRenderer;
            }
        }

    }

    public static abstract class TextureRenderer {
        boolean rendering;


        void clean(){
            if (!rendering){
                return;
            }
            onClean();
            this.rendering = false;
        }

        void render(TextureInfo textureInfo,SurfaceInfo surfaceInfo){
            if (!rendering){
                onCreate(textureInfo,surfaceInfo);
                rendering  = true;
            }
            onRender(textureInfo,surfaceInfo);
        }

        protected abstract void onCreate(TextureInfo textureInfo,SurfaceInfo surfaceInfo);


        protected abstract void onClean();


        protected  abstract void onRender(TextureInfo textureInfo,SurfaceInfo surfaceInfo);


    }

    public  static class TextureInfo {
        public int textureId;
        public int width;
        public int height;
        public GLMatrix textureMatrix = new GLMatrix();
    }

    public static class SurfaceInfo {
        public int width;

        public int height;
    }


}
