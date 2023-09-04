package com.norman.android.hdrsample.player;

import android.os.Handler;
import android.os.Looper;

import com.norman.android.hdrsample.handler.MessageHandler;

/**
 * 播放器的状态封装
 */
abstract class PlayerImpl implements Player {

    private static final int PLAY_UNINITIALIZED = 0;
    private static final int PLAY_PREPARE = 1;
    private static final int PLAY_START = 2;
    private static final int PLAY_PAUSE = 3;
    private static final int PLAY_RESUME = 4;
    private static final int PLAY_STOP = 5;
    private static final int PLAY_RELEASE = 6;
    private final String threadName;

    private int state = PLAY_UNINITIALIZED;

    private boolean started;

    private MessageHandler messageHandler;


    public PlayerImpl(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public final synchronized void prepare() {
        if (state != PLAY_UNINITIALIZED
                && state != PLAY_STOP) {
            return;
        }
        state = PLAY_PREPARE;
        post(this::onPlayPrepare);
    }


    @Override
    public synchronized void start() {
        if (state == PLAY_UNINITIALIZED
                || state == PLAY_STOP) {
            prepare();
            state = PLAY_START;
            post(this::onStart);
        } else if (state == PLAY_PREPARE) {
            state = PLAY_START;
            post(this::onStart);
        } else if (isPause()) {
            state = PLAY_RESUME;
            post(this::onPlayResume);
        }
    }


    @Override
    public synchronized void pause() {
        if (!isPlaying()) {
            return;
        }
        state = PLAY_PAUSE;
        post(this::onPlayPause);
    }

    @Override
    public synchronized void stop() {
        if (!isPrepared()) {
            return;
        }
        state = PLAY_STOP;
        post(this::onStop);
    }


    @Override
    public synchronized void release() {
        if (state == PLAY_RELEASE) {
            return;
        }
        state = PLAY_RELEASE;
        if (messageHandler != null){
            messageHandler.finish();
        }
    }


    synchronized void post(Runnable runnable) {
        if (isRelease()) return;
        if (messageHandler == null){
            messageHandler = MessageHandler.obtain(threadName,
                    new MessageHandler.LifeCycleCallback() {

                        @Override
                        public void onHandlerFinish() {
                            release();
                            onStop();
                            onPlayRelease();
                        }

                        @Override
                        public void onHandlerError(Exception exception) {
                            onPlayError(exception);
                        }
                    });
        }
        messageHandler.post(runnable);
    }


    @Override
    public synchronized boolean isPlaying() {
        return state == PLAY_START ||
                state == PLAY_RESUME;
    }


    @Override
    public synchronized boolean isPrepared() {
        return state != PLAY_UNINITIALIZED &&
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


    private void onStart(){
        if (started){
            return;
        }
        onPlayStart();
        started = true;
    }

    private void onStop(){
        if (!started){
            return;
        }
        started = false;
        onPlayStop();
    }

    protected abstract void onPlayPrepare();

    protected abstract void onPlayStart();

    protected abstract void onPlayResume();

    protected abstract void onPlayPause();

    protected abstract void onPlayStop();

    protected abstract void onPlayError(Exception exception);

    protected abstract void onPlayRelease();


    /**
     * 对Callback回调调用的封装，保证调用在Handler的线程中
     */
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

        final synchronized void executeCallback(CallbackExecutor listener) {
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
                    listener.onCallbackExecute(callback);
                }
            });
        }

        interface CallbackExecutor {
            void onCallbackExecute(Callback callback);
        }
    }

}
