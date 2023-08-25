package com.norman.android.hdrsample.player;

/**
 * 直接输出到Android的Surface上
 */
public abstract class AndroidVideoOutput extends VideoOutput {
    public static AndroidVideoOutput create() {
        return new AndroidVideoOutputImpl();
    }
}
