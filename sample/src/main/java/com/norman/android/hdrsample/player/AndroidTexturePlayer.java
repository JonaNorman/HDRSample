package com.norman.android.hdrsample.player;

import com.norman.android.hdrsample.opengl.GLMatrix;

public interface AndroidTexturePlayer extends AndroidVideoPlayer {


    void setTextureRenderer(TextureRenderer textureRenderer);

    static AndroidTexturePlayer create() {
        return new AndroidTexturePlayerImpl();
    }


    static AndroidTexturePlayer create(String threadName) {
        return new AndroidTexturePlayerImpl(threadName);
    }


    abstract class TextureRenderer {
        boolean rendering;


        void clean(){
            if (!rendering){
                return;
            }
            onClean();
            this.rendering = false;
        }

        void render(TextureInfo textureInfo,SurfaceInfo surfaceInfo){
            if (!rendering){
                onCreate(textureInfo,surfaceInfo);
                rendering  = true;
            }
            onRender(textureInfo,surfaceInfo);
        }

        protected abstract void onCreate(TextureInfo textureInfo,SurfaceInfo surfaceInfo);


        protected abstract void onClean();


        protected  abstract void onRender(TextureInfo textureInfo,SurfaceInfo surfaceInfo);


    }

    class TextureInfo {
        public int textureId;
        public int width;
        public int height;
        public GLMatrix textureMatrix = new GLMatrix();
    }

    class SurfaceInfo {
        public int width;

        public int height;
    }


}
