package com.norman.android.hdrsample.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private static final long SECOND_TO_MICRO_SCALE = 1000_000L;

    private static final long SECOND_TO_MILL_SCALE = 1000L;

    /**
     * 微妙转毫秒
     * @param timeUs
     * @return
     */
    public static long microToMill(long timeUs){
        return TimeUnit.MICROSECONDS.toMillis(timeUs);
    }

    /**
     * 纳秒转微妙
     * @param timeNs
     * @return
     */
    public static long nanoToMicro(long timeNs){
        return TimeUnit.NANOSECONDS.toMicros(timeNs);
    }

    /**
     * 微妙转纳秒
     * @param timeNs
     * @return
     */
    public static long microToNano(long timeNs){
        return TimeUnit.MICROSECONDS.toNanos(timeNs);
    }

    /**
     * 微妙转秒
     * @param timeUs
     * @return
     */
    public static float microToSecond(long timeUs){
        return timeUs*1.0f/ SECOND_TO_MICRO_SCALE;
    }

    /**
     * 秒转微妙
     * @param second
     * @return
     */

    public static long secondToMicro(float second){
        return (long) (second* SECOND_TO_MICRO_SCALE);
    }

    /**
     * 秒转毫秒
     * @param second
     * @return
     */
    public static long secondToMill(float second){
        return (long) (second* SECOND_TO_MILL_SCALE);
    }


}
