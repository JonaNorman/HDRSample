package com.norman.android.hdrsample.player.decode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MediaFormat的ColorFormat工具类
 * 功能1:根据colorFormat判断是哪种YUV420
 * 功能2:判断colorFormat判断表示编解码器是否支持10位YUV420解码，当前只测试了几款手机，其他手机逻辑不一定正确
 */
class ColorFormatHelper {

    //四种YUV420

    // 不同厂商支持的YUV420属性，参考自https://github.com/Parseus/codecinfo

    //博通
    private static final ColorFormatList BROADCOM_YUV420_LIST = new ColorFormatList(
            new ColorFormat("OMX_COLOR_FormatYUV420_10PackedPlanar", 0x7F00000C),
            new ColorFormat("OMX_COLOR_FormatYUV420_16PackedPlanar", 0x7F00000A),
            new ColorFormat("OMX_COLOR_FormatYUV420_UVSideBySide", 0x7F00000E));

    // 其他
    private static final ColorFormatList OTHER_YUV420_LIST = new ColorFormatList(
            new ColorFormat("OMX_INTEL_COLOR_FormatYUV420PackedSemiPlanar", 0x7FA00E00),
            new ColorFormat("OMX_INTEL_COLOR_FormatYUV420PackedSemiPlanar_Tiled", 0x7FA00F00),
            new ColorFormat("OMX_TI_COLOR_FormatYUV420PackedSemiPlanar", 0x7F000100),
            new ColorFormat("OMX_TI_COLOR_FormatYUV420PackedSemiPlanarInterlaced", 0x7F000001));

    //高通
    private static final ColorFormatList QUALCOMM_YUV420_LIST = new ColorFormatList(
            new ColorFormat("QOMX_COLOR_FormatYUV420PackedSemiPlanar16m2ka", 0x7FA30C02),
            new ColorFormat("QOMX_COLOR_FORMATYUV420PackedSemiPlanar32m", 0x7FA30C04),
            new ColorFormat("QOMX_COLOR_FORMATYUV420PackedSemiPlanar32m10bitCompressed", 0x7FA30C09),
            new ColorFormat("QOMX_COLOR_FORMATYUV420PackedSemiPlanar32mCompressed", 0x7FA30C06),
            new ColorFormat("QOMX_COLOR_FORMATYUV420PackedSemiPlanar32mMultiView", 0x7FA30C05),
            new ColorFormat("QOMX_COLOR_FormatYUV420PackedSemiPlanar512m", 0x7FA30C0B),
            new ColorFormat("QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka", 0x7FA30C03),
            new ColorFormat("QOMX_COLOR_FORMATYUV420SemiPlanarP010Venus", 0x7FA30C0A)
    );

