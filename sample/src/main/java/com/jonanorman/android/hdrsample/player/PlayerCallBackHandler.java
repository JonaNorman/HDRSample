package com.jonanorman.android.hdrsample.player;

import android.os.Handler;
import android.os.Looper;

class PlayerCallBackHandler {

    private final Handler defaultHandler;
    private Handler handler;
    private Player.Callback callback;


    PlayerCallBackHandler() {
        this.defaultHandler = Looper.myLooper() == null ?
                new Handler(Looper.getMainLooper())
                : new Handler(Looper.myLooper());
    }

    public synchronized void setHandler(Handler handler) {
        this.handler = handler == null ? defaultHandler : handler;
    }

    public synchronized void setCallback(Player.Callback callback) {
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

    public void process(float timeSecond, boolean end) {
        executeCallback(callback -> callback.onPlayProcess(timeSecond, end));
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
                synchronized (PlayerCallBackHandler.this) {
                    if (callback == null) {
                        return;
                    }
                }
                listener.onCallbackListen(callback);
            }
        });
    }

    interface CallbackListener {
        void onCallbackListen(Player.Callback callback);
    }
}
