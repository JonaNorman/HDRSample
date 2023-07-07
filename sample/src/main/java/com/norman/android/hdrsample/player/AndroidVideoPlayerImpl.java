package com.norman.android.hdrsample.player;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.norman.android.hdrsample.player.decode.AndroidDecoder;
import com.norman.android.hdrsample.player.extract.AndroidExtractor;
import com.norman.android.hdrsample.player.extract.AndroidVideoExtractor;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class AndroidVideoPlayerImpl extends AndroidPlayerImpl implements VideoPlayer {

    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";

    private List<VideoSizeChangeListener> videoSizeChangedListeners = new CopyOnWriteArrayList<>();

    private int videoWidth;

    private int videoHeight;

    public AndroidVideoPlayerImpl(AndroidDecoder decoder, String threadName) {
        super(decoder, AndroidVideoExtractor.create(), threadName);
    }

    @Override
    protected void onInputFormatPrepare(AndroidExtractor extractor, MediaFormat inputFormat) {
        AndroidVideoExtractor videoExtractor = (AndroidVideoExtractor) extractor;
        synchronized (this){
            videoWidth = videoExtractor.getWidth();
            videoHeight = videoExtractor.getHeight();
        }
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_WIDTH, videoWidth);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_HEIGHT, videoHeight);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_STANDARD, videoExtractor.getColorStandard());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_RANGE, videoExtractor.getColorRange());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_TRANSFER, videoExtractor.getColorTransfer());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        for (VideoSizeChangeListener videoSizeChangedListener : videoSizeChangedListeners) {
            videoSizeChangedListener.onVideoSizeChange(videoWidth, videoHeight);
        }
    }

    @Override
    protected void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat) {
        decoder.configure(new AndroidDecoder.Configuration(inputFormat, new VideoDecoderCallBack()));
    }

    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        int width = MediaFormatUtil.getInteger(outputFormat,MediaFormat.KEY_WIDTH);
        int height = MediaFormatUtil.getInteger(outputFormat,MediaFormat.KEY_HEIGHT);
        int cropLeft = MediaFormatUtil.getInteger(outputFormat,KEY_CROP_LEFT);
        int cropRight = MediaFormatUtil.getInteger(outputFormat,KEY_CROP_RIGHT);
        int cropTop = MediaFormatUtil.getInteger(outputFormat,KEY_CROP_TOP);
        int cropBottom = MediaFormatUtil.getInteger(outputFormat,KEY_CROP_BOTTOM);
        if (cropRight>0&& cropBottom>0){
            width= cropRight-cropLeft+1;
            height= cropBottom-cropTop+1;
        }
        boolean change = false;
        synchronized (this){
            if (width !=videoWidth || height != videoHeight){
                change = true;
            }
            videoWidth = width;
            videoHeight = height;
        }
        if (change){
            for (VideoSizeChangeListener videoSizeChangedListener : videoSizeChangedListeners) {
                videoSizeChangedListener.onVideoSizeChange(videoWidth, videoHeight);
            }
        }
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
}
