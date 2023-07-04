package com.norman.android.hdrsample.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;

import com.norman.android.hdrsample.player.decode.AndroidDecoder;
import com.norman.android.hdrsample.player.dumex.AndroidDemuxer;
import com.norman.android.hdrsample.player.source.FileSource;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.ExceptionUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;

abstract class AndroidPlayerImpl extends PlayerImpl implements AndroidPlayer {
    private static final String KEY_CSD_0 = "csd-0";
    private static final String KEY_CSD_1 = "csd-1";

    private static final int MAX_FRAME_JANK_MS = 50;

    private final PlayerImpl.CallBackHandler callBackHandler = new CallBackHandler();

    private final TimeSyncer timeSyncer = new TimeSyncer();

    private Long seekTimeUs;

    private AndroidDemuxer androidDemuxer;
    private AndroidDecoder androidDecoder;

    private FileSource fileSource;

    private Object frameWaiter = new Object();

    private volatile boolean repeat = true;

    private volatile boolean hasEnd;


    public AndroidPlayerImpl(AndroidDecoder decoder, AndroidDemuxer androidDemuxer, String threadName) {
        super(threadName);
        this.androidDemuxer = androidDemuxer;
        this.androidDecoder = decoder;
    }

    @Override
    public synchronized void setSource(FileSource fileSource) {
        this.fileSource = fileSource;
    }


    @Override
    public synchronized void seek(float timeSecond) {
        post(() -> onPlaySeek(timeSecond));
    }

    @Override
    public synchronized void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    @Override
    public synchronized void release() {
        super.release();
        notifyFrameWaiter();
    }

    @Override
    public void waitFrame() {
        waitFrame(0);
    }

    @Override
    public void waitFrame(float second) {
        long waitTime = TimeUtil.secondToMill(second);
        long startTime = System.currentTimeMillis();
        while (isPlaying() && !hasEnd) {
            try {
                synchronized (frameWaiter) {
                    frameWaiter.wait(waitTime);
                }
            } catch (InterruptedException e) {

            }
            long remainTime = waitTime - (System.currentTimeMillis() - startTime);
            if (remainTime <= 0) {
                return;
            }
            waitTime = remainTime;
        }
    }

    private void notifyFrameWaiter() {
        synchronized (frameWaiter) {
            frameWaiter.notifyAll();
        }
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


    protected void onPlayPrepare() {
        androidDemuxer.setSource(fileSource);
        if (!androidDemuxer.isAvailable()) {
            throw new RuntimeException("file can not play");
        }
        MediaFormat mediaFormat = new MediaFormat();
        MediaFormatUtil.setString(mediaFormat, MediaFormat.KEY_MIME, androidDemuxer.getMimeType());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_MAX_INPUT_SIZE, androidDemuxer.getMaxInputSize());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_PROFILE, androidDemuxer.getProfile());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_LEVEL, androidDemuxer.getProfileLevel());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_0, androidDemuxer.getCsd0Buffer());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_1, androidDemuxer.getCsd1Buffer());
        onInputFormatPrepare(androidDemuxer, mediaFormat);
        onDecoderConfigure(androidDecoder, mediaFormat);
    }

    protected void onPlayStart() {
        androidDecoder.start();
    }

    protected void onPlaySeek(float timeSecond) {
        androidDecoder.flush();
        timeSyncer.flush();
        seekTimeUs = TimeUtil.secondToMicro(timeSecond);
        androidDemuxer.seekPreSync(seekTimeUs);
        hasEnd = false;
    }

    protected void onPlayResume() {
        androidDecoder.resume();
    }

    protected void onPlayPause() {
        androidDecoder.pause();
        timeSyncer.flush();
    }


    protected void onPlayStop() {
        androidDecoder.stop();
        androidDemuxer.seekPreSync(0);
        timeSyncer.reset();
        hasEnd = false;
        seekTimeUs = null;
    }


    protected void onPlayRelease() {
        androidDemuxer.release();
        androidDecoder.release();
    }

    public AndroidDecoder getAndroidDecoder() {
        return androidDecoder;
    }

    public AndroidDemuxer getAndroidDemuxer() {
        return androidDemuxer;
    }

    @Override
    protected void onPlayError(Exception exception) {
        Log.e("AndroidPlayerImpl", Log.getStackTraceString(exception));
        callBackHandler.callError(exception);
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
        public boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
            if (!isPlaying()) {
                return false;
            }
            if (!onOutputBufferRender(TimeUtil.microToSecond(presentationTimeUs), outputBuffer)) {
                return false;
            }
            long sleepTime = TimeUtil.microToMill(timeSyncer.sync(presentationTimeUs));
            if (sleepTime <= -MAX_FRAME_JANK_MS) {
                return false;
            }
            if (seekTimeUs != null && presentationTimeUs < seekTimeUs) {
                return false;
            } else if (seekTimeUs != null) {
                seekTimeUs = null;
            }
            return true;
        }

        @Override
        public void onOutputBufferRelease(long presentationTimeUs, boolean render) {
            long sleepTime = TimeUtil.microToMill(timeSyncer.sync(presentationTimeUs));
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
            float timeSecond = TimeUtil.microToSecond(presentationTimeUs);
            if (onOutputBufferProcess(timeSecond, render)) {
                callBackHandler.callProcess(timeSecond);
                notifyFrameWaiter();
            }
        }


        @Override
        public void onDecodeError(Exception exception) {
            ExceptionUtil.throwRuntime(exception);
        }

        @Override
        public void onOutputBufferEndOfStream() {
            callBackHandler.callEnd();
            hasEnd = true;
            notifyFrameWaiter();
            if (repeat) {
                seek(0);
            }
        }

        @Override
        public void onOutputFormatChanged(MediaFormat format) {
            AndroidPlayerImpl.this.onOutputFormatChanged(format);
        }
    }

    protected abstract void onInputFormatPrepare(AndroidDemuxer demuxer, MediaFormat inputFormat);

    protected abstract void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat);

    protected abstract void onOutputFormatChanged(MediaFormat outputFormat);


    protected abstract boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer);

    protected abstract boolean onOutputBufferProcess(float timeSecond, boolean render);

}
