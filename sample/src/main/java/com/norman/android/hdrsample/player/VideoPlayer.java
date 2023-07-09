package com.norman.android.hdrsample.player;

import android.view.Surface;

public interface VideoPlayer extends Player {

    static VideoPlayer create(VideoOutput videoOutput){
        return new VideoPlayerImpl(videoOutput);
    }


    VideoOutput getOutput();


    void  setOutputSurface(Surface surface);

    int getWidth();


    int getHeight();


    void addSizeChangeListener(VideoSizeChangeListener changeListener);

    void removeSizeChangeListener(VideoSizeChangeListener changeListener);


    interface VideoSizeChangeListener {
        void onVideoSizeChange(int width, int height);
    }

}
