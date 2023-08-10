package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public interface Decoder {


    void create(String mimeType);

    void configure(Decoder.Configuration configuration);

    void reset();

    void start();

    void pause();

    void resume();

    void flush();

    void destroy();

    void release();


    boolean isCreated();

    boolean isConfigured();

    boolean isStarted();

    boolean isRunning();

    boolean isRelease();

    boolean isPaused();


    class Configuration {
        public final MediaFormat mediaFormat;
        public final CallBack callBack;

        public Configuration(@NonNull MediaFormat mediaFormat, @NonNull CallBack callBack) {
            this.mediaFormat = mediaFormat;
            this.callBack = callBack;
        }

    }

    interface CallBack {

        MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer);

        boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs);

        void onOutputBufferRelease(long presentationTimeUs);

        void onOutputBufferEndOfStream();

        void onOutputFormatChanged(MediaFormat format);

        void onDecodeError(Exception exception);

    }


}
