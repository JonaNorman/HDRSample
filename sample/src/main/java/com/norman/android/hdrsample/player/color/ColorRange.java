package com.norman.android.hdrsample.player.color;

import static com.norman.android.hdrsample.player.color.ColorRange.FULL;
import static com.norman.android.hdrsample.player.color.ColorRange.LIMITED;

import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 颜色范围
 */
@IntDef({
        LIMITED,
        FULL,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ColorRange {
   int LIMITED = MediaFormat.COLOR_RANGE_LIMITED;
   int FULL =  MediaFormat.COLOR_RANGE_FULL;

}
