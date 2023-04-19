package com.jonanorman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

class AndroidDecoderCallBackWrapper implements MediaCodecAsyncAdapter.CallBack {
    private final AndroidDecoder.CallBack callBack;

    public AndroidDecoderCallBackWrapper(AndroidDecoder.CallBack callBack) {
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
}
