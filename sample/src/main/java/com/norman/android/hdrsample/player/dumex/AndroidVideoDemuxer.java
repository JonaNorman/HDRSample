package com.norman.android.hdrsample.player.dumex;

public interface AndroidVideoDemuxer extends AndroidDemuxer {

    static AndroidVideoDemuxer create() {
        return new AndroidVideoDemuxerImpl();
    }

    int getWidth();

    int getHeight();

    int getFrameRate();

    int getColorStandard();

    int getColorRange();

    int getColorTransfer();
}
