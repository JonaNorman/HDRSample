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

    public static ByteBuffer growCapacity(ByteBuffer byteBuffer) {
        return growCapacity(byteBuffer, byteBuffer.capacity() * 2);
    }

    public static ByteBuffer growCapacity(ByteBuffer byteBuffer, int newCapacity) {
        final int oloCapacity = byteBuffer.capacity();
        if (newCapacity < oloCapacity) {
            throw new IllegalArgumentException("new capacity must greater than old");
        }
        final ByteBuffer outBuffer = byteBuffer.isDirect() ?
                ByteBuffer.allocateDirect(newCapacity) :
                ByteBuffer.allocate(newCapacity);

        final int oldPos = byteBuffer.position();
        final int oldLimit = byteBuffer.limit();
        byteBuffer.clear();
        outBuffer.put(byteBuffer);
        byteBuffer.position(oldPos);
        byteBuffer.limit(oldLimit);
        outBuffer.position(oldPos);
        return outBuffer;
    }

}
