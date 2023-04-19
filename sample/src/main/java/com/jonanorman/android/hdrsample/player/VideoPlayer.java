package com.jonanorman.android.hdrsample.player;


public interface VideoPlayer extends Player {
    static VideoPlayer createAndroidVideoPlayer(){
        return new AndroidVideoPlayerImpl();
    }

}
