package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.view.Surface;

import com.norman.android.hdrsample.player.decode.VideoDecoder;
import com.norman.android.hdrsample.player.extract.VideoExtractor;

public class VideoSurfaceOutput extends VideoOutput {
    private Surface decoderSurface;
    private VideoView videoView;

    private final VideoView.SurfaceSubscriber surfaceSubscriber = new VideoView.SurfaceSubscriber() {
        @Override
        public void onSurfaceAvailable(Surface surface, int width, int height) {
            setDecoderSurface(surface);
        }

        @Override
        public void onSurfaceRedraw() {

            waitNextFrame();
        }

        @Override
        public void onSurfaceSizeChange(int width, int height) {


        }

        @Override
        public void onSurfaceDestroy() {
            setDecoderSurface(null);
        }
    };


    public static VideoSurfaceOutput create() {
        return new VideoSurfaceOutput();
    }

    @Override
    protected void onDecoderPrepare(VideoPlayer videoPlayer, VideoExtractor videoExtractor, VideoDecoder videoDecoder, MediaFormat inputFormat) {
        super.onDecoderPrepare(videoPlayer, videoExtractor, videoDecoder, inputFormat);
        videoDecoder.setOutputMode(VideoDecoder.SURFACE_MODE);
        videoDecoder.setOutputSurface(decoderSurface);
    }


    @Override
    public synchronized void setOutputSurface(Surface surface) {
        setOutputVideoView(null);
        setDecoderSurface(surface);
    }

    synchronized void setDecoderSurface(Surface surface) {
        this.decoderSurface = surface;
        if (videoDecoder != null) {
            videoDecoder.setOutputSurface(surface);
        }
    }

    @Override
    protected void onVideoSizeChange(int width, int height) {
        super.onVideoSizeChange(width, height);
        if (videoView != null){
            videoView.setAspectRatio(width*1.0f/height);
        }
    }

    @Override
    public synchronized void setOutputVideoView(VideoView view) {
        if (videoView != view) {
            if (videoView != null)
                videoView.unsubscribe(surfaceSubscriber);
            videoView = view;
        }
        if (videoView != null) {
            videoView.setAspectRatio(getWidth()*1.0f/getHeight());
            videoView.subscribe(surfaceSubscriber);
        }
    }


}
