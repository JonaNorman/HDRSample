package com.jonanorman.android.hdrsample.player.decode;


import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

abstract class AndroidDecoderImpl implements AndroidDecoder {

    private static final int DECODE_UNINIT = 0;
    private static final int DECODE_CONFIGURE = 1;
    private static final int DECODE_START = 2;
    private static final int DECODE_PAUSE = 3;
    private static final int DECODE_RESUME = 4;
    private static final int DECODE_STOP = 5;
    private static final int DECODE_RELEASE = 6;

    private int state = DECODE_UNINIT;


    @Override
    public synchronized void configure(Decoder.Configuration configuration) {
        if (state != DECODE_UNINIT &&
                state != DECODE_STOP) {
            return;
        }
        state = DECODE_CONFIGURE;
        onConfigure(configuration);
    }


    @Override
    public synchronized void start() {
        if (state != DECODE_CONFIGURE) {
            return;
        }
        state = DECODE_START;
        onStart();
    }

    @Override
    public synchronized void pause() {
        if (!isRunning()) {
            return;
        }
        state = DECODE_PAUSE;
        onPause();
    }


    @Override
    public synchronized void resume() {
        if (!isPause()) {
            return;
        }
        state = DECODE_RESUME;
        onResume();
    }


    @Override
    public synchronized void flush() {
        if (!isStarted()) {
            return;
        }
        onFlush();
    }


    @Override
    public synchronized void stop() {
        if (!isConfigured()) {
            return;
        }
        state = DECODE_STOP;
        onStop();
    }


    @Override
    public synchronized void release() {
        if (state == DECODE_RELEASE) {
            return;
        }
        state = DECODE_RELEASE;
        onRelease();
    }


    @Override
    public synchronized boolean isRunning() {
        return state == DECODE_START || state == DECODE_RESUME;
    }

    @Override
    public synchronized boolean isRelease() {
        return state == DECODE_RELEASE;
    }

    @Override
    public synchronized boolean isPause() {
        return state == DECODE_PAUSE;
    }

    @Override
    public boolean isConfigured() {
        return state == DECODE_CONFIGURE
                || state == DECODE_START
                || state == DECODE_PAUSE
                || state == DECODE_RESUME;
    }

    @Override
    public boolean isStarted() {
        return state == DECODE_START
                || state == DECODE_PAUSE
                || state == DECODE_RESUME;
    }


    protected abstract void onConfigure(Decoder.Configuration configuration);

    protected abstract void onStart();

    protected abstract void onPause();

    protected abstract void onResume();

    protected abstract void onFlush();

    protected abstract void onStop();

    protected abstract void onRelease();

    static class CallBackWrapper implements MediaCodecAsyncAdapter.CallBack {
        private final CallBack callBack;

        public CallBackWrapper(CallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        public MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer) {
            return callBack.onInputBufferAvailable(byteBuffer);
        }

        @Override
        public boolean onOutputBufferAvailable(long presentationTimeUs, ByteBuffer outputBuffer) {
            return callBack.onOutputBufferAvailable(presentationTimeUs, outputBuffer);
        }

        @Override
        public void onOutputBufferRelease(long presentationTimeUs, boolean render) {
            callBack.onOutputBufferRelease(presentationTimeUs, render);
        }

        @Override
        public void onOutputFormatChanged(MediaFormat format) {
            callBack.onOutputFormatChanged(format);
        }

        @Override
        public void onError(Exception exception) {
            callBack.onError(exception);
        }

        @Override
        public void onOutputBufferEndOfStream() {
            callBack.onOutputBufferEndOfStream();
        }
    }
}
