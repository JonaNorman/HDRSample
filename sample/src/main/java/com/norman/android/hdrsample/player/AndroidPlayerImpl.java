package com.norman.android.hdrsample.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;

import com.norman.android.hdrsample.player.decode.AndroidDecoder;
import com.norman.android.hdrsample.player.extract.AndroidExtractor;
import com.norman.android.hdrsample.player.source.FileSource;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.ExceptionUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;

abstract class AndroidPlayerImpl extends AbstractPlayerImpl implements Player {
    private static final String KEY_CSD_0 = "csd-0";
    private static final String KEY_CSD_1 = "csd-1";

    private static final int MAX_FRAME_JANK_MS = 50;

    private final AbstractPlayerImpl.CallBackHandler callBackHandler = new CallBackHandler();

    private final TimeSyncer timeSyncer = new TimeSyncer();

    private Long seekTimeUs;

    private AndroidExtractor androidExtractor;
    private AndroidDecoder androidDecoder;

    private FileSource fileSource;

    private final Object frameWaiter = new Object();

    private volatile boolean repeat = true;

    private volatile boolean hasEnd;


    public AndroidPlayerImpl(AndroidDecoder decoder, AndroidExtractor androidExtractor, String threadName) {
        super(threadName);
        this.androidExtractor = androidExtractor;
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
        androidExtractor.setSource(fileSource);
        if (!androidExtractor.isAvailable()) {
            throw new RuntimeException("file can not play");
        }
        MediaFormat mediaFormat = new MediaFormat();
        MediaFormatUtil.setString(mediaFormat, MediaFormat.KEY_MIME, androidExtractor.getMimeType());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_MAX_INPUT_SIZE, androidExtractor.getMaxInputSize());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_PROFILE, androidExtractor.getProfile());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_LEVEL, androidExtractor.getProfileLevel());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_0, androidExtractor.getCsd0Buffer());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_1, androidExtractor.getCsd1Buffer());
        onInputFormatPrepare(androidExtractor, mediaFormat);
        onDecoderConfigure(androidDecoder, mediaFormat);
        callBackHandler.callPrepare();
    }

    protected void onPlayStart() {
        androidDecoder.start();
    }

    protected void onPlaySeek(float timeSecond) {
        androidDecoder.flush();
        timeSyncer.flush();
        seekTimeUs = TimeUtil.secondToMicro(timeSecond);
        androidExtractor.seekPreSync(seekTimeUs);
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
        androidExtractor.seekPreSync(0);
        timeSyncer.reset();
        hasEnd = false;
        seekTimeUs = null;
    }


    protected void onPlayRelease() {
        androidExtractor.release();
        androidDecoder.release();
    }

    public AndroidDecoder getAndroidDecoder() {
        return androidDecoder;
    }

    public AndroidExtractor getAndroidExtractor() {
        return androidExtractor;
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
            androidExtractor.readSampleBuffer(inputBuffer, bufferInfo);
            androidExtractor.advance();
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

    protected abstract void onInputFormatPrepare(AndroidExtractor extractor, MediaFormat inputFormat);

    protected abstract void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat);

    protected abstract void onOutputFormatChanged(MediaFormat outputFormat);


    protected abstract boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer);

    protected abstract boolean onOutputBufferProcess(float timeSecond, boolean render);

}
