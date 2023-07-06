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
    protected void onInputFormatPrepare(AndroidExtractor demuxer, MediaFormat inputFormat) {
        AndroidVideoExtractor videoDemuxer = (AndroidVideoExtractor) demuxer;
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_WIDTH, videoDemuxer.getWidth());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_HEIGHT, videoDemuxer.getHeight());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_STANDARD, videoDemuxer.getColorStandard());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_RANGE, videoDemuxer.getColorRange());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_TRANSFER, videoDemuxer.getColorTransfer());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        for (VideoSizeChangeListener videoSizeChangedListener : videoSizeChangedListeners) {
            videoSizeChangedListener.onVideoSizeChange(videoDemuxer.getWidth(), videoDemuxer.getHeight());
        }
    }

    @Override
    protected void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat) {
        decoder.configure(new AndroidDecoder.Configuration(inputFormat, new VideoDecoderCallBack()));
    }


    @Override
    public int getWidth() {
        AndroidVideoExtractor videoDemuxer = (AndroidVideoExtractor) getAndroidDemuxer();
        return videoDemuxer.getWidth();
    }

    @Override
    public int getHeight() {
        AndroidVideoExtractor videoDemuxer = (AndroidVideoExtractor) getAndroidDemuxer();
        return videoDemuxer.getHeight();
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
