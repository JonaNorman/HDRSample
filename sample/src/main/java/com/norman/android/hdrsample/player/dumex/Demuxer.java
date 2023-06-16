package com.norman.android.hdrsample.player.dumex;


import com.norman.android.hdrsample.player.source.FileSource;

public interface Demuxer {

    void setSource(FileSource fileSource);

    boolean isAvailable();

    boolean isRelease();

    void release();
}
