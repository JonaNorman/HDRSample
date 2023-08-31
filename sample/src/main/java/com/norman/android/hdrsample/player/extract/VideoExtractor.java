package com.norman.android.hdrsample.player.extract;

import com.norman.android.hdrsample.player.color.ColorRange;
import com.norman.android.hdrsample.player.color.ColorStandard;
import com.norman.android.hdrsample.player.color.ColorTransfer;

/**
 * 视频解封装器
 */
public interface VideoExtractor extends Extractor {


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
