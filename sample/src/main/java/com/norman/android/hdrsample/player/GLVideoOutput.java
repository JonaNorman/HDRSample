package com.norman.android.hdrsample.player;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 经过OpenGL中转
 */
public abstract class GLVideoOutput extends VideoOutput {


    /**
     * 视频的纹理来源
     */

    @IntDef({TextureSource.AUTO, TextureSource.BUFFER, TextureSource.EXT, TextureSource.Y2Y, TextureSource.OES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextureSource {
        /**
         * 纹理来源，自动从支持的方式中选择, 推荐使用这个
         */

        int AUTO = 0;
        /**
         * YUV420 Buffer中转纹理
         */
        int BUFFER = 1;
        /**
         * 从OES纹理或Y2Y纹理转成2D纹理
         */
        int EXT = 2;
        /**
         * 从Y2Y纹理转成2D纹理
         */
        int Y2Y = 3;
        /**
         * 从OES纹理转成2D纹理
         */
        int OES = 4;
    }


    @IntDef({HdrBitDepth.BIT_DEPTH_8, HdrBitDepth.BIT_DEPTH_10, HdrBitDepth.BIT_DEPTH_16})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HdrBitDepth {
        /**
         * HDR显示时用8位，8位导致HDR视频精度不够只适合在测试时使用
         */

        int BIT_DEPTH_8 = 0;
        /**
         * Surface支持HDR显示时用10位(如果不支持，会自动切成8位)，视频正常用10位就够了，但是alpha只有2位，如果希望加大alpha，可以选择BIT_DEPTH_16
         * 如果发现部分手机不支持SurfaceView直接加载HDR，也可以尝试改成BIT_DEPTH_16
         */

        int BIT_DEPTH_10 = 1;
        /**
         * 最终显示HDR纹理时用16位(如果不支持，会自动切成8位)
         */
        int BIT_DEPTH_16 = 2;
    }

    /**
     * 经过OpenGL中转
     *
     * @return
     */
    public static GLVideoOutput create() {
        return new GLVideoOutputImpl();
    }



    public abstract void setTextureSource(@TextureSource int textureSource);

    public abstract @TextureSource int getTextureSource();


    public abstract void setHdrBitDepth(@HdrBitDepth int hdrDisplayBitDepth);

    public abstract @HdrBitDepth int getHdrDisplayBitDepth();

    public abstract void addVideoTransform(GLVideoTransform videoTransform);




}
