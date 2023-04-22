package com.jonanorman.android.hdrsample.player;

public interface AndroidVideoSurfacePlayer extends AndroidVideoPlayer {

    static AndroidVideoSurfacePlayer createSurfacePlayer(){
        return new AndroidSurfacePlayerImpl();
    }

    static AndroidVideoSurfacePlayer createSurfacePlayer(String threadName){
        return new AndroidSurfacePlayerImpl(threadName);
    }


}
