package com.jonanorman.android.hdrsample.player;

import android.os.Handler;

import com.jonanorman.android.hdrsample.player.source.FileSource;

public interface Player {

    void setSource(FileSource fileSource);

    void prepare();

    void start();

    void seek(float timeSecond);


    void resume();

    void pause();

    void stop();

    void release();

    boolean isPlaying();

    boolean isPause();

    boolean isStop();

    boolean isRelease();

    boolean isPrepared();

    float getCurrentTime();

    void setCallback(Callback callback);

    void setCallback(Callback callback, Handler handler);

    void postFrame(Runnable runnable);

    interface Callback {

        default void onPlayPrepare() {

        }

        default void onPlayStart() {

        }

        default void onPlayPause() {

        }

        default void onPlayResume() {

        }

        default void onPlayStop() {

        }

        default void onPlayProcess(float timeSecond, boolean end) {

        }

        default void onPlayError(Throwable throwable) {

        }
    }


}
