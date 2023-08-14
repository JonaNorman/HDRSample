package com.norman.android.hdrsample.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;

import com.norman.android.hdrsample.exception.RuntimeException;
import com.norman.android.hdrsample.player.decode.Decoder;
import com.norman.android.hdrsample.player.extract.Extractor;
import com.norman.android.hdrsample.player.source.FileSource;
import com.norman.android.hdrsample.util.MediaFormatUtil;
import com.norman.android.hdrsample.util.TimeUtil;

import java.nio.ByteBuffer;

abstract class DecodePlayerImpl<D extends Decoder,E extends Extractor> extends PlayerImpl implements Player {
    private static final String KEY_CSD_0 = "csd-0";
    private static final String KEY_CSD_1 = "csd-1";

    private final PlayerImpl.CallBackHandler callBackHandler = new CallBackHandler();


    private E extractor;
    private D decoder;

    private FileSource fileSource;

    private volatile boolean repeat = true;

    private volatile  float currentTime;


    public DecodePlayerImpl(D decoder, E extractor, String threadName) {
        super(threadName);
        this.extractor = extractor;
        this.decoder = decoder;
    }

    @Override
    public synchronized void setSource(FileSource fileSource) {
        if (isPrepared()){
            throw new IllegalStateException("setSource must before prepare");
        }
        this.fileSource = fileSource;
    }


    @Override
    public synchronized void seek(float timeSecond) {
        post(() -> onPlaySeek(TimeUtil.secondToMicro(timeSecond)));
    }

    @Override
    public synchronized void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }


    @Override
    public float getCurrentTime() {
        return currentTime;
    }

    @Override
    public void setCallback(Callback callback) {
        setCallback(callback, null);
    }

    @Override
    public void setCallback(Callback callback, Handler handler) {
        callBackHandler.setCallback(callback);
        callBackHandler.setHandler(handler);
    }


    protected void onPlayPrepare() {
        extractor.setSource(fileSource);
        if (!extractor.isAvailable()) {
            throw new RuntimeException("file can not play");
        }
        decoder.create(extractor.getMimeType());
        MediaFormat mediaFormat = new MediaFormat();
        MediaFormatUtil.setString(mediaFormat, MediaFormat.KEY_MIME, extractor.getMimeType());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_MAX_INPUT_SIZE, extractor.getMaxInputSize());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_PROFILE, extractor.getProfile());
        MediaFormatUtil.setInteger(mediaFormat, MediaFormat.KEY_LEVEL, extractor.getProfileLevel());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_0, extractor.getCsd0Buffer());
        MediaFormatUtil.setByteBuffer(mediaFormat, KEY_CSD_1, extractor.getCsd1Buffer());
        onInputFormatConfigure(extractor,decoder, mediaFormat);
        decoder.configure(new Decoder.Configuration(mediaFormat, new DecoderCallBack()));
    }

    protected void onPlayStart() {
        decoder.start();
    }

    protected void onPlaySeek(long presentationTimeUs) {
        decoder.flush();
        extractor.seekPreSync(presentationTimeUs);
    }

    protected void onPlayResume() {
        decoder.resume();
    }

    protected void onPlayPause() {
        decoder.pause();
    }


    protected void onPlayStop() {
        decoder.destroy();
        extractor.seekPreSync(0);
    }


    protected void onPlayRelease() {
        extractor.release();
        decoder.release();
    }

    @Override
    protected void onPlayError(Exception exception) {
        callBackHandler.callError(exception);
    }

    class DecoderCallBack implements Decoder.CallBack {


        @Override
        public MediaCodec.BufferInfo onInputBufferAvailable(ByteBuffer inputBuffer) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            extractor.readSampleBuffer(inputBuffer, bufferInfo);
            extractor.advance();
            return bufferInfo;
        }

        @Override
        public boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs) {
            return  DecodePlayerImpl.this.onOutputBufferAvailable(outputBuffer,presentationTimeUs);
        }

        @Override
        public void onOutputBufferRender(long presentationTimeUs) {
            DecodePlayerImpl.this.onOutputBufferRender(presentationTimeUs);
            currentTime = TimeUtil.microToSecond(presentationTimeUs);
            callBackHandler.callProcess(currentTime);
        }


        @Override
        public void onDecodeError(Exception exception) {
            throw new RuntimeException(exception);
        }

        @Override
        public void onOutputBufferEndOfStream() {
            DecodePlayerImpl.this.onOutputBufferEndOfStream();
            callBackHandler.callEnd();
            if (repeat) {
                seek(0);
            }
        }

        @Override
        public void onOutputFormatChanged(MediaFormat format) {
            DecodePlayerImpl.this.onOutputFormatChanged(format);
        }
    }

    protected abstract void onInputFormatConfigure(E extractor, D decoder, MediaFormat inputFormat);


    protected abstract void onOutputFormatChanged(MediaFormat outputFormat);


    protected abstract boolean onOutputBufferAvailable(ByteBuffer outputBuffer, long presentationTimeUs);

    protected abstract void onOutputBufferRender(long presentationTimeUs);

    protected abstract void onOutputBufferEndOfStream();

}
