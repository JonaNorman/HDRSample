package com.norman.android.hdrsample;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.norman.android.hdrsample.player.GLVideoOutput;
import com.norman.android.hdrsample.player.VideoPlayer;
import com.norman.android.hdrsample.player.source.AssetFileSource;
import com.norman.android.hdrsample.player.view.VideoPlayerView;
import com.norman.android.hdrsample.todo.CubeLutVideoTransform;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HDRPlayActivity extends AppCompatActivity  implements View.OnClickListener {
    VideoPlayer videoPlayer;
    VideoPlayerView surfaceView;
    CubeLutVideoTransform videoTransform;

    AlertDialog cubeLutDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        surfaceView = findViewById(R.id.VideoPlayerView);
        GLVideoOutput videoOutput = GLVideoOutput.create();
        videoPlayer = VideoPlayer.create(videoOutput);
        videoPlayer.setSource(AssetFileSource.create("video/1.mp4"));
        videoTransform = new CubeLutVideoTransform();
        videoOutput.addVideoTransform(videoTransform);
        surfaceView.setVideoPlayer(videoPlayer);
        findViewById(R.id.ButtonCubeLut).setOnClickListener(this);
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

    private void showCubeLutDialog(){
        if (cubeLutDialog != null){
            if (!cubeLutDialog.isShowing()) {
                cubeLutDialog.show();
            }
            return;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(HDRPlayActivity.this);
        builderSingle.setTitle("select cubeLut3D");
        AssetManager assetManager = getResources().getAssets();

        LinkedList<String> pathList = new LinkedList<>();
        pathList.add("lut/pq2sdr");
        List<String> fileList = new ArrayList<>();
        while (!pathList.isEmpty()) {
            String path = pathList.poll();
            String[] names = null;
            try {
                names   = assetManager.list(path);
            }catch (Exception ignore){
                continue;
            }
            if (names.length == 0){
                fileList.add(path);
            }else{
                for (String name : names) {
                    pathList.add(path+"/"+name);
                }
            }

        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(HDRPlayActivity.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(fileList);
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(HDRPlayActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        videoTransform.setCubeLutForAsset(strName);
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        cubeLutDialog =  builderSingle.show();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ButtonCubeLut){
            showCubeLutDialog();
        }
    }
}