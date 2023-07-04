package com.norman.android.hdrsample.player.decode;

class AndroidVideoDecoderImpl extends AndroidDecoderImpl implements AndroidVideoDecoder {

    MediaCodecAsyncAdapter mediaCodecAdapter;

    @Override
    protected void onConfigure(AndroidDecoder.Configuration configuration) {
        mediaCodecAdapter = new MediaCodecAsyncAdapter(
                configuration.mediaFormat,
                null,
                new CallBackWrapper(configuration.callBack));
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
}
