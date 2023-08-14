package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.CallSuper;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;

public abstract class VideoOutput {

    private final Object nextFrameWaiter = new Object();

    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";


    private int width;

    private int height;

    private int colorStandard;
    private int colorRange;
    private int colorTransfer;
    private VideoPlayer videoPlayer;

    VideoDecoder videoDecoder;

    VideoExtractor videoExtractor;

   volatile int frameIndex;


    public abstract void setOutputSurface(Surface surface);


    public abstract void setOutputVideoView(VideoView view);


    protected void onDecoderPrepare(VideoPlayer videoPlayer, VideoExtractor videoExtractor, VideoDecoder videoDecoder, MediaFormat inputFormat) {
        this.videoPlayer = videoPlayer;
        this.videoDecoder = videoDecoder;
        this.videoExtractor = videoExtractor;
        colorStandard = videoExtractor.getColorStandard();
        colorRange = videoExtractor.getColorRange();
        colorTransfer = videoExtractor.getColorTransfer();
        setVideoSize(videoExtractor.getWidth(), videoExtractor.getHeight());
        inputFormat.setInteger(MediaFormat.KEY_COLOR_STANDARD, colorStandard);
        inputFormat.setInteger(MediaFormat.KEY_COLOR_RANGE, colorRange);
        inputFormat.setInteger(MediaFormat.KEY_COLOR_TRANSFER, colorTransfer);
        inputFormat.setInteger(MediaFormat.KEY_WIDTH, width);
        inputFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
    }

    @CallSuper
    protected void onDecodeStart() {

    }

    @CallSuper
    protected void onDecodePause() {
        ignoreWaitFrame();
    }

    @CallSuper
    protected void onDecodeResume() {

    }


    @CallSuper
    protected void onDecodeStop() {
        videoDecoder = null;
        videoExtractor = null;
        videoPlayer = null;
        ignoreWaitFrame();
    }


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
        if (width > 0 && height > 0) {
            setVideoSize(width, height);
        }
    }

    protected void onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {

    }


    protected void onOutputBufferRender(long presentationTimeUs) {
        frameIndex = frameIndex+1;
        notifyNextFrame();
    }


    protected void onVideoSizeChange(int width, int height) {
        this.width = width;
        this.height = height;
    }

    void setVideoSize(int width, int height) {
        int oldWidth = this.width;
        int oldHeight = this.height;
        this.width = width;
        this.height = height;
        if (oldWidth != width || oldHeight != height) {
            onVideoSizeChange(width, height);
        }
    }


    public void waitNextFrame() {
        waitNextFrame(0);
    }

    public void waitNextFrame(float waitSecond) {
        long waitTime = TimeUtil.secondToMill(waitSecond);
        long startTime = System.currentTimeMillis();
        int oldFrameIndex = frameIndex;
        while (videoPlayer != null && videoPlayer.isPlaying() && oldFrameIndex !=frameIndex) {
            try {
                synchronized (nextFrameWaiter) {
                    nextFrameWaiter.wait(waitTime);
                }
            } catch (InterruptedException ignored) {

            }
            long remainTime = waitTime - (System.currentTimeMillis() - startTime);
            if (remainTime <= 0) {
                return;
            }
            waitTime = remainTime;
        }
    }

    private void notifyNextFrame() {
        synchronized (nextFrameWaiter) {
            nextFrameWaiter.notifyAll();
        }
    }

    private void ignoreWaitFrame() {
        synchronized (nextFrameWaiter) {
            nextFrameWaiter.notifyAll();
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getColorStandard() {
        return colorStandard;
    }

    public int getColorRange() {
        return colorRange;
    }

    public int getColorTransfer() {
        return colorTransfer;
    }
}
