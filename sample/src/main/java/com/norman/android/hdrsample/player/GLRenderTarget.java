package com.norman.android.hdrsample.player;

abstract class GLRenderTarget {

     int renderWidth;
     int renderHeight;

    void setRenderSize(int renderWidth, int renderHeight) {
        if (renderWidth != this.renderWidth || this.renderHeight != renderHeight) {
            this.renderWidth = renderWidth;
            this.renderHeight = renderHeight;
            onRenderSizeChange(renderWidth, renderHeight);
        }
    }

    void startRender() {
        onRenderStart();
    }

    void finishRender() {
        onRenderFinish();
    }


    abstract void onRenderSizeChange(int renderWidth, int renderHeight);

    abstract void onRenderStart();

    abstract void onRenderFinish();
}
