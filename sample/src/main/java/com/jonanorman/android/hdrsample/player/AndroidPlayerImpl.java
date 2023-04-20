package com.jonanorman.android.hdrsample.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;

import com.jonanorman.android.hdrsample.player.decode.AndroidDecoder;
import com.jonanorman.android.hdrsample.player.dumex.AndroidDemuxer;
import com.jonanorman.android.hdrsample.player.source.FileSource;
import com.jonanorman.android.hdrsample.util.MediaFormatUtil;
import com.jonanorman.android.hdrsample.util.MessageHandler;
import com.jonanorman.android.hdrsample.util.ThrowableUtil;
import com.jonanorman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;

abstract class AndroidPlayerImpl extends PlayerImpl implements AndroidPlayer {
    private static final String KEY_CSD_0 = "csd-0";
    private static final String KEY_CSD_1 = "csd-1";
    private static final int PLAY_UNINIT = 0;
    private static final int PLAY_PREPARE = 1;
    private static final int PLAY_START = 2;
    private static final int PLAY_PAUSE = 3;
    private static final int PLAY_RESUME = 4;
    private static final int PLAY_STOP = 5;
    private static final int PLAY_RELEASE = 6;

    private final ConcurrentLinkedDeque<Runnable> postFrameQueue = new ConcurrentLinkedDeque();
    private final PlayerImpl.CallBackHandler callBackHandler = new CallBackHandler();

    private final TimeSyncer timeSyncer = new TimeSyncer();
    private final String threadName;
    final AndroidDemuxer androidDemuxer;
    final AndroidDecoder androidDecoder;
    private int state = PLAY_UNINIT;
    private Future stopFuture;

    private FileSource fileSource;
    private Float seekSecond;

    MessageHandler playHandler;


    public AndroidPlayerImpl(AndroidDecoder decoder, AndroidDemuxer androidDemuxer, String threadName) {
        this.androidDemuxer = androidDemuxer;
        this.androidDecoder = decoder;
        this.threadName = threadName;

    }

    @Override
    public synchronized void setSource(FileSource fileSource) {
        this.fileSource = fileSource;
    }

    @Override
    public synchronized void prepare() {
        if (state != PLAY_UNINIT
                && state != PLAY_STOP) {
            return;
        }
        state = PLAY_PREPARE;
        preparePlayHandler();
        playHandler.post(this::onPrepare);
    }


    @Override
    public synchronized void start() {
        if (state != PLAY_PREPARE) {
            return;
        }
        state = PLAY_START;
        playHandler.post(this::onStart);
    }


    @Override
    public synchronized void seek(float timeSecond) {
        if (playHandler == null) {
            seekSecond = timeSecond;
            return;
        }
        playHandler.post(() -> onSeek(timeSecond));
    }


    @Override
    public synchronized void resume() {
        if (!isPause()) {
            return;
        }
        state = PLAY_RESUME;
        playHandler.post(this::onResume);
    }


    @Override
    public synchronized void pause() {
        if (!isPlaying()) {
            return;
        }
        state = PLAY_PAUSE;
        playHandler.post(this::onPause);
    }


    @Override
    public synchronized void stop() {
        if (!isPrepared()) {
            return;
        }
        state = PLAY_STOP;
        stopFuture = playHandler.submit(this::onStop);
        playHandler = null;
    }


    @Override
    public synchronized void release() {
        if (state == PLAY_RELEASE) {
            return;
        }
        state = PLAY_RELEASE;
        postFrameQueue.clear();
        if (playHandler != null) {
            playHandler.recycle();
            playHandler = null;
        }
    }


