package com.norman.android.hdrsample.player;

/**
 * 视频播放器
 */
public interface VideoPlayer extends Player {

    static VideoPlayer create(VideoOutput videoOutput){
        return new VideoPlayerImpl(videoOutput);
    }


    /**
     * 视频播放数据输出到的地方
     * @return
     */
    VideoOutput getOutput();

}
