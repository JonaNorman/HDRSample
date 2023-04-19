package com.jonanorman.android.hdrsample.player.decode;

class AndroidVideoDecoderImpl extends AndroidDecoderImpl implements AndroidVideoDecoder {

    MediaCodecAsyncAdapter mediaCodecAdapter;

    @Override
    protected void onConfigure(Decoder.Configuration configuration) {
        if (!(configuration instanceof AndroidVideoDecoder.Configuration)) {
            throw new IllegalArgumentException("must configure AndroidVideoDecoder.Configuration");
        }
        AndroidVideoDecoder.Configuration config = (AndroidVideoDecoder.Configuration) configuration;
        mediaCodecAdapter = new MediaCodecAsyncAdapter(
                config.mediaFormat,
                null,
                new CallBackWrapper(config.callBack));
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
