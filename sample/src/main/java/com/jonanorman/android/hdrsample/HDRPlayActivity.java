package com.jonanorman.android.hdrsample;

import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.jonanorman.android.hdrsample.player.AndroidTexturePlayer;
import com.jonanorman.android.hdrsample.player.source.FileSource;
import com.jonanorman.android.hdrsample.player.view.VideoSurfaceView;
import com.jonanorman.android.hdrsample.util.DisplayUtil;

public class HDRPlayActivity extends AppCompatActivity {
    AndroidTexturePlayer videoPlayer;
    VideoSurfaceView surfaceView1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        surfaceView1 = findViewById(R.id.SurfaceView1);
        videoPlayer = AndroidTexturePlayer.createTexturePlayer();
        videoPlayer.setKeepBrightnessOnHDR(true);
        videoPlayer.setSource(FileSource.createAssetFileSource(getApplicationContext(), "1.mp4"));
        videoPlayer.prepare();
        surfaceView1.setVideoPlayer(videoPlayer);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}