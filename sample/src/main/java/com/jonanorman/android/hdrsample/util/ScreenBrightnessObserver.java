package com.jonanorman.android.hdrsample.util;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.math.MathUtils;


public class ScreenBrightnessObserver extends ContentObserver {

    private BrightnessChangeListener listener;
    private boolean hasRegister;

    private BrightnessInfo brightnessInfo;

    public ScreenBrightnessObserver() {
        super(new Handler(Looper.getMainLooper()));
    }

    public void setOnBrightnessChangeListener(BrightnessChangeListener listener) {
        this.listener = listener;
    }

    public synchronized void register() {
        if (hasRegister) {
            return;
        }
        Uri brightnessUri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        AppUtil.getAppContext().getContentResolver().registerContentObserver(brightnessUri,
                true, this);
        hasRegister = true;
    }

    public synchronized void unregister() {
        if (!hasRegister) {
            return;
        }
        AppUtil.getAppContext().getContentResolver().unregisterContentObserver(this);
        hasRegister = false;
    }

    @Override
    public void onChange(boolean selfChange) {
        loadBrightness();
        if (listener == null) return;
        listener.onChange(brightnessInfo);
    }


    public synchronized BrightnessInfo getBrightnessInfo() {
        if (brightnessInfo == null) {
            brightnessInfo = new BrightnessInfo();
            loadBrightness();
        }
        return brightnessInfo;
    }

    private synchronized void loadBrightness() {
        int brightness = DisplayUtil.getCurrentBrightness();
        int maxBrightness = DisplayUtil.getMaxBrightness();
        brightnessInfo.brightness = brightness;
        brightnessInfo.brightnessMax = maxBrightness;
        brightnessInfo.brightnessFloat = MathUtils.clamp(brightness * 1.0f / maxBrightness, 0.0f, 1.0f);

    }


    public interface BrightnessChangeListener {
        void onChange(BrightnessInfo brightness);
    }

    public static class BrightnessInfo {
       public int brightness = -1;
        public int brightnessMax = -1;
        public float brightnessFloat = -1;
    }

}

