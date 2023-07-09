package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.norman.android.hdrsample.opengl.GLEnvThreadManager;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.util.ExceptionUtil;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

class MediaCodecAsyncAdapter extends MediaCodec.Callback {

    private  Handler handler;
    private  MediaCodec mediaCodec;
    private  CallBack callBack;

    private  ResumeBuffer resumeBuffer;

    private boolean inputEndStream;

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
            boolean render = outputBuffer.hasRemaining();
            if (outSurfaceMode) {
                render = render && isValidSurface();
            }
            render = render && callBack.onOutputBufferAvailable(outputBuffer, info.presentationTimeUs);
            codec.releaseOutputBuffer(index, render);
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                callBack.onOutputBufferEndOfStream();
            } else {
                callBack.onOutputBufferRelease(info.presentationTimeUs, render);
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
    private boolean start;
    private boolean pause;
    private boolean release;
    private int flushNumber;

    private Surface outSurface;

    private HolderSurface holderSurface;

    private final boolean outSurfaceMode;


    public MediaCodecAsyncAdapter(MediaFormat mediaFormat,
                                  CallBack callback) {
        this.outSurfaceMode = false;
        init(mediaFormat,callback);
    }

    public MediaCodecAsyncAdapter(MediaFormat mediaFormat,
                                  Surface surface,
                                  CallBack callback) {
        this.outSurfaceMode = true;
        this.outSurface = surface;
        init(mediaFormat,callback);
    }

    void init(MediaFormat mediaFormat,
              CallBack callback){
        this.callBack = callback;
        this.mediaCodec = createMediaCodec(mediaFormat);
        this.mediaCodec.setCallback(this);
        this.mediaCodec.configure(mediaFormat, getOutSurface(), null, 0);
        Looper looper = Looper.myLooper();
        this.handler = new Handler(looper == null ? Looper.getMainLooper() : looper);
        this.resumeBuffer = new ResumeBuffer();
    }

    public synchronized void start() {
        if (isRelease() ||
                isStarted()) {
            return;
        }
        mediaCodec.start();
        start = true;
    }


    public synchronized void pause() {
        if (!isRunning()) {
            return;
        }
        pause = true;
    }

    public synchronized void resume() {
        if (!isPaused()) {
            return;
        }
        pause = false;
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
        if (flushNumber <= 0 && !isRelease()) {
            inputEndStream = false;
            mediaCodec.start();
        }
    }

    public synchronized void release() {
        if (isRelease()) {
            return;
        }
        release = true;
        mediaCodec.release();
        resumeBuffer.clean();
        handler.removeCallbacksAndMessages(null);
        if (holderSurface != null) {
            holderSurface.release();
            holderSurface = null;
        }
    }

    public synchronized void setOutputSurface(Surface surface) {
        if (isRelease() || outSurface == surface) {
            return;
        }
        if (!outSurfaceMode) {
            throw new IllegalArgumentException("already in buffer mode, can no longer set Surface");
        }
        outSurface = surface;
        mediaCodec.setOutputSurface(getOutSurface());
    }


    synchronized Surface getOutSurface() {
        if (outSurfaceMode){
            if (!isValidSurface()) {
                if (holderSurface == null) {
                    holderSurface = new HolderSurface();
                }
                return holderSurface.getSurface();
            }
        }
        return outSurface;
    }

    synchronized boolean isValidSurface() {
        return outSurface != null && outSurface.isValid();
    }

    private void resumeBuffer() {
        if (isReleaseOrFlushing() || isPaused()) {
            return;
        }
        resumeBuffer.resume();
    }


    public synchronized boolean isRelease() {
        return release;
    }

    public synchronized boolean isStarted() {
        return !release && start;
    }

    public synchronized boolean isRunning() {
        return isStarted() && !pause;
    }


    public synchronized boolean isPaused() {
        return isStarted() && pause;
    }

    private synchronized boolean isReleaseOrFlushing() {
        return isRelease() || flushNumber > 0;
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
        if (isReleaseOrFlushing()) {
            return;
        }
        asyncCallback.onOutputFormatChanged(codec, format);
    }

    private @NonNull MediaCodec createMediaCodec(MediaFormat mediaFormat) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        Integer frameRate = null;
        if (mediaFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
            frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            mediaFormat.setString(MediaFormat.KEY_FRAME_RATE, null);
        }
        String codecName = mediaCodecList.findDecoderForFormat(mediaFormat);
        if (frameRate != null) {
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        }
        try {
            if (codecName != null) {
                return MediaCodec.createByCodecName(codecName);
            } else {
                String mimeType = MediaFormatUtil.getString(mediaFormat, MediaFormat.KEY_MIME);
                return MediaCodec.createDecoderByType(mimeType);
            }
        } catch (IOException e) {
            throw ExceptionUtil.throwRuntime(e);
        }
    }


    interface CallBack {

        MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer);

        boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs);

        void onOutputBufferRelease(long presentationTimeUs, boolean render);

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


}
