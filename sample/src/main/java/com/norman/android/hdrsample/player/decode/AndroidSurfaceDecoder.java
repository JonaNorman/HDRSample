package com.norman.android.hdrsample.player.decode;

import android.view.Surface;

public interface AndroidSurfaceDecoder extends AndroidVideoDecoder {


    static AndroidSurfaceDecoder create() {
        return new AndroidSurfaceDecoderImpl();
    }

    void setOutputSurface(Surface surface);

    Surface getOutputSurface();

}
