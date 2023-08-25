package com.norman.android.hdrsample.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * 获取屏幕信息，可以用来获取是否是HDR屏幕、屏幕亮度
 */
public class DisplayUtil {

    private static final String TAG = "DisplayUtil";

    /**
     * 屏幕最大亮度，实际物理亮度
     */
    private static Float MAX_SCREEN_LUMINANCE = null;
    /**
     * 屏幕最大明度指的是设置里面亮度的最大值，一般是100或者255
     */
    private static Integer MAX_SCREEN_BRIGHTNESS = null;

    /**
     * 默认屏幕最大亮度
     */
    private static float DEFAULT_MAX_SCREEN_LUMINANCE = 100;

    /**
     * 默认屏幕最大明度
     */
    private static int DEFAULT_MAX_SCREEN_BRIGHTNESS = 255;

    /**
     * 屏幕是否支持DOLBY_VISION
     */
    private static boolean HDR_CAPABILITY_DOLBY_VISION = false;
    /**
     * 屏幕是否支持HDR10
     */
    private static boolean HDR_CAPABILITY_HDR10 = false;

    /**
     * 屏幕是否支持HLG
     */
    private static boolean HDR_CAPABILITY_HLG = false;

    /**
     * 屏幕是否支持HDR10+
     */
    private static boolean HDR_CAPABILITY_HDR10_PLUS = false;

    /**
     * 屏幕是否支持HDR
     */
    private static Boolean HDR_CAPABILITY_SCREEN = null;

    /**
     * 最大屏幕亮度
     * @return
     */
    public static synchronized float getMaxLuminance() {
        if (MAX_SCREEN_LUMINANCE == null) {
            float maxScreenLuminance = 0;
            Context context = AppUtil.getAppContext();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();
            float maxDesiredAverageLuminance = hdrCapabilities.getDesiredMaxAverageLuminance();
            float maxDesiredLuminance = hdrCapabilities.getDesiredMaxLuminance();
            LogUtil.v(TAG,"screen getDesiredMaxAverageLuminance: "+maxDesiredAverageLuminance);
            LogUtil.v(TAG,"screen getDesiredMaxLuminance: "+maxDesiredLuminance);
            maxScreenLuminance = Math.max(maxDesiredAverageLuminance, maxScreenLuminance);
            maxScreenLuminance = Math.max(maxDesiredLuminance, maxScreenLuminance);
            if (maxScreenLuminance <= 0) {
                MAX_SCREEN_LUMINANCE = DEFAULT_MAX_SCREEN_LUMINANCE;
            } else {
                MAX_SCREEN_LUMINANCE = maxScreenLuminance;
            }
            LogUtil.v(TAG,"MAX_SCREEN_LUMINANCE: "+MAX_SCREEN_LUMINANCE);
        }
        return MAX_SCREEN_LUMINANCE;
    }

    /***
     * 最大明度值，指的是设置里面亮度的最大值
     * @return
     */
    public static synchronized int getMaxBrightness() {
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
                MAX_SCREEN_BRIGHTNESS = DEFAULT_MAX_SCREEN_BRIGHTNESS;
            } else {
                MAX_SCREEN_BRIGHTNESS = maxBrightness;
            }
            LogUtil.v(TAG,"MAX_SCREEN_BRIGHTNESS: "+MAX_SCREEN_BRIGHTNESS);
        }
        return MAX_SCREEN_BRIGHTNESS;
    }

    /***
     * 当前明度，指的是设置里面的亮度
     * @return
     */
    public static int getBrightness() {
        try {
            Context context = AppUtil.getAppContext();
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            return getMaxBrightness();
        }
    }

    /**
     * 屏幕是否支持DolbyVision
     * @return
     */

    public static boolean isSupportDolbyVision() {
        loadHdrCapability();
        return HDR_CAPABILITY_DOLBY_VISION;
    }

    /**
     * 屏幕是否支持HLG
     * @return
     */
    public static boolean isSupportHlg() {
        loadHdrCapability();
        return HDR_CAPABILITY_HLG;
    }

    /**
     * 屏幕是否支持HDR10
     * @return
     */

    public static boolean isSupportHdr10() {
        loadHdrCapability();
        return HDR_CAPABILITY_HDR10;
    }

    /**
     * 屏幕是否支持HDR10+
     * @return
     */
    public static boolean isSupportHdr10Plus() {
        loadHdrCapability();
        return HDR_CAPABILITY_HDR10_PLUS;
    }

    /**
     * 屏幕是否支持HDR
     * @return
     */
    public static boolean isSupportHdr() {
        loadHdrCapability();
        return HDR_CAPABILITY_SCREEN;
    }


    private static synchronized void loadHdrCapability() {
        if (HDR_CAPABILITY_SCREEN != null) {
            return;
        }
        Context context = AppUtil.getAppContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();
        int[] hdrTypes = hdrCapabilities.getSupportedHdrTypes();
        if (hdrTypes != null && hdrTypes.length > 0) {
            for (int hdrType : hdrTypes) {
                if (hdrType == Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION) {
                    HDR_CAPABILITY_DOLBY_VISION = true;
                } else if (hdrType == Display.HdrCapabilities.HDR_TYPE_HLG) {
                    HDR_CAPABILITY_HLG = true;
                } else if (hdrType == Display.HdrCapabilities.HDR_TYPE_HDR10) {
                    HDR_CAPABILITY_HDR10 = true;
                } else if (hdrType == Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS) {
                    HDR_CAPABILITY_HDR10_PLUS = true;
                }
            }
            HDR_CAPABILITY_SCREEN = true;
        }
        if (HDR_CAPABILITY_SCREEN == null) {
            HDR_CAPABILITY_SCREEN = false;
        }
        LogUtil.v(TAG,"HDR_CAPABILITY_DOLBY_VISION: "+HDR_CAPABILITY_DOLBY_VISION);
        LogUtil.v(TAG,"HDR_CAPABILITY_HLG: "+HDR_CAPABILITY_HLG);
        LogUtil.v(TAG,"HDR_CAPABILITY_HDR10: "+HDR_CAPABILITY_HDR10);
        LogUtil.v(TAG,"HDR_CAPABILITY_HDR10_PLUS: "+HDR_CAPABILITY_HDR10_PLUS);
        LogUtil.v(TAG,"HDR_CAPABILITY_SCREEN: "+HDR_CAPABILITY_SCREEN);
    }
}