    @Override
    public synchronized boolean isPlaying() {
        if (state == PLAY_START ||
                state == PLAY_RESUME) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean isPrepared() {
        if (state == PLAY_UNINIT ||
                state == PLAY_STOP ||
                state == PLAY_RELEASE) {
            return false;
        }
        return true;
    }

    @Override
    public synchronized boolean isPause() {
        if (state == PLAY_PAUSE) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean isStop() {
        if (state == PLAY_STOP) {
            return true;
        }
        return false;
    }

    @Override
    public void postFrame(Runnable runnable) {
        if (isRelease()) return;
        postFrameQueue.offer(runnable);
    }

    @Override
    public synchronized boolean isRelease() {
        if (state == PLAY_PREPARE) {
            return true;
        }
        return false;
    }

    @Override
    public float getCurrentTime() {
        return TimeUtil.microToSecond(timeSyncer.getCurrentTimeUs());
    }

    @Override
    public void setCallback(Callback callback) {
        setCallback(callback, null);
    }

    @Override
    public void setCallback(Callback callback, Handler handler) {
        callBackHandler.setCallback(callback);
        callBackHandler.setHandler(handler);
    }


    private synchronized void preparePlayHandler() {
        if (playHandler != null) return;
        waitLastPlayStop();
        playHandler = MessageHandler.obtain(threadName);
        playHandler.addLifeCycleCallback(new MessageHandler.LifeCycleCallback() {
            @Override
            public void onHandlerRecycle() {
                internalRelease();
            }

            @Override
            public void onHandlerError(Exception exception) {
                internalRelease();
                callBackHandler.error(exception);
            }
        });
    }

    private synchronized void internalRelease() {
        state = PLAY_RELEASE;
        onRelease();
    }

    private void waitLastPlayStop() {
        if (stopFuture != null) {
            try {
                stopFuture.get();
            } catch (Exception e) {
            } finally {
                stopFuture = null;
            }
        }
    }


    protected void onPrepare() {
        androidDemuxer.setSource(fileSource);
        if (!androidDemuxer.isAvailable()) {
            throw new RuntimeException("not find video");
        }
        MediaFormat mediaFormat = new MediaFormat();
        MediaFormatUtil.setString(mediaFormat, MediaFormat.KEY_MIME, androidDemuxer.getMimeType());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_MAX_INPUT_SIZE, androidDemuxer.getMaxInputSize());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_PROFILE, androidDemuxer.getProfile());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_LEVEL, androidDemuxer.getProfileLevel());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_0, androidDemuxer.getCsd0Buffer());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_1, androidDemuxer.getCsd1Buffer());
        onInputFormatConfigure(mediaFormat);
        callBackHandler.prepare();
        synchronized (this) {
            if (seekSecond != null) {
                onSeek(seekSecond);
                seekSecond = null;
            }
        }
    }

    protected void onStart() {
        androidDecoder.start();
        callBackHandler.start();
    }

    protected void onSeek(float timeSecond) {
        androidDecoder.flush();
        androidDemuxer.seekToPreviousSync(TimeUtil.secondToMicro(timeSecond));
        timeSyncer.resetSync();
    }

    protected void onResume() {
        androidDecoder.resume();
        callBackHandler.resume();
    }

    protected void onPause() {
        androidDecoder.pause();
        callBackHandler.pause();
        timeSyncer.resetSync();
    }


    protected void onStop() {
        androidDecoder.stop();
        callBackHandler.stop();
        androidDemuxer.seekToPreviousSync(0);
        timeSyncer.clean();
    }


    protected void onRelease() {
        androidDemuxer.release();
        androidDecoder.release();
    }

    class VideoDecoderCallBack implements AndroidDecoder.CallBack {


        @Override
        public MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer inputBuffer) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            androidDemuxer.readSampleBuffer(inputBuffer, bufferInfo);
            androidDemuxer.advance();
            return bufferInfo;
        }

        @Override
        public boolean onOutputBufferAvailable(long presentationTimeUs, ByteBuffer outputBuffer) {
            boolean render = AndroidPlayerImpl.this.onOutputBufferRender(TimeUtil.microToSecond(presentationTimeUs), outputBuffer);
            long sleepTime = TimeUtil.microToMill(timeSyncer.syncTime(presentationTimeUs));
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
            return render && sleepTime > -100&& isPlaying();
        }

        @Override
        public void onOutputBufferRelease(long presentationTimeUs, boolean render) {
            float timeSecond = TimeUtil.microToSecond(presentationTimeUs);
            if (onOutputBufferProcess(timeSecond,  render)){
                callBackHandler.process(timeSecond, false);//todo
                while (!postFrameQueue.isEmpty()) {
                    Runnable runnable = postFrameQueue.poll();
                    runnable.run();
                }
            }
        }


        @Override
        public void onError(Exception exception) {
            ThrowableUtil.throwException(exception);
        }

        @Override
        public void onOutputFormatChanged(MediaFormat format) {
            AndroidPlayerImpl.this.onOutputFormatChanged(format);
        }
    }


    protected abstract void onInputFormatConfigure(MediaFormat inputFormat);

    protected abstract void onOutputFormatChanged(MediaFormat outputFormat);


    protected abstract boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer);

    protected abstract boolean onOutputBufferProcess(float timeSecond, boolean render);

}
