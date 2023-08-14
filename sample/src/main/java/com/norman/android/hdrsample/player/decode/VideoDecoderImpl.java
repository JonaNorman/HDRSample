package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import com.norman.android.hdrsample.util.ColorFormatUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.Objects;

class VideoDecoderImpl extends DecoderImpl implements VideoDecoder {


    MediaCodecAsyncAdapter mediaCodecAdapter;

    Surface outputSurface;

    private @OutputMode int outputMode = VideoDecoder.BUFFER_MODE;

    private String codecMimeType;

    @Override
    protected void onCreate(String mimeType) {
        this.codecMimeType = mimeType;
        mediaCodecAdapter = new MediaCodecAsyncAdapter(mimeType);
    }

    @Override
    protected void onConfigure(Decoder.Configuration configuration) {
        String mimeType = MediaFormatUtil.getString(configuration.mediaFormat, MediaFormat.KEY_MIME);
        if (!Objects.equals(mimeType, this.codecMimeType)) {
            throw new IllegalArgumentException("mimeType: " + mimeType + " is not same as " + codecMimeType);
        }
        mediaCodecAdapter.setOutputSurface(outputSurface);
        MediaFormat inputFormat = configuration.mediaFormat;
        if (outputMode == BUFFER_MODE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    mediaCodecAdapter.isSupportColorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUVP010)) {
                inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUVP010);
            } else {
                inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            }
        }else {
            inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,  MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }
        mediaCodecAdapter.configure(configuration.mediaFormat, outputMode == SURFACE_MODE, new MediaCodecCallBackWrapper(mediaCodecAdapter,configuration.callBack ));
    }


    public synchronized void setOutputSurface(Surface surface) {
        outputSurface = surface;
        if (mediaCodecAdapter != null) {
            mediaCodecAdapter.setOutputSurface(surface);
        }
    }

    @Override
    public void setOutputMode(@OutputMode int outputMode) {
        this.outputMode = outputMode;
    }

    @Override
    public boolean isSupportYUV42010BitBufferMode() {
        if (isRelease()){
            return  false;
        }
        if (!isCreated()) {
            throw new RuntimeException("videoDecoder has not been created");
        }
        return mediaCodecAdapter.isSupportYUV42010Bit();
    }

    @Override
    public boolean isSupportColorFormat(int colorFormat) {
        if (isRelease()){
            return  false;
        }
        if (!isCreated()) {
            throw new RuntimeException("videoDecoder has not been created");
        }
        return mediaCodecAdapter.isSupportColorFormat(colorFormat);
    }
    
    @Override
    protected void onStart() {
        mediaCodecAdapter.start();
    }

    @Override
    protected void onReset() {
        mediaCodecAdapter.reset();
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
    protected void onDestroy() {
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
        private final MediaCodecAsyncAdapter mediaCodecAsyncAdapter;

        public MediaCodecCallBackWrapper(MediaCodecAsyncAdapter mediaCodecAsyncAdapter,CallBack callBack) {
            this.callBack = callBack;
            this.mediaCodecAsyncAdapter = mediaCodecAsyncAdapter;
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
        public void onOutputBufferRender(long presentationTimeUs) {
            callBack.onOutputBufferRender(presentationTimeUs);
        }

        @Override
        public void onOutputFormatChanged(MediaFormat format) {
            int colorFormat = MediaFormatUtil.getInteger(format,MediaFormat.KEY_COLOR_FORMAT);
            format.setInteger(KEY_YUV420_TYPE, ColorFormatUtil.getYUV420Type(mediaCodecAsyncAdapter.getCodecName(), colorFormat));
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
