package com.norman.android.hdrsample.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BufferUtil {
    private static final int FLOAT_SIZE = 4;

    public static FloatBuffer createDirectFloatBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(data.length * FLOAT_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(data);
        buffer.clear();
        return buffer;
    }


    public static FloatBuffer createDirectFloatBuffer(int size) {
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(size * FLOAT_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        return buffer;
    }
}
