package com.norman.android.hdrsample.player;

import android.view.Surface;

public interface VideoPlayer extends Player {
    void setSurface(Surface surface);

    int getWidth();


    int getHeight();


    void addSizeChangeListener(VideoSizeChangeListener changeListener);

    void removeSizeChangeListener(VideoSizeChangeListener changeListener);


    interface VideoSizeChangeListener {
        void onVideoSizeChange(int width, int height);
    }

}
