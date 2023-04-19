package com.jonanorman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.jonanorman.android.hdrsample.util.MediaFormatUtil;
import com.jonanorman.android.hdrsample.util.ThrowableUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

class MediaCodecAsyncAdapter extends MediaCodec.Callback {

    private final Handler handler;
    private final MediaCodec mediaCodec;
    private final CallBack callBack;
    private final Queue<Integer> suspendInputBufferQueue = new LinkedList<>();
    private final Queue<Integer> suspendOutputBufferQueue = new LinkedList<>();
    private final Map<Integer, MediaCodec.BufferInfo> suspendOutputBufferInfoMap = new HashMap();

    private final MediaCodec.Callback asyncCallback = new MediaCodec.Callback() {


        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            ByteBuffer inputBuffer = codec.getInputBuffer(index);
            ByteBuffer byteBuffer = inputBuffer;
            byteBuffer.clear();
            MediaCodec.BufferInfo bufferInfo = callBack.onInputBufferAvailable(byteBuffer);
            if (bufferInfo == null) {
                bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            }
            codec.queueInputBuffer(index, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            ByteBuffer outputBuffer = codec.getOutputBuffer(index);
            outputBuffer.position(info.offset);
            outputBuffer.limit(info.offset + info.size);
            boolean render = outputBuffer.hasRemaining() && callBack.onOutputBufferAvailable(info.presentationTimeUs, outputBuffer);
            codec.releaseOutputBuffer(index, render);
            callBack.onOutputBufferRelease(info.presentationTimeUs, render);
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                flush();
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            callBack.onError(e);
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

    private List<Integer> inputResumeIdList = new ArrayList<>();
    private List<Integer> outputResumeIdList = new ArrayList<>();
    private List<MediaCodec.BufferInfo> outputResumeBufferInfoList = new ArrayList<>();


    public MediaCodecAsyncAdapter(MediaFormat mediaFormat,
                                  CallBack callback) {
        this(mediaFormat, null, callback);
    }

    public MediaCodecAsyncAdapter(MediaFormat mediaFormat,
                                  Surface surface,
                                  CallBack callback) {
        this.callBack = callback;
        this.mediaCodec = createMediaCodec(mediaFormat);
        this.mediaCodec.setCallback(this);
        this.mediaCodec.configure(mediaFormat, surface, null, 0);
        this.handler = new Handler(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper());
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
        cleanBuffer();
        flushNumber++;
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
            mediaCodec.start();
        }
    }

    public synchronized void release() {
        if (isRelease()) {
            return;
        }
        release = true;
        mediaCodec.release();
        cleanBuffer();
        handler.removeCallbacksAndMessages(null);
    }

    public synchronized void setOutputSurface(Surface surface) {
        if (isRelease()) {
            return;
        }
        mediaCodec.setOutputSurface(surface);
    }


    private void resumeBuffer() {
        synchronized (this) {
            if (isReleaseOrFlushing()) {
                return;
            }
            if (isPaused()) {
                return;
            }
            inputResumeIdList.clear();
            outputResumeIdList.clear();
            outputResumeBufferInfoList.clear();
            while (!suspendInputBufferQueue.isEmpty()) {
                Integer id = suspendInputBufferQueue.poll();
                inputResumeIdList.add(id);
            }
            while (!suspendOutputBufferQueue.isEmpty()) {
                Integer id = suspendOutputBufferQueue.poll();
                MediaCodec.BufferInfo bufferInfo = suspendOutputBufferInfoMap.remove(id);
                outputResumeIdList.add(id);
                outputResumeBufferInfoList.add(bufferInfo);
            }
        }
        for (Integer id : inputResumeIdList) {
            asyncCallback.onInputBufferAvailable(mediaCodec, id);
        }
        for (int i = 0; i < outputResumeIdList.size(); i++) {
            int id = outputResumeIdList.get(i);
            MediaCodec.BufferInfo bufferInfo = outputResumeBufferInfoList.get(i);
            asyncCallback.onOutputBufferAvailable(mediaCodec, id, bufferInfo);
        }
        inputResumeIdList.clear();
        outputResumeIdList.clear();
        outputResumeBufferInfoList.clear();
    }

    private synchronized void cleanBuffer() {
        suspendInputBufferQueue.clear();
        suspendOutputBufferQueue.clear();
        suspendOutputBufferInfoMap.clear();
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
            synchronized (this) {
                suspendInputBufferQueue.offer(index);
            }
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
            synchronized (this) {
                suspendOutputBufferQueue.offer(index);
                suspendOutputBufferInfoMap.put(index, info);
            }
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

    private MediaCodec createMediaCodec(MediaFormat mediaFormat) {
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
            ThrowableUtil.throwException(e);
        }
        return null;
    }


    interface CallBack {

        MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer);

        boolean onOutputBufferAvailable(long presentationTimeUs, ByteBuffer outputBuffer);

        void onOutputBufferRelease(long presentationTimeUs, boolean render);

        void onOutputFormatChanged(MediaFormat format);

        void onError(Exception exception);
    }


}
