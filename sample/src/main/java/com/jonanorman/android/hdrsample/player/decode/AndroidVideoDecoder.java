package com.jonanorman.android.hdrsample.player.decode;

import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.NonNull;

public interface AndroidVideoDecoder extends AndroidDecoder {

    static AndroidVideoDecoder createVideoDecoder() {
        return new AndroidVideoDecoderImpl();
    }


    class Configuration extends AndroidDecoder.Configuration {
        public Configuration(@NonNull MediaFormat mediaFormat, @NonNull AndroidDecoder.CallBack callBack) {
            super(mediaFormat, callBack);
        }
    }


}
