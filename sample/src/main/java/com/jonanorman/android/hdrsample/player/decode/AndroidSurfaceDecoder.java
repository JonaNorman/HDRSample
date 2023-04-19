package com.jonanorman.android.hdrsample.player.decode;

import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.NonNull;

public interface AndroidSurfaceDecoder extends AndroidVideoDecoder {


    static AndroidSurfaceDecoder createSurfaceDecoder() {
        return new AndroidSurfaceDecoderImpl();
    }

    void setOutputSurface(Surface surface);


    class Configuration extends AndroidVideoDecoder.Configuration {
        public final Surface surface;


        public Configuration(@NonNull MediaFormat mediaFormat, @NonNull AndroidDecoder.CallBack callBack, Surface surface) {
            super(mediaFormat, callBack);
            this.surface = surface;
        }
    }

}
