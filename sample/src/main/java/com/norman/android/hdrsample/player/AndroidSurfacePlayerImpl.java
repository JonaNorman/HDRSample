package com.norman.android.hdrsample.player;


import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.AndroidDecoder;
import com.norman.android.hdrsample.player.decode.AndroidSurfaceDecoder;
import com.norman.android.hdrsample.opengl.GLEnvThreadManager;
import com.norman.android.hdrsample.player.dumex.AndroidDemuxer;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

class AndroidSurfacePlayerImpl extends AndroidVideoPlayerImpl implements AndroidSurfacePlayer {
    private static final String SURFACE_PLAYER = "AndroidSurfacePlayer";

    private DecoderSurface decoderSurface = new DecoderSurface();


    public AndroidSurfacePlayerImpl() {
        this(SURFACE_PLAYER);
    }

    public AndroidSurfacePlayerImpl(String threadName) {
        super(AndroidSurfaceDecoder.create(), threadName);
    }

    public synchronized void setSurface(Surface surface) {
        decoderSurface.setOutputSurface(surface);
        post(new Runnable() {
            @Override
            public void run() {
                AndroidSurfaceDecoder surfaceDecoder = (AndroidSurfaceDecoder) getAndroidDecoder();
                surfaceDecoder.setOutputSurface(decoderSurface.getSurface());
            }
        },true);

    }


    @Override
    protected void onPlayRelease() {
        super.onPlayRelease();
        decoderSurface.release();
    }

    @Override
    protected void onInputFormatPrepare(AndroidDemuxer demuxer, MediaFormat inputFormat) {
        super.onInputFormatPrepare(demuxer, inputFormat);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }


    @Override
    protected void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat) {
        AndroidSurfaceDecoder androidSurfaceDecoder = (AndroidSurfaceDecoder) decoder;
        androidSurfaceDecoder.setOutputSurface(decoderSurface.getSurface());
        super.onDecoderConfigure(decoder, inputFormat);
    }


    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {

    }

    protected boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer) {
        AndroidSurfaceDecoder surfaceDecoder = (AndroidSurfaceDecoder) getAndroidDecoder();
        Surface surface =  surfaceDecoder.getOutputSurface();
        if (surface == null || !surface.isValid()){
            return false;
        }
        return true;
    }

    @Override
    protected boolean onOutputBufferProcess(float timeSecond, boolean render) {
        return render;
    }


    class DecoderSurface {
        private Surface outputSurface;

        private HolderSurface holderSurface;

        private boolean release;

        public synchronized void setOutputSurface(Surface surface) {
            this.outputSurface = surface;
        }

        public synchronized void release(){
            if (release){
                return;
            }
            release = true;
            if (holderSurface != null){
                holderSurface.release();
                holderSurface = null;
            }
        }
        public synchronized Surface getSurface() {
            if (release) {
                return null;
            }
            if (outputSurface != null) return outputSurface;
            if (holderSurface != null) return holderSurface;
            holderSurface = HolderSurface.create();
            return holderSurface;
        }
    }

    static final class HolderSurface extends Surface {

        private static GLEnvThreadManager ENV_THREAD_MANAGER;
        private static int THREAD_HOLDER_COUNT;

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
                if (ENV_THREAD_MANAGER == null) return;
                THREAD_HOLDER_COUNT--;
                if (THREAD_HOLDER_COUNT == 0) {
                    ENV_THREAD_MANAGER.release();
                } else {
                    ENV_THREAD_MANAGER.post(new Runnable() {
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
                if (ENV_THREAD_MANAGER == null || ENV_THREAD_MANAGER.isRelease()) {
                    ENV_THREAD_MANAGER = GLEnvThreadManager.create();
                }
                HolderSurface holderSurface = ENV_THREAD_MANAGER.submitSync(new Callable<HolderSurface>() {
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
                THREAD_HOLDER_COUNT++;
                return holderSurface;
            }
        }
    }
}
