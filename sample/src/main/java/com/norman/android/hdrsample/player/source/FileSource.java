package com.norman.android.hdrsample.player.source;

import android.content.Context;

import java.io.FileDescriptor;

public interface FileSource {

    static FileSource createForAsset(String assetPath) {
        return new AssetFileSource(assetPath);
    }

    Descriptor createFileDescriptor();

    String getPath();


    interface Descriptor {
        FileDescriptor getFileDescriptor();

        long getLength();

        long getStartOffset();

        void close();

        boolean isClose();
    }

}
