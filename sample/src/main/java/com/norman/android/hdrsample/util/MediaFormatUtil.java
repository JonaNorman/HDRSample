package com.norman.android.hdrsample.util;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.text.TextUtils;

import com.norman.android.hdrsample.player.color.ColorRange;
import com.norman.android.hdrsample.player.color.ColorStandard;
import com.norman.android.hdrsample.player.color.ColorTransfer;

import java.nio.ByteBuffer;

public class MediaFormatUtil {


    public static int getInteger(MediaFormat mediaFormat, String name) {
        return getInteger(mediaFormat, name, 0);
    }

    public static int getInteger(MediaFormat mediaFormat, String name, int defaultValue) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || !mediaFormat.containsKey(name)) {
            return defaultValue;
        }
        return mediaFormat.getInteger(name);
    }

    public static long getLong(MediaFormat mediaFormat, String name) {
        return getLong(mediaFormat, name, 0);
    }

    public static long getLong(MediaFormat mediaFormat, String name, long defaultValue) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || !mediaFormat.containsKey(name)) {
            return defaultValue;
        }
        return mediaFormat.getLong(name);
    }

    public static String getString(MediaFormat mediaFormat, String name) {
        return getString(mediaFormat, name, null);
    }

    public static String getString(MediaFormat mediaFormat, String name, String defaultValue) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || !mediaFormat.containsKey(name)) {
            return defaultValue;
        }
        return mediaFormat.getString(name);
    }

    public static ByteBuffer getByteBuffer(MediaFormat mediaFormat, String name) {
        return getByteBuffer(mediaFormat, name, null);
    }

    public static ByteBuffer getByteBuffer(MediaFormat mediaFormat, String name, ByteBuffer defaultValue) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || !mediaFormat.containsKey(name)) {
            return defaultValue;
        }
        return mediaFormat.getByteBuffer(name);
    }

    //set方法判断值为空就不设置直接return是因为在部分手机底层key存在value为null会直接空指针异常
    public static void setInteger(MediaFormat mediaFormat, String name, int value) {
        if (mediaFormat == null || TextUtils.isEmpty(name)) {
            return;
        }
        mediaFormat.setInteger(name, value);
    }


    public static void setLong(MediaFormat mediaFormat, String name, int value) {
        if (mediaFormat == null || TextUtils.isEmpty(name)) {
            return;
        }
        mediaFormat.setLong(name, value);
    }

    public static void setFloat(MediaFormat mediaFormat, String name, float value) {
        if (mediaFormat == null || TextUtils.isEmpty(name)) {
            return;
        }
        mediaFormat.setFloat(name, value);
    }


    public static void setString(MediaFormat mediaFormat, String name, String value) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || value == null) {
            return;
        }
        mediaFormat.setString(name, value);
    }


    public static void setByteBuffer(MediaFormat mediaFormat, String name, ByteBuffer buffer) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || buffer == null) {
            return;
        }
        mediaFormat.setByteBuffer(name, buffer);
    }

    /**
     * 根据profile判断视频是否是10位
     * @param mediaFormat
     * @return
     */
    public static boolean is10BitProfile(MediaFormat mediaFormat) {
        int profile = getInteger(mediaFormat, MediaFormat.KEY_PROFILE);
        return profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10 |
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10 ||
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10Plus ||
                profile == MediaCodecInfo.CodecProfileLevel.VP9Profile2 ||
                profile == MediaCodecInfo.CodecProfileLevel.VP9Profile3 ||
                profile == MediaCodecInfo.CodecProfileLevel.VP9Profile2HDR10Plus ||
                profile == MediaCodecInfo.CodecProfileLevel.VP9Profile3HDR10Plus;
    }


    /***
     * 获取颜色色域
     * @param mediaFormat
     * @return
     */
    @ColorStandard
    public static int getColorStandard(MediaFormat mediaFormat) {
      return  MediaFormatUtil.getInteger(mediaFormat, MediaFormat.KEY_COLOR_STANDARD, ColorStandard.BT709);
    }

    /**
     * 获取颜色范围
     * @param mediaFormat
     * @return
     */
    @ColorRange
    public static int getColorRange(MediaFormat mediaFormat) {
        return  MediaFormatUtil.getInteger(mediaFormat, MediaFormat.KEY_COLOR_RANGE, ColorRange.LIMITED);
    }

    /**
     * 获取颜色传递函数
     * @param mediaFormat
     * @return
     */
    @ColorTransfer
    public static int getColorTransfer(MediaFormat mediaFormat) {
        return  MediaFormatUtil.getInteger(mediaFormat, MediaFormat.KEY_COLOR_TRANSFER, ColorTransfer.SDR_VIDEO);
    }

    /***
     * 设置颜色色域
     * @param mediaFormat
     * @return
     */

    public static void setColorStandard(MediaFormat mediaFormat,@ColorStandard int colorStandard) {
          MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_COLOR_STANDARD, colorStandard);
    }

    /***
     * 设置颜色范围
     * @param mediaFormat
     * @return
     */

    public static void setColorRange(MediaFormat mediaFormat,@ColorRange int colorRange) {
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_COLOR_RANGE, colorRange);
    }

    /***
     * 设置传递函数
     * @param mediaFormat
     * @return
     */

    public static void setColorTransfer(MediaFormat mediaFormat,@ColorTransfer int colorTransfer) {
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_COLOR_TRANSFER, colorTransfer);
    }
}
