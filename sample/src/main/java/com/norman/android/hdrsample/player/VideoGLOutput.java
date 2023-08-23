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
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoGLOutput extends VideoOutput {

    private static final float DEFAULT_WAIT_TIME_SECOND = 0.2f;

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


    private GLRenderTextureTarget frontTarget = new GLRenderTextureTarget();

    private GLRenderTextureTarget backTarget = new GLRenderTextureTarget();

    private boolean bufferMode;

    private boolean textureY2YMode;
    private boolean profile10Bit;

    private @ColorSpace int colorSpace;

    private int maxContentLuminance;
    private int colorRange;
    private VideoView videoView;

    private VideoView.SurfaceSubscriber surfaceSubscriber = new VideoView.SurfaceSubscriber() {
        @Override
        public void onSurfaceAvailable(Surface surface, int width, int height) {

            playerSurface.setOutputSurface(surface);

        }

        @Override
        public void onSurfaceRedraw() {
            waitNextFrame(DEFAULT_WAIT_TIME_SECOND);
        }

        @Override
        public void onSurfaceDestroy() {
            playerSurface.setOutputSurface(null);
        }
    };

    private OutputSizeSubscriber outputSizeSubscriber = new OutputSizeSubscriber() {
        @Override
        public void onOutputSizeChange(int width, int height) {
            videoView.setAspectRatio(width * 1.0f / height);
        }
    };



    public static VideoGLOutput create() {
        return new VideoGLOutput();
    }


    @Override
    protected void onOutputStop() {
        playerSurface.release();
        envContextManager.detach();
        if (videoSurface != null) {
            videoSurface.release();
        }
    }

    @Override
    public synchronized void setOutputSurface(Surface surface) {
        setOutputVideoView(null);
        this.playerSurface.setOutputSurface(surface);
    }

    @Override
    public synchronized void setOutputVideoView(VideoView view) {
        if (videoView == view) {
            return;
        }
        VideoView oldView = videoView;
        if (oldView != null) {
            oldView.unsubscribe(surfaceSubscriber);
            unsubscribe(outputSizeSubscriber);
        }
        videoView = view;
        if (videoView != null) {
            subscribe(outputSizeSubscriber);
            videoView.subscribe(surfaceSubscriber);
        }
    }

    @Override
    protected void onOutputPrepare(MediaFormat inputFormat) {
        profile10Bit = MediaFormatUtil.is10BitProfile(inputFormat);
        GLEnvConfigSimpleChooser.Builder envConfigChooser = new GLEnvConfigSimpleChooser.Builder();
        if (profile10Bit) {
            envConfigChooser.setRedSize(10);
            envConfigChooser.setGreenSize(10);
            envConfigChooser.setBlueSize(10);
            envConfigChooser.setAlphaSize(2);
//            部分手机用RGBA1010102不支持HDR PQSurface，要用RGBA16161616才行
//            envConfigChooser.setRedSize(16);
//            envConfigChooser.setGreenSize(16);
//            envConfigChooser.setBlueSize(16);
//            envConfigChooser.setAlphaSize(16);
        }
        envContextManager = GLEnvContextManager.create(envConfigChooser.build());
        envContextManager.attach();
        envContext = envContextManager.getEnvContext();
        if (profile10Bit && videoDecoder.isSupport10BitYUV420BufferMode()) {
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
        colorRange = MediaFormatUtil.getColorRange(outputFormat);
        colorSpace = MediaFormatUtil.getInteger(outputFormat,KEY_COLOR_SPACE,COLOR_SPACE_SDR);
        ByteBuffer hdrStaticInfo = MediaFormatUtil.getByteBuffer(outputFormat, MediaFormat.KEY_HDR_STATIC_INFO);
        if (hdrStaticInfo != null) {
            hdrStaticInfo.clear();
            hdrStaticInfo.position(1);
            hdrStaticInfo.limit(hdrStaticInfo.capacity());
            hdrStaticInfo.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer shortBuffer = hdrStaticInfo.asShortBuffer();
            int primaryRChromaticityX = shortBuffer.get(0);
            int primaryRChromaticityY = shortBuffer.get(1);;
            int primaryGChromaticityX = shortBuffer.get(2);;
            int primaryGChromaticityY = shortBuffer.get(3);;
            int primaryBChromaticityX = shortBuffer.get(4);;
            int primaryBChromaticityY = shortBuffer.get(5);;
            int whitePointChromaticityX = shortBuffer.get(6);;
            int whitePointChromaticityY = shortBuffer.get(7);;
            int maxMasteringLuminance = shortBuffer.get(8);;
            int minMasteringLuminance = shortBuffer.get(9);;
            maxContentLuminance = shortBuffer.get(10);;
            maxContentLuminance =   maxContentLuminance <=0? 1000: maxContentLuminance;
            int maxFrameAverageLuminance = shortBuffer.get(11);
        }else{
            maxContentLuminance = 0;
        }
        if (bufferMode) {
            int strideWidth = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_STRIDE);
            int sliceHeight = MediaFormatUtil.getInteger(outputFormat, MediaFormat.KEY_SLICE_HEIGHT);
            int yuv420Type = MediaFormatUtil.getInteger(outputFormat, VideoDecoder.KEY_YUV420_TYPE);
            int bitDepth = strideWidth / width == 2 ? 10 : 8;
            bufferYUV420Renderer.setBufferFormat(strideWidth, sliceHeight, bitDepth, new Rect(cropLeft, cropTop, cropRight, cropBottom), yuv420Type);
        } else {
            if (colorSpace !=COLOR_SPACE_SDR && GLY2YExtensionRenderer.isContainY2YEXT()) {
                y2yExtTextureRenderer.setBitDepth(profile10Bit ? 10 : 8);
                y2yExtTextureRenderer.setColorRange(colorRange);
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
        GLTextureRenderer textureRenderer;
        if (bufferMode) {
            textureRenderer = texture2DRenderer;
            yuv420TextureTarget.setRenderSize(width, height);
            bufferYUV420Renderer.renderToTarget(yuv420TextureTarget);
            textureRenderer.setTextureId(yuv420TextureTarget.textureId);
        } else {
            textureRenderer = textureY2YMode ? y2yExtTextureRenderer : externalTextureRenderer;
            videoSurface.updateTexImage();
            videoSurface.getTransformMatrix(textureRenderer.getTextureMatrix());
        }
        GLTextureRenderer screenRenderer;
        int finalColorSpace = colorSpace;
        if (transformList.isEmpty()) {
            screenRenderer = textureRenderer;
        } else {
            frontTarget.setBitDepth(profile10Bit ? 16 : 8);
            frontTarget.setRenderSize(width, height);
            backTarget.setRenderSize(width, height);
            textureRenderer.renderToTarget(frontTarget);
            frontTarget.setColorSpace(colorSpace);
            frontTarget.setMaxContentLuminance(maxContentLuminance);
            for (GLVideoTransform videoTransform : transformList) {
                videoTransform.renderToTarget(frontTarget, backTarget);
                if (videoTransform.transformSuccess) {
                    GLRenderTextureTarget temp = frontTarget;
                    frontTarget = backTarget;
                    backTarget = temp;
                }
            }
            texture2DRenderer.setTextureId(frontTarget.textureId);
            finalColorSpace = frontTarget.colorSpace;
            screenRenderer = texture2DRenderer;
        }
        GLEnvWindowSurface windowSurface = playerSurface.getWindowSurface(finalColorSpace);
        if (windowSurface == null) {
            return;
        }
        envContext.makeCurrent(windowSurface);
        screenTarget.setRenderSize(windowSurface.getWidth(), windowSurface.getHeight());
        screenTarget.clearColor();
        screenRenderer.renderToTarget(screenTarget);
        windowSurface.setPresentationTime(TimeUtil.microToNano(presentationTimeUs));
        windowSurface.swapBuffers();
    }

    class PlayerSurface {
        private GLEnvWindowSurface windowSurface;

        private Surface outputSurface;

        private int colorSpace;

        public synchronized void setOutputSurface(Surface surface) {
            outputSurface = surface;
            if (surface == null || !surface.isValid()) {
                release();
            }
        }


        public synchronized void release() {
            if (windowSurface == null) {
                return;
            }
            windowSurface.release();
            windowSurface = null;
        }

        public synchronized boolean isValid() {
            return outputSurface != null && outputSurface.isValid();
        }

        public synchronized GLEnvWindowSurface getWindowSurface(@ColorSpace int  requestColorSpace) {
            if (outputSurface == null) {
                release();
                return null;
            }
            if (windowSurface == null ||
                    outputSurface != windowSurface.getSurface() ||
                    this.colorSpace != requestColorSpace) {
                release();
                GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, outputSurface);
                if (isSupportBT2020(outputSurface)) {
                    if (requestColorSpace == COLOR_SPACE_BT2020_PQ){
                        if (builder.isSupportBT2020PQ()){
                            builder.setColorSpace(GLEnvSurface.EGL_COLOR_SPACE_BT2020_PQ);
                        }
                    }else if (requestColorSpace == COLOR_SPACE_BT2020_HLG){
                        if (builder.isSupportBT2020HLG()){
                            builder.setColorSpace(GLEnvSurface.EGL_COLOR_SPACE_BT2020_HLG);
                        }
                    }
                }
                windowSurface = builder.build();
                this.colorSpace = requestColorSpace;
            }
            if (!windowSurface.isValid()) {
                release();
            }
            return windowSurface;
        }

        /**
         * 如果是SurfaceView的Surface(通过判断toString是否包含字符串null)或者版本13以上Surface就支持BT2020
         *
         * @param surface
         * @return
         */
        private boolean isSupportBT2020(Surface surface) {
            if (surface == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return true;
            }
            String str = surface.toString();
            String pattern = "Surface\\(name=([^)]+)\\)";
            Pattern regexPattern = Pattern.compile(pattern);
            Matcher matcher = regexPattern.matcher(str);
            if (!matcher.find()) {
                return false;
            }
            String extractedValue = matcher.group(1);
            return extractedValue.equals("null");
        }
    }
}
