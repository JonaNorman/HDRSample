package com.norman.android.hdrsample.player.extract;

import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 视频解封装器
 */
public interface VideoExtractor extends Extractor {


    @IntDef({
            MediaFormat.COLOR_STANDARD_BT709,
            MediaFormat.COLOR_STANDARD_BT601_PAL,
            MediaFormat.COLOR_STANDARD_BT601_NTSC,
            MediaFormat.COLOR_STANDARD_BT2020,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ColorStandard {
    }


    @IntDef({
            MediaFormat.COLOR_RANGE_LIMITED,
            MediaFormat.COLOR_RANGE_FULL,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ColorRange {}

    @IntDef({
            MediaFormat.COLOR_TRANSFER_LINEAR,
            MediaFormat.COLOR_TRANSFER_SDR_VIDEO,
            MediaFormat.COLOR_TRANSFER_ST2084,
            MediaFormat.COLOR_TRANSFER_HLG,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ColorTransfer {}

    static VideoExtractor create() {
        return new VideoExtractorImpl();
    }

    int getWidth();

    int getHeight();

    /**
     * 帧率
     */

    int getFrameRate();

    /**
     * 颜色空间
     *
     * @return
     */

    @ColorStandard
    int getColorStandard();

    /**
     * 颜色范围
     *
     * @return
     */

    @ColorRange
    int getColorRange();

    /**
     * 颜色传递函数
     *
     * @return
     */
    @ColorTransfer
    int getColorTransfer();
}
