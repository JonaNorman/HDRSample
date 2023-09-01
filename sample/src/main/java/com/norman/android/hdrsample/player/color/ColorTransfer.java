package com.norman.android.hdrsample.player.color;

import static com.norman.android.hdrsample.player.color.ColorTransfer.HLG;
import static com.norman.android.hdrsample.player.color.ColorTransfer.LINEAR;
import static com.norman.android.hdrsample.player.color.ColorTransfer.SDR_VIDEO;
import static com.norman.android.hdrsample.player.color.ColorTransfer.ST2084;

import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        LINEAR,
        SDR_VIDEO,
        ST2084,
        HLG,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ColorTransfer {
    int LINEAR = MediaFormat.COLOR_TRANSFER_LINEAR;
    int SDR_VIDEO = MediaFormat.COLOR_TRANSFER_SDR_VIDEO;
    int ST2084 = MediaFormat.COLOR_TRANSFER_ST2084;
    int HLG = MediaFormat.COLOR_TRANSFER_HLG;
}
