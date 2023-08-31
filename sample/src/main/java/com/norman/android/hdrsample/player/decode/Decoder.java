package com.norman.android.hdrsample.player.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * 解码器
 */
public interface Decoder {


    /**
     * 创建，对应的是destroy方法或者release方法，destroy后可以重新创建，release已经不能再创建
     * @param mimeType
     */
    void create(String mimeType);

    /**
     * 配置解码器，create、stop、reset后可以重新配置
     * @param configuration
     */

    void configure(Decoder.Configuration configuration);

    /**
     * 启动，configure后才能start
     */

    void start();

    /**
     * start后可以暂停
     */

    void pause();
    /**
     * pause后可以重新恢复
     */
    void resume();

    /**
     * 刷新数据(start后才有效)，会清空正在解码的数据，在拖动视频进度条可以使用
     */
    void flush();

    /**
     * 停止解码，要重新开始要从configure开始调用，再调用start
     */
    void stop();

    /**
     * 重置解码器，configure、start后才能调用
     */
    void reset();

    /**
     * 销毁，create后就可以调用
     */

    void destroy();

    void release();

    /**
     * 已经create
     * @return
     */

    boolean isCreated();

    /**
     * 已经配置过，reset、stop、destroy都会变成false
     * @return
     */

    boolean isConfigured();

    /**
     * 已经启动了
     * @return
     */

    boolean isStarted();

    /**
     * 正在运行中，start并且没有pause
     * @return
     */

    boolean isRunning();

    boolean isRelease();

    boolean isPaused();


    class Configuration {
        public final MediaFormat mediaFormat;
        public final CallBack callBack;

        public Configuration(@NonNull MediaFormat mediaFormat, @NonNull CallBack callBack) {
            this.mediaFormat = mediaFormat;
            this.callBack = callBack;
        }

    }

    interface CallBack {

        /**
         * 送入解码器解码的数据已经准备，可以往这里送入未解码的数据
         * @param byteBuffer
         * @return
         */
        MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer byteBuffer);

        /**
         * 解码一帧数据完成的回调
         * @param outputBuffer
         * @param presentationTimeUs 微妙
         * @return true表示这帧数据是要渲染要处理的，false表示略过这帧数据
         */
        boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs);

        /**
         * 解码数据已经渲染上去或者完成
         * 如果{@link  Decoder.CallBack#onOutputBufferAvailable(ByteBuffer, long)}返回false就不会回调这个方法
         *
         * @param presentationTimeUs
         */
        void onOutputBufferRender(long presentationTimeUs);

        /**
         * 解码已经结束，表示已经接收到解码结束的标记
         */
        void onOutputBufferEndOfStream();

        /**
         * 解码格式变化
         * @param format
         */

        void onOutputFormatChanged(MediaFormat format);

        void onDecodeError(Exception exception);

    }


}
