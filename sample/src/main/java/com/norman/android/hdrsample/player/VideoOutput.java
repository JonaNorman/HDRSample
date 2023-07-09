package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;

import java.nio.ByteBuffer;

public abstract class VideoOutput {

    private boolean prepare;

    private boolean release;


    synchronized void prepare() {
        if (prepare) return;
        prepare = true;
        onPrepare();
    }


    synchronized void release() {
        if (release) return;
        release = true;
        if (prepare) {
            onRelease();
        }
    }




    public abstract void setOutputSurface(Surface surface);

    protected  abstract void onPrepare();

    protected  abstract void onRelease();

    protected abstract void onDecoderPrepare(VideoDecoder decoder, MediaFormat inputFormat);

    protected abstract void onDecoderStop();

    protected  void onOutputFormatChanged(MediaFormat outputFormat){

    }


    protected boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer) {
        return true;
    }

    protected void onOutputBufferRelease(float timeSecond, boolean render) {

    }


}
