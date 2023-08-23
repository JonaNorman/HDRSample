package com.norman.android.hdrsample.transform;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.math.MathUtils;

import com.norman.android.hdrsample.util.AppUtil;
import com.norman.android.hdrsample.util.DisplayUtil;


class ScreenBrightnessObserver extends ContentObserver {

    private BrightnessChangeListener listener;
    private boolean hasListen;

    private BrightnessInfo brightnessInfo;

    public ScreenBrightnessObserver() {
        super(new Handler(Looper.getMainLooper()));
    }

    public void setOnBrightnessChangeListener(BrightnessChangeListener listener) {
        this.listener = listener;
    }

    public synchronized void listen() {
        if (hasListen) {
            return;
        }
        Uri brightnessUri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        AppUtil.getAppContext().getContentResolver().registerContentObserver(brightnessUri,
                true, this);
        hasListen = true;
    }

    public synchronized void unListen() {
        if (!hasListen) {
            return;
        }
        AppUtil.getAppContext().getContentResolver().unregisterContentObserver(this);
        hasListen = false;
    }

    public synchronized boolean isListen() {
        return hasListen;
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
        int maxBrightness = DisplayUtil.getMaxBrightness();
        int brightness = DisplayUtil.getBrightness();
        brightnessInfo.brightness = brightness;
        brightnessInfo.brightnessMax = maxBrightness;
        brightnessInfo.brightnessFloat = MathUtils.clamp(brightness * 1.0f / maxBrightness, 0.0f, 1.0f);

    }


    public interface BrightnessChangeListener {
        void onChange(BrightnessInfo brightness);
    }

    public static class BrightnessInfo {
       public int brightness;
        public int brightnessMax;
        public float brightnessFloat;
    }

}

