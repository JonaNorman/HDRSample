package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;

class DirectVideoOutputImpl extends DirectVideoOutput {

    private Surface decoderSurface;
    private VideoView videoView;

    private final VideoView.SurfaceSubscriber surfaceSubscriber = new VideoView.SurfaceSubscriber() {
        @Override
        public void onSurfaceAvailable(Surface surface, int width, int height) {
            setDecoderSurface(surface);
        }

        @Override
        public void onSurfaceRedraw() {
            waitNextFrame(DEFAULT_WAIT_TIME_SECOND);
        }

        @Override
        public void onSurfaceDestroy() {
            setDecoderSurface(null);
        }
    };

    private final OutputSizeSubscriber outputSizeSubscriber = new OutputSizeSubscriber() {
        @Override
        public void onOutputSizeChange(int width, int height) {
            videoView.setAspectRatio(width*1.0f/height);//保证视频比例和Surface比例一样
        }
    };


    @Override
    public synchronized void setOutputSurface(Surface surface) {
        setOutputVideoView(null);// videoView和Surface只能同时设置一个
        setDecoderSurface(surface);
    }

    synchronized void setDecoderSurface(Surface surface) {
        this.decoderSurface = surface;
        if (videoDecoder != null) {
            videoDecoder.setOutputSurface(surface);
        }
    }

    @Override
    protected void onOutputPrepare(MediaFormat inputFormat) {
        videoDecoder.setOutputMode(VideoDecoder.OutputMode.SURFACE_MODE);
        videoDecoder.setOutputSurface(decoderSurface);
    }

    @Override
    public  void setOutputVideoView(VideoView view) {
        synchronized (syncLock){
            if (videoView == view) {
                return;
            }
            VideoView oldView = videoView;
            if (oldView != null){//取消上一次绑定
                oldView.unsubscribe(surfaceSubscriber);
                unsubscribe(outputSizeSubscriber);
            }
            videoView = view;
            if (videoView != null) {
                subscribe(outputSizeSubscriber);
                videoView.subscribe(surfaceSubscriber);
            }
        }
    }
}
