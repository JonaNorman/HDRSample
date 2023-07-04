package com.norman.android.hdrsample.player.source;

import android.content.res.AssetFileDescriptor;

import com.norman.android.hdrsample.util.FileUtil;

import java.io.FileDescriptor;
import java.io.IOException;

public class AssetFileSource implements FileSource {

    final String assetPath;
    public AssetFileSource(String assetPath) {
        this.assetPath = assetPath;
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


    class AssetFileSourceDescriptor implements Descriptor {

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
                } catch (IOException e) {

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
