package com.norman.android.hdrsample.player;

import java.util.LinkedList;
import java.util.Queue;

abstract class GLRenderer {
    boolean create = false;

    GLRenderTarget outputTarget;

    Queue<Runnable> postQueue =  new LinkedList<>();

    synchronized void renderToTarget(GLRenderTarget renderTarget) {
        outputTarget = renderTarget;
        renderTarget.startRender();
        if (!create){
            create = true;
            onCreate();
        }
        while (!postQueue.isEmpty()) {
             Runnable runnable =  postQueue.poll();
             runnable.run();
        }

        onRender();
        renderTarget.finishRender();
    }

    protected synchronized void  post(Runnable runnable){
        postQueue.add(runnable);
    }


    protected abstract void onCreate();


    abstract void onRender();
}
