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

        /**
         * 文件长度
         * @return
         */
        long getLength();

        /**
         * 文件开始点的偏移值
         * @return
         */
        long getStartOffset();

        /**
         * 不用了要关闭
         */
        void close();

        boolean isClose();
    }

}
