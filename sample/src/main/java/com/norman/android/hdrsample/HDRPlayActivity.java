package com.norman.android.hdrsample;

import android.media.MediaFormat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.norman.android.hdrsample.player.DirectVideoOutput;
import com.norman.android.hdrsample.player.GLVideoOutput;
import com.norman.android.hdrsample.player.VideoOutput;
import com.norman.android.hdrsample.player.VideoPlayer;
import com.norman.android.hdrsample.player.VideoView;
import com.norman.android.hdrsample.player.color.ColorRange;
import com.norman.android.hdrsample.player.color.ColorSpace;
import com.norman.android.hdrsample.player.color.ColorStandard;
import com.norman.android.hdrsample.player.color.ColorTransfer;
import com.norman.android.hdrsample.player.source.AssetFileSource;
import com.norman.android.hdrsample.player.source.FileSource;
import com.norman.android.hdrsample.transform.CubeLutVideoTransform;
import com.norman.android.hdrsample.transform.HDRToSDRVideoTransform;
import com.norman.android.hdrsample.transform.shader.chromacorrect.ChromaCorrection;
import com.norman.android.hdrsample.transform.shader.gamma.GammaOETF;
import com.norman.android.hdrsample.transform.shader.gamutmap.GamutMap;
import com.norman.android.hdrsample.transform.shader.tonemap.ToneMap;
import com.norman.android.hdrsample.util.AssetUtil;
import com.norman.android.hdrsample.util.DisplayUtil;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class HDRPlayActivity extends AppCompatActivity implements View.OnClickListener {


    private static final List<Item<ChromaCorrection>> CHROMA_CORRECTION_MENU_LIST = Arrays.asList(new Item[]{
            new Item("无", ChromaCorrection.NONE),
            new Item("BT2446C", ChromaCorrection.BT2446C)
    });
    private static final List<Item<ToneMap>> TONE_MAP_MENU_LIST = Arrays.asList(new Item[]{
            new Item("无", ToneMap.NONE),
            new Item("Android8", ToneMap.ANDROID8),
            new Item("Android13", ToneMap.ANDROID13),
            new Item("BT2446A", ToneMap.BT2446A),
            new Item("BT2446C", ToneMap.BT2446C),
            new Item("Hable", ToneMap.HABLE)
    });

    private static final List<Item<GamutMap>> GAMUT_MAP_MENU_LIST = Arrays.asList(new Item[]{
            new Item("无", GamutMap.NONE),
            new Item("Clip", GamutMap.CLIP),
            new Item("Compress", GamutMap.COMPRESS),
            new Item("Adaptive_l0_cusp", GamutMap.ADAPTIVE_L0_CUSP)
    });

    private static final List<Item<GammaOETF>> GAMMA_OETF_MENU_LIST = Arrays.asList(new Item[]{
            new Item("无", GammaOETF.NONE),
            new Item("BT1886", GammaOETF.BT1886),
            new Item("BT709", GammaOETF.BT709)
    });

    VideoPlayer videoPlayer;
    VideoView videoView;
    CubeLutVideoTransform cubeLutVideoTransform;


    HDRToSDRVideoTransform hdrToSDRShaderTransform;


    GLVideoOutput glVideoOutput;

    DirectVideoOutput directVideoOutput;

    @VideoView.ViewType
    int viewType = VideoView.ViewType.TEXTURE_VIEW;

    @GLVideoOutput.TextureSource
    int textureSource = GLVideoOutput.TextureSource.AUTO;

    @GLVideoOutput.HdrBitDepth
    int hdrBitDepth = GLVideoOutput.HdrBitDepth.BIT_DEPTH_10;

    List<String> videoList = AssetUtil.list("video");

    List<String> pq2sdrCubeList = AssetUtil.list("lut/pq2sdr");
    List<String> hlg2sdrCubeList = AssetUtil.list("lut/hlg2sdr");

    String cubeLutAsset;

    int colorSpace = -1;

    int transformModeId = R.id.transform_mode_node;

    TextView textViewVideoInfo;

    TextView textViewScreenInfo;

    TextView textViewOpenGLSupportInfo;


    VideoOutput.OutputFormatSubscriber outputFormatSubscriber = new VideoOutput.OutputFormatSubscriber() {
        @Override
        public void onOutputFormatChange(MediaFormat outputFormat) {
            int space = MediaFormatUtil.getColorSpace(outputFormat);
            if (space != colorSpace) {
                colorSpace = space;
                cubeLutAsset = null;
                cubeLutVideoTransform.setCubeLut(null);
            }
            showVideoInfo(outputFormat);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        textViewVideoInfo = findViewById(R.id.TextViewVideoInfo);
        textViewScreenInfo = findViewById(R.id.TextViewScreenInfo);
        textViewOpenGLSupportInfo = findViewById(R.id.TextViewOpenGLSupportInfo);
        videoView = findViewById(R.id.VideoPlayerView);
        videoView.setViewType(viewType);
        directVideoOutput = DirectVideoOutput.create();
        directVideoOutput.setOutputVideoView(videoView);
        directVideoOutput.subscribe(outputFormatSubscriber);
        glVideoOutput = GLVideoOutput.create();
        glVideoOutput.setTextureSource(textureSource);
        glVideoOutput.setHdrBitDepth(hdrBitDepth);
        glVideoOutput.setOutputVideoView(videoView);
        glVideoOutput.subscribe(outputFormatSubscriber);


        videoPlayer = VideoPlayer.create();
        videoPlayer.setVideoOutput(glVideoOutput);
        videoPlayer.setSource(AssetFileSource.create(videoList.get(0)));
        cubeLutVideoTransform = new CubeLutVideoTransform();
        hdrToSDRShaderTransform = new HDRToSDRVideoTransform();
        glVideoOutput.addVideoTransform(cubeLutVideoTransform);
        glVideoOutput.addVideoTransform(hdrToSDRShaderTransform);

        initHDRToSDRShaderTransform();
        showTransformLayout(transformModeId);
        showHdrToSdrLayout(videoPlayer.getVideoOutput());
        showScreenInfo();
        showOpenGLSupportInfo();

        findViewById(R.id.ButtonCubeLut).setOnClickListener(this);
        findViewById(R.id.ButtonVideoList).setOnClickListener(this);
        findViewById(R.id.ButtonViewMode).setOnClickListener(this);
        findViewById(R.id.ButtonTextureSource).setOnClickListener(this);
        findViewById(R.id.ButtonBitDepth).setOnClickListener(this);
        findViewById(R.id.ButtonVideoOutput).setOnClickListener(this);
        findViewById(R.id.ButtonTransformMode).setOnClickListener(this);
        findViewById(R.id.ButtonGamutMap).setOnClickListener(this);
        findViewById(R.id.ButtonToneMap).setOnClickListener(this);
        findViewById(R.id.ButtonGammaEncode).setOnClickListener(this);
        findViewById(R.id.ButtonChromaCorrection).setOnClickListener(this);
    }

    private void initHDRToSDRShaderTransform() {
        hdrToSDRShaderTransform.setGammaOETF(GammaOETF.BT1886);
        hdrToSDRShaderTransform.setGamutMap(GamutMap.CLIP);
        hdrToSDRShaderTransform.setToneMap(ToneMap.ANDROID13);
        hdrToSDRShaderTransform.setChromaCorrection(ChromaCorrection.BT2446C);
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


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ButtonCubeLut) {
            showCubeLutMenu(v);
        } else if (id == R.id.ButtonVideoList) {
            showVideListMenu(v);
        } else if (id == R.id.ButtonVideoOutput) {
            showVideoOutputMenu(v);
        } else if (id == R.id.ButtonViewMode) {
            showViewModeMenu(v);
        } else if (id == R.id.ButtonTextureSource) {
            showTextureSourceMenu(v);
        } else if (id == R.id.ButtonBitDepth) {
            showHdrBitDepthMenu(v);
        } else if (id == R.id.ButtonTransformMode) {
            showTransformModeMenu(v);
        } else if (id == R.id.ButtonChromaCorrection) {
            showChromaCorrectMenu(v);
        } else if (id == R.id.ButtonToneMap) {
            showToneMapMenu(v);
        } else if (id == R.id.ButtonGamutMap) {
            showGamutMapMenu(v);
        } else if (id == R.id.ButtonGammaEncode) {
            showGamutEncodeMenu(v);
        }
    }

    private void showScreenInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("HDR10:");
        stringBuffer.append(DisplayUtil.isSupportHdr10()?"✓":"×");
        stringBuffer.append(" ");
        stringBuffer.append("HLG:");
        stringBuffer.append(DisplayUtil.isSupportHlg()?"✓":"×");
        stringBuffer.append(" ");
        stringBuffer.append("HDR10+:");
        stringBuffer.append(DisplayUtil.isSupportHdr10Plus()?"✓":"×");
        stringBuffer.append(" ");
        stringBuffer.append("DolbyVision:");
        stringBuffer.append(DisplayUtil.isSupportDolbyVision()?"✓":"×");
        textViewScreenInfo.setText(stringBuffer);
    }


    private void showOpenGLSupportInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("PQ:");
        stringBuffer.append(GLESUtil.isSupportBT2020PQ()?"✓":"×");
        stringBuffer.append(" ");
        stringBuffer.append("HLG:");
        stringBuffer.append(GLESUtil.isSupportBT2020HLG()?"✓":"×");
        stringBuffer.append(" ");
        stringBuffer.append("Linear:");
        stringBuffer.append(GLESUtil.isSupportBT2020Linear()?"✓":"×");
        textViewOpenGLSupportInfo.setText(stringBuffer);
    }

    private void showVideoInfo(MediaFormat outputFormat) {
        int colorStandard = MediaFormatUtil.getColorStandard(outputFormat);
        int colorTransfer = MediaFormatUtil.getColorTransfer(outputFormat);
        int colorRange = MediaFormatUtil.getColorRange(outputFormat);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("色域:");
        if (colorStandard == ColorStandard.BT709) {
            stringBuffer.append("BT709");
        } else if (colorStandard == ColorStandard.BT601_NTSC) {
            stringBuffer.append("BT601_NTSC");
        } else if (colorStandard == ColorStandard.BT601_PAL) {
            stringBuffer.append("BT601_PAL");
        } else if (colorStandard == ColorStandard.BT2020) {
            stringBuffer.append("BT2020");
        }else {
            stringBuffer.append("未知");
        }
        stringBuffer.append(" ");
        stringBuffer.append("传递函数:");
        if (colorTransfer == ColorTransfer.SDR_VIDEO) {
            stringBuffer.append("BT709");
        } else if (colorTransfer == ColorTransfer.HLG) {
            stringBuffer.append("HLG");
        } else if (colorTransfer == ColorTransfer.ST2084) {
            stringBuffer.append("ST2084");
        } else if (colorTransfer == ColorTransfer.LINEAR) {
            stringBuffer.append("LINEAR");
        }else {
            stringBuffer.append("未知");
        }
        stringBuffer.append(" ");
        stringBuffer.append("范围:");
        if (colorRange == ColorRange.LIMITED) {
            stringBuffer.append("limited");
        } else if (colorRange == ColorRange.FULL) {
            stringBuffer.append("full");
        }
        textViewVideoInfo.post(new Runnable() {
            @Override
            public void run() {
                textViewVideoInfo.setText(stringBuffer.toString());
            }
        });
    }

    private void showCubeLutMenu(View v) {
        if (colorSpace == -1) {
            Toast.makeText(this, "正在加载中，请稍后再点击", Toast.LENGTH_SHORT).show();
            return;
        }
        if (colorSpace == ColorSpace.VIDEO_SDR) {
            Toast.makeText(this, "不是HDR视频，不能加载HDR转SDR的LUT", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu pum = new PopupMenu(this, v);
        Menu menu = pum.getMenu();
        menu.add(0, -1, 0, "无");
        List<String> assetList = colorSpace == ColorSpace.VIDEO_BT2020_PQ ? pq2sdrCubeList : hlg2sdrCubeList;

        for (int i = 0; i < assetList.size(); i++) {
            String fileName = new File(assetList.get(i)).getName();
            int pos = fileName.lastIndexOf(".");
            if (pos > 0) {
                fileName = fileName.substring(0, pos);
            }
            menu.add(0, i, 0, fileName);
        }
        for (int i = -1; i < assetList.size(); i++) {
            menu.findItem(i).setCheckable(true);
        }
        menu.findItem(assetList.indexOf(cubeLutAsset)).setChecked(true);
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                cubeLutAsset = id < 0 ? null : assetList.get(id);
                cubeLutVideoTransform.setCubeLut(cubeLutAsset);
                return true;
            }
        });
        pum.show();
    }

    private void showVideListMenu(View v) {

        PopupMenu pum = new PopupMenu(this, v);
        Menu menu = pum.getMenu();
        for (int i = 0; i < videoList.size(); i++) {
            String fileName = new File(videoList.get(i)).getName();
            int pos = fileName.lastIndexOf(".");
            if (pos > 0) {
                fileName = fileName.substring(0, pos);
            }
            menu.add(0, i, 0, fileName);
        }
        for (int i = 0; i < menu.size(); i++) {
            menu.findItem(i).setCheckable(true);
        }
        FileSource fileSource = videoPlayer.getSource();
        int index = videoList.indexOf(fileSource.getPath());
        menu.findItem(index).setChecked(true);
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                videoPlayer.stop();
                videoPlayer.setSource(AssetFileSource.create(videoList.get(item.getItemId())));
                videoPlayer.start();
                return true;
            }
        });
        pum.show();
    }

    void showVideoOutputMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.video_output_menu);
        Menu menu = pum.getMenu();
        VideoOutput currentVideOutput = videoPlayer.getVideoOutput();
        if (currentVideOutput instanceof DirectVideoOutput) {
            menu.findItem(R.id.direct_video_output).setChecked(true);
        } else if (currentVideOutput instanceof GLVideoOutput) {
            menu.findItem(R.id.gl_video_output).setChecked(true);
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                VideoOutput selectOutput = null;
                if (item.getItemId() == R.id.direct_video_output) {
                    selectOutput = directVideoOutput;
                } else if (item.getItemId() == R.id.gl_video_output) {
                    selectOutput = glVideoOutput;
                }
                if (currentVideOutput == selectOutput) {
                    return true;
                }
                showHdrToSdrLayout(selectOutput);
                videoPlayer.stop();
                videoPlayer.setVideoOutput(selectOutput);
                videoPlayer.start();
                return true;
            }
        });
        pum.show();
    }

    void showViewModeMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.view_mode_menu);
        Menu menu = pum.getMenu();
        if (viewType == VideoView.ViewType.TEXTURE_VIEW) {
            menu.findItem(R.id.textureView).setChecked(true);
        } else if (viewType == VideoView.ViewType.SURFACE_VIEW) {
            menu.findItem(R.id.surfaceView).setChecked(true);
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.surfaceView) {
                    viewType = VideoView.ViewType.SURFACE_VIEW;
                } else if (item.getItemId() == R.id.textureView) {
                    viewType = VideoView.ViewType.TEXTURE_VIEW;
                }
                videoView.setViewType(viewType);
                return true;
            }
        });
        pum.show();
    }


    void showTextureSourceMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.texture_source_menu);
        Menu menu = pum.getMenu();
        if (textureSource == GLVideoOutput.TextureSource.AUTO) {
            menu.findItem(R.id.textureSourceAuto).setChecked(true);
        } else if (textureSource == GLVideoOutput.TextureSource.BUFFER) {
            menu.findItem(R.id.textureSourceBuffer).setChecked(true);
        } else if (textureSource == GLVideoOutput.TextureSource.EXT) {
            menu.findItem(R.id.textureSourceExt).setChecked(true);
        } else if (textureSource == GLVideoOutput.TextureSource.Y2Y) {
            menu.findItem(R.id.textureSourceY2Y).setChecked(true);
        } else if (textureSource == GLVideoOutput.TextureSource.OES) {
            menu.findItem(R.id.textureSourceOES).setChecked(true);
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.textureSourceAuto) {
                    textureSource = GLVideoOutput.TextureSource.AUTO;
                } else if (item.getItemId() == R.id.textureSourceBuffer) {
                    textureSource = GLVideoOutput.TextureSource.BUFFER;
                } else if (item.getItemId() == R.id.textureSourceExt) {
                    textureSource = GLVideoOutput.TextureSource.EXT;
                } else if (item.getItemId() == R.id.textureSourceY2Y) {
                    textureSource = GLVideoOutput.TextureSource.Y2Y;
                } else if (item.getItemId() == R.id.textureSourceOES) {
                    textureSource = GLVideoOutput.TextureSource.OES;
                }
                videoPlayer.stop();
                glVideoOutput.setTextureSource(textureSource);
                videoPlayer.start();
                return true;
            }
        });
        pum.show();
    }


    void showHdrBitDepthMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.hdr_bit_depth_menu);
        Menu menu = pum.getMenu();
        if (hdrBitDepth == GLVideoOutput.HdrBitDepth.BIT_DEPTH_8) {
            menu.findItem(R.id.hdr_bit_depth_8).setChecked(true);
        } else if (hdrBitDepth == GLVideoOutput.HdrBitDepth.BIT_DEPTH_10) {
            menu.findItem(R.id.hdr_bit_depth_10).setChecked(true);
        } else if (hdrBitDepth == GLVideoOutput.HdrBitDepth.BIT_DEPTH_16) {
            menu.findItem(R.id.hdr_bit_depth_16).setChecked(true);
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.hdr_bit_depth_8) {
                    hdrBitDepth = GLVideoOutput.HdrBitDepth.BIT_DEPTH_8;
                } else if (item.getItemId() == R.id.hdr_bit_depth_10) {
                    hdrBitDepth = GLVideoOutput.HdrBitDepth.BIT_DEPTH_10;
                } else if (item.getItemId() == R.id.hdr_bit_depth_16) {
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


    void showChromaCorrectMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        Menu menu = pum.getMenu();
        List<Item<ChromaCorrection>> menuList = CHROMA_CORRECTION_MENU_LIST;
        for (int i = 0; i < menuList.size(); i++) {
            String name = new File(menuList.get(i).title).getName();
            menu.add(0, i, 0, name);
        }
        for (int i = 0; i < menu.size(); i++) {
            Item<ChromaCorrection> item = menuList.get(i);
            MenuItem menuItem = menu.findItem(i);
            menuItem.setCheckable(true);
            menuItem.setChecked(item.value == hdrToSDRShaderTransform.getChromaCorrection());
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                hdrToSDRShaderTransform.setChromaCorrection(menuList.get(item.getItemId()).value);
                return true;
            }
        });
        pum.show();
    }

    void showToneMapMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        Menu menu = pum.getMenu();
        List<Item<ToneMap>> menuList = TONE_MAP_MENU_LIST;
        for (int i = 0; i < menuList.size(); i++) {
            String name = new File(menuList.get(i).title).getName();
            menu.add(0, i, 0, name);
        }
        for (int i = 0; i < menu.size(); i++) {
            Item<ToneMap> item = menuList.get(i);
            MenuItem menuItem = menu.findItem(i);
            menuItem.setCheckable(true);
            menuItem.setChecked(item.value == hdrToSDRShaderTransform.getToneMap());
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                hdrToSDRShaderTransform.setToneMap(menuList.get(item.getItemId()).value);
                return true;
            }
        });
        pum.show();
    }

    void showGamutMapMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        Menu menu = pum.getMenu();
        List<Item<GamutMap>> menuList = GAMUT_MAP_MENU_LIST;
        for (int i = 0; i < menuList.size(); i++) {
            String name = new File(menuList.get(i).title).getName();
            menu.add(0, i, 0, name);
        }
        for (int i = 0; i < menu.size(); i++) {
            Item<GamutMap> item = menuList.get(i);
            MenuItem menuItem = menu.findItem(i);
            menuItem.setCheckable(true);
            menuItem.setChecked(item.value == hdrToSDRShaderTransform.getGamutMap());
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                hdrToSDRShaderTransform.setGamutMap(menuList.get(item.getItemId()).value);
                return true;
            }
        });
        pum.show();
    }

    void showGamutEncodeMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        Menu menu = pum.getMenu();
        List<Item<GammaOETF>> menuList = GAMMA_OETF_MENU_LIST;
        for (int i = 0; i < menuList.size(); i++) {
            String name = new File(menuList.get(i).title).getName();
            menu.add(0, i, 0, name);
        }
        for (int i = 0; i < menu.size(); i++) {
            Item<GammaOETF> item = menuList.get(i);
            MenuItem menuItem = menu.findItem(i);
            menuItem.setCheckable(true);
            menuItem.setChecked(item.value == hdrToSDRShaderTransform.getGammaOETF());
        }
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                hdrToSDRShaderTransform.setGammaOETF(menuList.get(item.getItemId()).value);
                return true;
            }
        });
        pum.show();
    }

    void showTransformModeMenu(View v) {
        PopupMenu pum = new PopupMenu(this, v);
        pum.inflate(R.menu.transform_mode_menu);
        Menu menu = pum.getMenu();
        menu.findItem(transformModeId).setChecked(true);
        pum.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                transformModeId = item.getItemId();
                showTransformLayout(transformModeId);
                return true;
            }
        });
        pum.show();
    }

    void showTransformLayout(int transformModeId) {
        if (transformModeId == R.id.transform_mode_node) {
            cubeLutVideoTransform.disable();
            hdrToSDRShaderTransform.disable();
            findViewById(R.id.transformLayoutCubeLut).setVisibility(View.GONE);
            findViewById(R.id.transformLayoutShader).setVisibility(View.GONE);
        } else if (transformModeId == R.id.transform_mode_cube_lut) {
            cubeLutVideoTransform.enable();
            hdrToSDRShaderTransform.disable();
            findViewById(R.id.transformLayoutCubeLut).setVisibility(View.VISIBLE);
            findViewById(R.id.transformLayoutShader).setVisibility(View.GONE);
        } else if (transformModeId == R.id.transform_mode_cube_shader) {
            cubeLutVideoTransform.disable();
            hdrToSDRShaderTransform.enable();
            findViewById(R.id.transformLayoutCubeLut).setVisibility(View.GONE);
            findViewById(R.id.transformLayoutShader).setVisibility(View.VISIBLE);
        }
    }

    void showHdrToSdrLayout(VideoOutput videoOutput) {
        findViewById(R.id.HdrToSdrLayout).setVisibility(videoOutput instanceof GLVideoOutput ? View.VISIBLE : View.GONE);
    }


    static class Item<T> {
        public Item(String title, T value) {
            this.title = title;
            this.value = value;
        }

        String title;
        T value;
    }
}