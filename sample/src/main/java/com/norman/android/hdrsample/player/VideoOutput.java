package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.os.SystemClock;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 视频最终输出对象，VideoOutput又可以输出到Surface和VideoView
 */
public abstract class VideoOutput {

    public static final float DEFAULT_WAIT_TIME_SECOND = 0.2f;


    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";
    private final List<OutputFormatSubscriber> outputFormatSubscribers = new ArrayList<>();

    private final List<OutputSizeSubscriber> outputSizeSubscribers = new ArrayList<>();


    private final AtomicBoolean skipFrameWait = new AtomicBoolean();
    private final Object waitFrameLock = new Object();
    private volatile int frameIndex;
    private VideoPlayer videoPlayer;

    private MediaFormat outputFormat;

    protected VideoDecoder videoDecoder;

    protected VideoExtractor videoExtractor;

    private boolean release;


    protected int videoWidth;

    protected int videoHeight;

    protected int cropLeft;
    protected int cropRight;
    protected int cropTop;
    protected int cropBottom;

    private VideoView currentVideoView;

    private VideoView requestVideoView;


    private Surface outputSurface;

    private final VideoView.SurfaceSubscriber surfaceSubscriber = new VideoView.SurfaceSubscriber() {
        @Override
        public void onSurfaceAvailable(Surface surface, int width, int height) {
            setInternalOutputSurface(surface);
        }

        @Override
        public void onSurfaceRedraw() {
            waitNextFrame(DEFAULT_WAIT_TIME_SECOND);
        }

        @Override
        public void onSurfaceDestroy() {
            setInternalOutputSurface(null);
        }
    };

    private final OutputSizeSubscriber outputSizeSubscriber = new OutputSizeSubscriber() {
        @Override
        public void onOutputSizeChange(int width, int height) {
            synchronized (VideoOutput.this){
                if (currentVideoView != null){
                    currentVideoView.setAspectRatio(width*1.0f/height);//保证视频比例和Surface比例一样
                }
            }
        }
    };


    /**
     * 设置最终渲染到Surface上，和setOutputVideoView互斥，只能设置一个
     *
     * @param surface
     */
    public final synchronized void setOutputSurface(Surface surface) {
        setOutputVideoView(null);// videoView和Surface只能同时设置一个
        setInternalOutputSurface(surface);
    }

    /**
     * 设置最终渲染到VideoView上，和setOutputSurface互斥，只能设置一个
     *
     * @param view
     */
    public final  synchronized void setOutputVideoView(VideoView view) {
        requestVideoView = view;
        if (isPlayerPrepared()){
            attachVideoView();
        }
    }


    final synchronized void create(VideoPlayer videoPlayer) {
        if (videoPlayer == this.videoPlayer) {
            return;
        }
        if (this.videoPlayer != null) {
            throw new IllegalStateException("VideoOutput and VidePlayer only one to one");
        }
        if (release){
            throw new IllegalStateException("VideoOutput is released");
        }
        this.videoPlayer = videoPlayer;
        onOutputCreate();
    }


    final synchronized void release() {
        if (release) {
            return;
        }
        release = true;
        onOutputRelease();
    }


    /**
     * 视频准备解码
     *
     * @param videoExtractor
     * @param videoDecoder
     * @param inputFormat
     */
    final synchronized void prepare(
            VideoExtractor videoExtractor,
            VideoDecoder videoDecoder,
            MediaFormat inputFormat) {
        this.videoDecoder = videoDecoder;
        this.videoExtractor = videoExtractor;
        MediaFormatUtil.setColorStandard(inputFormat, videoExtractor.getColorStandard());
        MediaFormatUtil.setColorRange(inputFormat, videoExtractor.getColorRange());
        MediaFormatUtil.setColorTransfer(inputFormat, videoExtractor.getColorTransfer());
        inputFormat.setInteger(MediaFormat.KEY_WIDTH, videoExtractor.getWidth());
        inputFormat.setInteger(MediaFormat.KEY_HEIGHT, videoExtractor.getHeight());
        setVideoSize(videoExtractor.getWidth(), videoExtractor.getHeight());
        attachVideoView();
        onOutputPrepare(inputFormat);
    }


    /**
     * 开始解码
     */

    final synchronized void start() {
        onOutputStart();
    }

    /**
     * 暂停
     */

    final synchronized void pause() {
        skipWaitFrame();//暂停时跳过等待着的线程，防止一直卡在那里
        onOutputPause();
    }


    /**
     * 恢复
     */
    final void resume() {
        onOutputResume();
    }

    /**
     * 停止
     */

    final synchronized void stop() {
        detachVideoView();
        videoDecoder = null;
        videoExtractor = null;
        videoPlayer = null;
        frameIndex = 0;
        videoWidth = 0;
        videoHeight = 0;
        outputFormat = null;
        skipFrameWait.set(false);
        skipWaitFrame();//停止防止一直等待
        onOutputStop();
    }


    synchronized void attachVideoView(){
        if (currentVideoView == requestVideoView) {
            return;
        }
        detachVideoView();
        currentVideoView  = requestVideoView;
        if (currentVideoView != null){
            subscribe(outputSizeSubscriber);
            currentVideoView.subscribe(surfaceSubscriber);
        }
    }

   synchronized void detachVideoView(){
       VideoView oldView = currentVideoView;
       if (oldView != null){//取消上一次绑定
           oldView.unsubscribe(surfaceSubscriber);
           unsubscribe(outputSizeSubscriber);
       }
       currentVideoView = null;
    }

