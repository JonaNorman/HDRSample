package com.norman.android.hdrsample;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.norman.android.hdrsample.player.DirectVideoOutput;
import com.norman.android.hdrsample.player.GLVideoOutput;
import com.norman.android.hdrsample.player.VideoPlayer;
import com.norman.android.hdrsample.player.VideoView;
import com.norman.android.hdrsample.player.source.AssetFileSource;
import com.norman.android.hdrsample.transform.CubeLutVideoTransform;
import com.norman.android.hdrsample.transform.HDRToSDRVideoTransform;
import com.norman.android.hdrsample.transform.shader.chromacorrect.ChromaCorrection;
import com.norman.android.hdrsample.transform.shader.gamma.GammaOETF;
import com.norman.android.hdrsample.transform.shader.gamutmap.GamutMap;
import com.norman.android.hdrsample.transform.shader.tonemap.ToneMap;
import com.norman.android.hdrsample.util.AssetUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HDRPlayActivity extends AppCompatActivity implements View.OnClickListener,HdrToSdrShaderDialog.OnShaderSelectListener {
    VideoPlayer videoPlayer;
    VideoView videoView;
    CubeLutVideoTransform videoTransform;

    AlertDialog cubeLutDialog;

    AlertDialog videoListDialog;

    boolean loadLutSuccess;

    boolean loadVideoListSuccess;

    List<String> lutPathList = new ArrayList<>();

    List<String> lutNameList = new ArrayList<>();

    List<String> videoPathList = new ArrayList<>();

    List<String> videoNameList = new ArrayList<>();

    int selectLutPosition;
    HDRToSDRVideoTransform hdrToSDRVideoTransform;

    HdrToSdrShaderDialog hdrToSdrShaderDialog;

    GLVideoOutput glVideoOutput;

    DirectVideoOutput directVideoOutput;

    @VideoView.ViewType int viewType  =VideoView.ViewType.TEXTURE_VIEW;

    @GLVideoOutput.TextureSource int textureSource  =GLVideoOutput.TextureSource.AUTO;

    @GLVideoOutput.HdrBitDepth
    int hdrBitDepth = GLVideoOutput.HdrBitDepth.BIT_DEPTH_10;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        hdrToSdrShaderDialog = new HdrToSdrShaderDialog(this);
        hdrToSdrShaderDialog.setOnShaderSelectListener(this);
        videoView = findViewById(R.id.VideoPlayerView);
        videoView.setViewType(viewType);
        directVideoOutput = DirectVideoOutput.create();
        glVideoOutput = GLVideoOutput.create();
        glVideoOutput.setTextureSource(textureSource);
        glVideoOutput.setHdrBitDepth(hdrBitDepth);

        videoPlayer = VideoPlayer.create();
        videoPlayer.setVideoOutput(glVideoOutput);
        videoPlayer.setSource(AssetFileSource.create("video/1.mp4"));
        videoTransform = new CubeLutVideoTransform();
        hdrToSDRVideoTransform = new HDRToSDRVideoTransform();
        glVideoOutput.addVideoTransform(videoTransform);
        glVideoOutput.addVideoTransform(hdrToSDRVideoTransform);
        glVideoOutput.setOutputVideoView(videoView);

        findViewById(R.id.ButtonCubeLut).setOnClickListener(this);
        findViewById(R.id.ButtonHdrToSdr).setOnClickListener(this);
        findViewById(R.id.ButtonVideoList).setOnClickListener(this);
        findViewById(R.id.ButtonViewMode).setOnClickListener(this);
        findViewById(R.id.ButtonTextureSource).setOnClickListener(this);
        findViewById(R.id.ButtonBitDepth).setOnClickListener(this);


    }


    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.start();
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

    private void showCubeLutDialog() {
        if (cubeLutDialog != null) {
            if (!cubeLutDialog.isShowing()) {
                cubeLutDialog.show();
            }
            return;
        }
        loadLutList();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("3D CUBE LUT")
                //.setMessage("You can buy our products without registration too. Enjoy the shopping")
                .setSingleChoiceItems(lutNameList.toArray(new String[0]), selectLutPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectLutPosition = which;
                        String strName = lutPathList.get(which);
                        videoTransform.setCubeLut(strName);
                        dialog.dismiss();
                        cubeLutDialog = null;
                    }
                });
        cubeLutDialog = builder.show();


    }

    private void showVideoListDialog() {
        if (videoListDialog != null) {
            if (!videoListDialog.isShowing()) {
                videoListDialog.show();
            }
            return;
        }
        loadVideoList();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("视频列表")
                //.setMessage("You can buy our products without registration too. Enjoy the shopping")
                .setSingleChoiceItems(videoNameList.toArray(new String[0]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = videoPathList.get(which);

                        videoPlayer.stop();
                        videoPlayer.setSource(AssetFileSource.create(path));
                        videoPlayer.start();

                        dialog.dismiss();
                    }
                });
        videoListDialog = builder.show();


    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ButtonCubeLut) {
            showCubeLutDialog();
        }else if (id == R.id.ButtonHdrToSdr){
            hdrToSdrShaderDialog.show();
        }else if (id == R.id.ButtonVideoList){
            showVideoListDialog();
        }else if (id ==R.id.ButtonViewMode){
           showViewModeMenu(v);
        }else if (id ==R.id.ButtonTextureSource){
            showTextureSourceMenu(v);
        }else if (id ==R.id.ButtonBitDepth){
            showHdrBitDepthMenu(v);
        }
    }

    void showViewModeMenu(View v){
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.view_mode_menu);
        Menu menu = pum.getMenu();
        if (viewType == VideoView.ViewType.TEXTURE_VIEW){
            menu.findItem(R.id.textureView).setChecked(true);
        }else if (viewType == VideoView.ViewType.SURFACE_VIEW){
            menu.findItem(R.id.surfaceView).setChecked(true);
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.surfaceView){
                    viewType = VideoView.ViewType.SURFACE_VIEW;
                }else if (item.getItemId() == R.id.textureView){
                    viewType =VideoView.ViewType.TEXTURE_VIEW;
                }
                videoView.setViewType(viewType);
                return true;
            }
        });
        pum.show();
    }


    void showTextureSourceMenu(View v){
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.texture_source_menu);
        Menu menu = pum.getMenu();
        if (textureSource == GLVideoOutput.TextureSource.AUTO){
            menu.findItem(R.id.textureSourceAuto).setChecked(true);
        }else if (textureSource == GLVideoOutput.TextureSource.BUFFER){
            menu.findItem(R.id.textureSourceBuffer).setChecked(true);
        }else if (textureSource == GLVideoOutput.TextureSource.EXT){
            menu.findItem(R.id.textureSourceExt).setChecked(true);
        } else if (textureSource == GLVideoOutput.TextureSource.Y2Y){
            menu.findItem(R.id.textureSourceY2Y).setChecked(true);
        } else if (textureSource == GLVideoOutput.TextureSource.OES){
            menu.findItem(R.id.textureSourceOES).setChecked(true);
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.textureSourceAuto){
                    textureSource = GLVideoOutput.TextureSource.AUTO;
                }else if (item.getItemId() == R.id.textureSourceBuffer){
                    textureSource = GLVideoOutput.TextureSource.BUFFER;
                }else if (item.getItemId() == R.id.textureSourceExt){
                    textureSource =GLVideoOutput.TextureSource.EXT;
                } else if (item.getItemId() == R.id.textureSourceY2Y){
                    textureSource =GLVideoOutput.TextureSource.Y2Y;
                } else if (item.getItemId() == R.id.textureSourceOES){
                    textureSource =GLVideoOutput.TextureSource.OES;
                }
                videoPlayer.stop();
                glVideoOutput.setTextureSource(textureSource);
                videoPlayer.start();
                return true;
            }
        });
        pum.show();
    }


    void showHdrBitDepthMenu(View v){
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.hdr_bit_depth_menu);
        Menu menu = pum.getMenu();
        if (hdrBitDepth == GLVideoOutput.HdrBitDepth.BIT_DEPTH_8){
            menu.findItem(R.id.hdr_bit_depth_8).setChecked(true);
        }else if (hdrBitDepth == GLVideoOutput.HdrBitDepth.BIT_DEPTH_10){
            menu.findItem(R.id.hdr_bit_depth_10).setChecked(true);
        }else if (hdrBitDepth == GLVideoOutput.HdrBitDepth.BIT_DEPTH_16){
            menu.findItem(R.id.hdr_bit_depth_16).setChecked(true);
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.hdr_bit_depth_8){
                    hdrBitDepth = GLVideoOutput.HdrBitDepth.BIT_DEPTH_8;
                }else if (item.getItemId() == R.id.hdr_bit_depth_10){
                    hdrBitDepth = GLVideoOutput.HdrBitDepth.BIT_DEPTH_10;
                }else if (item.getItemId() == R.id.hdr_bit_depth_16){
                    hdrBitDepth = GLVideoOutput.HdrBitDepth.BIT_DEPTH_16;
                }
                videoPlayer.stop();
                glVideoOutput.setHdrBitDepth(hdrBitDepth);
                videoPlayer.start();
                return true;
            }
        });
        pum.show();
    }

    private void loadVideoList() {
        if (loadVideoListSuccess) {
            return;
        }
        loadVideoListSuccess = true;

        List<String> fileList = AssetUtil.list("video");
        List<String> nameList = new ArrayList<>();
        for (String path : fileList) {
            String fileName = new File(path).getName();
            int pos = fileName.lastIndexOf(".");
            if (pos > 0) {
                fileName = fileName.substring(0, pos);
            }
            nameList.add(fileName);
        }
        videoNameList = nameList;
        videoPathList = fileList;
    }


    private void loadLutList() {
        if (loadLutSuccess) {
            return;
        }
        loadLutSuccess = true;

        List<String> fileList = AssetUtil.list("lut/pq2sdr");
        List<String> nameList = new ArrayList<>();
        for (String path : fileList) {
            String fileName = new File(path).getName();
            int pos = fileName.lastIndexOf(".");
            if (pos > 0) {
                fileName = fileName.substring(0, pos);
            }
            nameList.add(fileName);
        }
        fileList.add(0, null);
        nameList.add(0, "无");
        lutNameList = nameList;
        lutPathList = fileList;
    }

    @Override
    public void onShaderSelect(ChromaCorrection chromaCorrection, ToneMap toneMap, GamutMap gamutMap, GammaOETF gammaOETF) {
        hdrToSDRVideoTransform.setChromaCorrection(chromaCorrection);
        hdrToSDRVideoTransform.setToneMap(toneMap);
        hdrToSDRVideoTransform.setGamutMap(gamutMap);
        hdrToSDRVideoTransform.setGammaOETF(gammaOETF);
    }
}