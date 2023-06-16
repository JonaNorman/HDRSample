package com.norman.android.hdrsample.player.decode;

public interface AndroidVideoDecoder extends AndroidDecoder {

    static AndroidVideoDecoder create() {
        return new AndroidVideoDecoderImpl();
    }


}
