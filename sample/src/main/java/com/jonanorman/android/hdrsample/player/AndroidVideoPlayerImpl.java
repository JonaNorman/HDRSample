package com.jonanorman.android.hdrsample.player;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.decode.AndroidDecoder;
import com.jonanorman.android.hdrsample.player.decode.AndroidVideoDecoder;
import com.jonanorman.android.hdrsample.player.dumex.AndroidVideoDemuxer;
import com.jonanorman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;

abstract class AndroidVideoPlayerImpl extends AndroidPlayerImpl implements AndroidVideoPlayer {

    public AndroidVideoPlayerImpl(AndroidDecoder decoder, String threadName) {
        super(decoder, AndroidVideoDemuxer.createVideoDemuxer(), threadName);
    }

    protected final void onInputFormatConfigure(MediaFormat inputFormat) {
        onVideoInputFormatConfigure(inputFormat);
        onVideoDecoderConfigure(inputFormat);
    }

    protected void onVideoInputFormatConfigure(MediaFormat inputFormat) {
        AndroidVideoDemuxer videoDemuxer = (AndroidVideoDemuxer) androidDemuxer;
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_WIDTH, videoDemuxer.getWidth());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_HEIGHT, videoDemuxer.getHeight());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_STANDARD, videoDemuxer.getColorStandard());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_RANGE, videoDemuxer.getColorRange());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_TRANSFER, videoDemuxer.getColorTransfer());
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
    }

    protected void onVideoDecoderConfigure(MediaFormat inputFormat) {
        androidDecoder.configure(new AndroidDecoder.Configuration(inputFormat, new VideoDecoderCallBack()));
    }


}
