package com.norman.android.hdrsample.player;

/**
 * 直接输出到Android的Surface上
 */
public abstract class DirectVideoOutput extends VideoOutput {
    public static DirectVideoOutput create() {
        return new DirectVideoOutputImpl();
    }
}
