package com.jonanorman.android.hdrsample.player.decode;

import android.view.Surface;

class AndroidSurfaceDecoderImpl extends AndroidVideoDecoderImpl implements AndroidSurfaceDecoder {


    @Override
    protected void onConfigure(Decoder.Configuration configuration) {
        if (!(configuration instanceof AndroidSurfaceDecoder.Configuration)) {
            throw new IllegalArgumentException("must configure AndroidSurfaceDecoder.Configuration");
        }
        AndroidSurfaceDecoder.Configuration config = (AndroidSurfaceDecoder.Configuration) configuration;
        mediaCodecAdapter = new MediaCodecAsyncAdapter(
                config.mediaFormat,
                config.surface,
                new CallBackWrapper(config.callBack));
    }


    @Override
    public synchronized void setOutputSurface(Surface surface) {
        if (!isConfigured()) {
            return;
        }
        mediaCodecAdapter.setOutputSurface(surface);
    }
}