    synchronized boolean isPlayerPrepared() {
        return videoPlayer != null &&videoPlayer.isPrepared();
    }

    /**
     * 每一帧数据回调
     *
     * @param outputBuffer
     * @param presentationTimeUs
     */
    final synchronized void onDecodeBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
        onOutputBufferAvailable(outputBuffer, presentationTimeUs);
    }

    /**
     * 解码格式回调
     *
     * @param outputFormat
     */

    final synchronized void onDecodeMediaFormatChanged(MediaFormat outputFormat) {
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
        this.outputFormat = outputFormat;
        onOutputFormatChanged(outputFormat);
        for (OutputFormatSubscriber outputFormatSubscriber : outputFormatSubscribers) {
            outputFormatSubscriber.onOutputFormatChange(outputFormat);
        }
    }

    /**
     * 视频大小设置
     *
     * @param width
     * @param height
     */

    private void setVideoSize(int width, int height) {
        if (videoWidth == width && videoHeight == height) {
            return;
        }
        videoWidth = width;
        videoHeight = height;
        for (OutputSizeSubscriber outputSizeSubscriber : outputSizeSubscribers) {
            outputSizeSubscriber.onOutputSizeChange(width, height);
        }
    }

    /**
     * 每帧数据渲染完成回调
     *
     * @param presentationTimeUs
     */
    final synchronized void onDecodeBufferRender(long presentationTimeUs) {
        boolean render = onOutputBufferRender(presentationTimeUs);
        if (render) {
            notifyNextFrame();
        }
    }


    synchronized void setInternalOutputSurface(Surface surface){
        if (outputSurface != surface){
            outputSurface = surface;
            onOutputSurfaceChange(outputSurface);
        }
    }



    protected void onOutputCreate(){

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

    protected void onOutputRelease(){

    }

    protected void onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {

    }

    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }

    /**
     * 渲染完成回调
     *
     * @param presentationTimeUs
     * @return
     */

    protected boolean onOutputBufferRender(long presentationTimeUs) {
        return true;
    }


    protected abstract void onOutputSurfaceChange(Surface surface);


    /**
     * 视频格式订阅，如果已经存在会直接回调，不然会解码出格式或者格式变化时回调
     *
     * @param outputFormatSubscriber
     */


    public synchronized void subscribe(OutputFormatSubscriber outputFormatSubscriber) {
        if (!outputFormatSubscribers.contains(outputFormatSubscriber)) {
            if (outputFormat != null) {
                outputFormatSubscriber.onOutputFormatChange(outputFormat);
            }
            outputFormatSubscribers.add(outputFormatSubscriber);
        }
    }

    /**
     * 取消视频格式订阅
     *
     * @param outputFormatSubscriber
     */

    public synchronized void unsubscribe(OutputFormatSubscriber outputFormatSubscriber) {
        outputFormatSubscribers.remove(outputFormatSubscriber);

    }

    /**
     * 视频大小订阅，如果已经知道视频大小了会直接回调，不然会解码出大小或者大小变化时回调
     *
     * @param outputSizeSubscriber
     */

    public synchronized void subscribe(OutputSizeSubscriber outputSizeSubscriber) {
        if (!outputSizeSubscribers.contains(outputSizeSubscriber)) {
            if (videoWidth > 0 && videoHeight > 0) {
                outputSizeSubscriber.onOutputSizeChange(videoWidth, videoHeight);
            }
            outputSizeSubscribers.add(outputSizeSubscriber);
        }
    }

    /**
     * 取消订阅
     *
     * @param outputSizeSubscriber
     */

    public synchronized void unsubscribe(OutputSizeSubscriber outputSizeSubscriber) {
        outputSizeSubscribers.remove(outputSizeSubscriber);

    }


    /**
     * 同步等待下一帧完成
     */
    public void waitNextFrame() {
        waitNextFrame(0);
    }

    /**
     * 同步等待下一帧完成
     *
     * @param waitSecond 秒
     */
    public void waitNextFrame(float waitSecond) {
        long waitTime = TimeUtil.secondToMill(waitSecond);
        long startTime = SystemClock.elapsedRealtime();
        int oldFrameIndex = frameIndex;
        while (true) {
            synchronized (waitFrameLock) {
                long remainTime = waitTime - (SystemClock.elapsedRealtime() - startTime);
                boolean needWait = remainTime > 0 && //超时不需要等待
                        videoPlayer != null && videoPlayer.isPlaying() &&//没有播放不需要等待
                        oldFrameIndex == frameIndex; //下一帧没有渲染完成不需要等待
                if (!needWait || skipFrameWait.getAndSet(false)){
                    skipFrameWait.getAndSet(false);
                    return;
                }
                try {
                    waitFrameLock.wait(remainTime);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    /**
     * 忽略这一次的等待
     */
    void skipWaitFrame() {
        skipFrameWait.set(true);
    }


    /**
     * 通知这一帧已经渲染完成
     */
    void notifyNextFrame() {
        frameIndex = frameIndex + 1;
    }

    public interface OutputFormatSubscriber {
        /**
         * 视频格式变化回调
         *
         * @param outputFormat
         */
        void onOutputFormatChange(MediaFormat outputFormat);
    }

    public interface OutputSizeSubscriber {
        /**
         * 视频大小回调
         *
         * @param width
         * @param height
         */
        void onOutputSizeChange(int width, int height);
    }


}
