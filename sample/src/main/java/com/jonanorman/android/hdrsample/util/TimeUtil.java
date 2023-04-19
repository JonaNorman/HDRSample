package com.jonanorman.android.hdrsample.util;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private static final long SECOND_SCALE = 1000_000L;

    public static long microToMill(long timeUs){
        return TimeUnit.MICROSECONDS.toMillis(timeUs);
    }

    public static long nanoToMicro(long timeNs){
        return TimeUnit.NANOSECONDS.toMicros(timeNs);
    }

    public static float microToSecond(long timeNs){
        return (float) (timeNs*1.0/SECOND_SCALE);
    }

    public static long secondToMicro(float second){
        return (long) (second*SECOND_SCALE);
    }


}
