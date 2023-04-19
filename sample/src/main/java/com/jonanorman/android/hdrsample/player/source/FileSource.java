package com.jonanorman.android.hdrsample.player.source;

import android.content.Context;

import java.io.FileDescriptor;

public interface FileSource {

    static FileSource createAssetFileSource(Context context, String assetPath) {
        return new AssetFileSource(context, assetPath);
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
