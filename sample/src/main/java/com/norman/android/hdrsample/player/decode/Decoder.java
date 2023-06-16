package com.norman.android.hdrsample.player.decode;

public interface Decoder {

    void configure(Configuration configuration);

    void start();

    void pause();

    void resume();

    void flush();

    void stop();

    void release();

    boolean isConfigured();

    boolean isStarted();

    boolean isRunning();

    boolean isRelease();

    boolean isPause();

    interface Configuration {

    }

}
