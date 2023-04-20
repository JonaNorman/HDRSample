package com.jonanorman.android.hdrsample.player;


public interface AndroidVideoPlayer extends AndroidPlayer {
    static AndroidVideoPlayer createVideoPlayer() {
        return new AndroidVideoPlayerImpl();
    }

}
