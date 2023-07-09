package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.nio.ByteBuffer;

class VideoDecoderImpl extends DecoderImpl implements VideoDecoder {

    MediaCodecAsyncAdapter mediaCodecAdapter;

    Surface outputSurface;

    private @OutputMode int outputMode = VideoDecoder.BUFFER_MODE;



    @Override
    protected void onConfigure(Decoder.Configuration configuration) {
        if (outputMode == BUFFER_MODE && outputSurface != null){
            throw new IllegalArgumentException("in bufferMode can not setOutputSurface");
        }
        if (outputMode == BUFFER_MODE){
            mediaCodecAdapter = new MediaCodecAsyncAdapter(
                    configuration.mediaFormat,
                    new MediaCodecCallBackWrapper(configuration.callBack));
        }else {
            mediaCodecAdapter = new MediaCodecAsyncAdapter(
                    configuration.mediaFormat,
                    outputSurface,
                    new MediaCodecCallBackWrapper(configuration.callBack));
        }
    }



    public synchronized void setOutputSurface(Surface surface) {
        outputSurface = surface;
        if (mediaCodecAdapter != null){
            mediaCodecAdapter.setOutputSurface(surface);
        }
    }

    @Override
    public void setOutputMode(@OutputMode int outputMode) {
        this.outputMode = outputMode;
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

    static class MediaCodecCallBackWrapper implements MediaCodecAsyncAdapter.CallBack {
        private final CallBack callBack;

        public MediaCodecCallBackWrapper(CallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        public MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer) {
            return callBack.onInputBufferAvailable(byteBuffer);
        }

        @Override
        public boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
            return callBack.onOutputBufferAvailable(outputBuffer, presentationTimeUs);
        }

        @Override
        public void onOutputBufferRelease(long presentationTimeUs) {
            callBack.onOutputBufferRelease(presentationTimeUs);
        }

        @Override
        public void onOutputFormatChanged(MediaFormat format) {
            callBack.onOutputFormatChanged(format);
        }

        @Override
        public void onMediaCodecError(Exception exception) {
            callBack.onDecodeError(exception);
        }

        @Override
        public void onOutputBufferEndOfStream() {
            callBack.onOutputBufferEndOfStream();
        }
    }
}
