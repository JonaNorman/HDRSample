package com.norman.android.hdrsample.player;


import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.opengl.GLEnvThreadManager;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.player.decode.AndroidDecoder;
import com.norman.android.hdrsample.player.decode.AndroidSurfaceDecoder;
import com.norman.android.hdrsample.player.extract.AndroidExtractor;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

class AndroidSurfacePlayerImpl extends AndroidVideoPlayerImpl implements SurfacePlayer {
    private static final String SURFACE_PLAYER = "AndroidSurfacePlayer";

    private final DecoderSurface decoderSurface = new DecoderSurface();


    public AndroidSurfacePlayerImpl() {
        this(SURFACE_PLAYER);
    }

    public AndroidSurfacePlayerImpl(String threadName) {
        super(AndroidSurfaceDecoder.create(), threadName);
    }

    public synchronized void setSurface(Surface surface) {
        decoderSurface.setSurface(surface);
        AndroidSurfaceDecoder surfaceDecoder = (AndroidSurfaceDecoder) getAndroidDecoder();
        surfaceDecoder.setOutputSurface(decoderSurface.getOutputSurface());
    }


    @Override
    protected void onPlayRelease() {
        super.onPlayRelease();
        decoderSurface.release();
    }

    @Override
    protected void onInputFormatPrepare(AndroidExtractor extractor, MediaFormat inputFormat) {
        super.onInputFormatPrepare(extractor, inputFormat);
        MediaFormatUtil.setInteger(inputFormat, MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }


    @Override
    protected void onDecoderConfigure(AndroidDecoder decoder, MediaFormat inputFormat) {
        AndroidSurfaceDecoder androidSurfaceDecoder = (AndroidSurfaceDecoder) decoder;
        androidSurfaceDecoder.setOutputSurface(decoderSurface.getOutputSurface());
        super.onDecoderConfigure(decoder, inputFormat);
    }


    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        super.onOutputFormatChanged(outputFormat);
    }

    @Override
    protected boolean onOutputBufferRender(float timeSecond, ByteBuffer buffer) {
        return decoderSurface.isValid();
    }

    @Override
    protected boolean onOutputBufferProcess(float timeSecond, boolean render) {
        return render;
    }

    class DecoderSurface {
        private Surface outputSurface;

        private SurfaceHolder holderSurface;

        private boolean release;

        public synchronized void setSurface(Surface surface) {
            this.outputSurface = surface;
        }

        public synchronized Surface getSurface() {
            return outputSurface;
        }

        public synchronized boolean isValid(){
            if (release){
                return false;
            }
            return outputSurface != null && outputSurface.isValid();
        }

        public synchronized void release() {
            if (release) {
                return;
            }
            release = true;
            if (holderSurface != null) {
                holderSurface.release();
                holderSurface = null;
            }
        }

        public synchronized Surface getOutputSurface() {
            if (release) {
                return null;
            }
            if (outputSurface != null) return outputSurface;
            if (holderSurface != null) return holderSurface.getSurface();
            holderSurface = new SurfaceHolder();
            return holderSurface.getSurface();
        }
    }

    static final class SurfaceHolder {

        private static GLEnvThreadManager ENV_THREAD_MANAGER;
        private static int THREAD_HOLDER_COUNT;

        private final GLTextureSurface textureSurface;
        private boolean release;


        public SurfaceHolder() {
            synchronized (SurfaceHolder.class) {
                if (ENV_THREAD_MANAGER == null || ENV_THREAD_MANAGER.isRelease()) {
                    ENV_THREAD_MANAGER = GLEnvThreadManager.create();
                }
                this.textureSurface = ENV_THREAD_MANAGER.submitSync(new Callable<GLTextureSurface>() {
                    @Override
                    public GLTextureSurface call()  {
                        return new GLTextureSurface(GLESUtil.createExternalTextureId());
                    }
                });
                THREAD_HOLDER_COUNT++;
            }
        }

        public synchronized Surface getSurface(){
            return textureSurface;
        }


        public synchronized void release() {
            if (release) {
                return;
            }
            release = true;
            textureSurface.release();
            synchronized (SurfaceHolder.class) {
                if (ENV_THREAD_MANAGER == null) return;
                THREAD_HOLDER_COUNT--;
                if (THREAD_HOLDER_COUNT == 0) {
                    ENV_THREAD_MANAGER.release();
                } else {
                    ENV_THREAD_MANAGER.post(new Runnable() {
                        @Override
                        public void run() {
                            GLESUtil.delTextureId(textureSurface.getTextureId());
                        }
                    });
                }
            }
        }

    }
}
