package com.norman.android.hdrsample.transform;

import android.media.MediaFormat;

import com.norman.android.hdrsample.util.DisplayUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AndroidTexturePlayerRenderer {
    private float contentLuminance;
    private float screenLuminance;

    boolean keepBrightnessOnHDR;


    public AndroidTexturePlayerRenderer() {


        screenLuminance = DisplayUtil.getMaxLuminance();

    }

    public synchronized void setKeepBrightnessOnHDR(boolean keepBrightnessOnHDR) {
        ScreenBrightnessObserver screenBrightnessObserver = new ScreenBrightnessObserver();

        this.keepBrightnessOnHDR = keepBrightnessOnHDR;
        if (keepBrightnessOnHDR) {
            screenBrightnessObserver.listen();
        } else {
            screenBrightnessObserver.unListen();
        }
        float brightness = 1;
        if (keepBrightnessOnHDR) {
            ScreenBrightnessObserver.BrightnessInfo brightnessInfo = screenBrightnessObserver.getBrightnessInfo();
            brightness = brightnessInfo.brightnessFloat;
        }

    }

    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        ByteBuffer hdrStaticInfo = MediaFormatUtil.getByteBuffer(outputFormat, MediaFormat.KEY_HDR_STATIC_INFO);
        if (hdrStaticInfo != null) {
            hdrStaticInfo.clear();
            hdrStaticInfo.position(1);
            hdrStaticInfo.limit(hdrStaticInfo.capacity());
            hdrStaticInfo.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer shortBuffer = hdrStaticInfo.asShortBuffer();
            int maxFrameAverageLuminance = shortBuffer.get(11);
//            hdrStaticInfo.putShort((short) ((primaryRChromaticityX() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) ((primaryRChromaticityY() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) ((primaryGChromaticityX() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) ((primaryGChromaticityY() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) ((primaryBChromaticityX() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) ((primaryBChromaticityY() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) ((whitePointChromaticityX() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) ((whitePointChromaticityY() * MAX_CHROMATICITY) + 0.5f));
//            hdrStaticInfo.putShort((short) (maxMasteringLuminance() + 0.5f));
//            hdrStaticInfo.putShort((short) (minMasteringLuminance() + 0.5f));
//            hdrStaticInfo.putShort((short) maxContentLuminance());
//            hdrStaticInfo.putShort((short) maxFrameAverageLuminance());
        }

    }

}
