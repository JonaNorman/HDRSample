package com.jonanorman.android.hdrsample.player.source;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.jonanorman.android.hdrsample.util.ThrowableUtil;

import java.io.FileDescriptor;
import java.io.IOException;

class AssetFileSource implements FileSource {

    final String assetPath;
    final Context context;

    public AssetFileSource(Context context, String assetPath) {
        this.context = context;
        this.assetPath = assetPath;
    }


    @Override
    public Descriptor createFileDescriptor() {
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            AssetManager assetManager = context.getAssets();
            assetFileDescriptor = assetManager.openFd(assetPath);
            return new AssetFileSourceDescriptor(assetFileDescriptor);
        } catch (IOException e) {
            try {
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
            } catch (IOException ex) {
            }
            ThrowableUtil.throwRuntimeException(e);
            return null;
        }
    }

    @Override
    public String getPath() {
        return assetPath;
    }


    class AssetFileSourceDescriptor implements Descriptor {

        AssetFileDescriptor assetFileDescriptor;
        boolean closed;
        Object lock = new Object();

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
