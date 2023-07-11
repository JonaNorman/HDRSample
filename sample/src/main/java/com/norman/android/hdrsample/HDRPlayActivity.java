package com.norman.android.hdrsample;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.norman.android.hdrsample.player.GLVideoOutput;
import com.norman.android.hdrsample.player.VideoPlayer;
import com.norman.android.hdrsample.player.source.AssetFileSource;
import com.norman.android.hdrsample.player.view.VideoPlayerView;
import com.norman.android.hdrsample.todo.CubeLutVideoTransform;

public class HDRPlayActivity extends AppCompatActivity {
    VideoPlayer videoPlayer;
    VideoPlayerView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        surfaceView = findViewById(R.id.VideoPlayerView);
        GLVideoOutput videoOutput = GLVideoOutput.create();
        videoPlayer = VideoPlayer.create(videoOutput);
        videoPlayer.setSource(AssetFileSource.create("video/1.mp4"));
        CubeLutVideoTransform videoTransform = new CubeLutVideoTransform();
        videoTransform.setCubeLutForAsset("lut/BT2446_BT2407_HDR10_to_SDR_3DLUT/BT2446_BT2407_HDR10_to_SDR_1000nits_rev03.cube");
        videoOutput.addVideoTransform(videoTransform);
        surfaceView.setVideoPlayer(videoPlayer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}