package com.norman.android.hdrsample.player.extract;

import android.media.MediaCodec;

import com.norman.android.hdrsample.player.source.FileSource;

import java.nio.ByteBuffer;

/**
 * 解封装器
 */
public interface Extractor {

    /**
     * 设置文件
     * @param fileSource
     */
    void setSource(FileSource fileSource);

    /**
     * 是否可用，在setSource后可以当前视频或者音频是否存在
     * @return
     */

    boolean isAvailable();

    boolean isRelease();

    void release();

    /**
     * 往前寻找关键帧
     * @param timeUs
     */
    void seekPreSync(long timeUs);

    /**
     * 寻找最近的关键帧
     * @param timeUs
     */
    void seekCloseSync(long timeUs);

    /**
     * 往后寻找关键帧
     * @param timeUs
     */
    void seekNextSync(long timeUs);

    /**
     * 读取一帧buffer
     * @param buffer
     * @param bufferInfo
     */

    void read(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo);

    void read(ByteBuffer buffer, int offset, MediaCodec.BufferInfo bufferInfo);

    /**
     * 指向后面一帧
     * @return 后续是否还存在数据
     */

    boolean advance();
    String getMimeType();

    /**
     * 时间长度
     * @return 微妙
     */
    long getDurationUs();

    int getMaxInputSize();

    int getProfile();

    int getProfileLevel();

    ByteBuffer getCsd0Buffer();

    ByteBuffer getCsd1Buffer();
}
