package com.jonanorman.android.hdrsample.player;

import android.os.SystemClock;

import com.jonanorman.android.hdrsample.util.TimeUtil;

class PlayerTimeSyncer {
    private long firstPlaySystemTimeUs;
    private long firstPlayTimeUs;

    private long currentTimeUs;


    public synchronized void resetSync() {
        firstPlaySystemTimeUs = 0;
        firstPlayTimeUs = 0;
    }

    public synchronized void clean() {
        resetSync();
        currentTimeUs = 0;
    }

    public synchronized long getCurrentTimeUs() {
        return currentTimeUs;
    }


    public synchronized long syncTime(long timeUs) {
        currentTimeUs = timeUs;
        long currentSystemUs = TimeUtil.nanoToMicro(SystemClock.elapsedRealtimeNanos());
        if (firstPlaySystemTimeUs == 0) {
            firstPlaySystemTimeUs = currentSystemUs;
            firstPlayTimeUs = timeUs;
            return 0;
        } else {
            long timeCost = currentSystemUs - firstPlaySystemTimeUs;
            long sleepTime = timeUs - firstPlayTimeUs - timeCost;
            return sleepTime;
        }
    }
}
