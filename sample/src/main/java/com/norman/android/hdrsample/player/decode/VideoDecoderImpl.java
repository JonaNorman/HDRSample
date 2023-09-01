package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import com.norman.android.hdrsample.player.color.ColorSpace;
import com.norman.android.hdrsample.player.color.ColorStandard;
import com.norman.android.hdrsample.player.color.ColorTransfer;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Android视频硬解的实现
 */
class VideoDecoderImpl extends DecoderImpl implements VideoDecoder {


    MediaCodecAsyncAdapter mediaCodecAdapter;

    /**
     * 解码到Surface上
     */
    Surface outputSurface;

    private @OutputMode int outputMode = OutputMode.BUFFER_MODE;

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
        if (outputMode == OutputMode.BUFFER_MODE){
            //buffer模式下如果支持Android13 YUVP010就设置COLOR_FormatYUVP010，不然就是设置YUV420格式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    mediaCodecAdapter.isSupportColorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUVP010)) {
                //YUVP010表示10位YUV420，这种格式10位用16位存储需要位移，并且是NV12
                inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUVP010);
            } else {
                // COLOR_FormatYUV420Flexible表示要求解码的是YUV420，无论是四种YUV420的哪一种都可以
                inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            }
        }else {
            // Surface模式下要设置COLOR_FormatSurface
            inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,  MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }
        mediaCodecAdapter.configure(configuration.mediaFormat, outputMode == OutputMode.SURFACE_MODE, new MediaCodecCallBackWrapper(mediaCodecAdapter,configuration.callBack ));
    }


    @Override
    public synchronized void setOutputSurface(Surface surface) {
        if (mediaCodecAdapter != null) {//已经启动后动态修改Surface
            outputSurface = surface;
            mediaCodecAdapter.setOutputSurface(surface);
        }else {//这个时候的Surface会在configure时设置进去
            outputSurface = surface;//
        }
    }

    @Override
    public void setOutputMode(@OutputMode int outputMode) {
        this.outputMode = outputMode;
    }

    /**
     * 解码到Buffer时是否支持10位YUV420，10位YUV420实际是16位存储的
     * @return
     */
    @Override
    public boolean isSupport10BitYUV420BufferMode() {
        if (isRelease()){
            return  false;
        }
        if (!isCreated()) {
            throw new RuntimeException("videoDecoder has not been created");
        }
        return mediaCodecAdapter.isSupport10BitYUV420();
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
        mediaCodecAdapter.stop();
    }

    @Override
    protected void onReset() {
        mediaCodecAdapter.reset();
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
        public void onOutputBufferComplete(long presentationTimeUs) {
            callBack.onOutputBufferRender(presentationTimeUs);
        }

        @Override
        public void onOutputFormatChanged(MediaFormat format) {
            // 根据解码器名称和colorFormat查找视频是哪种YUV420，把YUV420格式写入到format方便后续读取
            int colorFormat = MediaFormatUtil.getInteger(format,MediaFormat.KEY_COLOR_FORMAT);
            MediaFormatUtil.setYUV420Type(format,ColorFormatHelper.getYUV420Type(mediaCodecAsyncAdapter.getCodecName(), colorFormat));
            int colorStandard = MediaFormatUtil.getColorStandard(format);
            int colorTransfer = MediaFormatUtil.getColorTransfer(format);
            if (colorStandard == ColorStandard.BT2020 && colorTransfer == ColorTransfer.HLG) {
                MediaFormatUtil.setColorSpace(format,ColorSpace.VIDEO_BT2020_HLG);
            } else if (colorStandard == ColorStandard.BT2020 && colorTransfer == ColorTransfer.ST2084) {
                MediaFormatUtil.setColorSpace(format,ColorSpace.VIDEO_BT2020_PQ);
            }else if (colorStandard == ColorStandard.BT2020 && colorTransfer == ColorTransfer.LINEAR) {
                MediaFormatUtil.setColorSpace(format, ColorSpace.VIDEO_BT2020_LINEAR);
            } else {
                MediaFormatUtil.setColorSpace(format,ColorSpace.VIDEO_SDR);
            }
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
