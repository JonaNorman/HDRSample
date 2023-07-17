package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class VideoOutput {

    private boolean prepare;

    private boolean release;

    private int width;

    private int height;

    private int colorStandard;
    private int colorRange;
    private int colorTransfer;

    private final List<OnOutputFormatChangeCallback>  outputFormatChangeCallbackList = new CopyOnWriteArrayList<>();


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
        colorStandard = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_COLOR_STANDARD, MediaFormat.COLOR_STANDARD_BT709);
        colorRange = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_LIMITED);
        colorTransfer = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_COLOR_TRANSFER, MediaFormat.COLOR_TRANSFER_SDR_VIDEO);
        for (OnOutputFormatChangeCallback formatChangeCallback : outputFormatChangeCallbackList) {
            formatChangeCallback.onOutputFormatChange(this,outputFormat);
        }

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

    public int getColorStandard() {
        return colorStandard;
    }

    public int getColorRange() {
        return colorRange;
    }

    public int getColorTransfer() {
        return colorTransfer;
    }

    protected void onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {

    }


    protected void onOutputBufferRelease(long presentationTimeUs) {

    }

    public void addOnOutputFormatChangeCallback(OnOutputFormatChangeCallback  outputFormatChangeCallback){
        outputFormatChangeCallbackList.add(outputFormatChangeCallback);
    }

    public void removeOnOutputFormatChangeCallback(OnOutputFormatChangeCallback  outputFormatChangeCallback){
        outputFormatChangeCallbackList.remove(outputFormatChangeCallback);
    }


    public  interface  OnOutputFormatChangeCallback{
        void onOutputFormatChange(VideoOutput videoOutput,MediaFormat outputFormat);
    }
}
