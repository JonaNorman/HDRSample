package com.norman.android.hdrsample.player;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.norman.android.hdrsample.player.color.ColorRange;
import com.norman.android.hdrsample.player.color.YUV420Type;
import com.norman.android.hdrsample.player.shader.YUV420FragmentShader;
import com.norman.android.hdrsample.player.shader.YUV420VertexShader;
import com.norman.android.hdrsample.util.ColorMatrixUtil;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * YUV420四种格式转纹理
 * https://juejin.cn/post/7206577654933520444   解码10位YUV纹理
 * https://juejin.cn/post/7206577654933520444   不同YUV420格式转纹理的计算方式，下方代码是文章的优化版
 */
class GLYUV420Renderer extends GLRenderer {

    /**
     * YUV420 buffer数据对齐以后的字节宽度
     */
    private int strideWidth;
    /**
     * 对齐高度
     */
    private int sliceHeight;
    /**
     * 位深
     */
    private int bitDepth;
    /**
     * 位移大小，10位是用16位存储的，需要位移6位
     */

    private int bitMask;
    /**
     * buffer需要读取的数据大小，也就是YUV420图像的字节大小
     */

    private int bufferSize;


    /**
     * UV区域相对于Y的数据偏移，lumaBufferSize大于等于chromaSemiTexture的size，多出的size是对齐的绿边数据
     */
    private int lumaBufferSize;
    /**
     * 格式无效的清空下不需要绘制
     */

    private boolean formatValid;

    /**
     * buffer还没开始加载或者buffer格式不对
     */

    private boolean bufferAvailable;

    @YUV420Type
    private int yuv420Type;

    /**
     * buffer中的图像实际显示区域，因为宽度对齐以后有绿边需要裁剪
     */
    private Rect displayRect;


    /**
     * Y平面的纹理
     */
    private PlaneTexture lumaTexture;
    /**
     * NV21和NV12的UV平面，因为两个数据是在一起的，所以合在一个纹理里
     */

    private PlaneTexture chromaSemiTexture;

    /**
     * YV12和YV12的U平面
     */
    private PlaneTexture chromaPlanarUTexture;
    /**
     * YV12和YV12的U平面
     */
    private PlaneTexture chromaPlanarVTexture;


    private final FloatBuffer textureCoordinateBuffer;
    private final FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;

    private int lumaTextureUniform;


    private int chromaPlanarUTextureUniform;

    private int chromaPlanarVTextureUniform;

    private int chromaSemiTextureUniform;

    private int lumaSizeUniform;

    private int chromaPlanarUSizeUniform;

    private int chromaPlanarVSizeUniform;

    private int chromaSemiSizeUniform;


    private int yuvToRgbMatrixUniform;
    private int bitDepthUniform;
    private int bitMaskUniform;

    private @ColorRange int colorRange = ColorRange.LIMITED;

    private int programId;

    private YUV420FragmentShader fragmentShader;
    private YUV420VertexShader vertexShader = new YUV420VertexShader();

    public GLYUV420Renderer() {
        positionCoordinateBuffer = GLESUtil.createPositionFlatBuffer();
        textureCoordinateBuffer = GLESUtil.createTextureFlatBufferUpsideDown();
    }

    /**
     * 设置YUV Buffer的 格式
     *
     * @param requestStrideWidth buffer对齐以后的宽
     * @param requestSliceHeight buffer对齐以后的高
     * @param requestBitDepth    buffer的位数
     * @param requestDisplayRect buffer的图像实际的显示区域，因为对齐以后图像会有绿边需要裁剪
     * @param requestYuv420Type  yuv420格式
     */

