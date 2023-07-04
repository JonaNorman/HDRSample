package com.norman.android.hdrsample.util;

import android.content.res.AssetFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class CubeLut3D {

    private static final byte MATCH_RUNNING = 0;
    private static final byte MATCH_COMPLETE = 1;
    private static final byte MATCH_STOP = 2;

    private static final byte FLOAT_STATE_SIGN = 0;
    private static final byte FLOAT_STATE_INT = 1;

    private static final byte FLOAT_STATE_DECIMAL = 2;

    private static final byte FLOAT_STATE_EXPONENT = 3;

    private static final byte BYTE_LINE = '\n';
    private static final byte BYTE_SPACE = ' ';
    private static final byte BYTE_NUMBER = '#';
    private static final byte BYTE_0 = '0';
    private static final byte BYTE_9 = '9';
    private static final byte BYTE_DOT = '.';
    private static final byte BYTE_e = 'e';
    private static final byte BYTE_E = 'E';
    private static final byte BYTE_PLUS = '+';
    private static final byte BYTE_MINUS = '-';

    private static final byte[] MATCH_ARRAY_3D_SIZE = "LUT_3D_SIZE".getBytes(StandardCharsets.UTF_8);

    private static final byte[] MATCH_ARRAY_TITLE = "TITLE".getBytes(StandardCharsets.UTF_8);
    private static final int DEFAULT_LENGTH_READ_ARRAY = 65536;
    private static final int DEFAULT_LENGTH_MATCH_BUFFER = 128;


    public String title;
    public Integer size;

    public ByteBuffer buffer;

    private CubeLut3D(String assetName) {
        FileInputStream inputStream = null;
        try {
            AssetFileDescriptor assetFileDescriptor = FileUtil.openAssetFileDescriptor(assetName);
            assert assetFileDescriptor != null;
            inputStream = assetFileDescriptor.createInputStream();
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = assetFileDescriptor.getStartOffset();
            long declaredLength = assetFileDescriptor.getDeclaredLength();
            MappedByteBuffer assetMapBuffer = fileChannel
                    .map(FileChannel.MapMode.READ_ONLY,
                            startOffset,
                            declaredLength);
            byte matchStateNewLine = MATCH_RUNNING;
            byte matchStateFindNewLine = MATCH_STOP;
            byte matchStateTitle = MATCH_STOP;
            byte matchState3DSize = MATCH_STOP;
            byte matchState3DFloatBuffer = MATCH_STOP;

            byte[] readArray = new byte[DEFAULT_LENGTH_READ_ARRAY];
            int readLength = readArray.length;
            ByteBuffer matchBuffer = ByteBuffer.allocate(DEFAULT_LENGTH_MATCH_BUFFER);
            int matchIndex = -1;
            int floatValueSignPart = 1;// sign part
            float floatValuePart = 0; // float part
            float  floatDecimalPlace = 0.1f;
            int floatExponentPart = 0; // exponent part
            int floatExponentSignPart = 1; // exponent sign part
            byte floatState = FLOAT_STATE_SIGN;

            while (assetMapBuffer.hasRemaining()) {
                int remaining = assetMapBuffer.remaining();
                if (remaining < readLength) {
                    readLength = remaining;
                }
                assetMapBuffer.get(readArray, 0, readLength);
                for (int i = 0; i < readLength; i++) {
                    byte readByte = readArray[i];
                    matchIndex++;
                    if (matchState3DFloatBuffer == MATCH_RUNNING) {
                        if (readByte == BYTE_LINE || readByte == BYTE_SPACE) {
                            if (floatState != FLOAT_STATE_SIGN){
                                float finalValue =  floatValueSignPart * floatValuePart * (float)Math.pow(10, floatExponentSignPart*floatExponentPart);
                                buffer.putFloat(finalValue);
                                matchIndex = -1;
                                floatValueSignPart = 1;
                                floatValuePart = 0;
                                floatDecimalPlace = 0.1f;
                                floatExponentPart = 0;
                                floatExponentSignPart = 1;
                                floatState = FLOAT_STATE_SIGN;
                            }
                            continue;
                        }
                        if (floatState == FLOAT_STATE_SIGN) {
                            floatState = FLOAT_STATE_INT;
                            if (matchIndex == 0 && readByte == BYTE_PLUS) {
                                floatValueSignPart = 1;
                            } else if (matchIndex == 0 && readByte == BYTE_MINUS) {
                                floatValueSignPart = -1;
                            } else if (matchIndex == 0 && BYTE_0 <= readByte && readByte <= BYTE_9) {
                                floatValuePart = floatValuePart * 10 + readByte - BYTE_0;
                            } else {
                                throw new IllegalArgumentException("float parse fail");
                            }
                        } else if (floatState == FLOAT_STATE_INT) {
                            if (BYTE_0 <= readByte && readByte <= BYTE_9) {
                                floatValuePart = floatValuePart * 10 + readByte - BYTE_0;
                            } else if (readByte == BYTE_DOT) {
                                floatState = FLOAT_STATE_DECIMAL;
                            } else if (readByte == BYTE_e || readByte == BYTE_E) {
                                floatState = FLOAT_STATE_EXPONENT;
                            } else {
                                throw new IllegalArgumentException("float parse fail");
                            }
                        } else if (floatState == FLOAT_STATE_DECIMAL) {
                            if (BYTE_0 <= readByte && readByte <= BYTE_9) {
                                floatValuePart = floatValuePart +  (readByte - BYTE_0)*floatDecimalPlace;
                                floatDecimalPlace = floatDecimalPlace/10;
                            } else if ((readByte == BYTE_e || readByte == BYTE_E)) {
                                floatState = FLOAT_STATE_EXPONENT;
                            } else {
                                throw new IllegalArgumentException("float parse fail");
                            }

                        } else if (floatState == FLOAT_STATE_EXPONENT) {

                            if (matchIndex == 0 && readByte == BYTE_PLUS) {
                                floatExponentSignPart = 1;
                            } else if (matchIndex == 0 && readByte == BYTE_MINUS) {
                                floatExponentSignPart = -1;
                            } else if (BYTE_0 <= readByte && readByte <= BYTE_9) {
                                floatExponentPart = floatExponentPart * 10 + readByte - BYTE_0;
                            } else {
                                throw new IllegalArgumentException("float parse fail");
                            }
                        }
                        continue;
                    }

                    if (matchStateFindNewLine == MATCH_RUNNING) {
                        if (readByte == BYTE_LINE) {
                            matchStateNewLine = MATCH_RUNNING;
                            matchStateFindNewLine = MATCH_STOP;
                        }
                        continue;
                    }
                    if (matchStateNewLine == MATCH_RUNNING && readByte <= BYTE_SPACE) {
                        continue;
                    } else if (matchStateNewLine == MATCH_RUNNING) {
                        matchStateNewLine = MATCH_COMPLETE;
                        if (title == null) {
                            matchStateTitle = MATCH_RUNNING;
                        }
                        if (size == null) {
                            matchState3DSize = MATCH_RUNNING;
                        }
                        matchIndex =0;
                    }

                    if (matchIndex == 0 && readByte == BYTE_NUMBER) {
                        matchStateFindNewLine = MATCH_RUNNING;
                        continue;
                    }

                    if (matchStateTitle == MATCH_RUNNING) {
                        byte[] search = MATCH_ARRAY_TITLE;
                        if (matchIndex < search.length) {
                            if (readByte != search[matchIndex]) {
                                matchStateTitle = MATCH_STOP;
                            } else if (matchIndex == search.length - 1) {
                                matchBuffer.clear();
                            }
                        } else {
                            if (readByte == BYTE_LINE) {
                                matchBuffer.flip();
                                title = new String(matchBuffer.array(), matchBuffer.position(), matchBuffer.limit()).trim();
                                matchStateTitle = MATCH_COMPLETE;
                                matchStateNewLine = MATCH_RUNNING;
                                matchBuffer.clear();
                                continue;
                            } else {
                                if (!matchBuffer.hasRemaining())
                                    matchBuffer = BufferUtil.growCapacity(matchBuffer);
                                matchBuffer.put(readByte);
                            }
                        }
                    }

                    if (matchState3DSize == MATCH_RUNNING) {
                        byte[] search = MATCH_ARRAY_3D_SIZE;
                        if (matchIndex < search.length) {
                            if (readByte != search[matchIndex]) {
                                matchState3DSize = MATCH_STOP;
                            } else if (matchIndex == search.length - 1) {
                                matchBuffer.clear();
                            }
                        } else {
                            if (readByte == BYTE_LINE) {
                                matchBuffer.flip();
                                String result = new String(matchBuffer.array(), matchBuffer.position(), matchBuffer.limit()).trim();
                                size = Integer.parseInt(result);
                                matchState3DSize = MATCH_COMPLETE;
                                matchStateNewLine = MATCH_RUNNING;
                                matchBuffer.clear();
                                continue;
                            } else {
                                if (!matchBuffer.hasRemaining())
                                    matchBuffer = BufferUtil.growCapacity(matchBuffer);
                                matchBuffer.put(readByte);
                            }
                        }
                    }
                    if (matchStateTitle == MATCH_COMPLETE && matchState3DSize == MATCH_COMPLETE && matchState3DFloatBuffer == MATCH_STOP ) {
                        if (readByte == BYTE_LINE) {
                            matchBuffer.flip();
                            String result = new String(matchBuffer.array(), matchBuffer.position(), matchBuffer.limit()).trim();
                            String[] arr = result.split("\\s+");
                            try {
                                float a = Float.parseFloat(arr[0]);
                                float b = Float.parseFloat(arr[1]);
                                float c = Float.parseFloat(arr[2]);
                                int numChannels = 3;
                                int bytesPerChannel = Float.BYTES;
                                int bytesPerPixel = numChannels * bytesPerChannel;
                                buffer = ByteBuffer.allocateDirect(size * size * size * bytesPerPixel);
                                buffer.order(ByteOrder.nativeOrder());
                                buffer.putFloat(a);
                                buffer.putFloat(b);
                                buffer.putFloat(c);
                                matchState3DFloatBuffer = MATCH_RUNNING;
                                matchIndex = -1;
                            } catch (Exception ignored) {

                            }
                            matchStateNewLine = MATCH_RUNNING;
                            matchBuffer.clear();
                        } else {
                            if (!matchBuffer.hasRemaining())
                                matchBuffer = BufferUtil.growCapacity(matchBuffer);
                            matchBuffer.put(readByte);
                        }
                    }
                }
            }


        } catch (IOException e) {
            ExceptionUtil.throwRuntime(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }


    public static CubeLut3D createForAsset(String assetName) {
        return new CubeLut3D(assetName);
    }


}

