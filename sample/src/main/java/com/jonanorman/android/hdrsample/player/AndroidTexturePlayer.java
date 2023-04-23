package com.jonanorman.android.hdrsample.player;

public interface AndroidTexturePlayer extends AndroidVideoPlayer {


    static AndroidTexturePlayer createTexturePlayer(){
        return new AndroidTexturePlayerImpl();
    }

    static AndroidTexturePlayer createTexturePlayer(String threadName){
        return new AndroidTexturePlayerImpl(threadName);
    }

}
