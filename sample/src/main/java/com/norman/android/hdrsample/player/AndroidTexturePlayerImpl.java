package com.norman.android.hdrsample.player;

import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.AndroidDecoder;
import com.norman.android.hdrsample.player.decode.AndroidSurfaceDecoder;
import com.norman.android.hdrsample.opengl.GLEnvContextManager;
import com.norman.android.hdrsample.opengl.GLEnvContext;
import com.norman.android.hdrsample.opengl.GLEnvWindowSurface;
import com.norman.android.hdrsample.player.dumex.AndroidDemuxer;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;

class AndroidTexturePlayerImpl extends AndroidVideoPlayerImpl implements AndroidTexturePlayer {

    private static final String TEXTURE_PLAYER = "AndroidTexturePlayer";

    private final SurfaceInfo surfaceInfo = new SurfaceInfo();

    private final TextureInfo textureInfo = new TextureInfo();

    private int textureId;
    private SurfaceTexture renderSurfaceTexture;
    private Surface renderSurface;

    private GLEnvContextManager envContextManager;
    private GLEnvContext envContext;

    private PlayerSurface playerSurface = new PlayerSurface();

    private PlayRenderer playRenderer = new PlayRenderer();


    public AndroidTexturePlayerImpl() {
        this(TEXTURE_PLAYER);
    }

    public AndroidTexturePlayerImpl(String threadName) {
        super(AndroidSurfaceDecoder.create(), threadName);
    }

    @Override
    protected void onPlayPrepare() {
        prepareGLContext();
        super.onPlayPrepare();
    }

    private void prepareGLContext() {
        envContextManager = GLEnvContextManager.create();
        envContextManager.attach();
        envContext = envContextManager.getEnvContext();
        textureId = GLESUtil.createExternalTextureId();
        renderSurfaceTexture = new SurfaceTexture(textureId);
        renderSurface = new Surface(renderSurfaceTexture);
    }


    @Override
    protected void onPlayStop() {
        super.onPlayStop();
        releaseGLContext();
    }

    @Override
    protected void onPlayRelease() {
        super.onPlayRelease();
        releaseGLContext();
    }


    private void releaseGLContext() {
        playerSurface.clean();
        playRenderer.clean();
        envContextManager.detach();
        renderSurfaceTexture.release();
        renderSurface.release();
    }

    @Override
    public synchronized void setTextureRenderer(TextureRenderer renderer) {
        playRenderer.setOutputRenderer(renderer);
    }


    @Override
    public synchronized void setSurface(Surface surface) {
        this.playerSurface.setOutputSurface(surface);
    }

    @Override
    protected void onInputFormatPrepare(AndroidDemuxer demuxer, MediaFormat inputFormat) {
        super.onInputFormatPrepare(demuxer, inputFormat);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        textureInfo.width = MediaFormatUtil.getInteger(inputFormat, MediaFormat.KEY_WIDTH);
        textureInfo.height = MediaFormatUtil.getInteger(inputFormat, MediaFormat.KEY_HEIGHT);
        textureInfo.textureId = textureId;
    }

    @Override
    protected void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat) {
        AndroidSurfaceDecoder androidSurfaceDecoder = (AndroidSurfaceDecoder) decoder;
        androidSurfaceDecoder.setOutputSurface(renderSurface);
        super.onDecoderConfigure(decoder, inputFormat);
    }

    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }


    protected boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer) {
        return true;
    }

    @Override
    protected boolean onOutputBufferProcess(float timeSecond, boolean render) {
        GLEnvWindowSurface windowSurface = playerSurface.getWindowSurface();
        if (windowSurface == null || !render) {
            return false;
        }
        renderSurfaceTexture.updateTexImage();
        renderSurfaceTexture.getTransformMatrix(textureInfo.textureMatrix.get());
        envContext.makeCurrent(windowSurface);
        TextureRenderer renderer = playRenderer.getCurrentRenderer();
        surfaceInfo.width = windowSurface.getWidth();
        surfaceInfo.height = windowSurface.getHeight();
        renderer.render(textureInfo,surfaceInfo);
        windowSurface.swapBuffers();
        return true;
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


        private TextureRenderer  defaultRenderer = new AndroidVideoTextureRenderer();


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


}
