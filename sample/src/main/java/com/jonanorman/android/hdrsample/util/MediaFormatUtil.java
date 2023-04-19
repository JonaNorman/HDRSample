package com.jonanorman.android.hdrsample.util;

import android.media.MediaFormat;
import android.text.TextUtils;

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

    public static void setInteger(MediaFormat mediaFormat, String name, int value) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || value <= 0) {
            return;
        }
        mediaFormat.setInteger(name, value);
    }


    public static void setLong(MediaFormat mediaFormat, String name, int value) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || value <= 0) {
            return;
        }
        mediaFormat.setLong(name, value);
    }

    public static void setFloat(MediaFormat mediaFormat, String name, float value) {
        if (mediaFormat == null || TextUtils.isEmpty(name) || value <= 0) {
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
}
