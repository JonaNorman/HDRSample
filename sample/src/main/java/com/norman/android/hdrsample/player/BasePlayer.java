package com.norman.android.hdrsample.player;

import android.os.Handler;
import android.os.Looper;

import com.norman.android.hdrsample.handler.Future;
import com.norman.android.hdrsample.handler.MessageHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

abstract class BasePlayer implements Player {

    private static final int PLAY_UNINIT = 0;
    private static final int PLAY_PREPARE = 1;
    private static final int PLAY_START = 2;
    private static final int PLAY_PAUSE = 3;
    private static final int PLAY_RESUME = 4;
    private static final int PLAY_STOP = 5;
    private static final int PLAY_RELEASE = 6;

    private final List<Runnable> pendRunnableList = Collections.synchronizedList(new ArrayList<>());

    private final String threadName;

    private int state = PLAY_UNINIT;

    private Future stopFuture;

    private MessageHandler messageHandler;



    public BasePlayer(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public final synchronized void prepare() {
        if (state != PLAY_UNINIT
                && state != PLAY_STOP) {
            return;
        }
        state = PLAY_PREPARE;
        prepareHandler();
        post(new Runnable() {
            @Override
            public void run() {
                onPlayPrepare();
                Iterator<Runnable> iterator = pendRunnableList.iterator();
                while (iterator.hasNext()) {
                    Runnable runnable = iterator.next();
                    iterator.remove();
                    runnable.run();
                }
            }
        });
    }


    @Override
    public synchronized void play() {
        if (state == PLAY_UNINIT
                || state == PLAY_STOP) {
            prepare();
            state = PLAY_START;
            messageHandler.post(this::onPlayStart);
        } else if (state == PLAY_PREPARE) {
            state = PLAY_START;
            messageHandler.post(this::onPlayStart);
        } else if (isPause()) {
            state = PLAY_RESUME;
            messageHandler.post(this::onPlayResume);
        }
    }


    @Override
    public synchronized void pause() {
        if (!isPlaying()) {
            return;
        }
        state = PLAY_PAUSE;
        messageHandler.post(this::onPlayPause);
    }

    @Override
    public synchronized void stop() {
        if (!isPrepare()) {
            return;
        }
        state = PLAY_STOP;
        stopFuture = messageHandler.submit(this::onPlayStop);
        messageHandler = null;
        pendRunnableList.clear();
    }


    @Override
    public synchronized void release() {
        if (state == PLAY_RELEASE) {
            return;
        }
        state = PLAY_RELEASE;
        if (messageHandler != null) {
            messageHandler.finish();
            messageHandler = null;
        }
        stopFuture = null;
        pendRunnableList.clear();
    }


    public synchronized void post(Runnable runnable) {
        if (isRelease()) return;
        if (messageHandler == null) {
            pendRunnableList.add(runnable);
            return;
        }
        messageHandler.post(runnable);
    }

    private synchronized void prepareHandler() {
        if (messageHandler != null) return;
        if (stopFuture != null) {
            stopFuture.get();
            stopFuture = null;
        }
        messageHandler = MessageHandler.obtain(threadName, new MessageHandler.LifeCycleCallback() {
            @Override
            public void onHandlerFinish() {
                release();
                onPlayRelease();
            }

            @Override
            public void onHandlerError(Exception exception) {
                onPlayError(exception);
            }
        });
    }


    @Override
    public synchronized boolean isPlaying() {
        return state == PLAY_START ||
                state == PLAY_RESUME;
    }


    @Override
    public synchronized boolean isPrepare() {
        return state != PLAY_UNINIT &&
                state != PLAY_STOP &&
                state != PLAY_RELEASE;
    }

    @Override
    public synchronized boolean isPause() {
        return state == PLAY_PAUSE;
    }

    @Override
    public synchronized boolean isStop() {
        return state == PLAY_STOP;
    }

    @Override
    public synchronized boolean isRelease() {
        return state == PLAY_RELEASE;
    }

    protected abstract void onPlayPrepare();

    protected abstract void onPlayStart();

    protected abstract void onPlayResume();

    protected abstract void onPlayPause();

    protected abstract void onPlayStop();

    protected abstract void onPlayError(Exception exception);

    protected abstract void onPlayRelease();


    static class CallBackHandler {

        private final Handler defaultHandler;
        private Handler handler;
        private Callback callback;


        CallBackHandler() {
            this.defaultHandler = Looper.myLooper() == null ?
                    new Handler(Looper.getMainLooper())
                    : new Handler(Looper.myLooper());
            this.handler = defaultHandler;
        }

        public synchronized void setHandler(Handler handler) {
            this.handler = handler == null ? defaultHandler : handler;
        }

        public synchronized void setCallback(Callback callback) {
            this.callback = callback;
        }

        public void callProcess(float timeSecond) {
            executeCallback(callback -> callback.onPlayProcess(timeSecond));
        }



        public void callEnd() {
            executeCallback(callback -> callback.onPlayEnd());
        }

        public void callError(Exception exception) {
            if (exception == null) return;
            executeCallback(callback -> callback.onPlayError(exception));
        }

        final synchronized void executeCallback(CallbackListener listener) {
            synchronized (CallBackHandler.this) {
                if (callback == null) {
                    return;
                }
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

}