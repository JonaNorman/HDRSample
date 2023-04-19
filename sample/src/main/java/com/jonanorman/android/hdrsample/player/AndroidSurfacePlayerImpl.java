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
       if (playHandler == null)return;
        playHandler.post(new Runnable() {
            @Override
            public void run() {
                AndroidSurfaceDecoder surfaceDecoder = (AndroidSurfaceDecoder) androidDecoder;
                surfaceDecoder.setOutputSurface(surface);
            }
        });

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
        MediaFormatUtil.setInteger(inputFormat,MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }

    @Override
    protected void onVideoDecoderConfigure(MediaFormat inputFormat) {
        synchronized (this) {
            if (surface == null) {
                textureSurface = TextureSurface.create();
                surface = textureSurface;
            }
        }
        androidDecoder.configure(new AndroidSurfaceDecoder.Configuration(inputFormat, new VideoDecoderCallBack(), surface));
    }


    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }


    protected boolean onOutputBufferProcess(float timeSecond, ByteBuffer buffer) {
        return true;
    }

}
