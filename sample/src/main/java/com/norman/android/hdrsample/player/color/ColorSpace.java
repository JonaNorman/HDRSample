package com.norman.android.hdrsample.player.color;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 综合{@link ColorStandard} 和{@link ColorTransfer}组合在一起，方便后续判断
 */
@IntDef({ColorSpace.VIDEO_SDR, ColorSpace.VIDEO_BT2020_PQ, ColorSpace.VIDEO_BT2020_HLG, ColorSpace.VIDEO_BT2020_LINEAR})
@Retention(RetentionPolicy.SOURCE)
public @interface ColorSpace {
    /**
     * SDR视频 包含了BT709、BT601
     */
    int VIDEO_SDR = 0;
    /**
     * BT2020 PQ视频
     */

    int VIDEO_BT2020_PQ = 1;
    /**
     * BT2020 HLG视频
     */
    int VIDEO_BT2020_HLG = 2;
    /**
     * BT2020 线性视频
     */
    int VIDEO_BT2020_LINEAR = 3;
}
