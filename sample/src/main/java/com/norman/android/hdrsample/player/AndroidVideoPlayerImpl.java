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

    private List<VideoSizeChangeListener> videoSizeChangedListeners = new CopyOnWriteArrayList<>();

    public AndroidVideoPlayerImpl(AndroidDecoder decoder, String threadName) {
        super(decoder, AndroidVideoExtractor.create(), threadName);
    }

    @Override
    protected void onInputFormatPrepare(AndroidExtractor extractor, MediaFormat inputFormat) {
        AndroidVideoExtractor videoExtractor = (AndroidVideoExtractor) extractor;
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_WIDTH, videoExtractor.getWidth());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_HEIGHT, videoExtractor.getHeight());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_STANDARD, videoExtractor.getColorStandard());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_RANGE, videoExtractor.getColorRange());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_TRANSFER, videoExtractor.getColorTransfer());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        for (VideoSizeChangeListener videoSizeChangedListener : videoSizeChangedListeners) {
            videoSizeChangedListener.onVideoSizeChange(videoExtractor.getWidth(), videoExtractor.getHeight());
        }
    }

    @Override
    protected void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat) {
        decoder.configure(new AndroidDecoder.Configuration(inputFormat, new VideoDecoderCallBack()));
    }


    @Override
    public int getWidth() {
        AndroidVideoExtractor videoExtractor = (AndroidVideoExtractor) getAndroidExtractor();
        return videoExtractor.getWidth();
    }

    @Override
    public int getHeight() {
        AndroidVideoExtractor videoExtractor = (AndroidVideoExtractor) getAndroidExtractor();
        return videoExtractor.getHeight();
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
