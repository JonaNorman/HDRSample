package com.jonanorman.android.hdrsample;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.AndroidVideoSurfacePlayer;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvContext;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvHandler;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvWindowSurface;
import com.jonanorman.android.hdrsample.player.source.FileSource;
import com.jonanorman.android.hdrsample.util.GLESUtil;

import java.util.concurrent.atomic.AtomicBoolean;

class OESHDRPlayer implements AndroidVideoSurfacePlayer {
    AndroidVideoSurfacePlayer videoPlayer;
    GLEnvHandler envHandler;
    int textureId;
    SurfaceTexture surfaceTexture;
    Surface surface;

    GLEnvWindowSurface envWindowSurface;
    OESTextureRenderer oesTextureRenderer;
    AtomicBoolean hasSurface = new AtomicBoolean();


    public OESHDRPlayer() {
        videoPlayer = AndroidVideoSurfacePlayer.createSurfacePlayer();
        envHandler = GLEnvHandler.create();
        envHandler.postAndWait(new Runnable() {
            @Override
            public void run() {
                textureId = GLESUtil.createExternalTextureId();
                surfaceTexture = new SurfaceTexture(textureId);
                surface = new Surface(surfaceTexture);
                oesTextureRenderer = new OESTextureRenderer();
                oesTextureRenderer.setTextureId(textureId);
                surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        surfaceTexture.updateTexImage();
                        if (!hasSurface.get() || envWindowSurface == null) return;
                        surfaceTexture.getTransformMatrix(oesTextureRenderer.getTextureMatrix().get());
                        oesTextureRenderer.setSurfaceSize(envWindowSurface.getWidth(), envWindowSurface.getHeight());
                        GLEnvContext envContext = envHandler.getEnvContext();
                        envContext.makeCurrent(envWindowSurface);
                        oesTextureRenderer.render();
                        envWindowSurface.swapBuffers();
                    }
                });
            }
        });
        videoPlayer.setSurface(surface);
    }

    @Override
    public void setSource(FileSource fileSource) {
        videoPlayer.setSource(fileSource);
    }

    @Override
    public void prepare() {
        videoPlayer.prepare();
    }

    @Override
    public void start() {
        videoPlayer.start();
    }

    @Override
    public void seek(float timeSecond) {
        videoPlayer.seek(timeSecond);
    }


    @Override
    public void pause() {
        videoPlayer.pause();
    }

    @Override
    public void stop() {
        videoPlayer.stop();
    }

    @Override
    public void release() {
        videoPlayer.release();
    }

    @Override
    public boolean isPlaying() {
        return videoPlayer.isPlaying();
    }

    @Override
    public boolean isPause() {
        return videoPlayer.isPause();
    }

    @Override
    public boolean isStop() {
        return videoPlayer.isStop();
    }

    @Override
    public boolean isRelease() {
        return videoPlayer.isRelease();
    }

    @Override
    public boolean isPrepared() {
        return videoPlayer.isPrepared();
    }

    @Override
    public float getCurrentTime() {
        return videoPlayer.getCurrentTime();
    }

    @Override
    public void setCallback(Callback callback) {
        videoPlayer.setCallback(callback);
    }

    @Override
    public void setCallback(Callback callback, Handler handler) {
        videoPlayer.setCallback(callback, handler);
    }

    @Override
    public void postFrame(FrameRunnable runnable) {
        videoPlayer.postFrame(runnable);
    }

    @Override
    public void waitFrame() {
        videoPlayer.waitFrame();
    }


    @Override
    public void setSurface(Surface surface) {
        hasSurface.set(surface != null);
        if (surface == null) {
            envHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (envWindowSurface != null) {
                        envWindowSurface.release();
                    }
                }
            });
            return;
        }
        envHandler.post(new Runnable() {
            @Override
            public void run() {
                if (envWindowSurface != null) {
                    envWindowSurface.release();
                    envWindowSurface = null;
                }
                GLEnvContext envContext = envHandler.getEnvContext();
                GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, surface);
                envWindowSurface = builder.build();
            }
        });

    }
}
