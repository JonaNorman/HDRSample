package com.jonanorman.android.hdrsample.player.decode;

public interface Decoder {

    void configure(Configuration configuration);

    void start();

    void pause();

    void resume();

    void flush();

    void stop();

    void release();

    boolean isStarted();

    boolean isConfigured();

    boolean isRunning();

    boolean isRelease();

    boolean isPause();

    interface Configuration {

    }

}
