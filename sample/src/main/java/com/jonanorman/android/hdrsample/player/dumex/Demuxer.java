package com.jonanorman.android.hdrsample.player.dumex;


import android.media.MediaCodecInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jonanorman.android.hdrsample.player.source.FileSource;

import java.nio.ByteBuffer;

public interface Demuxer {

    void setSource(FileSource fileSource);

    boolean isAvailable();

    boolean isRelease();

    void release();
}
