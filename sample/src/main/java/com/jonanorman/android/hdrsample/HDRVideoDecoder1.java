package com.jonanorman.android.hdrsample;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

class HDRVideoDecoder1 {


    private static final int HAL_PIXEL_FORMAT_YCbCr_420_P010 = 0x11F;
    private static final int HAL_PIXEL_FORMAT_YCbCr_420_P010_UBWC = 0x124;
    private static final int HAL_PIXEL_FORMAT_YCbCr_420_P010_VENUS = 0x7FA30C0A;

    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";
    int decodeColorFormat;
    int decodeColorStandard;
    int decodeColorRange;
    int decodeColorTransfer;
    int decodeProfile;
    int decodeStrideWidth;
    int decodeStrideHeight;
    int decodeWidth;
    int decodeHeight;
    int decodeCropLeft;
    int decodeCropRight;
    int decodeCropTop;
    int decodeCropBottom;
    int decodeColorBitDepth;
    int decodeColorBitCount;
    int decodeColorBitMask;

    MediaFormat decodeFormat;

    private boolean isHdrProfile(int profile) {
        return profile == MediaCodecInfo.CodecProfileLevel.AVCProfileHigh10 ||
                profile == MediaCodecInfo.CodecProfileLevel.AV1ProfileMain10 ||
                profile == MediaCodecInfo.CodecProfileLevel.AV1ProfileMain10HDR10 ||
                profile == MediaCodecInfo.CodecProfileLevel.AV1ProfileMain10HDR10Plus ||
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10 ||
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10 ||
                profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10Plus;

    }

    private boolean isColorFormatYuv420(int colorFormat) {
        //        COLOR_FormatYUV420Planar//YV21  Y+U+V
//                COLOR_FormatYUV420PackedPlanar //YV12 Y+V+U
//                COLOR_FormatYUV420SemiPlanar/NV12  Y+UV
//                COLOR_FormatYUV420PackedSemiPlanar//NV21 Y+VU
        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {//YV21  Y+U+V
            return true;
        } else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {  //YV12 Y+V+U
            return true;
        } else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {//NV12  Y+UV
            return true;
        } else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar) {//NV21 Y+VU
            return true;
        } else if (isColorFormatYuv420P10(colorFormat)) {
            return true;
        }
        return false;
    }



    private boolean isColorFormatYuv420P10(int colorFormat) {
        return colorFormat == HAL_PIXEL_FORMAT_YCbCr_420_P010 || colorFormat == HAL_PIXEL_FORMAT_YCbCr_420_P010_UBWC || colorFormat == HAL_PIXEL_FORMAT_YCbCr_420_P010_VENUS || colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUVP010;
    }
}
