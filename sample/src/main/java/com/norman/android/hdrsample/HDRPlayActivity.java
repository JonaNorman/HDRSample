package com.norman.android.hdrsample;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.norman.android.hdrsample.player.AndroidSurfacePlayer;
import com.norman.android.hdrsample.player.AndroidTexturePlayer;
import com.norman.android.hdrsample.player.AndroidVideoPlayer;
import com.norman.android.hdrsample.player.Player;
import com.norman.android.hdrsample.player.source.FileSource;
import com.norman.android.hdrsample.player.view.VideoSurfaceView;
import com.norman.android.hdrsample.todo.CubeLutTextureRenderer;

public class HDRPlayActivity extends AppCompatActivity {
    AndroidTexturePlayer videoPlayer;
    VideoSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        surfaceView = findViewById(R.id.SurfaceView);
        videoPlayer = AndroidTexturePlayer.create();
        videoPlayer.setSource(FileSource.createForAsset("video/hdr10-video-with-sdr-container.mp4"));

        CubeLutTextureRenderer cubeLutTextureRenderer = new CubeLutTextureRenderer();
        cubeLutTextureRenderer.setCubeLutForAsset("lut/BT2446_BT2407_HDR10_to_SDR_3DLUT/BT2446_BT2407_HDR10_to_SDR_1000nits_rev03.cube");
        videoPlayer.setTextureRenderer(cubeLutTextureRenderer);
        surfaceView.setVideoPlayer(videoPlayer);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}