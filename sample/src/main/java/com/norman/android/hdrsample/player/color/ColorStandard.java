package com.norman.android.hdrsample.player.color;

import static com.norman.android.hdrsample.player.color.ColorStandard.BT2020;
import static com.norman.android.hdrsample.player.color.ColorStandard.BT601_NTSC;
import static com.norman.android.hdrsample.player.color.ColorStandard.BT601_PAL;
import static com.norman.android.hdrsample.player.color.ColorStandard.BT709;

import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        BT709,
        BT601_PAL,
        BT601_NTSC,
        BT2020,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ColorStandard {
    int BT709 = MediaFormat.COLOR_STANDARD_BT709;
    int BT601_PAL = MediaFormat.COLOR_STANDARD_BT601_PAL;
    int BT601_NTSC = MediaFormat.COLOR_STANDARD_BT601_NTSC;
    int BT2020 = MediaFormat.COLOR_STANDARD_BT2020;

}
