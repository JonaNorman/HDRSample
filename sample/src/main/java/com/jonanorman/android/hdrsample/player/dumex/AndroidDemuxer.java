package com.jonanorman.android.hdrsample.player.dumex;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public interface AndroidDemuxer extends Demuxer {

    void seekToPreviousSync(long timeUs);

    void seekToClosestSync(long timeUs);

    void seekToNextSync(long timeUs);

    void readSampleBuffer(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo);

    void readSampleBuffer(ByteBuffer buffer, int offset, MediaCodec.BufferInfo bufferInfo);

    boolean advance();
    String getMimeType();
    long getDurationUs();

    int getMaxInputSize();

    int getProfile();

    int getProfileLevel();

    ByteBuffer getCsd0Buffer();

    ByteBuffer getCsd1Buffer();
}
