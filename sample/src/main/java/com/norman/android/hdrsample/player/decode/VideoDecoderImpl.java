package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import com.norman.android.hdrsample.util.MediaFormatUtil;

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
        String mimeType = MediaFormatUtil.getString(configuration.mediaFormat,MediaFormat.KEY_MIME);


        mediaCodecAdapter  = new MediaCodecAsyncAdapter(mimeType);
        mediaCodecAdapter.setOutputSurface(outputSurface);
        MediaFormat inputFormat = configuration.mediaFormat;
        if (!inputFormat.containsKey(MediaFormat.KEY_COLOR_FORMAT)){
            if (outputMode == BUFFER_MODE){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mediaCodecAdapter.isSupportFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUVP010)) {
                    MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUVP010);
                }else {
                    MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT,  MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                }
            }else {
                MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT,  MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            }
        }
        mediaCodecAdapter.configure(configuration.mediaFormat,outputMode == SURFACE_MODE, new MediaCodecCallBackWrapper(configuration.callBack));
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
