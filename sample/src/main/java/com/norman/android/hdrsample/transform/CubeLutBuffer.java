package com.norman.android.hdrsample.transform;

import android.content.res.AssetFileDescriptor;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.norman.android.hdrsample.exception.IORuntimeException;
import com.norman.android.hdrsample.util.BufferUtil;
import com.norman.android.hdrsample.util.FileUtil;
import com.norman.android.hdrsample.util.GLESUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * 加载Cube文件中的Buffer的工具类
 * 刚开始参考<a href="https://github.com/Milchreis/processing-imageprocessing/blob/master/src/milchreis/imageprocessing/CubeLUT.java">CubeLUT</a>发现加载特别慢
 * 加载时间从3s左右优化成70ms，措施如下
 * 1. MappedByteBuffer减轻内核上下文切换带来的时间开销
 * 2. 原先读取一行String再去匹配，改成匹配缓存ByteBuffer，降低内存开销
 * 3. 原先String转Float，改成读取ByteBuffer中的字节计算出Float，降低String中重新创建Buffer的开销
 * 代码逻辑有点复杂，如果看不懂，可以尝试自己写，也许加载速度更快
 * 核心逻辑就是读取Cube文件中的 title size 和 RGB数据
 *  title就是从Cube文件读取像TITLE "BT2020_HLG_BT601_PAL"中的BT2020_HLG_BT601_PAL
 *  size就是从Cube文件读取像LUT_3D_SIZE 33中的33
 *  RGB数据就是从Cube文件读取像0.50289002 0.59033508 0.77007249读取数字转换成RGB Buffer
 */
public class CubeLutBuffer {

    private static final byte MATCH_RUNNING = 0;
    private static final byte MATCH_COMPLETE = 1;
    private static final byte MATCH_STOP = 2;

    /**
     * 匹配RGB float数据的符号位
     */
    private static final byte FLOAT_STATE_SIGN = 0;

    /**
     * 匹配RGB float数据的整数位
     */
    private static final byte FLOAT_STATE_INT = 1;
    /**
     * 匹配RGB float数据的小数位
     */
    private static final byte FLOAT_STATE_DECIMAL = 2;

    /**
     * 匹配RGB float数据的指数位
     */

    private static final byte FLOAT_STATE_EXPONENT = 3;

    /**
     * 换行
     */

    private static final byte BYTE_LINE = '\n';
    /**
     * 空格
     */
    private static final byte BYTE_SPACE = ' ';

    /**
     * # 代表注释，后面整行的数据都可以丢掉
     */
    private static final byte BYTE_NUMBER = '#';

    /**
     * 0
     */
    private static final byte BYTE_0 = '0';
    /**
     * 9
     */
    private static final byte BYTE_9 = '9';
    /**
     * 小数点
     */
    private static final byte BYTE_DOT = '.';

    /**
     * 指数小写
     */
    private static final byte BYTE_e = 'e';

    /**
     * 指数大写
     */
    private static final byte BYTE_E = 'E';

    /**
     * 匹配RGB float数据符号位的加号
     */
    private static final byte BYTE_PLUS = '+';

    /**
     * 匹配RGB float数据符号位的减号
     */
    private static final byte BYTE_MINUS = '-';

    /**
     * 匹配Cube文件的Size
     */

    private static final byte[] MATCH_ARRAY_3D_SIZE = "LUT_3D_SIZE".getBytes(StandardCharsets.UTF_8);

    /**
     * 匹配Cube文件中的Title
     */
    private static final byte[] MATCH_ARRAY_TITLE = "TITLE".getBytes(StandardCharsets.UTF_8);

    /**
     * 一次读取Buffer缓存的大小64k，拍脑袋瓜定的数值，感觉速度和内存都还可以
     * 65536=2^16,2^10是1k，那么65536就是64k,
     */
    private static final int DEFAULT_LENGTH_READ_ARRAY = 65536;

    /**
     * 匹配数据的缓存，也是拍脑袋瓜定的数值
     */
    private static final int DEFAULT_LENGTH_MATCH_BUFFER = 128;


    /**
     * 标题 譬如从Cube文件读取 TITLE "BT2020_HLG_BT601_PAL"中的BT2020_HLG_BT601_PAL
     */
    public String title;
    /**
     * 大小 表示RGB图像的宽高和深度， 譬如从Cube文件读取 LUT_3D_SIZE 33中的33
     */
    public Integer size;

    /**
     * RGB数据 每行中有3个float数字，依次是RGB，需要把String转float
     */

    public ByteBuffer buffer;

    private CubeLutBuffer(String assetName) {
        FileInputStream inputStream = null;
        try {
            // 读取asset文件
            AssetFileDescriptor assetFileDescriptor = FileUtil.openAssetFileDescriptor(assetName);
            inputStream = assetFileDescriptor.createInputStream();
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = assetFileDescriptor.getStartOffset();
            long declaredLength = assetFileDescriptor.getDeclaredLength();

            // 把文件转换成MappedByteBuffer
            // 读取大文件的一种方式(共享内存避免上下文切换带来的耗时)，注意只能读取2G以下的文件
            // 注意要传offset不能直接传0，不然会发现读取除出来的文字前面多了一些莫名奇妙的字符
            MappedByteBuffer assetMapBuffer = fileChannel
                    .map(FileChannel.MapMode.READ_ONLY,
                            startOffset,
                            declaredLength);

            //匹配不同的状态值
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
                    if (readByte == BYTE_LINE) {
                        matchStateNewLine = MATCH_RUNNING;
                    }
                }
            }


        } catch (IOException e) {
            throw  new IORuntimeException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 把buffer转换成纹理
     * @return
     */
    public  int createTextureId(){
        buffer.rewind();
        int textureId = GLESUtil.create3DTextureId();
        GLESUtil.checkGLError();
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, textureId);
        GLES30.glTexImage3D(GLES30.GL_TEXTURE_3D,
                0, GLES30.GL_RGB16F,//纹理RGBA16格式，保证数据精度够用
                size,//宽
                size,//高
                size,//深度
                0,
                GLES30.GL_RGB,
                GLES30.GL_FLOAT,
                buffer);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, 0);
        return textureId;
    }

    /**
     * 加载asset中的Cube文件
     * @param assetName
     * @return
     */

    public static CubeLutBuffer loadAsset(String assetName) {
        return new CubeLutBuffer(assetName);
    }


}

