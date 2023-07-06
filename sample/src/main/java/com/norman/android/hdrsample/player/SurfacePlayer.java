package com.norman.android.hdrsample.player;

public interface SurfacePlayer extends VideoPlayer {

    static SurfacePlayer create(){
        return new AndroidSurfacePlayerImpl();
    }

    static SurfacePlayer create(String threadName){
        return new AndroidSurfacePlayerImpl(threadName);
    }


}
