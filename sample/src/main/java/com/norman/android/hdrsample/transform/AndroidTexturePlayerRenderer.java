package com.norman.android.hdrsample.transform;

import android.media.MediaFormat;

import com.norman.android.hdrsample.util.DisplayUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AndroidTexturePlayerRenderer {
    private float contentLuminance;
    private float screenLuminance;

    boolean keepBrightnessOnHDR;


    public AndroidTexturePlayerRenderer() {


        screenLuminance = DisplayUtil.getMaxLuminance();

    }

    public synchronized void setKeepBrightnessOnHDR(boolean keepBrightnessOnHDR) {
        ScreenBrightnessObserver screenBrightnessObserver = new ScreenBrightnessObserver();

        this.keepBrightnessOnHDR = keepBrightnessOnHDR;
        if (keepBrightnessOnHDR) {
            screenBrightnessObserver.listen();
        } else {
            screenBrightnessObserver.unListen();
        }
        float brightness = 1;
        if (keepBrightnessOnHDR) {
            ScreenBrightnessObserver.BrightnessInfo brightnessInfo = screenBrightnessObserver.getBrightnessInfo();
            brightness = brightnessInfo.brightnessFloat;
        }

    }

    protected void onOutputFormatChanged(MediaFormat outputFormat) {


    }

}
