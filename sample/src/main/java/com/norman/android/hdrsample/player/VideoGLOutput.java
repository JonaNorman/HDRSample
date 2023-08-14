package com.norman.android.hdrsample.player;

import android.graphics.Rect;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import com.norman.android.hdrsample.opengl.GLEnvConfigSimpleChooser;
import com.norman.android.hdrsample.opengl.GLEnvContext;
import com.norman.android.hdrsample.opengl.GLEnvContextManager;
import com.norman.android.hdrsample.opengl.GLEnvSurface;
import com.norman.android.hdrsample.opengl.GLEnvWindowSurface;
import com.norman.android.hdrsample.opengl.GLTextureSurface;
import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoGLOutput extends VideoOutput {

    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";


    private GLEnvContextManager envContextManager;
    private GLEnvContext envContext;

    private GLTextureSurface videoSurface;

    private final PlayerSurface playerSurface = new PlayerSurface();

    private final GLTextureRenderer externalTextureRenderer = new GLTextureRenderer(GLTextureRenderer.TYPE_TEXTURE_EXTERNAL_OES);

    private final GLY2YExtensionRenderer y2yExtTextureRenderer = new GLY2YExtensionRenderer();

    private final GLTextureRenderer texture2DRenderer = new GLTextureRenderer(GLTextureRenderer.TYPE_TEXTURE_2D);

    private final GLYUV420Renderer bufferYUV420Renderer = new GLYUV420Renderer();

    private final GLRenderTextureTarget yuv420TextureTarget = new GLRenderTextureTarget();

    private final List<GLVideoTransform> transformList = new ArrayList<>();


    private final GLRenderScreenTarget screenTarget = new GLRenderScreenTarget();


    private  GLRenderTextureTarget frontTarget = new GLRenderTextureTarget();

    private  GLRenderTextureTarget backTarget = new GLRenderTextureTarget();



    private boolean bufferMode;

    private boolean textureY2YMode;
    private boolean profile10Bit;

    private boolean hdrColor;

    private VideoView videoView;

    private VideoView.SurfaceSubscriber surfaceSubscriber = new VideoView.SurfaceSubscriber() {
        @Override
        public void onSurfaceAvailable(Surface surface, int width, int height) {

            playerSurface.setOutputSurface(surface);

        }

        @Override
        public void onSurfaceRedraw() {
            waitNextFrame();
        }

        @Override
        public void onSurfaceSizeChange(int width, int height) {


        }

        @Override
        public void onSurfaceDestroy() {
            playerSurface.setOutputSurface(null);
        }
    };


    public static VideoGLOutput create() {
        return new VideoGLOutput();
    }


    @Override
    protected void onDecodeStop() {
        super.onDecodeStop();
        playerSurface.release();
        envContextManager.detach();
        if (videoSurface != null){
            videoSurface.release();
        }
    }

    @Override
    public synchronized void setOutputSurface(Surface surface) {
        setOutputVideoView(null);
        this.playerSurface.setOutputSurface(surface);
    }

    @Override
    protected void onVideoSizeChange(int width, int height) {
        super.onVideoSizeChange(width, height);
        if (videoView != null){
            videoView.setAspectRatio(width*1.0f/height);
        }
    }

    @Override
    public void setOutputVideoView(VideoView view) {
        if (videoView != view) {
            if (videoView != null)
                videoView.unsubscribe(surfaceSubscriber);
            videoView = view;
        }
        if (videoView != null) {
            videoView.setAspectRatio(getWidth()*1.0f/getHeight());
            videoView.subscribe(surfaceSubscriber);
        }

    }

    @Override
    protected void onDecoderPrepare(VideoPlayer videoPlayer, VideoExtractor videoExtractor, VideoDecoder videoDecoder, MediaFormat inputFormat) {
        super.onDecoderPrepare(videoPlayer, videoExtractor, videoDecoder, inputFormat);

        GLEnvConfigSimpleChooser.Builder envConfigChooser = new GLEnvConfigSimpleChooser.Builder();
        envConfigChooser.setRedSize(16);
        envConfigChooser.setGreenSize(16);
        envConfigChooser.setBlueSize(16);
        envConfigChooser.setAlphaSize(16);
        envContextManager = GLEnvContextManager.create(envConfigChooser.build());
        envContextManager.attach();
        envContext = envContextManager.getEnvContext();


        profile10Bit = MediaFormatUtil.is10BitProfile(inputFormat);
        if (profile10Bit && videoDecoder.isSupportYUV42010BitBufferMode()) {
            bufferMode = true;
            videoDecoder.setOutputMode(VideoDecoder.BUFFER_MODE);
        } else {
            bufferMode = false;
            videoDecoder.setOutputMode(VideoDecoder.SURFACE_MODE);
            videoSurface = new GLTextureSurface(GLESUtil.createExternalTextureId());
            videoDecoder.setOutputSurface(videoSurface);
            externalTextureRenderer.setTextureId(videoSurface.getTextureId());
            y2yExtTextureRenderer.setTextureId(videoSurface.getTextureId());
        }
    }


    public synchronized void addVideoTransform(GLVideoTransform videoTransform) {
        transformList.add(videoTransform);
    }

    @Override
    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        super.onOutputFormatChanged(outputFormat);
        hdrColor = MediaFormatUtil.isHdrColor(outputFormat);
        if (bufferMode) {
            int strideWidth = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_STRIDE);
            int sliceHeight = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_SLICE_HEIGHT);
            int left = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_LEFT);
            int right = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_RIGHT);
            int top = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_TOP);
            int bottom = MediaFormatUtil.getInteger(outputFormat, KEY_CROP_BOTTOM);
            int width = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_WIDTH);
            int height = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_HEIGHT);
            if (strideWidth == 0 || sliceHeight == 0) {
                strideWidth = width;
                sliceHeight = height;
            }
            if (right == 0 || bottom == 0) {
                right = width ;
                bottom = height;
            }else {
                right = right+1;
                bottom = bottom+1;
            }
            int bitDepth = strideWidth / width == 2 ? 10 : 8;
            int yuv420Type = MediaFormatUtil.getInteger(outputFormat,VideoDecoder.KEY_YUV420_TYPE);
            bufferYUV420Renderer.setBufferFormat(strideWidth, sliceHeight,bitDepth , new Rect(left, top, right, bottom), yuv420Type);
        } else {
            if (hdrColor && GLY2YExtensionRenderer.isContainY2YEXT()) {
                y2yExtTextureRenderer.setBitDepth(profile10Bit ? 10 : 8);
                y2yExtTextureRenderer.setColorRange(getColorRange());
                textureY2YMode = true;
            } else {
                textureY2YMode = false;
            }
        }
    }

    @Override
    protected void onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
        super.onOutputBufferAvailable(outputBuffer, presentationTimeUs);
        if (bufferMode) {
            bufferYUV420Renderer.updateBuffer(outputBuffer);
        }
    }

    @Override
    protected synchronized void onOutputBufferRender(long presentationTimeUs) {
        if (!playerSurface.isValid()) {
            return;
        }
        GLTextureRenderer  textureRenderer;
        if (bufferMode){
            textureRenderer = texture2DRenderer;
            yuv420TextureTarget.setRenderSize(getWidth(), getHeight());
            bufferYUV420Renderer.renderToTarget(yuv420TextureTarget);
            textureRenderer.setTextureId(yuv420TextureTarget.textureId);
        }else {
            textureRenderer  =   textureY2YMode? y2yExtTextureRenderer : externalTextureRenderer;
            videoSurface.updateTexImage();
            videoSurface.getTransformMatrix(textureRenderer.getTextureMatrix().get());
        }

        GLTextureRenderer screenRenderer;
        if (transformList.isEmpty()) {
            screenRenderer = textureRenderer;
        } else {
            frontTarget.setRenderSize(getWidth(), getHeight());
            backTarget.setRenderSize(getWidth(), getHeight());
            textureRenderer.renderToTarget(frontTarget);
            for (GLVideoTransform videoTransform : transformList) {
                videoTransform.renderToTarget(frontTarget, backTarget);
                if (videoTransform.transformSuccess) {
                    GLRenderTextureTarget temp = frontTarget;
                    frontTarget = backTarget;
                    backTarget = temp;
                }
            }
            texture2DRenderer.setTextureId(frontTarget.textureId);
            screenRenderer =texture2DRenderer;
        }
        boolean bt2020PQ = getColorStandard() == MediaFormat.COLOR_STANDARD_BT2020 && getColorTransfer() == MediaFormat.COLOR_TRANSFER_ST2084;
        GLEnvWindowSurface windowSurface = playerSurface.getWindowSurface(bt2020PQ);
        if (windowSurface == null){
            return;
        }
        envContext.makeCurrent(windowSurface);
        screenTarget.setRenderSize(windowSurface.getWidth(), windowSurface.getHeight());
        screenTarget.clearColor();
        screenRenderer.renderToTarget(screenTarget);
        windowSurface.swapBuffers();
    }

    class PlayerSurface {
        private GLEnvWindowSurface windowSurface;

        private Surface outputSurface;

        private boolean bt202PQEnable;

        public synchronized void setOutputSurface(Surface surface) {
            outputSurface = surface;
            if (surface == null || !surface.isValid()) {
                if (windowSurface != null) {
                    windowSurface.release();
                    windowSurface = null;
                }
            }
        }


        public synchronized void release() {
            if (windowSurface == null) {
                return;
            }
            windowSurface.release();
            windowSurface = null;
        }
        public synchronized  boolean isValid(){
            return outputSurface  != null&& outputSurface.isValid();
        }

        public synchronized GLEnvWindowSurface getWindowSurface(boolean bt2020PQColorSpace) {
            if (outputSurface == null) {
                if (windowSurface != null) {
                    windowSurface.release();
                    windowSurface = null;
                }
                bt202PQEnable = false;
                return null;
            }
            boolean requestBT2020PQ = bt2020PQColorSpace && isSupportBT2020(outputSurface);
            if (windowSurface == null) {
                GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, outputSurface);
                if (requestBT2020PQ){
                    builder.setColorSpace(GLEnvSurface.COLOR_SPACE_BT2020_PQ);
                }
                bt202PQEnable = requestBT2020PQ;
                windowSurface = builder.build();
            } else if (outputSurface != windowSurface.getSurface() || bt202PQEnable !=requestBT2020PQ) {
                windowSurface.release();
                GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, outputSurface);
                if (requestBT2020PQ){
                    builder.setColorSpace(GLEnvSurface.COLOR_SPACE_BT2020_PQ);
                }
                bt202PQEnable = requestBT2020PQ;
                windowSurface = builder.build();
            }
            if (!windowSurface.isValid()) {
                windowSurface.release();
                bt202PQEnable = false;
                windowSurface = null;
            }
            return windowSurface;
        }

        private boolean isSupportBT2020(Surface surface) {
            if (surface == null){
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                return true;
            }
            String str = surface.toString();
            String pattern = "Surface\\(name=([^)]+)\\)";
            Pattern regexPattern = Pattern.compile(pattern);
            Matcher matcher = regexPattern.matcher(str);
            if (!matcher.find()){
                return false;
            }
            String extractedValue = matcher.group(1);
            return extractedValue.equals("null");
        }
    }
}
