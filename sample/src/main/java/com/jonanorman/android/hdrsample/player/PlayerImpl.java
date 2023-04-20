package com.jonanorman.android.hdrsample.player;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.jonanorman.android.hdrsample.util.TimeUtil;

abstract class PlayerImpl implements Player {
    static class CallBackHandler {

        private final Handler defaultHandler;
        private Handler handler;
        private Callback callback;


        CallBackHandler() {
            this.defaultHandler = Looper.myLooper() == null ?
                    new Handler(Looper.getMainLooper())
                    : new Handler(Looper.myLooper());
        }

        public synchronized void setHandler(Handler handler) {
            this.handler = handler == null ? defaultHandler : handler;
        }

        public synchronized void setCallback(Callback callback) {
            this.callback = callback;
        }

        public void prepare() {
            executeCallback(callback -> callback.onPlayPrepare());
        }

        public void start() {
            executeCallback(callback -> callback.onPlayStart());
        }

        public void pause() {
            executeCallback(callback -> callback.onPlayPause());
        }


        public void resume() {
            executeCallback(callback -> callback.onPlayResume());
        }

        public void stop() {
            executeCallback(callback -> callback.onPlayStop());
        }

        public void process(float timeSecond) {
            executeCallback(callback -> callback.onPlayProcess(timeSecond));
        }

        public void end() {
            executeCallback(callback -> callback.onPlayEnd());
        }

        public void error(Throwable throwable) {
            if (throwable == null) return;
            executeCallback(callback -> callback.onPlayError(throwable));
        }

        final synchronized void executeCallback(CallbackListener listener) {
            if (listener == null) {
                return;
            }
            if (handler == null) {
                return;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (CallBackHandler.this) {
                        if (callback == null) {
                            return;
                        }
                    }
                    listener.onCallbackListen(callback);
                }
            });
        }

        interface CallbackListener {
            void onCallbackListen(Callback callback);
        }
    }

    static class TimeSyncer {
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
}
