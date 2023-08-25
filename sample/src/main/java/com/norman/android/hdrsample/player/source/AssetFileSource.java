package com.norman.android.hdrsample.player.source;

import android.content.res.AssetFileDescriptor;

import com.norman.android.hdrsample.util.FileUtil;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * asset文件
 */
public class AssetFileSource implements FileSource {

    final String assetPath;

    AssetFileSource(String assetPath) {
        this.assetPath = assetPath;
    }

    public static AssetFileSource create(String assetPath) {
        return new AssetFileSource(assetPath);
    }


    @Override
    public Descriptor createFileDescriptor() {
        AssetFileDescriptor assetFileDescriptor = FileUtil.openAssetFileDescriptor(assetPath);
        return new AssetFileSourceDescriptor(assetFileDescriptor);
    }

    @Override
    public String getPath() {
        return assetPath;
    }


    static class AssetFileSourceDescriptor implements Descriptor {

        AssetFileDescriptor assetFileDescriptor;
        boolean closed;
        final Object lock = new Object();

        public AssetFileSourceDescriptor(AssetFileDescriptor assetFileDescriptor) {
            this.assetFileDescriptor = assetFileDescriptor;
        }

        @Override
        public FileDescriptor getFileDescriptor() {
            return assetFileDescriptor.getFileDescriptor();
        }

        @Override
        public long getLength() {
            return assetFileDescriptor.getLength();
        }

        @Override
        public long getStartOffset() {
            return assetFileDescriptor.getStartOffset();
        }

        @Override
        public void close() {
            synchronized (lock) {
                if (closed) {
                    return;
                }
                try {
                    assetFileDescriptor.close();
                } catch (IOException ignored) {

                }
                closed = true;
            }
        }

        @Override
        public boolean isClose() {
            synchronized (lock) {
                return closed;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }
    }


}
