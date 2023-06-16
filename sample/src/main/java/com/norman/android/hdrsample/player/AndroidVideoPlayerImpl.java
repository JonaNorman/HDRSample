package com.norman.android.hdrsample.player;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.norman.android.hdrsample.player.decode.AndroidDecoder;
import com.norman.android.hdrsample.player.dumex.AndroidDemuxer;
import com.norman.android.hdrsample.player.dumex.AndroidVideoDemuxer;
import com.norman.android.hdrsample.util.MediaFormatUtil;

abstract class AndroidVideoPlayerImpl extends AndroidPlayerImpl implements AndroidVideoPlayer {

    public AndroidVideoPlayerImpl(AndroidDecoder decoder, String threadName) {
        super(decoder, AndroidVideoDemuxer.create(), threadName);
    }

    @Override
    protected void onInputFormatPrepare(AndroidDemuxer demuxer, MediaFormat inputFormat) {
        AndroidVideoDemuxer videoDemuxer = (AndroidVideoDemuxer) demuxer;
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_WIDTH, videoDemuxer.getWidth());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_HEIGHT, videoDemuxer.getHeight());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_STANDARD, videoDemuxer.getColorStandard());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_RANGE, videoDemuxer.getColorRange());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_TRANSFER, videoDemuxer.getColorTransfer());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
    }

    @Override
    protected void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat) {
        decoder.configure(new AndroidDecoder.Configuration(inputFormat, new VideoDecoderCallBack()));
    }
}
