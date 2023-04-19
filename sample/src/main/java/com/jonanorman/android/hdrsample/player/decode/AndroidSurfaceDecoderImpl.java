package com.jonanorman.android.hdrsample.player.decode;

import android.view.Surface;

class AndroidSurfaceDecoderImpl extends AndroidDecoderImpl implements AndroidSurfaceDecoder {

    MediaCodecAsyncAdapter mediaCodecAdapter;

    @Override
    protected void onConfigure(Decoder.Configuration configuration) {
        if (!(configuration instanceof AndroidSurfaceDecoder.Configuration)) {
            throw new IllegalArgumentException("must configure AndroidSurfaceDecoder.Configuration");
        }
        AndroidSurfaceDecoder.Configuration config = (AndroidSurfaceDecoder.Configuration) configuration;
        mediaCodecAdapter = new MediaCodecAsyncAdapter(
                config.mediaFormat,
                config.surface,
                new AndroidDecoderCallBackWrapper(config.callBack));
    }

    @Override
    protected void onStart() {
        mediaCodecAdapter.start();
    }


    @Override
    protected void onPause() {
        mediaCodecAdapter.pause();
    }

    @Override
    protected void onResume() {
        mediaCodecAdapter.resume();
    }

    @Override
    protected void onFlush() {
        mediaCodecAdapter.flush();
    }

    @Override
    protected void onStop() {
        mediaCodecAdapter.release();
        mediaCodecAdapter = null;
    }

    @Override
    protected void onRelease() {
        if (mediaCodecAdapter != null) {
            mediaCodecAdapter.release();
            mediaCodecAdapter = null;
        }
    }
    @Override
    public synchronized void setOutputSurface(Surface surface) {
        if (!isConfigured()) {
            return;
        }
        mediaCodecAdapter.setOutputSurface(surface);
    }
}
