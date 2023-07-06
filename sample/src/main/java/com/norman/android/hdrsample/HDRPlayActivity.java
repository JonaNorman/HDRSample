package com.norman.android.hdrsample;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.norman.android.hdrsample.player.TexturePlayer;
import com.norman.android.hdrsample.player.source.AssetFileSource;
import com.norman.android.hdrsample.player.view.VideoPlayerView;
import com.norman.android.hdrsample.todo.CubeLutTextureRenderer;

public class HDRPlayActivity extends AppCompatActivity {
    TexturePlayer videoPlayer;
    VideoPlayerView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        surfaceView = findViewById(R.id.VideoPlayerView);
        videoPlayer = TexturePlayer.create();
        videoPlayer.setSource(AssetFileSource.create("video/1.mp4"));

        CubeLutTextureRenderer cubeLutTextureRenderer = new CubeLutTextureRenderer();
        cubeLutTextureRenderer.setCubeLutForAsset("lut/BT2446_BT2407_HDR10_to_SDR_3DLUT/BT2446_BT2407_HDR10_to_SDR_1000nits_rev03.cube");
        videoPlayer.setTextureRenderer(cubeLutTextureRenderer);
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