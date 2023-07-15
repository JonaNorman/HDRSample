package com.norman.android.hdrsample.player;

abstract class GLRenderer {
    boolean create = false;

    GLRenderTarget outputTarget;

    void renderToTarget(GLRenderTarget renderTarget) {
        outputTarget = renderTarget;
        renderTarget.startRender();
        if (!create){
            create = true;
            onCreate();
        }
        onRender();
        renderTarget.finishRender();
    }


    protected abstract void onCreate();


    abstract void onRender();
}
