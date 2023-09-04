package com.norman.android.hdrsample.player;

/**
 * 直接输出到Android的Surface上
 */
public abstract class DirectVideoOutput extends VideoOutput {
    /**
     * 直接用解码到Surface
     *
     * @return
     */
    public static DirectVideoOutput create() {
        return new DirectVideoOutputImpl();
    }
}
