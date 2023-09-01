package com.norman.android.hdrsample.transform;

import android.content.res.AssetFileDescriptor;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.norman.android.hdrsample.exception.IORuntimeException;
import com.norman.android.hdrsample.util.BufferUtil;
import com.norman.android.hdrsample.util.FileUtil;
import com.norman.android.hdrsample.util.GLESUtil;
import com.norman.android.hdrsample.util.LogUtil;

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
 * 3. 原先String转Float，改成读取byte计算出Float，降低String中重新创建Buffer的开销
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
    private static final int DEFAULT_LENGTH_READ_BUFFER = 65536;

    /**
     * 匹配Title和Size的读取缓存，也是拍脑袋瓜定的数值
     */
    private static final int DEFAULT_LENGTH_TITLE_OR_SIZE_MATCH_BUFFER = 128;



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

    public ByteBuffer rgbBuffer;

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
            byte matchStateNextLine = MATCH_RUNNING;//新的一行匹配模式
            byte matchStateComment = MATCH_STOP;//注释模式
            byte matchStateTitle = MATCH_STOP;//title匹配模式
            byte matchStateSize = MATCH_STOP;//size匹配模式
            byte matchStateRGBBuffer = MATCH_STOP;//rgb匹配模式

            // 从assetMapBuffer一次读取readArray进行处理
            byte[] readBuffer = new byte[DEFAULT_LENGTH_READ_BUFFER];
            int readLength = readBuffer.length;
            // 匹配title和size的Buffer
            ByteBuffer titleOrSizeMatchBuffer = ByteBuffer.allocate(DEFAULT_LENGTH_TITLE_OR_SIZE_MATCH_BUFFER);
            int matchIndex = -1;//匹配的索引
            int floatValueSignPart = 1;// float的符号位
            float floatValuePart = 0; // float的整数部分和小数部分
            float  floatDecimalPlace = 0.1f;//float的小数位数
            int floatExponentPart = 0; // float的指数部分
            int floatExponentSignPart = 1; // float的指数里面的符号位
            byte floatState = FLOAT_STATE_SIGN;//匹配float已经到哪个状态，刚开始标记还在符号位

            while (assetMapBuffer.hasRemaining()) {//数据还没读完
                int remaining = assetMapBuffer.remaining();
                if (remaining < readLength) {//最后剩余的数据
                    readLength = remaining;
                }
                //一次读取readBuffer
                assetMapBuffer.get(readBuffer, 0, readLength);

                for (int i = 0; i < readLength; i++) {
                    byte readByte = readBuffer[i];
                    matchIndex++;
                    //开始匹配RGB数据
                    if (matchStateRGBBuffer == MATCH_RUNNING) {
                        //找到空格、换行、读取到最后表示前面的float读取完成了可以加到rgbBuffer中了
                        if (readByte == BYTE_LINE || readByte == BYTE_SPACE) {
                            if (floatState != FLOAT_STATE_SIGN){//只有不是在找符号位，数据才是有效的
                                //根据符号位、整数位、小数位、指数位计算float的数据
                                float finalValue =  floatValueSignPart * floatValuePart * (float)Math.pow(10, floatExponentSignPart*floatExponentPart);
                                rgbBuffer.putFloat(finalValue);
                                //为下一次读取float还原状态
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
                        if (floatState == FLOAT_STATE_SIGN) {//找符号位
                            floatState = FLOAT_STATE_INT;//变成找整数位
                            if (matchIndex == 0 && readByte == BYTE_PLUS) {//正数
                                floatValueSignPart = 1;
                            } else if (matchIndex == 0 && readByte == BYTE_MINUS) {//负数
                                floatValueSignPart = -1;
                            } else if (matchIndex == 0 && BYTE_0 <= readByte && readByte <= BYTE_9) {//第一个字符没有符号就是正数
                                floatValuePart = floatValuePart * 10 + readByte - BYTE_0;
                            } else {
                                throw new IllegalArgumentException("float parse fail");
                            }
                        } else if (floatState == FLOAT_STATE_INT) {//找整数位
                            if (BYTE_0 <= readByte && readByte <= BYTE_9) {//找到一个数就乘以10
                                floatValuePart = floatValuePart * 10 + readByte - BYTE_0;
                            } else if (readByte == BYTE_DOT) {//小数点，表示开始小数位匹配
                                floatState = FLOAT_STATE_DECIMAL;
                            } else if (readByte == BYTE_e || readByte == BYTE_E) {//指数
                                floatState = FLOAT_STATE_EXPONENT;
                            } else {
                                throw new IllegalArgumentException("float parse fail");
                            }
                        } else if (floatState == FLOAT_STATE_DECIMAL) {//小数位
                            if (BYTE_0 <= readByte && readByte <= BYTE_9) {//
                                floatValuePart = floatValuePart +  (readByte - BYTE_0)*floatDecimalPlace;
                                floatDecimalPlace = floatDecimalPlace/10;//位数就是除以10
                            } else if ((readByte == BYTE_e || readByte == BYTE_E)) {
                                floatState = FLOAT_STATE_EXPONENT;
                            } else {
                                throw new IllegalArgumentException("float parse fail");
                            }

                        } else if (floatState == FLOAT_STATE_EXPONENT) {//指数

                            if (matchIndex == 0 && readByte == BYTE_PLUS) {//指数的符号是正
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

                    if (matchStateComment == MATCH_RUNNING) {//注释模式
                        if (readByte == BYTE_LINE) {//换行
                            matchStateNextLine = MATCH_RUNNING;//新的一行开始
                            matchStateComment = MATCH_STOP;//注释模式结束
                        }
                        //如果是注释模式就直接读取下一个字符
                        continue;
                    }


                    if (matchStateNextLine == MATCH_RUNNING && readByte <= BYTE_SPACE) {
                        //新的一行开始前面的空格不需要读
                        continue;
                    } else if (matchStateNextLine == MATCH_RUNNING) {//开始找新的一行
                        matchStateNextLine = MATCH_COMPLETE;//新的一行找到了，开始匹配tile和size
                        if (title == null) {//还没匹配title就开启tile模式
                            matchStateTitle = MATCH_RUNNING;
                        }
                        if (size == null) {//还没匹配size就开启size模式
                            matchStateSize = MATCH_RUNNING;
                        }
                        matchIndex =0;
                    }

                    //匹配到开头是#，注释模式开始
                    if (matchIndex == 0 && readByte == BYTE_NUMBER) {
                        matchStateComment = MATCH_RUNNING;
                        continue;
                    }


                    //找TITLE后的字符记录到matchBuffer，转换成title

                    if (matchStateTitle == MATCH_RUNNING) {//匹配Title
                        byte[] search = MATCH_ARRAY_TITLE;
                        if (matchIndex < search.length) {//小于TITLE的长度
                            if (readByte != search[matchIndex]) {//匹配TITLE失败
                                matchStateTitle = MATCH_STOP;//停止匹配Title
                            } else if (matchIndex == search.length - 1) {//匹配TITLE标记完成，表示可以匹配TITLE后面的字符
                                titleOrSizeMatchBuffer.clear();//为开始记录Title后面的字符把索引清空
                            }
                        } else {//TITLE后面的字符
                            if (readByte == BYTE_LINE) {//换行
                                titleOrSizeMatchBuffer.flip();//写入索引改成读取索引
                                // buffer中有效的数据转化成title
                                title = new String(titleOrSizeMatchBuffer.array(), titleOrSizeMatchBuffer.position(), titleOrSizeMatchBuffer.limit()).trim();
                                title =   title.replaceAll("^\"|\"$", "");//去除前后的引号
                                matchStateTitle = MATCH_COMPLETE;//title匹配完成
                                matchStateNextLine = MATCH_RUNNING;//开始找下一行
                                titleOrSizeMatchBuffer.clear();
                                continue;
                            } else {//还没到换行就记录字符
                                if (!titleOrSizeMatchBuffer.hasRemaining())//大小不够扩容
                                    titleOrSizeMatchBuffer = BufferUtil.growCapacity(titleOrSizeMatchBuffer);
                                titleOrSizeMatchBuffer.put(readByte);//把匹配的Title后字符记录到Buffer中
                            }
                        }
                    }

                    //找LUT_3D_SIZE后的字符记录到matchBuffer，转换成size
                    if (matchStateSize == MATCH_RUNNING) {// 匹配size
                        byte[] search = MATCH_ARRAY_3D_SIZE;
                        if (matchIndex < search.length) {//小于LUT_3D_SIZE的长度
                            if (readByte != search[matchIndex]) {//匹配LUT_3D_SIZE失败
                                matchStateSize = MATCH_STOP;
                            } else if (matchIndex == search.length - 1) {
                                //匹配LUT_3D_SIZ标记完成，表示可以匹配LUT_3D_SIZ后面的字符
                                titleOrSizeMatchBuffer.clear();
                            }
                        } else {
                            if (readByte == BYTE_LINE) {//换行
                                titleOrSizeMatchBuffer.flip();//写入索引改成读取索引
                                // buffer中有效的数据转化成size
                                String result = new String(titleOrSizeMatchBuffer.array(), titleOrSizeMatchBuffer.position(), titleOrSizeMatchBuffer.limit()).trim();
                                size = Integer.parseInt(result);
                                matchStateSize = MATCH_COMPLETE;//匹配LUT_3D_SIZ完成
                                matchStateNextLine = MATCH_RUNNING;//开始找下一行
                                titleOrSizeMatchBuffer.clear();
                                continue;
                            } else {//还没到换行就记录字符
                                if (!titleOrSizeMatchBuffer.hasRemaining())//大小不够扩容
                                    titleOrSizeMatchBuffer = BufferUtil.growCapacity(titleOrSizeMatchBuffer);
                                titleOrSizeMatchBuffer.put(readByte);//把匹配的LUT_3D_SIZE后字符记录到Buffer中
                            }
                        }
                    }


                    // 匹配title和size完成，还没开始匹配rgb数据时中间有可能有一堆数据，
                    // 如果发现有一行的数据满足RGB数据的条件就标记可以开始开始匹配RGB模式，读取byte计算出Float
                    if (matchStateTitle == MATCH_COMPLETE
                            && matchStateSize == MATCH_COMPLETE
                            && matchStateRGBBuffer == MATCH_STOP) {
                        if (readByte == BYTE_LINE) {//换行
                            titleOrSizeMatchBuffer.flip();
                            //读取一行的数据
                            String result = new String(titleOrSizeMatchBuffer.array(), titleOrSizeMatchBuffer.position(), titleOrSizeMatchBuffer.limit()).trim();
                            //用空格分割字符串，因为RGB数据中间是空格
                            String[] arr = result.split("\\s+");
                            try {//没有异常表示存在rgb数据
                                float r = Float.parseFloat(arr[0]);
                                float g = Float.parseFloat(arr[1]);
                                float b = Float.parseFloat(arr[2]);
                                int numChannels = 3;//3个通道
                                int bytesPerChannel = Float.BYTES;//一个通道是float
                                int bytesPerPixel = numChannels * bytesPerChannel;//一个像素的字节大小
                                //根据size算出rgbBuffer大小
                                rgbBuffer = ByteBuffer.allocateDirect(size * size * size * bytesPerPixel);
                                rgbBuffer.order(ByteOrder.nativeOrder());
                                rgbBuffer.putFloat(r);//
                                rgbBuffer.putFloat(g);
                                rgbBuffer.putFloat(b);
                                matchStateRGBBuffer = MATCH_RUNNING;//开始RGB转换模式
                                matchIndex = -1;
                            } catch (Exception ignored) {

                            }
                            matchStateNextLine = MATCH_RUNNING;//匹配下一行
                            titleOrSizeMatchBuffer.clear();
                        } else {//还没到换行就记录字符
                            if (!titleOrSizeMatchBuffer.hasRemaining())//大小不够扩容
                                titleOrSizeMatchBuffer = BufferUtil.growCapacity(titleOrSizeMatchBuffer);
                            titleOrSizeMatchBuffer.put(readByte);//加入到buffer中
                        }
                    }
                    if (readByte == BYTE_LINE) {//换行
                        matchStateNextLine = MATCH_RUNNING;
                    }
                }
            }

            // 最后有可能数字没加入到Buffer中

            if (floatState != FLOAT_STATE_SIGN){
                float finalValue =  floatValueSignPart * floatValuePart * (float)Math.pow(10, floatExponentSignPart*floatExponentPart);
                rgbBuffer.putFloat(finalValue);
            }
            if (rgbBuffer.hasRemaining()){
                LogUtil.w("cubeLut rgbBuffer load fail, there is still data not written");
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
        rgbBuffer.rewind();
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
                rgbBuffer);
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

