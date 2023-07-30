package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;

public class SurfaceVideoOutput extends VideoOutput {
    private Surface surface;
    private VideoDecoder videoDecoder;


    public static SurfaceVideoOutput create(){
        return new SurfaceVideoOutput();
    }
    @Override
    protected synchronized void onDecoderPrepare(VideoDecoder decoder, MediaFormat inputFormat) {
        decoder.setOutputMode(VideoDecoder.SURFACE_MODE);
        decoder.setOutputSurface(surface);
        videoDecoder = decoder;
    }

    @Override
    protected synchronized void onDecoderStop() {
        videoDecoder = null;
    }


    @Override
    public synchronized void setOutputSurface(Surface surface) {
        this.surface = surface;
        if (videoDecoder != null) {
            videoDecoder.setOutputSurface(surface);
        }
    }
}
