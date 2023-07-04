package com.norman.android.hdrsample.player.decode;

import android.view.Surface;

class AndroidSurfaceDecoderImpl extends AndroidVideoDecoderImpl implements AndroidSurfaceDecoder {

    private Surface outputSurface;


    @Override
    protected void onConfigure(AndroidDecoder.Configuration configuration) {
        mediaCodecAdapter = new MediaCodecAsyncAdapter(
                configuration.mediaFormat,
                outputSurface,
                new CallBackWrapper(configuration.callBack));
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
