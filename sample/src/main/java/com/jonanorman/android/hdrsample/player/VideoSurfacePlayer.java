package com.jonanorman.android.hdrsample.player;

import android.view.Surface;

public interface VideoSurfacePlayer extends VideoPlayer {

    static VideoSurfacePlayer createAndroidVideoPlayer(){
        return new AndroidSurfacePlayerImpl();
    }
    void setSurface(Surface surface);
}
