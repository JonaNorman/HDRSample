package com.norman.android.hdrsample.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collector;

public class CubeLut {

    public String title;
    public int size = 0;
    public float[] domainMin;
    public float[] domainMax;

    public ByteBuffer buffer;

    private CubeLut(String assetName) {
        FileInputStream inputStream = null;
        AssetFileDescriptor assetFileDescriptor =null;
        try {
            long time = System.currentTimeMillis();
            assetFileDescriptor = FileUtil.openAssetFileDescriptor(assetName);
            inputStream = assetFileDescriptor.createInputStream();
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer myMappedBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            byte[] bytes = new byte[(int) fileChannel.size()];
            myMappedBuffer.get(bytes);
            String content = new String(bytes, StandardCharsets.UTF_8);
            time = System.currentTimeMillis();

            String[] arr = content.split("\n");
            Log.v("1111111","jjjjjjjj"+(System.currentTimeMillis()-time)+"ms");


            for (int i = 0; i < arr.length; i++) {
                String line = arr[i];
                line = line.trim();

                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts[0].equals("LUT_IN_VIDEO_RANGE") || parts[0].equals("LUT_OUT_VIDEO_RANGE")) {
                    continue;
                }
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

                    Log.v("1111111","a"+(System.currentTimeMillis()-time)+"ms");
                    time = System.currentTimeMillis();
                    int numChannels = 3;
                    int bytesPerChannel = Float.BYTES;
                    int bytesPerPixel = numChannels * bytesPerChannel;
                    buffer = ByteBuffer.allocateDirect(size * size * size * bytesPerPixel);
                    buffer.order(ByteOrder.nativeOrder());

                    String[]  arr1 =  new String[arr.length-i];

                    System.arraycopy(arr,i,arr1,0,arr1.length);
                    Log.v("1111111","b"+(System.currentTimeMillis()-time)+"ms");
                    time = System.currentTimeMillis();

                    String result=  String.join(" ", arr1);


                    Log.v("1111111","c"+(System.currentTimeMillis()-time)+"ms");
                    time = System.currentTimeMillis();


                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        float[] aa  = toFloatArray(result);
                        Log.v("1111111","d"+(System.currentTimeMillis()-time)+"ms");
                        time = System.currentTimeMillis();
                        buffer.asFloatBuffer().put(aa);
                    }
                    break;
                }

            }


            Log.v("1111111","time"+(System.currentTimeMillis()-time)+"ms");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
            }
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }


    public static CubeLut createForAsset(String assetName) {

        return new CubeLut(assetName);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    float[] toFloatArray(String result) {
        class FloatArray {
            int size;
            float[] array;
            FloatArray() {
                this.size = 0;
                this.array = new float[10];
            }
            void add(float f) {
                if (size == array.length) {
                    array = Arrays.copyOf(array, array.length * 2);
                }
                array[size++] = f;
            }
            FloatArray combine(FloatArray other) {
                float[] resultArray = new float[array.length + other.array.length];
                System.arraycopy(this.array, 0, resultArray, 0, size);
                System.arraycopy(other.array, 0, resultArray, size, other.size);
                this.array = resultArray;
                this.size += other.size;
                return this;
            }
            float[] result() {
                return Arrays.copyOf(array, size);
            }
        }
        return Arrays.stream(result.split(" ")).map(s1 -> Float.parseFloat(s1)).collect(
                Collector.of(
                        FloatArray::new, FloatArray::add,
                        FloatArray::combine, FloatArray::result));
    }



}

