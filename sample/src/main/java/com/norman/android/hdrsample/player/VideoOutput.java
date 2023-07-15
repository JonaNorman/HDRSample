package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;

import java.nio.ByteBuffer;

public abstract class VideoOutput {

    private boolean prepare;

    private boolean release;

    private int width;

    private int height;


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

    synchronized void stop() {
        if (release || !prepare) return;
        onDecoderStop();
    }







    public abstract void setOutputSurface(Surface surface);


    protected abstract void onDecoderPrepare(VideoDecoder decoder, MediaFormat inputFormat);

    protected abstract void onDecoderStop();


    protected   void onPrepare(){

    }

    protected   void onRelease(){

    }

    protected  void onOutputFormatChanged(MediaFormat outputFormat){

    }


    protected void onVideoSizeChange(int width,int height){
        this.width =width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    protected void onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {

    }


    protected void onOutputBufferRelease(long presentationTimeUs) {

    }


}
