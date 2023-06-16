package com.norman.android.hdrsample.player.decode;

import android.view.Surface;

class AndroidSurfaceDecoderImpl extends AndroidVideoDecoderImpl implements AndroidSurfaceDecoder {

    private Surface outputSurface;


    @Override
    protected void onConfigure(Decoder.Configuration configuration) {
        if (!(configuration instanceof AndroidDecoder.Configuration)) {
            throw new IllegalArgumentException("must configure AndroidDecoder.Configuration");
        }
        AndroidDecoder.Configuration config = (AndroidDecoder.Configuration) configuration;
        mediaCodecAdapter = new MediaCodecAsyncAdapter(
                config.mediaFormat,
                outputSurface,
                new CallBackWrapper(config.callBack));
    }


    @Override
    public synchronized void setOutputSurface(Surface surface) {
        if(surface == null){
            throw new NullPointerException("surface must not null");
        }
        outputSurface = surface;
        if (isConfigured()){
            mediaCodecAdapter.setOutputSurface(surface);
        }
    }

    @Override
    public synchronized Surface getOutputSurface() {
        return outputSurface;
    }
}
