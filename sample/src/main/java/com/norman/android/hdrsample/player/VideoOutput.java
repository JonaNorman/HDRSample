package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.os.SystemClock;
import android.view.Surface;

import androidx.annotation.IntDef;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频最终输出对象，VideoOutput又可以输出到Surface和VideoView
 */
public abstract class VideoOutput {

    public static final float DEFAULT_WAIT_TIME_SECOND = 0.2f;


    public static final String KEY_COLOR_SPACE = "color-space";

    /**
     * SDR视频 包含了BT709、BT601
     */
    public static final int COLOR_SPACE_SDR = 0;

    /**
     * BT2020 PQ视频
     */

    public static final int COLOR_SPACE_BT2020_PQ = 1;

    /**
     * BT2020 HLG视频
     */
    public static final int COLOR_SPACE_BT2020_HLG = 2;

    /**
     * BT2020 线性视频
     */
    public static final int COLOR_SPACE_BT2020_LINEAR = 3;



    @IntDef({COLOR_SPACE_SDR, COLOR_SPACE_BT2020_PQ, COLOR_SPACE_BT2020_HLG,COLOR_SPACE_BT2020_LINEAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ColorSpace {
    }

    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";
    protected final Object syncLock = new Object();
    private final List<OutputFormatSubscriber> outputFormatSubscribers = new ArrayList<>();

    private final List<OutputSizeSubscriber> outputSizeSubscribers = new ArrayList<>();


    private boolean frameSkipWait;
    private int frameIndex;
    private VideoPlayer videoPlayer;

    protected VideoDecoder videoDecoder;

    protected VideoExtractor videoExtractor;


    protected int videoWidth;

    protected int videoHeight;

    protected int cropLeft;
    protected int cropRight;
    protected int cropTop;
    protected int cropBottom;

    protected MediaFormat outputFormat;


    /**
     * 经过OpenGL中转
     *
     * @return
     */
    public static GLVideoOutput createGLOutput() {
        return new GLVideoOutputImpl();
    }


    /**
     * 直接用解码到Surface
     *
     * @return
     */
    public static DirectVideoOutput createAndroidOutput() {
        return new DirectVideoOutputImpl();
    }


    public abstract void setOutputSurface(Surface surface);


    public abstract void setOutputVideoView(VideoView view);


    /**
     * 视频准备解码
     *
     * @param videoPlayer
     * @param videoExtractor
     * @param videoDecoder
     * @param inputFormat
     */
    final void onDecoderPrepare(VideoPlayer videoPlayer,
                                VideoExtractor videoExtractor,
                                VideoDecoder videoDecoder,
                                MediaFormat inputFormat) {
        synchronized (syncLock) {
            this.videoPlayer = videoPlayer;
            this.videoDecoder = videoDecoder;
            this.videoExtractor = videoExtractor;
        }
        inputFormat.setInteger(MediaFormat.KEY_COLOR_STANDARD, videoExtractor.getColorStandard());
        inputFormat.setInteger(MediaFormat.KEY_COLOR_RANGE, videoExtractor.getColorRange());
        inputFormat.setInteger(MediaFormat.KEY_COLOR_TRANSFER, videoExtractor.getColorTransfer());
        inputFormat.setInteger(MediaFormat.KEY_WIDTH, videoExtractor.getWidth());
        inputFormat.setInteger(MediaFormat.KEY_HEIGHT, videoExtractor.getHeight());
        setVideoSize(videoExtractor.getWidth(), videoExtractor.getHeight());
        onOutputPrepare(inputFormat);
    }

    /**
     * 开始解码
     */

    final void onDecodeStart() {
        onOutputStart();
    }

    /**
     * 暂停
     */

    final void onDecodePause() {
        skipWaitFrame();
        onOutputPause();
    }


    /**
     * 恢复
     */
    final void onDecodeResume() {
        onOutputResume();
    }

    /**
     * 停止
     */

    final void onDecodeStop() {
        synchronized (syncLock) {//
            videoDecoder = null;
            videoExtractor = null;
            videoPlayer = null;
            frameIndex = 0;
            videoWidth = 0;
            videoHeight = 0;
            outputFormat = null;
            frameSkipWait = false;
        }
        skipWaitFrame();//停止防止一直等待
        onOutputStop();
    }

    /**
     * 每一帧数据回调
     *
     * @param outputBuffer
     * @param presentationTimeUs
     */
    final void onDecodeBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
        onOutputBufferAvailable(outputBuffer, presentationTimeUs);
    }

    /**
     * 解码格式回调
     *
     * @param outputFormat
     */

    final void onDecodeMediaFormatChanged(MediaFormat outputFormat) {
        int width = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_WIDTH);
        int height = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_HEIGHT);
        //buffer模式的清空下使用，防止绿边，因为codec的宽高会根据解码器对齐4、8或16
        cropLeft = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_LEFT);
        cropRight = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_RIGHT);
        cropTop = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_TOP);
        cropBottom = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_BOTTOM);
        if (width <= 0 || height <= 0) {
            width = this.videoWidth;
            height = this.videoHeight;
        }
        if (cropRight == 0 || cropBottom == 0) {
            cropRight = width;
            cropBottom = height;
        } else {
            //KEY_CROP_RIGHT、KEY_CROP_BOTTOM的大小是从0开始的，要加1
            cropRight = cropRight + 1;
            cropBottom = cropBottom + 1;
            width = cropRight - cropLeft;
            height = cropBottom - cropTop;
        }
        setVideoSize(width, height);
        int colorStandard = MediaFormatUtil.getColorStandard(outputFormat);
        int colorTransfer = MediaFormatUtil.getColorTransfer(outputFormat);
        if (colorStandard == MediaFormat.COLOR_STANDARD_BT2020 && colorTransfer == MediaFormat.COLOR_TRANSFER_HLG) {
            outputFormat.setInteger(KEY_COLOR_SPACE, COLOR_SPACE_BT2020_HLG);
        } else if (colorStandard == MediaFormat.COLOR_STANDARD_BT2020 && colorTransfer == MediaFormat.COLOR_TRANSFER_ST2084) {
            outputFormat.setInteger(KEY_COLOR_SPACE, COLOR_SPACE_BT2020_PQ);
        }else if (colorStandard == MediaFormat.COLOR_STANDARD_BT2020 && colorTransfer == MediaFormat.COLOR_TRANSFER_LINEAR) {
            outputFormat.setInteger(KEY_COLOR_SPACE, COLOR_SPACE_BT2020_LINEAR);
        } else {
            outputFormat.setInteger(KEY_COLOR_SPACE, COLOR_SPACE_SDR);
        }
        synchronized (syncLock) {
            this.outputFormat = outputFormat;
            onOutputFormatChanged(outputFormat);
            for (OutputFormatSubscriber outputFormatSubscriber : outputFormatSubscribers) {
                outputFormatSubscriber.onOutputFormatChange(outputFormat);
            }
        }
    }

    /**
     * 视频大小设置
     *
     * @param width
     * @param height
     */

    private void setVideoSize(int width, int height) {
        synchronized (syncLock) {
            if (videoWidth == width && videoHeight == height) {
                return;
            }
            videoWidth = width;
            videoHeight = height;
            for (OutputSizeSubscriber outputSizeSubscriber : outputSizeSubscribers) {
                outputSizeSubscriber.onOutputSizeChange(width, height);
            }
        }
    }

    /**
     * 每帧数据渲染完成回调
     *
     * @param presentationTimeUs
     */
    final void onDecodeBufferRender(long presentationTimeUs) {
        boolean render = onOutputBufferRender(presentationTimeUs);
        if (render) {
            notifyNextFrame();
        }
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

    protected boolean onOutputBufferRender(long presentationTimeUs) {
        return true;
    }


    /**
     * 视频格式订阅，如果已经存在会直接回调，不然会解码出格式或者格式变化时回调
     *
     * @param outputFormatSubscriber
     */


    public void subscribe(OutputFormatSubscriber outputFormatSubscriber) {
        synchronized (syncLock) {
            if (!outputFormatSubscribers.contains(outputFormatSubscriber)) {
                if (outputFormat != null) {
                    outputFormatSubscriber.onOutputFormatChange(outputFormat);
                }
                outputFormatSubscribers.add(outputFormatSubscriber);
            }
        }
    }

    /**
     * 取消视频格式订阅
     *
     * @param outputFormatSubscriber
     */

    public void unsubscribe(OutputFormatSubscriber outputFormatSubscriber) {
        synchronized (syncLock) {
            outputFormatSubscribers.remove(outputFormatSubscriber);
        }
    }

    /**
     * 视频大小订阅，如果已经知道视频大小了会直接回调，不然会解码出大小或者大小变化时回调
     *
     * @param outputSizeSubscriber
     */

    public void subscribe(OutputSizeSubscriber outputSizeSubscriber) {
        synchronized (syncLock) {
            if (!outputSizeSubscribers.contains(outputSizeSubscriber)) {
                if (videoWidth > 0 && videoHeight > 0) {
                    outputSizeSubscriber.onOutputSizeChange(videoWidth, videoHeight);
                }
                outputSizeSubscribers.add(outputSizeSubscriber);
            }
        }
    }

    /**
     * 取消订阅
     *
     * @param outputSizeSubscriber
     */

    public void unsubscribe(OutputSizeSubscriber outputSizeSubscriber) {
        synchronized (syncLock) {
            outputSizeSubscribers.remove(outputSizeSubscriber);
        }
    }


    /**
     * 同步等待下一帧完成
     */
    public void waitNextFrame() {
        waitNextFrame(0);
    }

    /**
     * 同步等待下一帧完成
     */
    public void waitNextFrame(float waitSecond) {
        long waitTime = TimeUtil.secondToMill(waitSecond);
        long startTime = SystemClock.elapsedRealtime();
        int oldFrameIndex = frameIndex;
        while (true) {
            synchronized (syncLock) {
                VideoPlayer player = videoPlayer;
                long remainTime = waitTime - (SystemClock.elapsedRealtime() - startTime);
                boolean needWait = remainTime > 0 && player != null &&
                        player.isPlaying() &&
                        oldFrameIndex == frameIndex && !frameSkipWait;
                if (!needWait) {
                    frameSkipWait = false;
                    return;
                }
                try {
                    syncLock.wait(remainTime);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    /**
     * 忽略这一次的等待
     */
    void skipWaitFrame() {
        synchronized (syncLock) {
            frameSkipWait = true;
            syncLock.notifyAll();
        }
    }


    /**
     * 通知这一帧已经渲染完成
     */
    void notifyNextFrame() {
        synchronized (syncLock) {
            frameIndex = frameIndex + 1;
            syncLock.notifyAll();
        }
    }

    public interface OutputFormatSubscriber {
        void onOutputFormatChange(MediaFormat outputFormat);
    }

    public interface OutputSizeSubscriber {
        void onOutputSizeChange(int width, int height);
    }


}
