package com.norman.android.hdrsample.player.extract;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.norman.android.hdrsample.exception.IORuntimeException;
import com.norman.android.hdrsample.player.color.ColorRange;
import com.norman.android.hdrsample.player.color.ColorStandard;
import com.norman.android.hdrsample.player.color.ColorTransfer;
import com.norman.android.hdrsample.player.source.FileSource;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

class VideoExtractorImpl implements VideoExtractor {
    private static final String KEY_CSD_0 = "csd-0";
    private static final String KEY_CSD_1 = "csd-1";
    private String mimeType;
    private long durationUs;
    private int width;
    private int height;
    private int maxInputSize;
    private int frameRate;
    private int profile;
    private int profileLevel;
    private @ColorStandard int colorStandard;
    private @ColorRange int colorRange;
    private @ColorTransfer int colorTransfer;
    private ByteBuffer csd0Buffer;
    private ByteBuffer csd1Buffer;
    private MediaExtractor extractor;
    private boolean release;


    @Override
    public synchronized void setSource(FileSource fileSource) {
        if (isRelease()) {
            return;
        }
        releaseExtractor();
        resetMetaInfo();
        extractor = new MediaExtractor();
        FileSource.Descriptor fileSourceDescriptor = fileSource.createFileDescriptor();
        try {
            extractor.setDataSource(
                    fileSourceDescriptor.getFileDescriptor(),
                    fileSourceDescriptor.getStartOffset(),
                    fileSourceDescriptor.getLength());
        } catch (IOException e) {
            throw  new IORuntimeException(e);
        } finally {
            if (fileSourceDescriptor != null) {
                fileSourceDescriptor.close();
            }
        }
        boolean hasVideo = false;
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = MediaFormatUtil.getString(format, MediaFormat.KEY_MIME);
            if (mime != null && mime.toLowerCase().startsWith("video")) {
                mimeType = mime;
                durationUs = MediaFormatUtil.getLong(format, MediaFormat.KEY_DURATION);
                width = MediaFormatUtil.getInteger(format, MediaFormat.KEY_WIDTH);
                height = MediaFormatUtil.getInteger(format, MediaFormat.KEY_HEIGHT);
                frameRate = MediaFormatUtil.getInteger(format, MediaFormat.KEY_FRAME_RATE);
                csd0Buffer = MediaFormatUtil.getByteBuffer(format, KEY_CSD_0);
                csd1Buffer = MediaFormatUtil.getByteBuffer(format, KEY_CSD_1);
                profile = MediaFormatUtil.getInteger(format, MediaFormat.KEY_PROFILE);
                profileLevel = MediaFormatUtil.getInteger(format, MediaFormat.KEY_LEVEL);
                maxInputSize = MediaFormatUtil.getInteger(format, MediaFormat.KEY_MAX_INPUT_SIZE);
                colorStandard = MediaFormatUtil.getColorStandard(format);
                colorRange = MediaFormatUtil.getColorRange(format);
                colorTransfer = MediaFormatUtil.getColorTransfer(format);
                extractor.selectTrack(i);
                hasVideo = true;
                break;
            }
        }
        if (!hasVideo) {
            releaseExtractor();
        }
    }

    /**
     * 是否存在Video轨道
     * @return
     */
    @Override
    public synchronized boolean isAvailable() {
        return extractor != null && !isRelease();
    }

    @Override
    public synchronized boolean isRelease() {
        return release;
    }


    @Override
    public synchronized void seekPreSync(long timeUs) {
        if (!isAvailable()) {
            return;
        }
        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
    }

    @Override
    public synchronized void seekCloseSync(long timeUs) {
        if (!isAvailable()) {
            return;
        }
        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
    }

    @Override
    public synchronized void seekNextSync(long timeUs) {
        if (!isAvailable()) {
            return;
        }
        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_NEXT_SYNC);
    }

    @Override
    public synchronized void release() {
        if (release) {
            return;
        }
        release = true;
        releaseExtractor();
    }

    private synchronized void releaseExtractor() {
        if (extractor != null) extractor.release();
        extractor = null;
    }

    @Override
    public synchronized void read(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        read(buffer, 0, bufferInfo);
    }


    @Override
    public synchronized void read(ByteBuffer buffer, int offset, MediaCodec.BufferInfo bufferInfo) {
        if (!isAvailable()) {
            return;
        }
        bufferInfo.offset = offset;
        bufferInfo.size = extractor.readSampleData(buffer, offset);
        bufferInfo.presentationTimeUs = extractor.getSampleTime();
        int flags = extractor.getSampleFlags();
        if (flags == android.media.MediaExtractor.SAMPLE_FLAG_SYNC) {
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
        } else if (flags == android.media.MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) {
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_PARTIAL_FRAME;
        }
        if (bufferInfo.size == -1 || bufferInfo.presentationTimeUs == -1) {
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            bufferInfo.size = 0;
            bufferInfo.presentationTimeUs = 0;
            buffer.clear();
        } else {
            buffer.position(offset);
            buffer.limit(offset + bufferInfo.size);
        }
    }

    @Override
    public synchronized boolean advance() {
        if (!isAvailable()) {
            return false;
        }
        return extractor.advance();
    }

    @Override
    public synchronized String getMimeType() {
        return mimeType;
    }

    @Override
    public synchronized long getDurationUs() {
        return durationUs;
    }

    @Override
    public synchronized int getWidth() {
        return width;
    }

    @Override
    public synchronized int getHeight() {
        return height;
    }

    @Override
    public synchronized int getMaxInputSize() {
        return maxInputSize;
    }

    @Override
    public synchronized int getFrameRate() {
        return frameRate;
    }

    @Override
    public synchronized int getProfile() {
        return profile;
    }

    @Override
    public synchronized int getProfileLevel() {
        return profileLevel;
    }

    @Override
    public synchronized int getColorStandard() {
        return colorStandard;
    }

    @Override
    public synchronized int getColorRange() {
        return colorRange;
    }

    @Override
    public synchronized int getColorTransfer() {
        return colorTransfer;
    }

    @Override
    public synchronized ByteBuffer getCsd0Buffer() {
        return csd0Buffer;
    }

    @Override
    public synchronized ByteBuffer getCsd1Buffer() {
        return csd1Buffer;
    }


    private void resetMetaInfo() {
        mimeType = null;
        durationUs = 0;
        width = 0;
        height = 0;
        maxInputSize = 0;
        frameRate = 0;
        profile = 0;
        profileLevel = 0;
        colorStandard = 0;
        colorRange = 0;
        colorTransfer = 0;
        csd0Buffer = null;
        csd1Buffer = null;
    }
}
