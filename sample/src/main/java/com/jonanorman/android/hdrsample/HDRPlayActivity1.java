//package com.jonanorman.android.hdrsample;
//
//import android.content.Context;
//import android.content.res.AssetFileDescriptor;
//import android.content.res.AssetManager;
//import android.graphics.SurfaceTexture;
//import android.media.MediaCodec;
//import android.media.MediaCodecList;
//import android.media.MediaExtractor;
//import android.media.MediaFormat;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.view.Display;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.WindowManager;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
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
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.List;
//
//public class HDRPlayActivity1 extends AppCompatActivity {
//
//    private static final String KEY_CSD_0 = "csd-0";
//    private static final String KEY_CSD_1 = "csd-1";
//    private static final String KEY_CROP_LEFT = "crop-left";
//    private static final String KEY_CROP_RIGHT = "crop-right";
//    private static final String KEY_CROP_TOP = "crop-top";
//    private static final String KEY_CROP_BOTTOM = "crop-bottom";
//
//    int extractorTrackIndex = -1;
//    String extractorMimeType = null;
//    long extractorDurationUs = 0;
//    int extractorWidth = 0;
//    int extractorHeight = 0;
//    int extractorFrameRate;
//    int extractorProfile;
//    int extractorProfileLevel;
//    ByteBuffer extractorCsd0Buffer = null;
//    ByteBuffer extractorCsd1Buffer = null;
//    boolean hasVideo = false;
//    MediaFormat extractorFormat = null;
//    MediaExtractor extractor = null;
//    MediaCodec mediaCodec;
//    HDRPlayActivity1.CodecCallBack decodeCallBack;
//    MediaFormat decodeFormat;
//    int decodeColorFormat;
//    int decodeColorStandard;
//    int decodeColorRange;
//    int decodeColorTransfer;
//    int decodeWidth;
//    int decodeHeight;
//
//    GLRenderClient renderClient;
//    GLTexture oesTexture;
//    SurfaceTexture oesSurfaceTexture;
//
//    GLTextureLayer rgbTextureLayer;
//
//
//
//    Surface surface;
//    Handler handler;
//    TimeStamp timeStamp;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_hdr_player);
//
//
//        SurfaceView surfaceView = findViewById(R.id.surfaceview);
//        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                HDRPlayActivity1.this.surface = holder.getSurface();
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
////        TextureView textureView = findViewById(R.id.textureView);
////        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
////            @Override
////            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
////                HDRPlayActivity1.this.surface = new Surface(surface);
////                handler.post(new Runnable() {
////                    @Override
////                    public void run() {
////                        startDecode();
////                    }
////                });
////            }
////
////            @Override
////            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
////
////            }
////
////            @Override
////            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
////                return false;
////            }
////
////            @Override
////            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
////
////            }
////        });
//        HandlerThread handlerThread = new HandlerThread("HdrPlayer");
//        handlerThread.start();
//        timeStamp = TimeStamp.ofMicros(0);
//        handler = new Handler(handlerThread.getLooper());
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                    //获取Android上的显示亮度
//                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                    Display display = wm.getDefaultDisplay();
//                    Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();
//
//                    hdrCapabilities.getSupportedHdrTypes();
//
//                }
//                EGL14RenderClientFactory egl14RenderClientFactory = new EGL14RenderClientFactory();
//                EGLConfigSimpleChooser.Builder simpleChooser = new EGLConfigSimpleChooser.Builder();
//                simpleChooser.setRedSize(10);
//                simpleChooser.setGreenSize(10);
//                simpleChooser.setBlueSize(10);
//                egl14RenderClientFactory.setEGLConfigChooser(simpleChooser.build());
//                renderClient = egl14RenderClientFactory.create();
//                renderClient.attachCurrentThread();
//                oesTexture = new GL20Texture(renderClient, GLTexture.Type.TEXTURE_OES);
//                oesTexture.setMinFilter(GLTexture.FilterMode.LINEAR);
//                oesTexture.setMagFilter(GLTexture.FilterMode.LINEAR);
//                oesTexture.setTextureMatrix(new Matrix4(GLTexture.TEXTURE_FLIP_Y_MATRIX));
//                rgbTextureLayer = new GLTextureLayer();
//                rgbTextureLayer.setFragmentShaderCode(AssetsUtils.getString(HDRPlayActivity1.this, "oes_hdr.glsl"));
//
//                oesSurfaceTexture = new SurfaceTexture(oesTexture.getTextureId());
//
//                oesSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//                    @Override
//                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//
//
//                        if (count>0){
//                            surfaceTexture.updateTexImage();
//                            rgbTextureRenderToSurface(oesTexture);
//                            count--;
//                        }
//
//
//
//
//                    }
//                },new Handler(Looper.myLooper()));
//
//            }
//        });
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                load("1.mp4");
//            }
//        });
//
//
//    }
//
//    int count =1000;
//    boolean aaa = true;
//
//    private void load(String assetName) {
//        AssetFileDescriptor fd = null;
//        try {
//            if (extractor != null) extractor.release();
//            extractor = new MediaExtractor();
//            AssetManager assetManager = getAssets();
//            fd = assetManager.openFd(assetName);
//            extractor.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
//            hasVideo = false;
//            decodeFormat = null;
//            for (int i = 0; i < extractor.getTrackCount(); i++) {
//                MediaFormat format = extractor.getTrackFormat(i);
//                String mime = format.containsKey(MediaFormat.KEY_MIME) ? format.getString(MediaFormat.KEY_MIME) : null;
//                if (mime != null && mime.toLowerCase().startsWith("video")) {
//                    extractorTrackIndex = i;
//                    extractorMimeType = mime;
//                    extractorDurationUs = format.containsKey(MediaFormat.KEY_DURATION) ? format.getLong(MediaFormat.KEY_DURATION) : 0;
//                    extractorWidth = format.containsKey(MediaFormat.KEY_WIDTH) ? format.getInteger(MediaFormat.KEY_WIDTH) : 0;
//                    extractorHeight = format.containsKey(MediaFormat.KEY_HEIGHT) ? format.getInteger(MediaFormat.KEY_HEIGHT) : 0;
//                    extractorFrameRate = format.containsKey(MediaFormat.KEY_FRAME_RATE) ? format.getInteger(MediaFormat.KEY_FRAME_RATE) : 0;
//                    extractorCsd0Buffer = format.containsKey(KEY_CSD_0) ? format.getByteBuffer(KEY_CSD_0) : null;
//                    extractorCsd1Buffer = format.containsKey(KEY_CSD_1) ? format.getByteBuffer(KEY_CSD_1) : null;
//                    extractorProfile = format.containsKey(MediaFormat.KEY_PROFILE) ? format.getInteger(MediaFormat.KEY_PROFILE) : -1;
//                    extractorProfileLevel = format.containsKey(MediaFormat.KEY_LEVEL) ? format.getInteger(MediaFormat.KEY_LEVEL) : -1;
//                    extractorFormat = format;
//                    extractor.selectTrack(extractorTrackIndex);
//                    extractor.seekTo(0, MediaExtractor.SEEK_TO_NEXT_SYNC);
//                    hasVideo = true;
//                    break;
//                }
//            }
//        } catch (IOException e) {
//            RuntimeException runtimeException = new RuntimeException(e);
//            runtimeException.setStackTrace(e.getStackTrace());
//            throw runtimeException;
//
//        } finally {
//            if (!hasVideo) resetVideoFormat();
//            if (fd != null) {
//                try {
//                    fd.close();
//                } catch (IOException e) {
//                }
//            }
//        }
//
//    }
//
//    private void resetVideoFormat() {
//        extractorTrackIndex = -1;
//        extractorMimeType = null;
//        extractorDurationUs = 0;
//        extractorWidth = 0;
//        extractorHeight = 0;
//        extractorFrameRate = 0;
//        extractorCsd0Buffer = null;
//        extractorCsd1Buffer = null;
//        extractorFormat = null;
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        handler.getLooper().quit();
//    }
//
//    public void startDecode() {
//        try {
//            if (mediaCodec != null) {
//                mediaCodec.stop();
//                mediaCodec.release();
//            }
//            MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
//            String codecName = findCodecName(mediaCodecList);
//            if (codecName != null) {
//                mediaCodec = MediaCodec.createByCodecName(codecName);
//            } else {
//                mediaCodec = MediaCodec.createDecoderByType(extractorMimeType);
//            }
//            MediaFormat inputDecodeFormat = new MediaFormat();
//            inputDecodeFormat.setString(MediaFormat.KEY_MIME, extractorMimeType);
//            inputDecodeFormat.setInteger(MediaFormat.KEY_WIDTH, extractorWidth);
//            inputDecodeFormat.setInteger(MediaFormat.KEY_HEIGHT, extractorHeight);
//            inputDecodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, extractorWidth * extractorHeight);
//            if (extractorCsd0Buffer != null) {
//                inputDecodeFormat.setByteBuffer("csd-0", extractorCsd0Buffer);
//            }
//            if (extractorCsd1Buffer != null) {
//                inputDecodeFormat.setByteBuffer("csd-1", extractorCsd1Buffer);
//            }
//            decodeCallBack = new HDRPlayActivity1.CodecCallBack();
//            mediaCodec.setCallback(decodeCallBack);
//
//            mediaCodec.configure(inputDecodeFormat, new Surface(oesSurfaceTexture), null, 0);
//            mediaCodec.start();
//
//        } catch (IOException e) {
//            RuntimeException runtimeException = new RuntimeException(e);
//            runtimeException.setStackTrace(e.getStackTrace());
//            throw runtimeException;
//        }
//
//    }
//
//
//    private String findCodecName(MediaCodecList mediaCodecList) {
//        MediaFormat findCodecFormat = extractorFormat;
//        Integer frameRate = null;
//        if (findCodecFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
//            frameRate = findCodecFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
//            findCodecFormat.setString(MediaFormat.KEY_FRAME_RATE, null);
//        }
//        String codecName = mediaCodecList.findDecoderForFormat(findCodecFormat);
//        if (frameRate != null) {
//            findCodecFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
//        }
//        return codecName;
//    }
//
//
//    class CodecCallBack extends MediaCodec.Callback {
//
//        @Override
//        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
//            ByteBuffer inputBuffer = codec.getInputBuffer(index);
//            ByteBuffer byteBuffer = inputBuffer;
//            byteBuffer.clear();
//            int sampleSize = extractor.readSampleData(byteBuffer, 0);
//            long sampleTimeUs = extractor.getSampleTime() > 0 ? extractor.getSampleTime() : extractor.getSampleTime();
//            int flags = extractor.getSampleFlags();
//            int sampleFlags = 0;
//            if (flags == MediaExtractor.SAMPLE_FLAG_SYNC) {
//                sampleFlags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
//            } else if (flags == MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) {
//                sampleFlags = MediaCodec.BUFFER_FLAG_PARTIAL_FRAME;
//            }
//            if (sampleSize == -1 || sampleTimeUs == -1) {
//                sampleFlags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
//                sampleSize = 0;
//                sampleTimeUs = 0;
//            }
//            byteBuffer.position(0);
//            byteBuffer.limit(sampleSize);
//            codec.queueInputBuffer(index, 0, sampleSize, sampleTimeUs, sampleFlags);
//            extractor.advance();
//        }
//
//        @Override
//        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
//            if (info.size <= 0) {
//                codec.releaseOutputBuffer(index, false);
//                return;
//            }
//            codec.releaseOutputBuffer(index, true);
//            try {
//                Thread.sleep(15);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
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
//            int width = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_WIDTH);
//            int height = getInteger(decodeFormat, extractorFormat, MediaFormat.KEY_HEIGHT);
//            int cropLeft = getInteger(decodeFormat, extractorFormat, KEY_CROP_LEFT);
//            int cropRight = getInteger(decodeFormat, extractorFormat, KEY_CROP_RIGHT);
//            int cropTop = getInteger(decodeFormat, extractorFormat, KEY_CROP_TOP);
//            int cropBottom = getInteger(decodeFormat, extractorFormat, KEY_CROP_BOTTOM);
//            decodeWidth = getSize(width, cropLeft, cropRight);
//            if (decodeWidth <= 0) {
//                throw new RuntimeException("not find width");
//            }
//            decodeHeight = getSize(height, cropTop, cropBottom);
//            if (decodeHeight <= 0) {
//                throw new RuntimeException("not find height");
//            }
//            oesTexture.setWidth(decodeWidth);
//            oesTexture.setHeight(decodeHeight);
//        }
//    }
//
//
//
//
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
//    private int getSize(int size, int left, int right) {
//        List<Integer> sizeList = new ArrayList<>();
//        if (size > 0) {
//            sizeList.add(size);
//        }
//        if (right - left > 0) {
//            sizeList.add(right - left + 1);
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
//
//
//
//}