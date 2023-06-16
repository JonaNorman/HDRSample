package com.norman.android.hdrsample.player;

public interface AndroidSurfacePlayer extends AndroidVideoPlayer {

    static AndroidSurfacePlayer create(){
        return new AndroidSurfacePlayerImpl();
    }

    static AndroidSurfacePlayer create(String threadName){
        return new AndroidSurfacePlayerImpl(threadName);
    }


}
