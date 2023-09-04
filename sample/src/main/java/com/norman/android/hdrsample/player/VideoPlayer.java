package com.norman.android.hdrsample.player;

/**
 * 视频播放器
 */
public interface VideoPlayer extends Player {

    static VideoPlayer create(){
        return new VideoPlayerImpl();
    }

    void setVideoOutput(VideoOutput videoOutput);

    VideoOutput getVideoOutput();

}
