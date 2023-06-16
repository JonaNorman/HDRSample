package com.norman.android.hdrsample.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private static final long SECOND_TO_MICRO_SCALE = 1000_000L;

    private static final long SECOND_TO_MILL_SCALE = 1000L;

    public static long microToMill(long timeUs){
        return TimeUnit.MICROSECONDS.toMillis(timeUs);
    }

    public static long nanoToMicro(long timeNs){
        return TimeUnit.NANOSECONDS.toMicros(timeNs);
    }

    public static float microToSecond(long timeUs){
        return (float) (timeUs*1.0/ SECOND_TO_MICRO_SCALE);
    }

    public static long secondToMicro(float second){
        return (long) (second* SECOND_TO_MICRO_SCALE);
    }

    public static long secondToMill(float second){
        return (long) (second* SECOND_TO_MILL_SCALE);
    }


}