    public void setBufferFormat(int requestStrideWidth,
                                int requestSliceHeight,
                                int requestBitDepth,
                                Rect requestDisplayRect,
                                @YUV420Type int requestYuv420Type) {

        if (sliceHeight != requestSliceHeight
                || strideWidth != requestStrideWidth
                || bitDepth != requestBitDepth
                || yuv420Type != requestYuv420Type
                || Objects.equals(requestDisplayRect, displayRect)) {//相同的清空下不需要重新创建纹理
            strideWidth = requestStrideWidth;
            sliceHeight = requestSliceHeight;
            bitDepth = requestBitDepth;
            yuv420Type = requestYuv420Type;
            displayRect = requestDisplayRect;
            formatValid = strideWidth > 0 && sliceHeight > 0 && bitDepth > 0 && displayRect != null;
            if (!formatValid) {//格式不对不需要加载纹理
                bufferAvailable = false;
                return;
            }
            if (lumaTexture != null) {
                GLESUtil.delTextureId(lumaTexture.textureId);
                lumaTexture = null;
            }
            if (chromaSemiTexture != null) {
                GLESUtil.delTextureId(chromaSemiTexture.textureId);
                chromaSemiTexture = null;
            }
            if (chromaPlanarUTexture != null) {
                GLESUtil.delTextureId(chromaPlanarUTexture.textureId);
                chromaPlanarUTexture = null;
            }
            if (chromaPlanarVTexture != null) {
                GLESUtil.delTextureId(chromaPlanarVTexture.textureId);
                chromaPlanarVTexture = null;
            }
            int byteCount = (int) Math.ceil(bitDepth / 8.0);//位数除以8向上取整，譬如10其实是16位存储的，每个字节8位，最终就是2字节
            bitMask = byteCount * 8 - bitDepth;//多余的位数最终要移除掉


            int videoHeight = displayRect.bottom - displayRect.top;//视频实际的高度
            int lumaPlaneWidth = strideWidth / byteCount;//strideWidth是字节宽度，除以字节大小就是Y平面的实际大小
            int lumaPlaneHeight = videoHeight;//不用sliceHeight是因为videoHeight是不包含真正的高度，可以不用对高进行绿边裁剪
            int chromaSize;

            lumaTexture = new PlaneTexture(lumaPlaneWidth, lumaPlaneHeight, byteCount);//Y平面
            if (yuv420Type == YUV420Type.NV12 || yuv420Type == YUV420Type.NV21) {
                //NV12和NV21 的UV平面高度和宽度是Y平面的一半，colorCount表示一个通道里面有两个数据也就是UV在一起
                int chromaSemiWidth = lumaPlaneWidth / 2;
                int chromaSemiHeight = lumaPlaneHeight / 2;
                chromaSemiTexture = new PlaneTexture(chromaSemiWidth, chromaSemiHeight, byteCount, 2);
                chromaSize = chromaSemiTexture.bufferSize;
            } else {
                //YV12和YV21的宽度和Y平面是一样的，高度是1/4
                int chromaWidth = lumaPlaneWidth;
                int chromaHeight = lumaPlaneHeight / 4;
                chromaPlanarUTexture = new PlaneTexture(chromaWidth, chromaWidth, byteCount);
                chromaPlanarVTexture = new PlaneTexture(chromaWidth, chromaHeight, byteCount);
                chromaSize = chromaPlanarUTexture.bufferSize + chromaPlanarVTexture.bufferSize;
            }
            lumaBufferSize = strideWidth * sliceHeight;
            bufferSize = lumaBufferSize + chromaSize;//
            float left = displayRect.left * 1.0f / lumaPlaneWidth;
            float right = displayRect.right * 1.0f / lumaPlaneWidth;
            float top = displayRect.top * 1.0f / videoHeight;//
            float bottom = displayRect.bottom * 1.0f / videoHeight;

            // 纹理坐标设置顺序，注意bottom在纹理坐标中0但是现在输入的bottom是1，这样做是因为纹理和图像是上下颠倒的，这样做就不需要手动颠倒了
            //    left bottom
            //    right bottom
            //    left top
            //    right  top

            textureCoordinateBuffer.clear();
            textureCoordinateBuffer.put(left);
            textureCoordinateBuffer.put(bottom);
            textureCoordinateBuffer.put(right);
            textureCoordinateBuffer.put(bottom);

            textureCoordinateBuffer.put(left);
            textureCoordinateBuffer.put(top);
            textureCoordinateBuffer.put(right);
            textureCoordinateBuffer.put(top);
        }
    }


