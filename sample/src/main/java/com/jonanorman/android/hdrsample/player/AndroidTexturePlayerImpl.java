package com.jonanorman.android.hdrsample.player;

import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.decode.AndroidSurfaceDecoder;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvAttachManager;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvContext;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvWindowSurface;
import com.jonanorman.android.hdrsample.util.GLESUtil;
import com.jonanorman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

class AndroidTexturePlayerImpl extends AndroidVideoPlayerImpl implements AndroidTexturePlayer {

    private static final String TEXTURE_PLAYER = "AndroidTexturePlayer";
    int textureId;
    SurfaceTexture surfaceTexture;
    Surface surface;

    GLEnvAttachManager envAttachManager;
    GLEnvContext envContext;
    GLEnvWindowSurface envWindowSurface;
    AndroidTexturePlayerRenderer texturePlayerRenderer;
    volatile Surface playerSurface;

    public AndroidTexturePlayerImpl() {
        this(TEXTURE_PLAYER);
    }

    public AndroidTexturePlayerImpl(String threadName) {
        super(AndroidSurfaceDecoder.createSurfaceDecoder(), threadName);
    }

    @Override
    protected void onPrepare() {
        prepareGLEnvContext();
        super.onPrepare();
    }

    private void prepareGLEnvContext() {
        envAttachManager = GLEnvAttachManager.create();
        envAttachManager.attachCurrentThread();
        envContext = envAttachManager.getEnvContext();
        textureId = GLESUtil.createExternalTextureId();
        surfaceTexture = new SurfaceTexture(textureId);
        surface = new Surface(surfaceTexture);
        texturePlayerRenderer = new AndroidTexturePlayerRenderer();
        texturePlayerRenderer.setTextureId(textureId);
    }

    private void releaseGLEnvContext() {
        if (envAttachManager != null) {
            envAttachManager.detachCurrentThread();
            surfaceTexture.release();
            surface.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseGLEnvContext();
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        releaseGLEnvContext();
    }

    @Override
    public synchronized void setSurface(Surface surface) {
        this.playerSurface = surface;
        if (playHandler == null) return;
        playHandler.post(new Runnable() {
            @Override
            public void run() {
                if (surface == null) {
                    if (envWindowSurface != null) {
                        envWindowSurface.release();
                        envWindowSurface = null;
                    }
                    return;
                }
                if (envWindowSurface == null) {
                    GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, surface);
                    envWindowSurface = builder.build();
                } else if (surface != envWindowSurface.getSurface()) {
                    envWindowSurface.release();
                    GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, surface);
                    envWindowSurface = builder.build();
                }
            }
        });
    }


    @Override
    protected void onVideoInputFormatConfigure(MediaFormat inputFormat) {
        super.onVideoInputFormatConfigure(inputFormat);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }

    @Override
    protected void onVideoDecoderConfigure(MediaFormat inputFormat) {
        androidDecoder.configure(new AndroidSurfaceDecoder.Configuration(inputFormat, new VideoDecoderCallBack(), surface));
    }


    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }


    protected boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer) {
        return true;
    }

    @Override
    protected boolean onOutputBufferProcess(float timeSecond, boolean render) {
        if (render) {
            surfaceTexture.updateTexImage();
            if (playerSurface != null) {
                surfaceTexture.getTransformMatrix(texturePlayerRenderer.getTextureMatrix().get());
                texturePlayerRenderer.setSurfaceSize(envWindowSurface.getWidth(), envWindowSurface.getHeight());
                envContext.makeCurrent(envWindowSurface);
                texturePlayerRenderer.render();
                envWindowSurface.swapBuffers();
            }
        }
        return render;
    }


}
