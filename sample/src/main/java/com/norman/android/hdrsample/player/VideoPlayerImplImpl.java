package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.os.SystemClock;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;

class VideoPlayerImplImpl extends DecodePlayerImpl<VideoDecoder, VideoExtractor> implements VideoPlayer {

    private static final String VIDEO_PLAYER_NAME = "VideoPlayer";

    /**
     * 最大掉帧时间
     */
    private static final int MAX_FRAME_JANK_MS = 50;

    private final VideoOutput videoOutput;

    private final TimeSyncer timeSyncer = new TimeSyncer();//视频音画同步

    private Long seekTimeUs;

    public VideoPlayerImplImpl(VideoOutput videoOutput) {
        this(VIDEO_PLAYER_NAME, videoOutput);
    }

    public VideoPlayerImplImpl(String threadName, VideoOutput videoOutput) {
        super(VideoDecoder.create(), VideoExtractor.create(), threadName);
        this.videoOutput = videoOutput;
    }

    @Override
    protected void onPlaySeek(long presentationTimeUs) {
        super.onPlaySeek(presentationTimeUs);
        seekTimeUs = presentationTimeUs;
        timeSyncer.flush();//seek时候清空时间同步信息
    }

    @Override
    protected void onPlayStart() {
        super.onPlayStart();
        videoOutput.onDecodeStart();
    }

    @Override
    protected void onPlayPause() {
        super.onPlayPause();
        timeSyncer.flush();//
        videoOutput.onDecodePause();
    }

    @Override
    protected void onPlayResume() {
        super.onPlayResume();
        videoOutput.onDecodeResume();
    }

    @Override
    protected void onPlayStop() {
        super.onPlayStop();
        timeSyncer.reset();
        seekTimeUs = null;
        videoOutput.onDecodeStop();

    }

    @Override
    protected void onInputFormatConfigure(VideoExtractor extractor, VideoDecoder decoder, MediaFormat inputFormat) {
        videoOutput.onDecoderPrepare(this,extractor,decoder, inputFormat);
    }

    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        videoOutput.onDecodeMediaFormatChanged(outputFormat);
    }

    @Override
    protected boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
        if (!isPlaying()) {
            return false;
        }
        if (seekTimeUs != null && presentationTimeUs < seekTimeUs) {//seek是往前找的关键帧，如果还没到就不需要渲染
            return false;
        } else if (seekTimeUs != null) {
            seekTimeUs = null;
        }
        long sleepTime = TimeUtil.microToMill(timeSyncer.sync(presentationTimeUs));
        if (sleepTime <= -MAX_FRAME_JANK_MS) {//掉帧不需要渲染
            return false;
        }
        videoOutput.onDecodeBufferAvailable(outputBuffer,presentationTimeUs);
        return true;
    }

    @Override
    protected void onOutputBufferEndOfStream() {
        seekTimeUs = null;//视频播放结束清空原先的seek信息
    }

    @Override
    protected void onOutputBufferRender(long presentationTimeUs) {
        long sleepTime = TimeUtil.microToMill(timeSyncer.sync(presentationTimeUs));
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);//还没渲染的时间点就等待
            } catch (InterruptedException ignored) {
            }
        }
        videoOutput.onDecodeBufferRender(presentationTimeUs);
    }

    @Override
    protected void onPlayRelease() {
        super.onPlayRelease();
        videoOutput.onDecodeStop();
    }

    @Override
    public VideoOutput getOutput() {
        return videoOutput;
    }

    static class TimeSyncer {
        private long firstSystemTimeUs;
        private long firstPlayTimeUs;



        public synchronized void flush() {
            firstSystemTimeUs = 0;
            firstPlayTimeUs = 0;
        }

        public synchronized void reset() {
            firstSystemTimeUs = 0;
            firstPlayTimeUs = 0;
        }


        public synchronized long sync(long timeUs) {
            long currentSystemUs = TimeUtil.nanoToMicro(SystemClock.elapsedRealtimeNanos());
            if (firstSystemTimeUs == 0) {
                firstSystemTimeUs = currentSystemUs;
                firstPlayTimeUs = timeUs;
                return 0;
            } else {
                long timeCost = currentSystemUs - firstSystemTimeUs;
                long sleepTime = timeUs - firstPlayTimeUs - timeCost;
                return sleepTime;
            }
        }
    }
}
