package com.norman.android.hdrsample;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.norman.android.hdrsample.opengl.GLShaderCode;
import com.norman.android.hdrsample.transform.shader.chromacorrect.ChromaCorrection;
import com.norman.android.hdrsample.transform.shader.gamma.GammaOETF;
import com.norman.android.hdrsample.transform.shader.gamutmap.GamutMap;
import com.norman.android.hdrsample.transform.shader.tonemap.ToneMap;

public class HdrToSdrShaderDialog extends Dialog implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Spinner spinnerChromaCorrection;
    private Spinner spinnerToneMap;
    private Spinner spinnerGamutMap;

    private Spinner spinnerDisplayGamma;


    private ChromaCorrection chromaCorrection = ChromaCorrection.NONE;

    private ToneMap toneMap = ToneMap.NONE;

    private GamutMap gamutMap = GamutMap.NONE;

    private GammaOETF gammaOETF = GammaOETF.NONE;

    private OnShaderSelectListener onShaderSelectListener;


    public HdrToSdrShaderDialog(@NonNull Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_hdr_to_sdr_shader);
        Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        spinnerChromaCorrection = findViewById(R.id.spinner_chroma_correction);
        spinnerChromaCorrection.setOnItemSelectedListener(this);
        spinnerChromaCorrection.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new ShaderItem[]{
                        new ShaderItem(ChromaCorrection.NONE, "无"),
                        new ShaderItem(ChromaCorrection.BT2446C, "BT2446C")}));

        spinnerToneMap = findViewById(R.id.spinner_tone_map);
        spinnerToneMap.setOnItemSelectedListener(this);
        spinnerToneMap.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new ShaderItem[]{
                        new ShaderItem(ToneMap.NONE, "无"),
                        new ShaderItem(ToneMap.ANDROID8, "ANDROID8"),
                        new ShaderItem(ToneMap.ANDROID13, "ANDROID13"),
                        new ShaderItem(ToneMap.BT2446A, "BT2446A"),
                        new ShaderItem(ToneMap.BT2446C, "BT2446C"),
                        new ShaderItem(ToneMap.HABLE, "HABLE")
                }));

        spinnerGamutMap = findViewById(R.id.spinner_gamut_map);
        spinnerGamutMap.setOnItemSelectedListener(this);
        spinnerGamutMap.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new ShaderItem[]{
                        new ShaderItem(GamutMap.NONE, "无"),
                        new ShaderItem(GamutMap.CLIP, "CLIP"),
                        new ShaderItem(GamutMap.COMPRESS, "COMPRESS"),
                        new ShaderItem(GamutMap.ADAPTIVE_L0_CUSP, "ADAPTIVE_L0_CUSP"),
                }));

        spinnerDisplayGamma = findViewById(R.id.spinner_gamma_function);
        spinnerDisplayGamma.setOnItemSelectedListener(this);
        spinnerDisplayGamma.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new ShaderItem[]{
                        new ShaderItem(GammaOETF.NONE, "无"),
                        new ShaderItem(GammaOETF.BT1886, "BT1886"),
                        new ShaderItem(GammaOETF.S170M, "S170M"),
                        new ShaderItem(GammaOETF.BT709, "BT709"),
                }));
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ShaderItem shaderItem = (ShaderItem) parent.getItemAtPosition(position);
        GLShaderCode shaderCode = shaderItem.shaderCode;
        if (shaderCode instanceof ChromaCorrection) {
            chromaCorrection = (ChromaCorrection) shaderCode;
        } else if (shaderCode instanceof ToneMap) {
            toneMap = (ToneMap) shaderCode;
        } else if (shaderCode instanceof GamutMap) {
            gamutMap = (GamutMap) shaderCode;
        } else if (shaderCode instanceof GammaOETF) {
            gammaOETF = (GammaOETF) shaderCode;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            if (onShaderSelectListener != null) {
                onShaderSelectListener.onShaderSelect(chromaCorrection,toneMap,gamutMap,gammaOETF);
            }
            dismiss();
        } else if (id == R.id.btn_cancel) {
            dismiss();
        }
    }

    public void setOnShaderSelectListener(OnShaderSelectListener onShaderSelectListener) {
        this.onShaderSelectListener = onShaderSelectListener;
    }

    private static class ShaderItem {
        GLShaderCode shaderCode;
        String name;

        public ShaderItem(GLShaderCode shaderCode, String name) {
            this.shaderCode = shaderCode;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    interface OnShaderSelectListener {
        void onShaderSelect(ChromaCorrection chromaCorrection,
                            ToneMap toneMap,
                            GamutMap gamutMap,
                            GammaOETF gammaOETF);
    }
}
