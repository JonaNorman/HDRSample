package com.norman.android.hdrsample.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import com.norman.android.hdrsample.player.source.FileSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class CubeLut {

    public String title;
    public int size = 0;
    public float[] domainMin;
    public float[] domainMax;

    public ByteBuffer buffer;

    private CubeLut(InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts[0].equals("LUT_1D_SIZE")) {
                    throw new RuntimeException("not support LUT_1D_SIZE");
                } else if (parts[0].equals("LUT_2D_SIZE")) {
                    throw new RuntimeException("not support LUT_2D_SIZE");

                } else if (parts[0].equals("LUT_3D_SIZE")) {
                    size = Integer.parseInt(parts[1]);

                } else if (parts[0].equals("TITLE")) {
                    title = parts[0];

                } else if (parts[0].equals("DOMAIN_MIN")) {

                    domainMin = new float[]{
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]),
                            Float.parseFloat(parts[3])
                    };

                } else if (parts[0].equals("DOMAIN_MAX")) {
                    domainMax = new float[]{
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]),
                            Float.parseFloat(parts[3])
                    };
                } else {
                    if (buffer == null) {
                        int numChannels = 3;
                        int bytesPerChannel = 4;
                        int bytesPerPixel = numChannels * bytesPerChannel;
                        buffer = ByteBuffer.allocateDirect(size * size * size * bytesPerPixel);
                        buffer.order(ByteOrder.nativeOrder());
                    }
                    for (int i = 0; i < 3; i++) {
                        buffer.putFloat(Float.parseFloat(parts[i]));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }

    }


    public static CubeLut createForAsset(String assetName) {
        try {
            AssetFileDescriptor assetFileDescriptor = FileUtil.openAssetFileDescriptor(assetName);
            return new CubeLut(assetFileDescriptor.createInputStream());
        } catch (IOException e) {
            ExceptionUtil.throwRuntime(e);
        }
        return null;
    }

}

