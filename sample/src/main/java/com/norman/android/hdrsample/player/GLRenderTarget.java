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

    void cleanRender(){
        if (isRendering()){
            onRenderClean();
        }else {
            startRender();
            onRenderClean();
            finishRender();
        }
    }


    abstract void onRenderSizeChange(int renderWidth, int renderHeight);

    abstract void onRenderStart();

    abstract void onRenderFinish();

    abstract void onRenderClean();
}
