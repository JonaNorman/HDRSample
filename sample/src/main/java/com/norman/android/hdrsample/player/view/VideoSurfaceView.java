package com.norman.android.hdrsample.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.norman.android.hdrsample.player.VideoPlayer;

public class VideoSurfaceView extends SurfaceView implements VideoView{

    SurfaceHolderCallBack surfaceHolderCallBack;

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void setVideoPlayer(VideoPlayer videoPlayer) {
        if (surfaceHolderCallBack != null) {
            if (surfaceHolderCallBack.videoPlayer == videoPlayer) {
                return;
            }
            if (surfaceHolderCallBack.videoPlayer != videoPlayer) {
                getHolder().removeCallback(surfaceHolderCallBack);
            }
        }
        if (videoPlayer == null) {
            return;
        }
        surfaceHolderCallBack = new SurfaceHolderCallBack(videoPlayer);
        getHolder().addCallback(surfaceHolderCallBack);
    }


    class SurfaceHolderCallBack implements SurfaceHolder.Callback2 {
        VideoPlayer videoPlayer;

        public SurfaceHolderCallBack(VideoPlayer videoPlayer) {
            this.videoPlayer = videoPlayer;
            if (getHolder().getSurface().isValid()) {
                videoPlayer.setSurface(getHolder().getSurface());
                videoPlayer.start();
            }
        }

        @Override
        public void surfaceRedrawNeeded(@NonNull SurfaceHolder holder) {
            videoPlayer.waitFrame();
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            videoPlayer.setSurface(holder.getSurface());
            videoPlayer.start();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            videoPlayer.setSurface(null);
            videoPlayer.pause();
        }
    }

    ;
}
