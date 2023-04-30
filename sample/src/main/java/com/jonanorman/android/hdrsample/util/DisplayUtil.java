package com.jonanorman.android.hdrsample.util;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaCodecInfo;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class DisplayUtil {

    private static Float MAX_SCREEN_LUMINANCE = null;

    private static Integer MAX_SCREEN_BRIGHTNESS = null;

    public static synchronized float getMaxLuminance() {
        if (MAX_SCREEN_LUMINANCE == null) {
            float maxScreenLuminance = 0;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Context context = AppUtil.getAppContext();
                    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    Display display = windowManager.getDefaultDisplay();
                    Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();
                    maxScreenLuminance = Math.max(hdrCapabilities.getDesiredMaxAverageLuminance(), maxScreenLuminance);
                    maxScreenLuminance = Math.max(hdrCapabilities.getDesiredMaxLuminance(), maxScreenLuminance);
                }
            } catch (Exception e) {

            }
            if (maxScreenLuminance <= 0) {
                return 100;
            }
            MAX_SCREEN_LUMINANCE = maxScreenLuminance;
        }
        return MAX_SCREEN_LUMINANCE;
    }

    public static int getMaxBrightness() {
        if (MAX_SCREEN_BRIGHTNESS == null) {
            int maxBrightness = 0;
            try {
                Context context = AppUtil.getAppContext();
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                Field[] fields = powerManager.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().equals("BRIGHTNESS_ON")) {
                        field.setAccessible(true);
                        maxBrightness = (int) field.get(powerManager);
                        break;
                    }
                }
            } catch (Exception e) {

            }
            try {
                if (maxBrightness <= 0) {
                    Resources system = Resources.getSystem();
                    int resId = system.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");
                    maxBrightness = system.getInteger(resId);
                }
            } catch (Exception e) {

            }
            if (maxBrightness <= 0) {
                maxBrightness = 255;
            }
            MAX_SCREEN_BRIGHTNESS = maxBrightness;
        }
        return MAX_SCREEN_BRIGHTNESS;
    }

    public static int getCurrentBrightness() {
        try {
            Context context = AppUtil.getAppContext();
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            return getMaxBrightness();
        }
    }

    private boolean isHdrProfile(int profile) {
// TODO: 2023/4/30
//        hdrCapabilities.getSupportedHdrTypes();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    val configuration = resources.configuration
//                    return configuration.isScreenHdr
//                }
        return profile == MediaCodecInfo.CodecProfileLevel.AVCProfileHigh10 ||
                profile == MediaCodecInfo.CodecProfileLevel.AV1ProfileMain10 ||
                profile == MediaCodecInfo.CodecProfileLevel.AV1ProfileMain10HDR10 ||
                profile == MediaCodecInfo.CodecProfileLevel.AV1ProfileMain10HDR10Plus ||
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10 ||
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10 ||
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10Plus;

    }
}
