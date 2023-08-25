package com.norman.android.hdrsample.player.source;

import java.io.FileDescriptor;

/**
 * 文件
 */
public interface FileSource {

    /**
     * 创建文件描述符
     * @return
     */
    Descriptor createFileDescriptor();

    String getPath();


    interface Descriptor {
        FileDescriptor getFileDescriptor();

        long getLength();

        long getStartOffset();

        /**
         * 不用了要关闭
         */
        void close();

        boolean isClose();
    }

}
