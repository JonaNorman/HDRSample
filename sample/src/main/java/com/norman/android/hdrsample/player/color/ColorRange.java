package com.norman.android.hdrsample.player.color;

import static com.norman.android.hdrsample.player.color.ColorRange.COLOR_RANGE_FULL;
import static com.norman.android.hdrsample.player.color.ColorRange.COLOR_RANGE_LIMITED;

import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        COLOR_RANGE_LIMITED,
        COLOR_RANGE_FULL,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ColorRange {
   int COLOR_RANGE_LIMITED = MediaFormat.COLOR_RANGE_LIMITED;
   int COLOR_RANGE_FULL =  MediaFormat.COLOR_RANGE_FULL;

}
