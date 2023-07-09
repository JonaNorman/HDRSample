package com.norman.android.hdrsample.player.extract;

public interface VideoExtractor extends Extractor {

    static VideoExtractor create() {
        return new VideoExtractorImpl();
    }

    int getWidth();

    int getHeight();

    int getFrameRate();

    int getColorStandard();

    int getColorRange();

    int getColorTransfer();
}
