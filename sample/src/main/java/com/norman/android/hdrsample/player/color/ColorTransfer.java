package com.norman.android.hdrsample.player.color;

import static com.norman.android.hdrsample.player.color.ColorTransfer.COLOR_TRANSFER_HLG;
import static com.norman.android.hdrsample.player.color.ColorTransfer.COLOR_TRANSFER_LINEAR;
import static com.norman.android.hdrsample.player.color.ColorTransfer.COLOR_TRANSFER_SDR_VIDEO;
import static com.norman.android.hdrsample.player.color.ColorTransfer.COLOR_TRANSFER_ST2084;

import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        COLOR_TRANSFER_LINEAR,
        COLOR_TRANSFER_SDR_VIDEO,
        COLOR_TRANSFER_ST2084,
        COLOR_TRANSFER_HLG,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ColorTransfer {
    int COLOR_TRANSFER_LINEAR = MediaFormat.COLOR_TRANSFER_LINEAR;
    int COLOR_TRANSFER_SDR_VIDEO = MediaFormat.COLOR_TRANSFER_SDR_VIDEO;
    int COLOR_TRANSFER_ST2084 = MediaFormat.COLOR_TRANSFER_ST2084;
    int COLOR_TRANSFER_HLG = MediaFormat.COLOR_TRANSFER_HLG;
}
