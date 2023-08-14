package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.norman.android.hdrsample.exception.IORuntimeException;
import com.norman.android.hdrsample.opengl.GLEnvThreadManager;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.util.ColorFormatUtil;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 异步加在MediaCodec
 */
class MediaCodecAsyncAdapter extends MediaCodec.Callback {


    private final ResumeBuffer resumeBuffer = new ResumeBuffer();
    private final MediaCodec mediaCodec;
    private final String mimeType;

    private final MediaCodec.Callback asyncCallback = new MediaCodec.Callback() {


        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            if (inputEndStream) {
                return;
            }
            ByteBuffer byteBuffer = codec.getInputBuffer(index);
            if (byteBuffer == null) return;
            byteBuffer.clear();
            MediaCodec.BufferInfo bufferInfo = callBack.onInputBufferAvailable(byteBuffer);
            if (bufferInfo == null) {
                bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            }
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                bufferInfo.offset = 0;
                bufferInfo.size = 0;
                inputEndStream = true;
            }
            codec.queueInputBuffer(index, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            ByteBuffer outputBuffer = codec.getOutputBuffer(index);
            if (outputBuffer == null) return;
            outputBuffer.clear();
            outputBuffer.position(info.offset);
            outputBuffer.limit(info.offset + info.size);
            boolean render = outputBuffer.hasRemaining() && info.presentationTimeUs >= 0;
            if (outSurfaceMode && render) {
                synchronized (MediaCodecAsyncAdapter.this) {
                    render = outputSurface != null && outputSurface.isValid();
                }
            }
            render = render && callBack.onOutputBufferAvailable(outputBuffer, info.presentationTimeUs);
            codec.releaseOutputBuffer(index, render);
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                callBack.onOutputBufferEndOfStream();
            } else {
                if (render) {
                    callBack.onOutputBufferRender(info.presentationTimeUs);
                }
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            callBack.onMediaCodecError(e);
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            callBack.onOutputFormatChanged(format);
        }
    };

    private boolean configured;
    private boolean started;
    private boolean paused;
    private boolean released;
    private int flushNumber;

    private boolean outSurfaceMode;

    private Surface outputSurface;

    private HolderSurface holderSurface;

    private Handler handler;

    private CallBack callBack;


    private volatile boolean inputEndStream;


    public MediaCodecAsyncAdapter(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            throw new NullPointerException("mimeType is null");
        }
        this.mimeType = mimeType;
        try {
            mediaCodec = MediaCodec.createDecoderByType(mimeType);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        MediaCodecInfo mediaCodecInfo = mediaCodec.getCodecInfo();
        String infoBuilder = "mediacodec create" + "\n" +
                "mimeType->" + mimeType + "\n" +
                "name->" + mediaCodec.getName() + "\n" +
                "supportedTypes->" + Arrays.toString(mediaCodecInfo.getSupportedTypes());
        LogUtil.d(infoBuilder);
    }


    public synchronized String getCodecName(){
        if (isReleased()) {
            return null;
        }
        return mediaCodec.getName();
    }

    public synchronized boolean isSupportColorFormat(int colorFormat) {
        if (isReleased()) {
            return false;
        }
        MediaCodecInfo.CodecCapabilities codecCapabilities = mediaCodec.getCodecInfo().getCapabilitiesForType(mimeType);
        for (int format : codecCapabilities.colorFormats) {
            if (format == colorFormat) {
                return true;
            }
        }
        return false;
    }


    /**
     * 是否支持10位YUV420，10位YUV420实际是16位存储的
     * @return
     */
    public synchronized boolean isSupport10BitYUV420() {
        if (isReleased()) {
            return false;
        }
        MediaCodecInfo mediaCodecInfo = mediaCodec.getCodecInfo();
        MediaCodecInfo.CodecCapabilities codecCapabilities = mediaCodecInfo.getCapabilitiesForType(mimeType);
        for (int colorFormat : codecCapabilities.colorFormats) {
            if (ColorFormatUtil.isSupport10BitYUV420(mediaCodecInfo.getName(),colorFormat)){
                return true;
            }
        }
        return false;
    }

    public synchronized void configure(MediaFormat mediaFormat,
                                       boolean surfaceMode,
                                       CallBack callback) {
        if (isReleased() || isConfigured()) {
            return;
        }
        this.outSurfaceMode = surfaceMode;
        this.callBack = callback;
        this.mediaCodec.setCallback(this);
        if (!outSurfaceMode && outputSurface != null) {
            throw new IllegalArgumentException("bufferMode can not setOutputSurface");
        }
        Surface surface = outputSurface;
        if (outSurfaceMode && surface == null) {
            if (holderSurface == null) {
                holderSurface = new HolderSurface();
            }
            surface = holderSurface;
        }
        this.mediaCodec.configure(mediaFormat, surface, null, 0);
        Looper looper = Looper.myLooper();
        this.handler = new Handler(looper == null ? Looper.getMainLooper() : looper);
        this.configured = true;
        String infoBuilder = "mediacodec configure" + "\n" +
                "mediaFormat->" + mediaFormat.toString() + "\n" +
                "surfaceMode->" + (surfaceMode ? "true" : "false") + "\n";
        LogUtil.d(infoBuilder);
    }

    public synchronized void reset() {
        if (isReleased() || !isConfigured()) {
            return;
        }
        mediaCodec.setCallback(null);
        mediaCodec.reset();
        configured = false;
        paused = false;
        started = false;
        handler.removeCallbacksAndMessages(null);
        handler = null;
        flushNumber = 0;
        outSurfaceMode = false;
        inputEndStream = false;
        resumeBuffer.clear();
        if (holderSurface != null) {
            holderSurface.release();
            holderSurface = null;
        }
    }




    public synchronized void start() {
        if (isReleased() || isStarted()) {
            return;
        }
        if (!isConfigured()) {
            throw new RuntimeException("mediacodec has not been configured");
        }
        mediaCodec.start();
        started = true;
    }


    public synchronized void stop(){
        if (isReleased() ){
            return;
        }
        if (!isStarted()){
            if (isConfigured()){
                throw new RuntimeException("stop must after mediacodec start");
            }
            return;
        }
        mediaCodec.setCallback(null);
        mediaCodec.stop();
        configured = false;
        paused = false;
        started = false;
        handler.removeCallbacksAndMessages(null);
        handler = null;
        flushNumber = 0;
        outSurfaceMode = false;
        inputEndStream = false;
        resumeBuffer.clear();
        if (holderSurface != null) {
            holderSurface.release();
            holderSurface = null;
        }
    }


    public synchronized void pause() {
        if (!isRunning()) {
            return;
        }
        paused = true;
    }

    public synchronized void resume() {
        if (!isPaused()) {
            return;
        }
        paused = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                resumeBuffer();
            }
        });
    }

    public synchronized void flush() {
        if (!isStarted()) {
            return;
        }
        flushNumber++;
        resumeBuffer.clear();
        mediaCodec.flush();
        //mediaCodec异步模式中调用flush要等待looper的消息执行完毕才能调用其他方法如start方法
        handler.post(new Runnable() {
            @Override
            public void run() {
                finishFlush();
            }
        });
    }

    private synchronized void finishFlush() {
        flushNumber--;
        if (flushNumber <= 0 && isConfigured()) {
            inputEndStream = false;
            mediaCodec.start();
        }
    }

    public synchronized void release() {
        if (isReleased()) {
            return;
        }
        released = true;
        mediaCodec.setCallback(null);
        mediaCodec.release();
        resumeBuffer.clear();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (holderSurface != null) {
            holderSurface.release();
            holderSurface = null;
        }
    }

    public synchronized void setOutputSurface(Surface surface) {
        if (isReleased()) {
            return;
        }
        outputSurface = surface;
        if (!isConfigured()) {
           return;
        }
        if (!outSurfaceMode) {
            throw new IllegalArgumentException("already in buffer mode, can no longer set Surface");
        }
        if (surface == null) {
            if (holderSurface == null) {
                holderSurface = new HolderSurface();
            }
            surface = holderSurface;
        }
        mediaCodec.setOutputSurface(surface);
    }


    private void resumeBuffer() {
        if (isDirtyCallback() || isPaused()) {
            return;
        }
        resumeBuffer.resume();
    }


    public synchronized boolean isReleased() {
        return released;
    }

    public synchronized boolean isConfigured() {
        return !released && configured;
    }

    public synchronized boolean isStarted() {
        return !released && configured && started;
    }

    public synchronized boolean isRunning() {
        return isStarted() && !paused;
    }


    public synchronized boolean isPaused() {
        return isStarted() && paused;
    }

    private synchronized boolean isDirtyCallback() {
        return flushNumber > 0 || !isStarted();
    }


    @Override
    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
        if (isDirtyCallback()) {
            return;
        }
        if (isRunning()) {
            asyncCallback.onInputBufferAvailable(codec, index);
        } else {
            resumeBuffer.addInput(index);
        }
    }

    @Override
    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
        if (isDirtyCallback()) {
            return;
        }
        if (isRunning()) {
            asyncCallback.onOutputBufferAvailable(codec, index, info);
        } else {
            resumeBuffer.addOutput(index, info);
        }
    }

    @Override
    public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
        if (isDirtyCallback()) {
            return;
        }
        release();
        asyncCallback.onError(codec, e);
    }

    @Override
    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
        String infoBuilder = "mediacodec onOutputFormatChanged" + "\n" +
                "mediaFormat->" + format + "\n";
        LogUtil.d(infoBuilder);
        if (isDirtyCallback()) {
            return;
        }
        asyncCallback.onOutputFormatChanged(codec, format);
    }


    interface CallBack {

        MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer);

        boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs);

        void onOutputBufferRender(long presentationTimeUs);

        void onOutputBufferEndOfStream();

        void onOutputFormatChanged(MediaFormat format);

        void onMediaCodecError(Exception exception);
    }

    /**
     * 恢复暂停时候解码出来的Buffer
     */
    class ResumeBuffer {

        private final List<Integer> inputBufferList = new ArrayList<>();

        private final List<Pair<Integer, MediaCodec.BufferInfo>> outputBufferList = new ArrayList<>();

        public synchronized void clear() {
            inputBufferList.clear();
            outputBufferList.clear();
        }

        public synchronized void addInput(int id) {
            inputBufferList.add(id);
        }

        public synchronized void addOutput(int id, MediaCodec.BufferInfo bufferInfo) {
            outputBufferList.add(new Pair<>(id, bufferInfo));
        }

        public synchronized void resume() {
            for (Integer id : inputBufferList) {
                asyncCallback.onInputBufferAvailable(mediaCodec, id);
            }
            for (Pair<Integer, MediaCodec.BufferInfo> outputBufferInfo : outputBufferList) {
                asyncCallback.onOutputBufferAvailable(mediaCodec, outputBufferInfo.first, outputBufferInfo.second);
            }
            clear();
        }

    }

    /**
     * 用Surface模式解码如果刚开始不设置Surface会报错，建立一个占位的Surface解决这个问题
     */
    static final class HolderSurface extends GLTextureSurface {

        private static GLEnvThreadManager ENV_THREAD_MANAGER;
        private static int THREAD_HOLDER_COUNT;

        private static int obtainTextureId() {
            synchronized (HolderSurface.class) {
                if (ENV_THREAD_MANAGER == null || ENV_THREAD_MANAGER.isRelease()) {
                    ENV_THREAD_MANAGER = GLEnvThreadManager.create();
                }
                int textureId = ENV_THREAD_MANAGER.submitSync(GLESUtil::createExternalTextureId);
                THREAD_HOLDER_COUNT++;
                return textureId;
            }
        }

        public HolderSurface() {
            super(obtainTextureId());
        }


        @Override
        protected void onRelease() {
            synchronized (HolderSurface.class) {
                if (ENV_THREAD_MANAGER == null) return;
                THREAD_HOLDER_COUNT--;
                if (THREAD_HOLDER_COUNT == 0) {
                    ENV_THREAD_MANAGER.release();
                } else {
                    ENV_THREAD_MANAGER.post(new Runnable() {
                        @Override
                        public void run() {
                            GLESUtil.delTextureId(getTextureId());
                        }
                    });
                }
            }
        }
    }
}
