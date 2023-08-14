package com.norman.android.hdrsample.player;

public interface VideoPlayer extends Player {

    static VideoPlayer create(VideoOutput videoOutput){
        return new VideoPlayerImplImpl(videoOutput);
    }


    VideoOutput getOutput();

}
