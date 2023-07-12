package com.norman.android.hdrsample.player;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class VideoPlayerImpl extends DecodePlayer<VideoDecoder, VideoExtractor> implements VideoPlayer {

    private static final String VIDEO_PLAYER_NAME = "VideoPlayer";

    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";

    private static final int MAX_FRAME_JANK_MS = 50;

    private final List<VideoSizeChangeListener> videoSizeChangedListeners = new CopyOnWriteArrayList<>();

    private int videoWidth;

    private int videoHeight;

    private final VideoOutput videoOutput;

    private final TimeSyncer timeSyncer = new TimeSyncer();

    private Long seekTimeUs;

    public VideoPlayerImpl(VideoOutput videoOutput) {
        this(VIDEO_PLAYER_NAME, videoOutput);
    }

    public VideoPlayerImpl(String threadName, VideoOutput videoOutput) {
        super(VideoDecoder.create(), VideoExtractor.create(), threadName);
        this.videoOutput = videoOutput;
    }


    @Override
    protected void onPlayPrepare() {
        videoOutput.prepare();
        super.onPlayPrepare();
    }

    @Override
    protected void onPlaySeek(long presentationTimeUs) {
        super.onPlaySeek(presentationTimeUs);
        seekTimeUs = presentationTimeUs;
        timeSyncer.flush();
    }

    @Override
    protected void onPlayPause() {
        super.onPlayPause();
        timeSyncer.flush();
    }

    @Override
    protected void onPlayStop() {
        super.onPlayStop();
        timeSyncer.reset();
        seekTimeUs = null;
    }

    @Override
    protected void onInputFormatPrepare(VideoExtractor extractor, VideoDecoder decoder, MediaFormat inputFormat) {
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_STANDARD, extractor.getColorStandard());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_RANGE, extractor.getColorRange());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_TRANSFER, extractor.getColorTransfer());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_WIDTH, extractor.getWidth());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_HEIGHT, extractor.getHeight());
        videoOutput.onDecoderPrepare(decoder, inputFormat);
        if (setVideoSize(extractor.getWidth(), extractor.getHeight())) {
            for (VideoSizeChangeListener videoSizeChangedListener : videoSizeChangedListeners) {
                videoSizeChangedListener.onVideoSizeChange(videoWidth, videoHeight);
            }
            videoOutput.onVideoSizeChange(videoWidth,videoHeight);
        }
    }

    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        int width = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_WIDTH);
        int height = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_HEIGHT);
        int cropLeft = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_LEFT);
        int cropRight = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_RIGHT);
        int cropTop = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_TOP);
        int cropBottom = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_BOTTOM);
        if (cropRight > 0 && cropBottom > 0) {
            width = cropRight - cropLeft + 1;
            height = cropBottom - cropTop + 1;
        }
        videoOutput.onOutputFormatChanged(outputFormat);
        if (setVideoSize(width, height)) {
            for (VideoSizeChangeListener videoSizeChangedListener : videoSizeChangedListeners) {
                videoSizeChangedListener.onVideoSizeChange(videoWidth, videoHeight);
            }
            videoOutput.onVideoSizeChange(videoWidth,videoHeight);
        }
    }

    @Override
    protected boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
        if (!isPlaying()) {
            return false;
        }
        if (seekTimeUs != null && presentationTimeUs < seekTimeUs) {
            return false;
        } else if (seekTimeUs != null) {
            seekTimeUs = null;
        }
        long sleepTime = TimeUtil.microToMill(timeSyncer.sync(presentationTimeUs));
        if (sleepTime <= -MAX_FRAME_JANK_MS) {
            return false;
        }
        videoOutput.onOutputBufferAvailable(outputBuffer,presentationTimeUs);
        return true;
    }

    @Override
    protected void onOutputBufferRelease(long presentationTimeUs) {
        long sleepTime = TimeUtil.microToMill(timeSyncer.sync(presentationTimeUs));
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignored) {
            }
        }
        videoOutput.onOutputBufferRelease(presentationTimeUs);
    }

    @Override
    protected void onPlayRelease() {
        super.onPlayRelease();
        videoOutput.release();
    }

    synchronized boolean setVideoSize(int width, int height) {
        int oldWidth = videoWidth;
        int oldHeight = videoHeight;
        videoWidth = width;
        videoHeight = height;
        return oldWidth != videoWidth || oldHeight != videoHeight;
    }

    @Override
    public VideoOutput getOutput() {
        return videoOutput;
    }

    @Override
    public void setOutputSurface(Surface surface) {
        videoOutput.setOutputSurface(surface);
    }

    @Override
    public synchronized int getWidth() {
        return videoWidth;
    }

    @Override
    public synchronized int getHeight() {
        return videoHeight;
    }

    @Override
    public void addSizeChangeListener(VideoSizeChangeListener changeListener) {
        if (videoSizeChangedListeners.contains(changeListener)) return;
        videoSizeChangedListeners.add(changeListener);
    }

    @Override
    public void removeSizeChangeListener(VideoSizeChangeListener changeListener) {
        if (!videoSizeChangedListeners.contains(changeListener)) return;
        videoSizeChangedListeners.remove(changeListener);
    }

    static class TimeSyncer {
        private long firstSystemTimeUs;
        private long firstPlayTimeUs;

        private long currentTimeUs;


        public synchronized void flush() {
            firstSystemTimeUs = 0;
            firstPlayTimeUs = 0;
        }

        public synchronized void reset() {
            firstSystemTimeUs = 0;
            firstPlayTimeUs = 0;
            currentTimeUs = 0;
        }

        public synchronized long getCurrentTimeUs() {
            return currentTimeUs;
        }


        public synchronized long sync(long timeUs) {
            currentTimeUs = timeUs;
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
