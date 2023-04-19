//package com.jonanorman.android.hdrsample;
//
//import android.content.Context;
//import android.content.res.AssetFileDescriptor;
//import android.content.res.AssetManager;
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaCodecList;
//import android.media.MediaExtractor;
//import android.media.MediaFormat;
//import android.opengl.GLES20;
//import android.opengl.GLES30;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.os.PowerManager;
//import android.provider.Settings;
//import android.view.Display;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.WindowManager;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.math.MathUtils;
//
//import com.jonanorman.android.hdrsample.layer.GLTextureLayer;
//import com.jonanorman.android.hdrsample.math.Matrix4;
//import com.jonanorman.android.hdrsample.math.TimeStamp;
//import com.jonanorman.android.hdrsample.opengl.EGLConfigSimpleChooser;
//import com.jonanorman.android.hdrsample.opengl.GLFrameBuffer;
//import com.jonanorman.android.hdrsample.opengl.GLRenderClient;
//import com.jonanorman.android.hdrsample.opengl.GLTexture;
//import com.jonanorman.android.hdrsample.opengl.egl14.EGL14RenderClientFactory;
//import com.jonanorman.android.hdrsample.opengl.gl20.GL20FrameBuffer;
//import com.jonanorman.android.hdrsample.opengl.gl20.GL20Texture;
//import com.jonanorman.android.renderclient.sample.R;
//
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.ArrayList;
//import java.util.List;
//
//public class HDRPlayActivity extends AppCompatActivity {
//
//
//
//    float maxScreenLuminance = 100;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_hdr_player);
//        SurfaceView surfaceView = findViewById(R.id.surfaceview);
//        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                HDRPlayActivity.this.surface = holder.getSurface();
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        startDecode();
//                    }
//                });
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//
//
//            }
//        });
//        HandlerThread handlerThread = new HandlerThread("HdrPlayer");
//        handlerThread.start();
//        timeStamp = TimeStamp.ofMicros(0);
//        handler = new Handler(handlerThread.getLooper());
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                EGL14RenderClientFactory egl14RenderClientFactory = new EGL14RenderClientFactory();
//                EGLConfigSimpleChooser.Builder simpleChooser = new EGLConfigSimpleChooser.Builder();
//                simpleChooser.setRenderGL30(true);
//                egl14RenderClientFactory.setEGLConfigChooser(simpleChooser.build());
//                renderClient = egl14RenderClientFactory.create();
//                renderClient.attachCurrentThread();
//                yuvTexture = new GL20Texture(renderClient, GLTexture.Type.TEXTURE_2D);
//                yuvTexture.setMinFilter(GLTexture.FilterMode.NEAREST);
//                yuvTexture.setMagFilter(GLTexture.FilterMode.NEAREST);
//                yuvTexture.setTextureMatrix(new Matrix4(GLTexture.TEXTURE_FLIP_Y_MATRIX));
//                rgbFrameBuffer = new GL20FrameBuffer(renderClient, 1, 1);
//                yuvTextureLayer = new GLTextureLayer();
//                rgbTextureLayer = new GLTextureLayer();
//                yuvTextureLayer.setVertexShaderCode(AssetsUtils.getString(HDRPlayActivity.this, "yuvtorgb.vsh"));
//                yuvTextureLayer.setFragmentShaderCode(AssetsUtils.getString(HDRPlayActivity.this, "yuvtorgb.fsh"));
//
//
//
//                toneMappingFrameBuffer = new GL20FrameBuffer(renderClient, 1, 1);
//                toneMappingTextureLayer = new GLTextureLayer();
//                toneMappingTextureLayer.setFragmentShaderCode(AssetsUtils.getString(HDRPlayActivity.this, "tonemapping_pq.glsl"));
//            }
//        });
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                load("1.mp4");
//            }
//        });
//    }
//
//
//
//
//
//
//    class CodecCallBack extends MediaCodec.Callback {
//
//        @Override
//        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
//
//        }
//
//        @Override
//        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
//            ByteBuffer outputBuffer = codec.getOutputBuffer(index);
//
//            if (info.size <= 0) {
//                codec.releaseOutputBuffer(index, false);
//                return;
//            }
//
//            float multipleSize = info.size * 1.0f / extractorWidth / extractorHeight;
//            toastMultipleSize(multipleSize);
//            if (multipleSize >= 3.0) {
//                decodeColorBitDepth = 10;
//                decodeColorBitCount = 16;
//                decodeColorBitMask = 6;
//            } else {
//                decodeColorBitDepth = 8;
//                decodeColorBitCount = 8;
//                decodeColorBitMask = 0;
//            }
//            yuvTextureLayer.setShaderParam("bitDepth", decodeColorBitDepth);
//            yuvTextureLayer.setShaderParam("bitMask", decodeColorBitMask);
//
//
//
//            if (aaa){
//                aaa =true;
//                bufferToYuvTexture(outputBuffer, info);
//                yuvTextureToRgbTexture();
//                rgbTextureToToneMapping();
//                rgbTextureRenderToSurface(toneMappingFrameBuffer.getAttachColorTexture());
//                codec.releaseOutputBuffer(index, false);
//            }
//
//        }
//
//        @Override
//        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
//            RuntimeException runtimeException = new RuntimeException(e);
//            throw runtimeException;
//        }
//
//        @Override
//        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
//            decodeFormat = format;
//            decodeColorFormat = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_COLOR_FORMAT);
//            if (!isColorFormatYuv420(decodeColorFormat)) {
//                throw new RuntimeException("can not support color format " + decodeColorFormat);
//            }
//            boolean yuv420p10 = false;
//            if (isColorFormatYuv420P10(decodeColorFormat)) {
//                yuv420p10 = true;
//                decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
//            }
//            decodeColorStandard = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_COLOR_STANDARD);
//            if (decodeColorStandard == -1 || decodeColorStandard == 0) {
//                decodeColorStandard = MediaFormat.COLOR_STANDARD_BT709;
//            }
//            decodeColorRange = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_COLOR_RANGE);
//            if (decodeColorRange == -1 || decodeColorRange == 0) {
//                decodeColorRange = MediaFormat.COLOR_RANGE_LIMITED;
//            }
//            decodeColorTransfer = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_COLOR_TRANSFER);
//            if (decodeColorTransfer == -1 || decodeColorTransfer == 0) {
//                decodeColorTransfer = MediaFormat.COLOR_TRANSFER_SDR_VIDEO;
//            }
//            decodeProfile = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_PROFILE);
//            if (decodeProfile == -1) {
//                throw new RuntimeException("not find profile");
//            }
//            int width = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_WIDTH);
//            int height = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_HEIGHT);
//            int cropLeft = getInteger(decodeFormat, extractorFormat, KEY_CROP_LEFT);
//            int cropRight = getInteger(decodeFormat, extractorFormat, KEY_CROP_RIGHT);
//            int cropTop = getInteger(decodeFormat, extractorFormat, KEY_CROP_TOP);
//            int cropBottom = getInteger(decodeFormat, extractorFormat, KEY_CROP_BOTTOM);
//            int strideWidth = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_STRIDE);
//            int sliceHeight = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_SLICE_HEIGHT);
//            decodeWidth = getSize(width, strideWidth, cropLeft, cropRight);
//            if (decodeWidth <= 0) {
//                throw new RuntimeException("not find width");
//            }
//            decodeHeight = getSize(height, sliceHeight, cropTop, cropBottom);
//            if (decodeHeight <= 0) {
//                throw new RuntimeException("not find height");
//            }
//            if (cropLeft != -1) {
//                decodeCropLeft = cropLeft;
//            } else {
//                decodeCropLeft = 0;
//            }
//            if (cropRight != -1) {
//                decodeCropRight = cropRight;
//            } else {
//                decodeCropRight = decodeCropLeft + decodeWidth - 1;
//            }
//
//            if (cropTop != -1) {
//                decodeCropTop = cropTop;
//            } else {
//                decodeCropTop = 0;
//            }
//            if (cropBottom != -1) {
//                decodeCropBottom = cropBottom;
//            } else {
//                decodeCropBottom = decodeCropTop + decodeHeight - 1;
//            }
//
//            if (strideWidth != -1) {
//                decodeStrideWidth = strideWidth;
//            } else {
//                decodeStrideWidth = decodeWidth;
//            }
//            if (sliceHeight != -1) {
//                decodeStrideHeight = sliceHeight;
//            } else {
//                decodeStrideHeight = decodeHeight;
//            }
//
//            if (yuv420p10) {
//                decodeStrideWidth = decodeStrideWidth / 2;
//                strideWidth = strideWidth / 2;
//            }
//            yuvTexture.setWidth(strideWidth);
//            yuvTexture.setHeight(sliceHeight);
//            rgbFrameBuffer.setSize(strideWidth, sliceHeight);
//            GLTexture rgbTexture = rgbFrameBuffer.getAttachColorTexture();
//            GLTexture oldTexture = rgbTexture.bind();
//            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(strideWidth * sliceHeight * 8);
//            GLES20.glTexImage2D(
//                    GLES20.GL_TEXTURE_2D,
//                    0,
//                    GLES30.GL_RGB10_A2,
//                    strideWidth,
//                    sliceHeight,
//                    0,
//                    GLES20.GL_RGBA,
//                    GLES30.GL_UNSIGNED_INT_2_10_10_10_REV,
//                    byteBuffer);
////            GLES20.glTexImage2D(
////                    GLES20.GL_TEXTURE_2D,
////                    0,
////                    GLES30.GL_RGBA16F,
////                    strideWidth,
////                    sliceHeight,
////                    0,
////                    GLES20.GL_RGBA,
////                    GLES20.GL_FLOAT,
////                    byteBuffer);
//            oldTexture.bind();
//
//            toneMappingFrameBuffer.setSize(strideWidth, sliceHeight);
//
//            yuvTextureLayer.setShaderParam("strideWidth", decodeStrideWidth);
//            yuvTextureLayer.setShaderParam("slideHeight", decodeStrideHeight);
//            yuvTextureLayer.setShaderParam("colorFormat", decodeColorFormat);
//            yuvTextureLayer.setShaderParam("colorStandard", decodeColorStandard);
//            yuvTextureLayer.setShaderParam("colorRange", decodeColorRange);
//            yuvTextureLayer.setShaderParam("yuv_texture", yuvTexture.getTextureId());
//
//
//            ByteBuffer hdrStaticInfo  = decodeFormat.getByteBuffer(MediaFormat.KEY_HDR_STATIC_INFO);
//            hdrStaticInfo.clear();
//            hdrStaticInfo.position(1);
//            hdrStaticInfo.limit(hdrStaticInfo.capacity());
//
//            ByteBuffer buffer = ByteBuffer.allocate(24);
//            buffer.order(ByteOrder.LITTLE_ENDIAN);
//            buffer.put(hdrStaticInfo);
//            buffer.clear();
//
//
//            int maxFrameAverageLuminance = buffer.asShortBuffer().get(11);
//            if (maxFrameAverageLuminance<=0){
//                maxFrameAverageLuminance = 400;
//            }
//
////            hdrStaticInfo.putShort((short) ((primaryRChromaticityX() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) ((primaryRChromaticityY() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) ((primaryGChromaticityX() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) ((primaryGChromaticityY() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) ((primaryBChromaticityX() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) ((primaryBChromaticityY() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) ((whitePointChromaticityX() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) ((whitePointChromaticityY() * MAX_CHROMATICITY) + 0.5f));
////            hdrStaticInfo.putShort((short) (maxMasteringLuminance() + 0.5f));
////            hdrStaticInfo.putShort((short) (minMasteringLuminance() + 0.5f));
////            hdrStaticInfo.putShort((short) maxContentLuminance());
////            hdrStaticInfo.putShort((short) maxFrameAverageLuminance());
//
//            toneMappingTextureLayer.setShaderParam("max_peak_luminance", maxFrameAverageLuminance);
//            float brightnessRate = 1.0f;
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                //获取Android上的显示亮度
//                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                Display display = wm.getDefaultDisplay();
//                Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();
//                maxScreenLuminance = Math.max(hdrCapabilities.getDesiredMaxAverageLuminance(), maxScreenLuminance);
//                maxScreenLuminance = Math.max(hdrCapabilities.getDesiredMaxLuminance(), maxScreenLuminance);
//                hdrCapabilities.getSupportedHdrTypes();
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                    val configuration = resources.configuration
////                    return configuration.isScreenHdr
////                }
//
//            }
//
//            try {
//                int currentBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
//                int maxBrightness = 256;
//                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                if(powerManager != null) {
//                    Field[] fields = powerManager.getClass().getDeclaredFields();
//                    for (Field field: fields) {
//
//                        //https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/os/PowerManager.java
//
//                        if(field.getName().equals("BRIGHTNESS_ON")) {
//                            field.setAccessible(true);
//                            try {
//                                maxBrightness = (int) field.get(powerManager);
//
//                            } catch (IllegalAccessException e) {
//                                e.printStackTrace();
//                            }
//                            break;
//                        }
//                    }
//                }
//                brightnessRate = MathUtils.clamp(currentBrightness*1.0f/maxBrightness,0.1f,1.0f);
//            } catch (Settings.SettingNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            toneMappingTextureLayer.setShaderParam("max_screen_luminance", maxScreenLuminance*brightnessRate);
//
//
//        }
//    }
//
//    private void rgbTextureToToneMapping() {
//        toneMappingTextureLayer.setTexture(rgbFrameBuffer.getAttachColorTexture());
//        toneMappingTextureLayer.render(toneMappingFrameBuffer, timeStamp);
//    }
//
//
//
//    private void bufferToYuvTexture(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
//        int offset = bufferInfo.offset;
//        int size = bufferInfo.size;
//        long presentationTimeUs = bufferInfo.presentationTimeUs;
//        int flags = bufferInfo.flags;
//        outputBuffer.position(offset);
//        outputBuffer.limit(offset + size);
//        yuvTexture.active(0);
//        GLTexture oldTexture = yuvTexture.bind();
//        if (decodeStrideWidth % 4 > 0) {
//            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
//        } else {
//            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4);
//        }
//        int yuvWidth = decodeStrideWidth;
//        int yuvHeight = decodeStrideHeight;
//        int internalformat = decodeColorBitCount == 16 ? GLES30.GL_R16UI : GLES30.GL_R8UI;
//        int format = GLES30.GL_RED_INTEGER;
//        int type = decodeColorBitCount == 16 ? GLES30.GL_UNSIGNED_SHORT : GLES30.GL_UNSIGNED_BYTE;
//
//
//        GLES20.glTexImage2D(
//                GLES20.GL_TEXTURE_2D,
//                0,
//                internalformat,
//                yuvWidth,
//                (int) (yuvHeight * 1.5),
//                0,
//                format,
//                type,
//                outputBuffer);
//        oldTexture.bind();
//
//    }
//
//
//    private void yuvTextureToRgbTexture() {
//        yuvTextureLayer.setTexture(yuvTexture);
//        yuvTextureLayer.render(rgbFrameBuffer, timeStamp);
//    }
//
//    private void rgbTextureRenderToSurface(GLTexture rgbTexture) {
//        rgbTextureLayer.setTexture(rgbTexture);
//        rgbTextureLayer.render(surface, timeStamp);
//    }
//
//
//    private int getInteger(MediaFormat decodeFormat, MediaFormat extractorFormat, String key) {
//        if (decodeFormat.containsKey(key)) {
//            return decodeFormat.getInteger(key);
//        } else if (extractorFormat.containsKey(key)) {
//            return extractorFormat.getInteger(key);
//        } else {
//            return -1;
//        }
//    }
//
//    private int getSize(int size, int strideSize, int left, int right) {
//        List<Integer> sizeList = new ArrayList<>();
//        if (size > 0) {
//            sizeList.add(size);
//        }
//        if (right - left > 0) {
//            sizeList.add(right - left + 1);
//        }
//        if (strideSize > 0) {
//            sizeList.add(strideSize);
//        }
//        if (sizeList.size() <= 0) {
//            return -1;
//        }
//        int finalSize = Integer.MAX_VALUE;
//        for (Integer integer : sizeList) {
//            finalSize = Math.min(finalSize, integer);
//        }
//        return finalSize;
//    }
//
//
//}