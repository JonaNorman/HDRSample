package com.norman.android.hdrsample.util;

import android.os.Handler;
import android.os.SystemClock;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MessageThreadPool {

    private final static String DEFAULT_POOL_NAME = "MessageThreadPool";
    private final static String DEFAULT_THREAD_NAME = "MessageThread";

    private final static Map<String, MessageThreadPool> POOL_MAP = new ConcurrentHashMap<>();


    public static final int CACHE_POOL_SIZE = 5;
    public static final int CACHE_SECOND = 30;
    public static final int INTERVAL_SECOND = 5;
    private final List<MessageThread> threadCache = new ArrayList<>();
    private volatile int poolCacheSize = CACHE_POOL_SIZE;
    private volatile int maxCacheTime = CACHE_SECOND;
    private volatile int intervalCleanTime = INTERVAL_SECOND;
    private boolean cacheCleaning;


    private MessageThreadPool() {
    }


    public void setCacheTime(int maxCacheTime) {
        this.maxCacheTime = maxCacheTime;
    }

    public void setPoolCacheSize(int poolSize) {
        this.poolCacheSize = poolSize;
    }

    public void setIntervalCleanTime(int intervalCleanTime) {
        this.intervalCleanTime = intervalCleanTime;
    }

    MessageThread obtain() {
        return obtain(null);
    }

    synchronized MessageThread obtain(String name) {
        if (name == null) name = DEFAULT_THREAD_NAME;
        cleanCacheThread();
        Iterator<MessageThread> iterator = threadCache.iterator();
        while (iterator.hasNext()) {
            MessageThread messageThread = iterator.next();
            if (messageThread.isIdle()) {
                iterator.remove();
                messageThread.setName(name);
                return messageThread;
            }
        }
        MessageThread messageThread = new MessageThread(name);
        messageThread.start();
        return messageThread;
    }


    synchronized void recycle(MessageThread handlerThread) {
        if (handlerThread.isTerminated()) {
            return;
        }
        handlerThread.cacheTime = SystemClock.elapsedRealtime();
        threadCache.add(handlerThread);
        startCleanThread();
    }

    private boolean cleanCacheThread() {
        Iterator<MessageThread> iterator = threadCache.iterator();
        while (iterator.hasNext()) {
            MessageThread thread = iterator.next();
            long duration = SystemClock.elapsedRealtime() - thread.cacheTime;
            if (thread.isTerminated()
                    || threadCache.size() > poolCacheSize
                    || duration > TimeUnit.SECONDS.toMillis(maxCacheTime)) {
                iterator.remove();
                thread.quit();
            }
        }
        return threadCache.isEmpty();
    }

    private void startCleanThread() {
        if (cacheCleaning) {
            return;
        }
        cacheCleaning = true;
        MessageThread handlerThread = obtain("CleanMessageThread");
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (MessageThreadPool.this){
                    if (cleanCacheThread()) {
                        cacheCleaning = false;
                        handlerThread.quit();
                    } else {
                        handler.postDelayed(this, intervalCleanTime * 1000);
                    }
                }
            }
        });
    }

    public static MessageThreadPool get(String name) {
        MessageThreadPool pool = POOL_MAP.get(name);
        if (pool != null) {
            return pool;
        }
        pool = new MessageThreadPool();
        POOL_MAP.put(name, pool);
        return pool;
    }

    public static MessageThreadPool get() {
        return get(DEFAULT_POOL_NAME);
    }

}
