package com.norman.android.hdrsample.player;

import android.os.Handler;

import com.norman.android.hdrsample.player.source.FileSource;

public interface Player {

    void setSource(FileSource fileSource);

    void prepare();

    void play();


    void seek(float timeSecond);

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

    void waitForNextFrame();

    void waitForNextFrame(float second);



    void setRepeat(boolean repeat);

    interface Callback {

         void onPlayProcess(float timeSecond);

         void onPlayEnd();

         void onPlayError(Exception exception);
    }


}