    public void updateBuffer(ByteBuffer outputBuffer) {
        if (!formatValid) {
            return;
        }

        int offset = outputBuffer.position();
        int remaining = outputBuffer.remaining();
        if (remaining < bufferSize) {
            throw new IllegalArgumentException("bufferSize is less than required size");
        }
        bufferAvailable = true;

        int lumaLimit = offset + lumaTexture.bufferSize;//Y平面需要读取的数据大小
        outputBuffer.clear();
        outputBuffer.position(offset);
        outputBuffer.limit(lumaLimit);
        lumaTexture.updateBuffer(outputBuffer);

        if (yuv420Type == YUV420Type.NV12 || yuv420Type == YUV420Type.NV21) {
            int chromaSemiLimit = lumaBufferSize + chromaSemiTexture.bufferSize;//UV平面需要读取的数据大小
            outputBuffer.clear();
            outputBuffer.position(lumaBufferSize);
            outputBuffer.limit(chromaSemiLimit);
            chromaSemiTexture.updateBuffer(outputBuffer);

        } else if (yuv420Type == YUV420Type.YV21) {
            int chromaPlanarULimit = lumaBufferSize + chromaPlanarUTexture.bufferSize;//U平面数据大小
            outputBuffer.clear();
            outputBuffer.position(lumaBufferSize);
            outputBuffer.limit(chromaPlanarULimit);
            chromaPlanarUTexture.updateBuffer(outputBuffer);

            outputBuffer.clear();
            outputBuffer.position(chromaPlanarULimit);
            outputBuffer.limit(chromaPlanarULimit + chromaPlanarVTexture.bufferSize);//V平面数据大小
            chromaPlanarVTexture.updateBuffer(outputBuffer);
        } else if (yuv420Type == YUV420Type.YV12) {

            int chromaPlanarVLimit = lumaBufferSize + chromaPlanarVTexture.bufferSize;//V平面数据大小
            outputBuffer.clear();
            outputBuffer.position(lumaBufferSize);
            outputBuffer.limit(chromaPlanarVLimit);

            chromaPlanarVTexture.updateBuffer(outputBuffer);

            outputBuffer.clear();
            outputBuffer.position(chromaPlanarVLimit);
            outputBuffer.limit(chromaPlanarVLimit + chromaPlanarUTexture.bufferSize);//U平面数据大小
            chromaPlanarUTexture.updateBuffer(outputBuffer);

        }
    }

    @Override
    protected void onCreate() {


    }


