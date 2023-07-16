package com.norman.android.hdrsample.player;

abstract class GLRenderTarget {

     int renderWidth;
     int renderHeight;

     boolean rendering;

    void setRenderSize(int renderWidth, int renderHeight) {
        if (renderWidth != this.renderWidth || this.renderHeight != renderHeight) {
            this.renderWidth = renderWidth;
            this.renderHeight = renderHeight;
            onRenderSizeChange(renderWidth, renderHeight);
        }
    }

    void startRender() {
        rendering = true;
        onRenderStart();
    }

    void finishRender() {
        onRenderFinish();
        rendering = false;
    }

    boolean isRendering() {
        return rendering;
    }

    void clearColor(){
        if (isRendering()){
            onRenderClearColor();
        }else {
            startRender();
            onRenderClearColor();
            finishRender();
        }
    }


    abstract void onRenderSizeChange(int renderWidth, int renderHeight);

    abstract void onRenderStart();

    abstract void onRenderFinish();

    abstract void onRenderClearColor();
}
