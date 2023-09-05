package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;

class DirectVideoOutputImpl extends DirectVideoOutput {

    private Surface decoderSurface;


    @Override
    protected void onOutputPrepare(MediaFormat inputFormat) {
        videoDecoder.setOutputMode(VideoDecoder.OutputMode.SURFACE_MODE);
        videoDecoder.setOutputSurface(decoderSurface);
    }

    @Override
    protected void onOutputSurfaceChange(Surface surface) {
        this.decoderSurface = surface;
        if (videoDecoder != null) {
            videoDecoder.setOutputSurface(surface);
        }
    }
}
