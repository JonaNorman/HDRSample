package com.jonanorman.android.hdrsample.player;

import android.os.Handler;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.source.FileSource;

public interface Player {

    void setSource(FileSource fileSource);

    void prepare();

    void start();

    void setSurface(Surface surface);

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

    void postFrame(FrameRunnable runnable);

    void waitFrame();

    interface Callback {


        default void onPlayProcess(float timeSecond) {

        }

        default void onPlayEnd() {


        }


        default void onPlayError(Throwable throwable) {

        }
    }

    interface FrameRunnable {
        void onFrameProcess(float timeSecond);

    }


}
