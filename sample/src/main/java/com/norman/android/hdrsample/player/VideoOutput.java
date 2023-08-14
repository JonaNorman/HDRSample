package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class VideoOutput {

    private final Object nextFrameWaiter = new Object();

    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";


    private final List<OutputFormatSubscriber> outputFormatSubscribers = new ArrayList<>();

    private final List<OutputSizeSubscriber> outputSizeSubscribers = new ArrayList<>();

    protected final Object outputFormatSync = new Object();

    protected final Object sizeSync = new Object();


    private VideoPlayer videoPlayer;

    protected VideoDecoder videoDecoder;

    protected VideoExtractor videoExtractor;


    protected int width;

    protected int height;

    protected int cropLeft;
    protected int cropRight;
    protected int cropTop;
    protected int cropBottom;

    protected int colorStandard;
    protected int colorRange;
    protected int colorTransfer;

    volatile int frameIndex;

    MediaFormat outputFormat;


    public abstract void setOutputSurface(Surface surface);


    public abstract void setOutputVideoView(VideoView view);


    final void onDecoderPrepare(VideoPlayer videoPlayer, VideoExtractor videoExtractor, VideoDecoder videoDecoder, MediaFormat inputFormat) {
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
        onOutputPrepare(inputFormat);
    }


    final void onDecodeStart() {
        onOutputStart();
    }


    final void onDecodePause() {
        notifyNextFrame();
        onOutputPause();
    }


    final void onDecodeResume() {
        onOutputResume();
    }


    final void onDecodeStop() {
        videoDecoder = null;
        videoExtractor = null;
        videoPlayer = null;
        frameIndex = 0;
        synchronized (sizeSync) {
            width = 0;
            height = 0;
        }
        synchronized (outputFormatSync) {
            outputFormat = null;
        }
        notifyNextFrame();
        onOutputStop();
    }

    final void onDecodeBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
        onOutputBufferAvailable(outputBuffer, presentationTimeUs);
    }

    final void onDecodeMediaFormatChanged(MediaFormat outputFormat) {
        int width = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_WIDTH);
        int height = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_HEIGHT);
        cropLeft = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_LEFT);
        cropRight = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_RIGHT);
        cropTop = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_TOP);
        cropBottom = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_BOTTOM);
        if (width<=0|| height<=0){
            width = this.width;
            height = this.height;
        }
        if (cropRight == 0 || cropBottom == 0) {
            cropRight = width;
            cropBottom = height;
        } else {
            cropRight = cropRight + 1;
            cropBottom = cropBottom + 1;
            width = cropRight - cropLeft;
            height = cropBottom - cropTop;
        }
        setVideoSize(width, height);
        synchronized (outputFormatSync) {
            this.outputFormat = outputFormat;
            onOutputFormatChanged(outputFormat);
            for (OutputFormatSubscriber outputFormatSubscriber : outputFormatSubscribers) {
                outputFormatSubscriber.onOutputFormatChange(outputFormat);
            }
        }
    }

    private void setVideoSize(int width, int height) {
        int oldWidth = this.width;
        int oldHeight = this.height;
        this.width = width;
        this.height = height;
        if (oldWidth != width || oldHeight != height) {
            onVideoSizeChange(width, height);
        }
    }

    final void onVideoSizeChange(int width, int height) {
        synchronized (sizeSync) {
            this.width = width;
            this.height = height;
            for (OutputSizeSubscriber outputSizeSubscriber : outputSizeSubscribers) {
                outputSizeSubscriber.onOutputSizeChange(width, height);
            }
        }
    }

    final void onDecodeBufferRender(long presentationTimeUs) {
        onOutputBufferRender(presentationTimeUs);
        frameIndex = frameIndex + 1;
        notifyNextFrame();
    }


    protected void onOutputPrepare(MediaFormat inputFormat) {

    }

    protected void onOutputStart() {

    }

    protected void onOutputPause() {

    }

    protected void onOutputResume() {

    }

    protected void onOutputStop() {

    }

    protected void onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {

    }

    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }

    protected void onOutputBufferRender(long presentationTimeUs) {

    }

    public void subscribe(OutputFormatSubscriber outputFormatSubscriber) {
        synchronized (outputFormatSync) {
            if (!outputFormatSubscribers.contains(outputFormatSubscriber)) {
                if (outputFormat != null) {
                    outputFormatSubscriber.onOutputFormatChange(outputFormat);
                }
                outputFormatSubscribers.add(outputFormatSubscriber);
            }
        }
    }

    public void unsubscribe(OutputFormatSubscriber outputFormatSubscriber) {
        synchronized (outputFormatSync) {
            outputFormatSubscribers.remove(outputFormatSubscriber);
        }
    }


    public void subscribe(OutputSizeSubscriber outputSizeSubscriber) {
        synchronized (sizeSync) {
            if (!outputSizeSubscribers.contains(outputSizeSubscriber)) {
                if (width > 0 && height > 0) {
                    outputSizeSubscriber.onOutputSizeChange(width, height);
                }
                outputSizeSubscribers.add(outputSizeSubscriber);
            }
        }
    }

    public void unsubscribe(OutputSizeSubscriber outputSizeSubscriber) {
        synchronized (sizeSync) {
            outputSizeSubscribers.remove(outputSizeSubscriber);
        }
    }


    public void waitNextFrame() {
        waitNextFrame(0);
    }

    public void waitNextFrame(float waitSecond) {
        long waitTime = TimeUtil.secondToMill(waitSecond);
        long startTime = System.currentTimeMillis();
        int oldFrameIndex = frameIndex;
        while (videoPlayer != null && videoPlayer.isPlaying() && oldFrameIndex != frameIndex) {
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

    interface OutputFormatSubscriber {
        void onOutputFormatChange(MediaFormat outputFormat);
    }

    interface OutputSizeSubscriber {
        void onOutputSizeChange(int width, int height);
    }
}
