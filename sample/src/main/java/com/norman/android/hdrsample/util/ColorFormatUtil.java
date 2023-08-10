package com.norman.android.hdrsample.util;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorFormatUtil {

    public static final int YV21 = 1;// Y+U+V
    public static final int YV12 = 2;//Y+V+U
    public static final int NV12 = 3;//Y+UV
    public static final int NV21 = 4;//Y+VU

    @IntDef({YV21, YV12, NV12, NV21})
    @Retention(RetentionPolicy.SOURCE)
    public @interface YUV420Type {
    }

    private static final ColorFormatList BROADCOM_YUV420_LIST = new ColorFormatList(
            new ColorFormat("OMX_COLOR_FormatYUV420_10PackedPlanar", 0x7F00000C),
            new ColorFormat("OMX_COLOR_FormatYUV420_16PackedPlanar", 0x7F00000A),
            new ColorFormat("OMX_COLOR_FormatYUV420_UVSideBySide", 0x7F00000E));

    private static final ColorFormatList OTHER_YUV420_LIST = new ColorFormatList(
            new ColorFormat("OMX_INTEL_COLOR_FormatYUV420PackedSemiPlanar", 0x7FA00E00),
            new ColorFormat("OMX_INTEL_COLOR_FormatYUV420PackedSemiPlanar_Tiled", 0x7FA00F00),
            new ColorFormat("OMX_TI_COLOR_FormatYUV420PackedSemiPlanar", 0x7F000100),
            new ColorFormat("OMX_TI_COLOR_FormatYUV420PackedSemiPlanarInterlaced", 0x7F000001));

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

    private static final ColorFormatList SONY_YUV420_LIST = new ColorFormatList(
            new ColorFormat("OMX_COLOR_FormatYUV420MBPackedSemiPlanar", 0x7FFFFFFE),
            new ColorFormat("OMX_STE_COLOR_FormatYUV420PackedSemiPlanarMB", 0x7FA00000)
    );


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

    static ColorFormatList findYUV420List(String codecName) {
        codecName = codecName.toLowerCase();
        if (codecName.contains("brcm")) {
            return BROADCOM_YUV420_LIST;
        } else if (codecName.contains("qcom")
                || codecName.contains("qti")
                || codecName.contains("ittiam")) {
            return QUALCOMM_YUV420_LIST;
        } else if (codecName.contains("omx.sec")
                || codecName.contains("exynos")) {
            return SAMSUNG_YUV420_LIST;
        } else if (codecName.contains("omx.st")) {
            return SONY_YUV420_LIST;
        } else if (codecName.contains("omx.ti")
                || codecName.contains("intel")
                || codecName.contains("omx.rk")) {
            return OTHER_YUV420_LIST;
        } else {
            return STANDARD_YUV420_LIST;
        }
    }

    public static int getYUV420Type(String codecName, int colorFormat) {
        ColorFormatList yuv420List = findYUV420List(codecName);
        if (yuv420List == null) {
            return 0;
        }
        ColorFormat yuv420Format = yuv420List.findColorFormat(colorFormat);
        if (yuv420Format == null) {
            return 0;
        }
        String name = yuv420Format.name;
        if (name.contains("PackedSemiPlanar")) {
            return NV21;
        } else if (name.contains("SemiPlanar")) {
            return NV12;
        } else if (name.contains("PackedPlanar")) {
            return YV12;
        } else {
            return YV21;
        }
    }

    public static boolean isYUV42010Bit(String codecName, int colorFormat) {
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
            return false;
        }
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