    //三星
    private static final ColorFormatList SAMSUNG_YUV420_LIST = new ColorFormatList(
            new ColorFormat("OMX_SEC_COLOR_Format10bitYUV420SemiPlanar", 0x7F000015),
            new ColorFormat("OMX_SEC_COLOR_FormatANBYUV420SemiPlanar", 0x100),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420Planar_SBS_LR", 0x7FC0000B),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420Planar_SBS_RL", 0x7FC0000C),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420Planar_TB_LR", 0x7FC0000D),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420Planar_TB_RL", 0x7FC0000E),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420SemiPlanar_SBS_LR", 0x7FC00007),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420SemiPlanar_SBS_RL", 0x7FC00008),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420SemiPlanar_TB_LR", 0x7FC00009),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420SemiPlanar_TB_RL", 0x7FC0000A),
            new ColorFormat("OMX_SEC_COLOR_FormatYUV420SemiPlanarInterlace", 0x7F000014)
    );

    //索尼

    private static final ColorFormatList SONY_YUV420_LIST = new ColorFormatList(
            new ColorFormat("OMX_COLOR_FormatYUV420MBPackedSemiPlanar", 0x7FFFFFFE),
            new ColorFormat("OMX_STE_COLOR_FormatYUV420PackedSemiPlanarMB", 0x7FA00000)
    );

    //Android标准
    private static final ColorFormatList STANDARD_YUV420_LIST = new ColorFormatList(
            new ColorFormat("COLOR_FormatYUV420Flexible", 0x7F420888),
            new ColorFormat("COLOR_FormatYUV420PackedPlanar", 0x14),
            new ColorFormat("COLOR_FormatYUV420PackedSemiPlanar", 0x27),
            new ColorFormat("COLOR_FormatYUV420Planar", 0x13),
            new ColorFormat("COLOR_FormatYUV420Planar16", 0x7F42016B),
            new ColorFormat("COLOR_FormatYUV420SemiPlanar", 0x15),
            new ColorFormat("COLOR_QCOM_FormatYUV420PackedSemiPlanar32m", 0x7FA30C04),
            new ColorFormat("COLOR_QCOM_FormatYUV420PackedSemiPlanar64x32Tile2m8ka", 0x7FA30C03),
            new ColorFormat("COLOR_QCOM_FormatYUV420SemiPlanar", 0x7FA30C00),
            new ColorFormat("COLOR_TI_FormatYUV420PackedSemiPlanar", 0x7F000100),
            new ColorFormat("COLOR_FormatYUVP010", 0x36)
    );

    /**
     * 根据不同的解码器的名称找到对应的YUV420属性
     *
     * @param codecName
     * @return
     */
    static ColorFormatList findYUV420List(String codecName) {
        codecName = codecName.toLowerCase();
        if (codecName.contains("brcm")) {//博通
            return BROADCOM_YUV420_LIST;
        } else if (codecName.contains("qcom")
                || codecName.contains("qti")
                || codecName.contains("ittiam")) {//高通
            return QUALCOMM_YUV420_LIST;
        } else if (codecName.contains("omx.sec")
                || codecName.contains("exynos")) {//三星
            return SAMSUNG_YUV420_LIST;
        } else if (codecName.contains("omx.st")) {//索尼
            return SONY_YUV420_LIST;
        } else if (codecName.contains("omx.ti")
                || codecName.contains("intel")
                || codecName.contains("omx.rk")) {//其他
            return OTHER_YUV420_LIST;
        } else {
            return STANDARD_YUV420_LIST;
        }
    }

    /**
     * 根据解码器名称和colorFormat查找视频是哪种YUV420，如果判断不出来返回0，0表示未知
     * @param codecName
     * @param colorFormat
     * @return
     */
    public static int getYUV420Type(String codecName, int colorFormat) {
        ColorFormatList yuv420List = findYUV420List(codecName);
        if (yuv420List == null) {
            return 0;
        }
        ColorFormat yuv420Format = yuv420List.findColorFormat(colorFormat);
        if (yuv420Format == null) {//兜底
            yuv420Format = STANDARD_YUV420_LIST.findColorFormat(colorFormat);
        }
        if (yuv420Format == null){
            return 0;
        }
        //根据YUV420的名称来查找是哪种YUV420
        String name = yuv420Format.name;
        if (name.contains("PackedSemiPlanar")) {//Y+VU
            return VideoDecoder.NV21;
        } else if (name.contains("SemiPlanar") || name.contains("YUVP010")) {//Y+UV YUVP010根据文档是NV12
            return VideoDecoder.NV12;
        } else if (name.contains("PackedPlanar")) {//Y+V+U
            return VideoDecoder.YV12;
        } else {
            return VideoDecoder.YV21;// Y+U+V
        }
    }

    /**
     * 根据解码器名称和colorFormat查找解码是否支持10位YUV420，10位YUV420实际是16位存储的(不同手机不一定都是这样，现在测试几款手机都是这样，暂且这么认为)
     * @param codecName
     * @param colorFormat
     * @return
     */
    public static boolean isSupport10BitYUV420(String codecName, int colorFormat) {
        ColorFormatList yuv420List = findYUV420List(codecName);
        if (yuv420List == null) {
            return false;
        }
        ColorFormat yuv420Format = yuv420List.findColorFormat(colorFormat);
        if (yuv420Format == null) {
            return false;
        }
        String name = yuv420Format.name;
        if (name.contains("Compressed") || name.contains("MultiView") || name.contains("64x32")) {
            return false;//根据上面的属性列表去除肯定不是10位的情况
        }
        // 包含10、16、32的属性是支持10位YUV420的
        return name.contains("10") || name.contains("16") || name.contains("32");
    }


    static class ColorFormatList {


        List<ColorFormat> colorFormatList = new ArrayList<>();

        public ColorFormatList(ColorFormat... colorFormats) {
            colorFormatList.addAll(Arrays.asList(colorFormats));
        }

        public ColorFormat findColorFormat(int colorFormat) {
            for (ColorFormat format : colorFormatList) {
                if (format.value == colorFormat) {
                    return format;
                }
            }
            return null;
        }

    }


    static class ColorFormat {
        String name;
        int value;

        public ColorFormat(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }


}