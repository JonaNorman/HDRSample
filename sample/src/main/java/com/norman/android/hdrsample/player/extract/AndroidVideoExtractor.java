package com.norman.android.hdrsample.player.extract;

public interface AndroidVideoExtractor extends AndroidExtractor {

    static AndroidVideoExtractor create() {
        return new AndroidVideoExtractorImpl();
    }

    int getWidth();

    int getHeight();

    int getFrameRate();

    int getColorStandard();

    int getColorRange();

    int getColorTransfer();
}
