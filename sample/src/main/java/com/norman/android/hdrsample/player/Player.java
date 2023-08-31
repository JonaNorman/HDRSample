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

    /**
     * 准备，新建线程配置解码器
     */
    void prepare();

    /**
     * 开始播放，没有prepare也会自动prepare后启动播放器，如果暂停会自动resume
     */

    void play();


    /**
     * seek时间
     * @param timeSecond
     */
    void seek(float timeSecond);

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止播放，要重新启动，需要调用prepare
     */

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
        /**
         * 播放中的回调
         * @param timeSecond 秒
         */

         void onPlayProcess(float timeSecond);

        /**
         * 整个文件播放完成回调
         */
        void onPlayEnd();

         void onPlayError(Exception exception);
    }


}
