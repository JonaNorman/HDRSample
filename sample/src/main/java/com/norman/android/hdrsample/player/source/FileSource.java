package com.norman.android.hdrsample.player.source;

import java.io.FileDescriptor;

public interface FileSource {

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
