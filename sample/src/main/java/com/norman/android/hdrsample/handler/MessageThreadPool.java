package com.norman.android.hdrsample.handler;

import android.os.Handler;
import android.os.SystemClock;


import com.norman.android.hdrsample.util.TimeUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/***
 * MessageHandler运行的复用线程池
 */
public class MessageThreadPool {

    private final static String DEFAULT_POOL_NAME = "MessageThreadPool";
    private final static String DEFAULT_THREAD_NAME = "MessageThread";

    private final static Map<String, MessageThreadPool> POOL_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存的线程数量大小
     */
    public static final int CACHE_POOL_SIZE = 5;

    /**
     * 缓存时间
     */
    public static final int CACHE_SECOND = 30;
    /**
     * 清理多余线程的轮询时间
     */
    public static final int INTERVAL_SECOND = 5;
    private final List<MessageThread> threadCache = new ArrayList<>();
    private volatile int poolCacheSize = CACHE_POOL_SIZE;
    private volatile int maxCacheTime = CACHE_SECOND;
    private volatile int intervalCleanTime = INTERVAL_SECOND;
    private boolean cacheCleaning;


    private MessageThreadPool() {
    }


    /****
     * 缓存时间
     * @param maxCacheTime 单位秒
     */
    public void setCacheTime(int maxCacheTime) {
        this.maxCacheTime = maxCacheTime;
    }

    /***
     * 缓存个数
     * @param poolSize
     */
    public void setPoolCacheSize(int poolSize) {
        this.poolCacheSize = poolSize;
    }


    /**
     * 缓存清理时间
     * @param intervalCleanTime 单位秒
     */
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
            //只有缓存线程池中的线程空闲了才可以复用，要不然虽然已经加到线程池中但是还有些任务因为异步关闭可能还在执行中
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
            //线程已经终止、超过缓存时间、超过数量限制就清除线程
            if (thread.isTerminated()
                    || threadCache.size() > poolCacheSize
                    || duration > TimeUnit.SECONDS.toMillis(maxCacheTime)) {
                iterator.remove();
                thread.quit();
            }
        }
        return threadCache.isEmpty();
    }

    //启动清理线程，如果已经在清理中就直接return
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
                    if (cleanCacheThread()) {//多余的线程已经清理完毕就关闭自己
                        cacheCleaning = false;
                        handlerThread.quit();
                    } else {//轮询清理
                        handler.postDelayed(this, TimeUtil.secondToMill(intervalCleanTime));
                    }
                }
            }
        });
    }

    /**
     * 根据名字获取MessageThread缓存池
     * @param name
     * @return
     */
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
