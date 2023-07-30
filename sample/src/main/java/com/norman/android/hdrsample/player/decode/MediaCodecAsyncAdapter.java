package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.norman.android.hdrsample.exception.IORuntimeException;
import com.norman.android.hdrsample.opengl.GLEnvThreadManager;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

class MediaCodecAsyncAdapter extends MediaCodec.Callback {


    private final MediaCodec mediaCodec;

    private final OutputSurface outSurface;

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
            outputBuffer.position(info.offset);
            outputBuffer.limit(info.offset + info.size);
            boolean render = outputBuffer.hasRemaining() && info.presentationTimeUs >= 0;
            if (outSurfaceMode) {
                render = render && outSurface.isValid();
            }
            render = render && callBack.onOutputBufferAvailable(outputBuffer, info.presentationTimeUs);
            codec.releaseOutputBuffer(index, render);
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                callBack.onOutputBufferEndOfStream();
            } else {
                if (!outSurfaceMode || render) {
                    callBack.onOutputBufferRelease(info.presentationTimeUs);
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

    private Handler handler;

    private CallBack callBack;

    private ResumeBuffer resumeBuffer;

    private boolean inputEndStream;


    public MediaCodecAsyncAdapter(String mimeType) {
        try {
            this.mimeType = mimeType;
            if (TextUtils.isEmpty(mimeType)){
                throw new NullPointerException("mimeType is null");
            }
            mediaCodec = MediaCodec.createDecoderByType(mimeType);
            outSurface = new OutputSurface();

            MediaCodecInfo mediaCodecInfo =  mediaCodec.getCodecInfo();
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("mediacodec").append("\n");
            infoBuilder.append("mimeType->").append(mimeType).append("\n");
            infoBuilder.append("name->").append(mediaCodec.getName()).append("\n");
            infoBuilder.append("supportedTypes->").append(Arrays.toString(mediaCodecInfo.getSupportedTypes()));
            LogUtil.d(infoBuilder.toString());

        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
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
        this.mediaCodec.configure(mediaFormat, outSurfaceMode ? outSurface.getOutSurface() : null, null, 0);
        Looper looper = Looper.myLooper();
        this.handler = new Handler(looper == null ? Looper.getMainLooper() : looper);
        this.resumeBuffer = new ResumeBuffer();
        this.configured = true;
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("mediacodec configure").append("\n");
        infoBuilder.append("mediaFormat->").append(mediaFormat.toString()).append("\n");
        infoBuilder.append("surfaceMode->").append(surfaceMode?"true":false).append("\n");
        LogUtil.d(infoBuilder.toString());
    }

    public synchronized boolean isSupportFormat(int colorFormat) {
        if (isReleased()) {
            return false;
        }
        if (!isConfigured()) {
            throw new RuntimeException("mediacodec has not been configured yet");
        }
        MediaCodecInfo.CodecCapabilities codecCapabilities = mediaCodec.getCodecInfo().getCapabilitiesForType(mimeType);
        for (int format : codecCapabilities.colorFormats) {
            if (format == colorFormat){
                return true;
            }
        }
        return false;
    }


    public synchronized void start() {
        if (isReleased() || isStarted()) {
            return;
        }
        if (!isConfigured()) {
            throw new RuntimeException("mediacodec has not been configured yet");
        }
        mediaCodec.start();
        started = true;
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
        resumeBuffer.clean();
        mediaCodec.flush();
        handler.post(new Runnable() {
            @Override
            public void run() {
                finishFlush();
            }
        });
    }

    private synchronized void finishFlush() {
        flushNumber--;
        if (flushNumber <= 0 && !isReleased()) {
            inputEndStream = false;
            mediaCodec.start();
        }
    }

    public synchronized void release() {
        if (isReleased()) {
            return;
        }
        released = true;
        mediaCodec.release();
        resumeBuffer.clean();
        handler.removeCallbacksAndMessages(null);
        outSurface.release();
    }

    public synchronized void setOutputSurface(Surface surface) {
        if (isReleased()) {
            return;
        }
        outSurface.setOutputSurface(surface);
        if (isConfigured()) {
            if (!outSurfaceMode) {
                throw new IllegalArgumentException("already in buffer mode, can no longer set Surface");
            }
            mediaCodec.setOutputSurface(outSurface.getOutSurface());
        }
    }


    private void resumeBuffer() {
        if (isReleaseOrFlushing() || isPaused()) {
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

    private synchronized boolean isReleaseOrFlushing() {
        return isReleased() || flushNumber > 0;
    }


    @Override
    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
        if (isReleaseOrFlushing()) {
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
        if (isReleaseOrFlushing()) {
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
        if (isReleaseOrFlushing()) {
            return;
        }
        release();
        asyncCallback.onError(codec, e);
    }

    @Override

    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("mediacodec onOutputFormatChanged").append("\n");
        infoBuilder.append("mediaFormat->").append(format.toString()).append("\n");
        LogUtil.d(infoBuilder.toString());
        if (isReleaseOrFlushing()) {
            return;
        }
        asyncCallback.onOutputFormatChanged(codec, format);
    }


    interface CallBack {

        MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer);

        boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs);

        void onOutputBufferRelease(long presentationTimeUs);

        void onOutputBufferEndOfStream();

        void onOutputFormatChanged(MediaFormat format);

        void onMediaCodecError(Exception exception);
    }

    class ResumeBuffer {

        private final List<Integer> inputBufferList = new ArrayList<>();

        private final List<Pair<Integer, MediaCodec.BufferInfo>> outputBufferList = new ArrayList<>();

        public synchronized void clean() {
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
            clean();
        }

    }

    static final class HolderSurface {

        private static GLEnvThreadManager ENV_THREAD_MANAGER;
        private static int THREAD_HOLDER_COUNT;

        private final GLTextureSurface textureSurface;
        private boolean release;


        public HolderSurface() {
            synchronized (SurfaceHolder.class) {
                if (ENV_THREAD_MANAGER == null || ENV_THREAD_MANAGER.isRelease()) {
                    ENV_THREAD_MANAGER = GLEnvThreadManager.create();
                }
                this.textureSurface = ENV_THREAD_MANAGER.submitSync(new Callable<GLTextureSurface>() {
                    @Override
                    public GLTextureSurface call() {
                        return new GLTextureSurface(GLESUtil.createExternalTextureId());
                    }
                });
                THREAD_HOLDER_COUNT++;
            }
        }

        public synchronized Surface getSurface() {
            return textureSurface;
        }


        public synchronized void release() {
            if (release) {
                return;
            }
            release = true;
            textureSurface.release();
            synchronized (SurfaceHolder.class) {
                if (ENV_THREAD_MANAGER == null) return;
                THREAD_HOLDER_COUNT--;
                if (THREAD_HOLDER_COUNT == 0) {
                    ENV_THREAD_MANAGER.release();
                } else {
                    ENV_THREAD_MANAGER.post(new Runnable() {
                        @Override
                        public void run() {
                            GLESUtil.delTextureId(textureSurface.getTextureId());
                        }
                    });
                }
            }
        }

    }

    static class OutputSurface {
        private Surface outSurface;

        private HolderSurface holderSurface;

        private boolean released;

        public synchronized boolean isReleased() {
            return released;
        }

        public synchronized void release() {
            if (isReleased()) {
                return;
            }
            released = true;
            if (holderSurface != null) {
                holderSurface.release();
                holderSurface = null;
            }
        }

        public synchronized void setOutputSurface(Surface surface) {
            outSurface = surface;
        }

        public synchronized boolean isValid() {
            return !isReleased() && outSurface != null && outSurface.isValid();
        }

        public synchronized Surface getOutSurface() {
            if (outSurface == null) {
                if (holderSurface == null) {
                    holderSurface = new HolderSurface();
                }
                return holderSurface.getSurface();
            }
            return outSurface;
        }
    }
}
