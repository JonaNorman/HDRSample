package com.jonanorman.android.hdrsample.player;

import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.decode.AndroidSurfaceDecoder;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvAttachManager;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvContext;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvWindowSurface;
import com.jonanorman.android.hdrsample.util.DisplayUtil;
import com.jonanorman.android.hdrsample.util.GLESUtil;
import com.jonanorman.android.hdrsample.util.MediaFormatUtil;
import com.jonanorman.android.hdrsample.util.ScreenBrightnessObserver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

class AndroidTexturePlayerImpl extends AndroidVideoPlayerImpl implements AndroidTexturePlayer {

    private static final String TEXTURE_PLAYER = "AndroidTexturePlayer";
    int textureId;
    SurfaceTexture surfaceTexture;
    Surface surface;

    GLEnvAttachManager envAttachManager;
    GLEnvContext envContext;
    GLEnvWindowSurface envWindowSurface;
    AndroidTexturePlayerRenderer texturePlayerRenderer;
    ScreenBrightnessObserver screenBrightnessObserver = new ScreenBrightnessObserver();

    boolean keepBrightnessOnHDR;
    volatile Surface playerSurface;

    float screenLuminance;

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
        screenLuminance =  DisplayUtil.getMaxLuminance();
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
        screenBrightnessObserver.unregister();
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
        ByteBuffer hdrStaticInfo = MediaFormatUtil.getByteBuffer(outputFormat, MediaFormat.KEY_HDR_STATIC_INFO);
        if (hdrStaticInfo != null) {
            hdrStaticInfo.clear();
            hdrStaticInfo.position(1);
            hdrStaticInfo.limit(hdrStaticInfo.capacity());
            hdrStaticInfo.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer shortBuffer = hdrStaticInfo.asShortBuffer();
            texturePlayerRenderer.setContentLuminance(shortBuffer.get(11));//maxFrameAverageLuminance
        }

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
                float brightness = 1;
                if (keepBrightnessOnHDR) {
                    ScreenBrightnessObserver.BrightnessInfo brightnessInfo = screenBrightnessObserver.getBrightnessInfo();
                    brightness = brightnessInfo.brightnessFloat;
                }
                texturePlayerRenderer.setScreenLuminance(screenLuminance*brightness);
                texturePlayerRenderer.render();
                envWindowSurface.swapBuffers();
            }
        }
        return render;
    }


    @Override
    public synchronized void setKeepBrightnessOnHDR(boolean keepBrightnessOnHDR) {
        this.keepBrightnessOnHDR = keepBrightnessOnHDR;
        if (keepBrightnessOnHDR) {
            screenBrightnessObserver.register();
        } else {
            screenBrightnessObserver.unregister();
        }
    }
}
