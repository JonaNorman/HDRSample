package com.jonanorman.android.hdrsample.player;


import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.decode.AndroidSurfaceDecoder;
import com.jonanorman.android.hdrsample.player.surface.TextureSurface;
import com.jonanorman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;

class AndroidSurfacePlayerImpl extends AndroidVideoPlayerImpl implements VideoSurfacePlayer {
    public static final String SURFACE_PLAYER = "AndroidVideoPlayer";

    private Surface surface;

    private TextureSurface textureSurface;

    public AndroidSurfacePlayerImpl() {
        this(SURFACE_PLAYER);
    }

    public AndroidSurfacePlayerImpl(String threadName) {
        super(AndroidSurfaceDecoder.createSurfaceDecoder(), threadName);
    }

    public synchronized void setSurface(Surface surface) {
        this.surface = surface;
        if (playHandler == null) return;
        AndroidSurfaceDecoder surfaceDecoder = (AndroidSurfaceDecoder) androidDecoder;
        if (!surfaceDecoder.isConfigured()) {
            playHandler.waitAllMessage();
        }
        if (surface == null){
            surface = getTextureSurface();
        }
        surfaceDecoder.setOutputSurface(surface);
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        if (textureSurface != null) {
            textureSurface.release();
        }
    }

    @Override
    protected void onVideoInputFormatConfigure(MediaFormat inputFormat) {
        super.onVideoInputFormatConfigure(inputFormat);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }

    private synchronized TextureSurface getTextureSurface() {
        if (textureSurface != null) return textureSurface;
        textureSurface = TextureSurface.create();
        return textureSurface;
    }

    @Override
    protected void onVideoDecoderConfigure(MediaFormat inputFormat) {
        synchronized (this) {
            if (surface == null) {
                surface = getTextureSurface();
            }
        }
        androidDecoder.configure(new AndroidSurfaceDecoder.Configuration(inputFormat, new VideoDecoderCallBack(), surface));
    }


    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }


    protected boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer) {
        return true;
    }

    @Override
    protected boolean onOutputBufferProcess(float timeSecond, boolean render) {
        return render;
    }

}
