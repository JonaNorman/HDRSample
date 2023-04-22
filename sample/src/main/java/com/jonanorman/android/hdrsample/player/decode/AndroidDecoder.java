package com.jonanorman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public interface AndroidDecoder extends Decoder {


    class Configuration implements Decoder.Configuration {
        public final MediaFormat mediaFormat;
        public final CallBack callBack;

        public Configuration(@NonNull MediaFormat mediaFormat, @NonNull CallBack callBack) {
            this.mediaFormat = mediaFormat;
            this.callBack = callBack;
        }

    }

    interface CallBack {

        MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer);

        boolean onOutputBufferAvailable(long presentationTimeUs, ByteBuffer outputBuffer);

        void onOutputBufferRelease(long presentationTimeUs, boolean render);

        void onOutputBufferEndOfStream();

        void onOutputFormatChanged(MediaFormat format);

        void onError(Exception exception);

    }


}
