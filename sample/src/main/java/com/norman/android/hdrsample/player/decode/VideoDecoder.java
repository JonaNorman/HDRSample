package com.norman.android.hdrsample.player.decode;

import android.view.Surface;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface VideoDecoder extends Decoder {

    //解码时会在OutputFormat中增加这行属性，表示解码出来的视频是哪种YUV420
    String KEY_YUV420_TYPE = "yuv420Type";
    int BUFFER_MODE = 1;
    int SURFACE_MODE = 2;
    @IntDef({BUFFER_MODE, SURFACE_MODE})
    @Retention(RetentionPolicy.SOURCE)
    @interface OutputMode {
    }

    static VideoDecoder create() {
        return new VideoDecoderImpl();
    }

    /**
     * 解码Surface，只能在SurfaceMode时使用
     * 支持动态设置Surface，初始化也可以不穿Surface
     * @param surface
     */
    void setOutputSurface(Surface surface);


    /**
     * 解码模式是Surface还是Buffer
     * @param outputMode
     */
    void setOutputMode(@OutputMode int outputMode);

    /**
     * 解码到Buffer时是否支持10位YUV420，10位YUV420实际是16位存储的
     * @return
     */
    boolean isSupport10BitYUV420BufferMode();

    boolean isSupportColorFormat(int colorFormat);

}
