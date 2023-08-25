package com.norman.android.hdrsample.player;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 经过OpenGL中转
 */
public abstract class GLVideoOutput extends VideoOutput {


    /**
     * 纹理来源，自动从支持的方式中选择, 推荐使用这个
     */

    public static final int TEXTURE_SOURCE_TYPE_AUTO = 0;

    /**
     * YUV420 Buffer中转纹理
     */
    public static final int TEXTURE_SOURCE_TYPE_BUFFER = 1;
    /**
     * 从OES纹理或Y2Y纹理转成2D纹理
     */
    public static final int TEXTURE_SOURCE_TYPE_EXT = 2;
    /**
     * 从Y2Y纹理转成2D纹理
     */
    public static final int TEXTURE_SOURCE_TYPE_Y2Y = 3;

    /**
     * 从OES纹理转成2D纹理
     */
    public static final int TEXTURE_SOURCE_TYPE_OES = 4;

    /**
     * 视频的纹理类型
     */

    @IntDef({TEXTURE_SOURCE_TYPE_AUTO, TEXTURE_SOURCE_TYPE_BUFFER, TEXTURE_SOURCE_TYPE_EXT, TEXTURE_SOURCE_TYPE_Y2Y, TEXTURE_SOURCE_TYPE_OES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextureSourceType {
    }


    /**
     * 最终显示HDR纹理时用10位(如果不支持，会自动切成8位)，视频正常用10位就够了，但是alpha只有2位，如果希望加大alpha，可以选择HDR_DISPLAY_BIT_DEPTH_16
     * 如果发现部分手机不支持SurfaceView直接加载HDR，也可以尝试改成HDR_DISPLAY_BIT_DEPTH_16
     */

    public static final int HDR_DISPLAY_BIT_DEPTH_10 = 10;

    /**
     * 最终显示HDR纹理时用16位(如果不支持，会自动切成8位)
     */
    public static final int HDR_DISPLAY_BIT_DEPTH_16 = 16;

    @IntDef({HDR_DISPLAY_BIT_DEPTH_10, HDR_DISPLAY_BIT_DEPTH_16})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HdrDisplayBitDepth {
    }

    public abstract void addVideoTransform(GLVideoTransform videoTransform);

    public static GLVideoOutput create() {
        return new GLVideoOutputImpl();
    }

    public static GLVideoOutput create(@TextureSourceType int textureSourceType) {
        return new GLVideoOutputImpl(textureSourceType);
    }


    public static class Builder {

        @TextureSourceType
        int textureSoureceType;

        @HdrDisplayBitDepth
        int hdrDisplayBitDepth;


        public Builder() {

        }


        public void setTextureSoureceType(@TextureSourceType int textureSoureceType) {
            this.textureSoureceType = textureSoureceType;
        }

        public void setHdrDisplayBitDepth(int hdrDisplayBitDepth) {
            this.hdrDisplayBitDepth = hdrDisplayBitDepth;
        }

        public GLVideoOutput build() {
            return new GLVideoOutputImpl(textureSoureceType);
        }
    }
}
