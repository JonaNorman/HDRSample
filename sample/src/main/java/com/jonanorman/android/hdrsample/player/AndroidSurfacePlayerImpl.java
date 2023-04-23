package com.jonanorman.android.hdrsample.player;


import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.decode.AndroidSurfaceDecoder;
import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvHandler;
import com.jonanorman.android.hdrsample.util.GLESUtil;
import com.jonanorman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

class AndroidSurfacePlayerImpl extends AndroidVideoPlayerImpl implements AndroidSurfacePlayer {
    private static final String SURFACE_PLAYER = "AndroidSurfacePlayer";

    private Surface surface;

    private HolderSurface textureSurface;

    public AndroidSurfacePlayerImpl() {
        this(SURFACE_PLAYER);
    }

    public AndroidSurfacePlayerImpl(String threadName) {
        super(AndroidSurfaceDecoder.createSurfaceDecoder(), threadName);
    }

    public synchronized void setSurface(Surface surface) {
        this.surface = surface;
        if (playHandler == null) return;
        AndroidSurfaceDecoder surfaceDecoder = (AndroidSurfaceDecoder) androidDecoder;
        if (!surfaceDecoder.isConfigured()) {
            playHandler.waitAllMessage();
        }
        if (surface == null) {
            surface = getTextureSurface();
        }
        surfaceDecoder.setOutputSurface(surface);
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        synchronized (this) {
            if (textureSurface != null) {
                textureSurface.release();
            }
        }
    }

    @Override
    protected void onVideoInputFormatConfigure(MediaFormat inputFormat) {
        super.onVideoInputFormatConfigure(inputFormat);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }

    private synchronized HolderSurface getTextureSurface() {
        if (textureSurface != null) return textureSurface;
        textureSurface = HolderSurface.create();
        return textureSurface;
    }

    @Override
    protected void onVideoDecoderConfigure(MediaFormat inputFormat) {
        synchronized (this) {
            if (surface == null) {
                surface = getTextureSurface();
            }
        }
        androidDecoder.configure(new AndroidSurfaceDecoder.Configuration(inputFormat, new VideoDecoderCallBack(), surface));
    }


    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }


    protected boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer) {
        return true;
    }

    @Override
    protected boolean onOutputBufferProcess(float timeSecond, boolean render) {
        return render;
    }

    static final class HolderSurface extends Surface {

        private static GLEnvHandler HANDLER;
        private static int HANDLE_HOLDER_COUNT;

        private static Object HANDLER_LOCK = new Object();

        private SurfaceTexture surfaceTexture;
        private int textureId;

        private boolean release;

        private HolderSurface(SurfaceTexture texture, int textureId) {
            super(texture);
            this.surfaceTexture = texture;
            this.textureId = textureId;
            this.surfaceTexture.setOnFrameAvailableListener(surfaceTexture -> surfaceTexture.updateTexImage());
        }


        @Override
        public synchronized void release() {
            if (release) {
                return;
            }
            release = true;
            super.release();
            this.surfaceTexture.release();
            synchronized (HANDLER_LOCK) {
                if (HANDLER == null) return;
                HANDLE_HOLDER_COUNT--;
                if (HANDLE_HOLDER_COUNT == 0) {
                    HANDLER.recycle();
                } else {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            GLESUtil.delTextureId(textureId);
                        }
                    });
                }
            }
        }

        public static HolderSurface create() {
            synchronized (HANDLER_LOCK) {
                if (HANDLER == null || HANDLER.isRecycle()) {
                    HANDLER = GLEnvHandler.create();
                }
                HolderSurface holderSurface = HANDLER.submitAndWait(new Callable<HolderSurface>() {
                    @Override
                    public HolderSurface call() {
                        int textureId = GLESUtil.createExternalTextureId();
                        SurfaceTexture surfaceTexture = new SurfaceTexture(textureId) {
                            boolean release = false;
                            boolean finalize = false;

                            @Override
                            public synchronized void release() {
                                if (release || finalize) return;
                                super.release();
                                release = false;
                            }

                            @Override
                            protected synchronized void finalize() throws Throwable {
                                finalize = true;
                                super.finalize();
                            }
                        };
                        return new HolderSurface(surfaceTexture, textureId);
                    }
                });
                HANDLE_HOLDER_COUNT++;
                return holderSurface;
            }
        }
    }
}
