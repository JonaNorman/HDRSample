package com.norman.android.hdrsample.player;

abstract class GLRenderer {
    boolean create = false;

    GLRenderTarget outputTarget;

    /**
     * 渲染到目标
     * @param renderTarget
     */
    synchronized void renderToTarget(GLRenderTarget renderTarget) {
        outputTarget = renderTarget;
        renderTarget.startRender();
        if (!create){//没有创建会创建
            create = true;
            onCreate();
        }
        onRender();
        renderTarget.finishRender();
    }


    protected abstract void onCreate();


    abstract void onRender();
}
