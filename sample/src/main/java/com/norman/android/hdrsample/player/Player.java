package com.norman.android.hdrsample.player;

import android.os.Handler;

import com.norman.android.hdrsample.player.source.FileSource;

/**
 * 播放器
 */
public interface Player {

    /**
     * 设置播放文件，只有prepare之前或者stop后才能设置
     * @param fileSource
     */
    void setSource(FileSource fileSource);

    void prepare();

    void play();


    /**
     * seek时间
     * @param timeSecond
     */
    void seek(float timeSecond);

    void pause();

    void stop();

    void release();

    boolean isPlaying();

    boolean isPause();

    boolean isStop();

    boolean isRelease();

    /**
     * 已经准备好了
     * @return
     */

    boolean isPrepared();

    /**
     * 当前播放到的时间，单位s
     * @return
     */
    float getCurrentTime();

    void setCallback(Callback callback);

    void setCallback(Callback callback, Handler handler);

    /**
     * 是否重复播放
     * @param repeat
     */

    void setRepeat(boolean repeat);

    interface Callback {

         void onPlayProcess(float timeSecond);

         void onPlayEnd();

         void onPlayError(Exception exception);
    }


}
