package com.jonanorman.android.hdrsample.player;

public interface AndroidSurfacePlayer extends AndroidVideoPlayer {

    static AndroidSurfacePlayer createSurfacePlayer(){
        return new AndroidSurfacePlayerImpl();
    }

    static AndroidSurfacePlayer createSurfacePlayer(String threadName){
        return new AndroidSurfacePlayerImpl(threadName);
    }


}