    protected void changeProgram() {
        GLESUtil.delProgramId(programId);
        fragmentShader = new YUV420FragmentShader(yuv420Type);
        programId = GLESUtil.createProgramId(vertexShader.getCode(), fragmentShader.getCode());
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, YUV420VertexShader.POSITION);
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, YUV420VertexShader.INPUT_TEXTURE_COORDINATE);

        lumaTextureUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.LUMA_TEXTURE);
        chromaSemiTextureUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.CHROMA_SEMI_TEXTURE);
        chromaPlanarUTextureUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.CHROMA_PLANAR_U_TEXTURE);
        chromaPlanarVTextureUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.CHROMA_PLANAR_V_TEXTURE);

        lumaSizeUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.LUMA_SIZE);
        chromaPlanarUSizeUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.CHROMA_PLANAR_U_SIZE);
        chromaPlanarVSizeUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.CHROMA_PLANAR_V_SIZE);
        chromaSemiSizeUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.CHROMA_SEMI_SIZE);

        yuvToRgbMatrixUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.YUV_TO_RGB_MATRIX);
        bitDepthUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.BIT_DEPTH);
        bitMaskUniform = GLES20.glGetUniformLocation(programId, YUV420FragmentShader.BIT_MASK);
    }


    @Override
    void onRender() {
        if (!bufferAvailable) {
            return;
        }
        if (fragmentShader == null || fragmentShader.getYuv420Type() != yuv420Type) {
            changeProgram();
        }
        GLES20.glUseProgram(programId);
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
        GLES20.glEnableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glVertexAttribPointer(positionCoordinateAttribute,
                GLESUtil.FLAT_VERTEX_LENGTH,
                GLES20.GL_FLOAT,
                false,
                0,
                positionCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute,
                GLESUtil.FLAT_VERTEX_LENGTH,
                GLES20.GL_FLOAT,
                false,
                0,
                textureCoordinateBuffer);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lumaTexture.textureId);
        GLES20.glUniform1i(lumaTextureUniform, 0);
        GLES20.glUniform2f(lumaSizeUniform, lumaTexture.width, lumaTexture.height);
        if (yuv420Type == YUV420Type.NV12 || yuv420Type == YUV420Type.NV21) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, chromaSemiTexture.textureId);
            GLES20.glUniform1i(chromaSemiTextureUniform, 1);
            GLES20.glUniform2f(chromaSemiSizeUniform, chromaSemiTexture.width, chromaSemiTexture.height);
        } else {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, chromaPlanarUTexture.textureId);
            GLES20.glUniform1i(chromaPlanarUTextureUniform, 1);
            GLES20.glUniform2f(chromaPlanarUSizeUniform, chromaPlanarUTexture.width, chromaPlanarUTexture.height);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, chromaPlanarVTexture.textureId);
            GLES20.glUniform1i(chromaPlanarVTextureUniform, 2);
            GLES20.glUniform2f(chromaPlanarVSizeUniform, chromaPlanarVTexture.width, chromaPlanarVTexture.height);
        }
        GLES20.glUniformMatrix4fv(yuvToRgbMatrixUniform,
                1,
                false,
                ColorMatrixUtil.getYuvToRgbMatrix(bitDepth, colorRange),
                0);
        GLES20.glUniform1i(bitDepthUniform, bitDepth);
        GLES20.glUniform1i(bitMaskUniform, bitMask);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glUseProgram(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//虽然glActiveTexture有多次，但是绑定纹理回去就只要调用一次就行，不需要多次glActiveTexture
    }

    /**
     * YUV每个平面的纹理数据封装
     */
    static class PlaneTexture {
        final int textureId;

        final int width;

        final int height;

        final int byteCount;
        final int bufferSize;
        final int alignment;

        final int bufferFormat;
        final int bufferType;

        final int internalFormat;

        public PlaneTexture(int width, int height, int byteCount) {
            this(width, height, byteCount, 1);
        }

        public PlaneTexture(int width, int height, int byteCount, int colorCount) {
            this.byteCount = byteCount;
            this.textureId = GLESUtil.createNearestTextureId();
            this.width = width;
            this.height = height;
            // internalFormat表示纹理的内部格式，R或者RG表示颜色通道，UI表示无符号量化存储
            if (byteCount == 1 && colorCount == 1) {//8位 1通道
                internalFormat = GLES30.GL_R8UI;
            } else if (byteCount == 1 && colorCount == 2) {//8位 2通道
                internalFormat = GLES30.GL_RG8UI;
            } else if (byteCount == 2 && colorCount == 1) {// 16位 1通道
                internalFormat = GLES30.GL_R16UI;
            } else if (byteCount == 2 && colorCount == 2) {// 16位 2通道
                internalFormat = GLES30.GL_RG16UI;
            } else {
                throw new IllegalArgumentException("not support byteCount->" + byteCount + "colorCount" + colorCount);
            }
            bufferFormat = colorCount == 1 ? GLES30.GL_RED_INTEGER : GLES30.GL_RG_INTEGER;// 几个通道就几个颜色
            bufferType = byteCount == 1 ? GLES30.GL_UNSIGNED_BYTE : GLES30.GL_UNSIGNED_SHORT;// 1字节就用GL_UNSIGNED_BYTE无符号字节，2字节也就是GL_UNSIGNED_SHORT无符号short
            bufferSize = width * height * byteCount * colorCount;//根据字节数量和通道大小算出最终数据大小

            //根据每个通道的字节设置OpenGL对齐大小加速读取，OpenGL对齐大小只能是1、2、4、8
            int byteWidth = byteCount * width * colorCount;//通道的字节大小*宽度*通道数量
            if (byteWidth % 8 == 0) alignment = 8;
            else if (byteWidth % 4 == 0) alignment = 4;
            else if (byteWidth % 2 == 0) alignment = 2;
            else alignment = 1;

            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, alignment);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    internalFormat,//纹理内部格式
                    width,
                    height,
                    0,
                    bufferFormat,//buffer 通道格式
                    bufferType,//buffer的字节存储方式
                    null);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4);//OpenGL的字节对齐默认是4，需要还原


        }

        public void updateBuffer(ByteBuffer buffer) {
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, alignment);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexSubImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    width,
                    height,
                    bufferFormat,
                    bufferType,
                    buffer);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//和上方glTexImage2D不一样，设置大小以后就只需要glTexSubImage2D更新数据，据说能加快速度
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4);

        }
    }


}
