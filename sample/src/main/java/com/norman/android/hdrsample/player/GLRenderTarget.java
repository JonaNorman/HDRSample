package com.norman.android.hdrsample.player;

abstract class GLRenderTarget {

    int width;
    int height;
    private boolean created;

    private boolean recreate;

    private boolean rendering;


    void setRenderSize(int renderWidth, int renderHeight) {
        if (renderWidth != this.width
                || this.height != renderHeight) {
            this.width = renderWidth;
            this.height = renderHeight;
            requestRecreate();
        }
    }

    void startRender() {
        if (recreate){
            recreate = false;
            destroy();
        }
        create();
        rendering = true;
        onStart();
    }

    void finishRender() {
        onFinish();
        rendering = false;
        if (recreate){
            recreate =false;
            destroy();
            create();
        }
    }

    boolean isRendering() {
        return rendering;
    }

    void clearColor() {
        if (isRendering()) {//已经在渲染中了就不需要去start了
            onClearColor();
        } else {
            startRender();
            onClearColor();
            finishRender();
        }
    }

    void requestRecreate() {
        recreate = true;
    }

    private void create(){
        if (!created) {
            created = true;
            onCreate();
        }
    }

    void destroy(){
        if (!created){
            return;
        }
        created = false;
        onDestroy();
    }


    abstract void onCreate();

    abstract void onDestroy();

    abstract void onStart();

    abstract void onFinish();

    abstract void onClearColor();
}
