package com.norman.android.hdrsample.player.color;

import static com.norman.android.hdrsample.player.color.ColorStandard.COLOR_STANDARD_BT2020;
import static com.norman.android.hdrsample.player.color.ColorStandard.COLOR_STANDARD_BT601_NTSC;
import static com.norman.android.hdrsample.player.color.ColorStandard.COLOR_STANDARD_BT601_PAL;
import static com.norman.android.hdrsample.player.color.ColorStandard.COLOR_STANDARD_BT709;

import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        COLOR_STANDARD_BT709,
        COLOR_STANDARD_BT601_PAL,
        COLOR_STANDARD_BT601_NTSC,
        COLOR_STANDARD_BT2020,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ColorStandard {
    int COLOR_STANDARD_BT709 = MediaFormat.COLOR_STANDARD_BT709;
    int COLOR_STANDARD_BT601_PAL = MediaFormat.COLOR_STANDARD_BT601_PAL;
    int COLOR_STANDARD_BT601_NTSC = MediaFormat.COLOR_STANDARD_BT601_NTSC;
    int COLOR_STANDARD_BT2020 = MediaFormat.COLOR_STANDARD_BT2020;

}
