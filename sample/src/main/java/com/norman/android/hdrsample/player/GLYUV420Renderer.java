package com.norman.android.hdrsample.player;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.norman.android.hdrsample.util.ColorFormatUtil;
import com.norman.android.hdrsample.util.ColorMatrixUtil;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

class GLYUV420Renderer extends GLRenderer {

    private static final String VERTEX_SHADER = "#version 300 es\n" +
            "in vec4 position;\n" +
            "in vec4 inputTextureCoordinate;\n" +
            "out vec2 textureCoordinate;\n" +
            "void main() {\n" +
            "    gl_Position =position;\n" +
            "    textureCoordinate =inputTextureCoordinate.xy;\n" +
            "}";

    private static final String FRAGMENT_SHADER = "#version 300 es\n" +
            "#define YV21 1\n" +
            "#define YV12 2\n" +
            "#define NV12 3\n" +
            "#define NV21 4\n" +
            "\n" +
            "precision highp float;\n" +
            "precision highp int;\n" +
            "\n" +
            "uniform highp usampler2D lumaTexture;\n" +
            "uniform highp usampler2D chromaSemiTexture;\n" +
            "uniform highp usampler2D chromaPlanarUTexture;\n" +
            "uniform highp usampler2D chromaPlanarVTexture;\n" +
            "\n" +
            "uniform vec2 lumaSize;\n" +
            "uniform vec2 chromaPlanarUSize;\n" +
            "uniform vec2 chromaPlanarVSize;\n" +
            "uniform vec2 chromaSemiSize;\n" +
            "\n" +
            "uniform mat4 yuvToRgbMatrix;\n" +
            "uniform int bitDepth;\n" +
            "uniform int bitMask;\n" +
            "uniform int yuv420Type;\n" +
            "\n" +
            "in  vec2 textureCoordinate;\n" +
            "out vec4 outColor;\n" +
            "\n" +
            "#define MAX_COLOR_VALUE  (pow(2.0,float(bitDepth))-1.0)\n" +
            "\n" +
            "vec3 yuvToRgb(vec3 yuv){\n" +
            "    vec4 color = yuvToRgbMatrix *vec4(yuv, 1.0);\n" +
            "    return color.rgb;\n" +
            "}\n" +
            "\n" +
            "float normalizedColor(uint color){\n" +
            "    return float(color>>bitMask)/MAX_COLOR_VALUE;\n" +
            "}\n" +
            "\n" +
            "\n" +
            "vec2 normalizedColor(uvec2 color){\n" +
            "    return vec2(color>>bitMask)/MAX_COLOR_VALUE;\n" +
            "}\n" +
            "\n" +
            "ivec2 quantizedCoord(vec2 coord, vec2 size){\n" +
            "    return ivec2(coord*(size-1.0)+0.5);\n" +
            "}\n" +
            "\n" +
            "float getLumaColor(vec2 textureCoord){\n" +
            "    uint color = texelFetch(lumaTexture, quantizedCoord(textureCoord, lumaSize), 0).x;\n" +
            "    return normalizedColor(color);\n" +
            "}\n" +
            "\n" +
            "vec2 getChromaSemiColor(vec2 textureCoord){\n" +
            "    uvec2 color = texelFetch(chromaSemiTexture, quantizedCoord(textureCoord, chromaSemiSize), 0).xy;\n" +
            "    return normalizedColor(color);\n" +
            "}\n" +
            "\n" +
            "float getChromaPlanarUColor(vec2 textureCoord){\n" +
            "    uint color = texelFetch(chromaPlanarUTexture, quantizedCoord(textureCoord, chromaPlanarUSize), 0).x;\n" +
            "    return normalizedColor(color);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "float getChromaPlanarVColor(vec2 textureCoord){\n" +
            "    uint color = texelFetch(chromaPlanarVTexture, quantizedCoord(textureCoord, chromaPlanarVSize), 0).x;\n" +
            "    return normalizedColor(color);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "\n" +
            "vec3 getYV21Color(vec2 textureCoord){\n" +
            "    float y = getLumaColor(textureCoord);\n" +
            "    float u =  getChromaPlanarUColor(textureCoord);\n" +
            "    float v =  getChromaPlanarVColor(textureCoord);\n" +
            "    return vec3(y, u, v);\n" +
            "}\n" +
            "\n" +
            "vec3 getYV12Color(vec2 textureCoord){\n" +
            "    float y = getLumaColor(textureCoord);\n" +
            "    float u =  getChromaPlanarUColor(textureCoord);\n" +
            "    float v =  getChromaPlanarVColor(textureCoord);\n" +
            "    return vec3(y, u, v);\n" +
            "\n" +
            "}\n" +
            "\n" +
            "vec3 getNV12Color(vec2 textureCoord){\n" +
            "    float y = getLumaColor(textureCoord);\n" +
            "    vec2 uv =  getChromaSemiColor(textureCoord);\n" +
            "    return vec3(y, uv);\n" +
            "\n" +
            "}\n" +
            "\n" +
            "vec3 getNV21Color(vec2 textureCoord){\n" +
            "    float y = getLumaColor(textureCoord);\n" +
            "    vec2 vu =  getChromaSemiColor(textureCoord);\n" +
            "    return vec3(y, vu.yx);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "\n" +
            "void main() {\n" +
            "    vec3 yuv = vec3(0.0);\n" +
            "    if (yuv420Type == YV21){ //i420  Y+U+V\n" +
            "        yuv = getYV21Color(textureCoordinate);\n" +
            "    } else if (yuv420Type == YV12){ //YV12 Y+V+U\n" +
            "        yuv = getYV12Color(textureCoordinate);\n" +
            "    } else if (yuv420Type == NV12){ //NV12  Y+UV\n" +
            "        yuv = getNV12Color(textureCoordinate);\n" +
            "    } else if (yuv420Type == NV21){ ///NV21 Y+VU\n" +
            "        yuv = getNV21Color(textureCoordinate);\n" +
            "    }\n" +
            "    outColor.rgb =yuvToRgb(yuv);\n" +
            "    outColor.a = 1.0;\n" +
            "}";

    private int strideWidth;
    private int sliceHeight;
    private int bitDepth;

    private int bitMask;

    private int bufferSize;

    private boolean formatValid;

    private boolean bufferAvailable;

    @ColorFormatUtil.YUV420Type
    private int yuv420Type;

    private Rect displayRect;


    private PlaneTexture lumaTexture;

    private PlaneTexture chromaSemiTexture;

    private PlaneTexture chromaPlanarUTexture;

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

    private int yuv420TypeUniform;

    private @ColorMatrixUtil.ColorRange int colorRange = ColorMatrixUtil.COLOR_RANGE_LIMITED;

    private int programId;

    public GLYUV420Renderer() {
        positionCoordinateBuffer = GLESUtil.createPositionFlatBuffer();
        textureCoordinateBuffer = GLESUtil.createTextureFlatBufferUpsideDown();
    }

    public void setBufferFormat(int requestStrideWidth,
                                int requestSliceHeight,
                                int requestBitDepth,
                                Rect requestDisplayRect,
                                @ColorFormatUtil.YUV420Type int requestYuv420Type) {

        if (sliceHeight != requestSliceHeight
                || strideWidth != requestStrideWidth
                || bitDepth != requestBitDepth
                || yuv420Type != requestYuv420Type
                || Objects.equals(requestDisplayRect, displayRect)) {
            strideWidth = requestStrideWidth;
            sliceHeight = requestSliceHeight;
            bitDepth = requestBitDepth;
            yuv420Type = requestYuv420Type;
            displayRect = requestDisplayRect;
            formatValid = strideWidth > 0 && sliceHeight > 0 && bitDepth > 0;
            if (!formatValid) {
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
            int byteCount = (int) Math.ceil(bitDepth / 8.0);
            bitMask = byteCount * 8 - bitDepth;
            bufferSize = strideWidth * sliceHeight * 3 / 2;
            int planeWidth = strideWidth / byteCount;
            int planeHeight = sliceHeight;

            lumaTexture = new PlaneTexture(planeWidth, planeHeight, byteCount);
            if (yuv420Type == ColorFormatUtil.NV12 || yuv420Type == ColorFormatUtil.NV21) {
                chromaSemiTexture = new PlaneTexture(planeWidth / 2, planeHeight / 2, byteCount, 2);
            } else {
                chromaPlanarUTexture = new PlaneTexture(planeWidth, planeHeight / 4, byteCount);
                chromaPlanarVTexture = new PlaneTexture(planeWidth, planeHeight / 4, byteCount);
            }
            float left = displayRect == null ? 0 : displayRect.left * 1.0f / planeWidth;
            float right = displayRect == null ? 1 : displayRect.right * 1.0f / planeWidth;
            float top = displayRect == null ? 0 : displayRect.top * 1.0f / planeHeight;
            float bottom = displayRect == null ? 1 : displayRect.bottom * 1.0f / planeHeight;

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

        int lumaLimit = offset + lumaTexture.bufferSize;
        outputBuffer.clear();
        outputBuffer.position(offset);
        outputBuffer.limit(lumaLimit);
        lumaTexture.updateBuffer(outputBuffer);

        if (yuv420Type == ColorFormatUtil.NV12 || yuv420Type == ColorFormatUtil.NV21) {
            int chromaSemiLimit = lumaLimit + chromaSemiTexture.bufferSize;
            outputBuffer.clear();
            outputBuffer.position(lumaLimit);
            outputBuffer.limit(chromaSemiLimit);
            chromaSemiTexture.updateBuffer(outputBuffer);

        } else if (yuv420Type == ColorFormatUtil.YV21) {
            int chromaPlanarULimit = lumaLimit + chromaPlanarUTexture.bufferSize;
            outputBuffer.clear();
            outputBuffer.position(lumaLimit);
            outputBuffer.limit(chromaPlanarULimit);
            chromaPlanarUTexture.updateBuffer(outputBuffer);

            outputBuffer.clear();
            outputBuffer.position(chromaPlanarULimit);
            outputBuffer.limit(chromaPlanarULimit + chromaPlanarVTexture.bufferSize);
            chromaPlanarVTexture.updateBuffer(outputBuffer);
        } else if (yuv420Type == ColorFormatUtil.YV12) {

            int chromaPlanarVLimit = lumaLimit + chromaPlanarVTexture.bufferSize;
            outputBuffer.clear();
            outputBuffer.position(lumaLimit);
            outputBuffer.limit(chromaPlanarVLimit);

            chromaPlanarVTexture.updateBuffer(outputBuffer);

            outputBuffer.clear();
            outputBuffer.position(chromaPlanarVLimit);
            outputBuffer.limit(chromaPlanarVLimit + chromaPlanarUTexture.bufferSize);
            chromaPlanarUTexture.updateBuffer(outputBuffer);

        }
    }

    @Override
    protected void onCreate() {
        programId = GLESUtil.createProgramId(VERTEX_SHADER, FRAGMENT_SHADER);
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");

        lumaTextureUniform = GLES20.glGetUniformLocation(programId, "lumaTexture");
        chromaSemiTextureUniform = GLES20.glGetUniformLocation(programId, "chromaSemiTexture");
        chromaPlanarUTextureUniform = GLES20.glGetUniformLocation(programId, "chromaPlanarUTexture");
        chromaPlanarVTextureUniform = GLES20.glGetUniformLocation(programId, "chromaPlanarVTexture");

        lumaSizeUniform = GLES20.glGetUniformLocation(programId, "lumaSize");
        chromaPlanarUSizeUniform = GLES20.glGetUniformLocation(programId, "chromaPlanarUSize");
        chromaPlanarVSizeUniform = GLES20.glGetUniformLocation(programId, "chromaPlanarVSize");
        chromaSemiSizeUniform = GLES20.glGetUniformLocation(programId, "chromaSemiSize");

        yuvToRgbMatrixUniform = GLES20.glGetUniformLocation(programId, "yuvToRgbMatrix");
        bitDepthUniform = GLES20.glGetUniformLocation(programId, "bitDepth");
        bitMaskUniform = GLES20.glGetUniformLocation(programId, "bitMask");
        yuv420TypeUniform = GLES20.glGetUniformLocation(programId, "yuv420Type");

    }


    @Override
    void onRender() {
        if (!bufferAvailable) {
            return;
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
        if (yuv420Type == ColorFormatUtil.NV12 || yuv420Type == ColorFormatUtil.NV21) {
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
        GLES20.glUniform1i(yuv420TypeUniform, yuv420Type);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glUseProgram(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLESUtil.checkGLError();
    }

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
            if (byteCount == 1 && colorCount == 1) {
                internalFormat = GLES30.GL_R8UI;
            } else if (byteCount == 1 && colorCount == 2) {
                internalFormat = GLES30.GL_RG8UI;
            } else if (byteCount == 2 && colorCount == 1) {
                internalFormat = GLES30.GL_R16UI;
            } else if (byteCount == 2 && colorCount == 2) {
                internalFormat = GLES30.GL_RG16UI;
            } else {
                throw new IllegalArgumentException("not support byteCount->" + byteCount + "colorCount" + colorCount);
            }
            bufferFormat = colorCount == 1 ? GLES30.GL_RED_INTEGER : GLES30.GL_RG_INTEGER;
            bufferType = byteCount == 1 ? GLES30.GL_UNSIGNED_BYTE : GLES30.GL_UNSIGNED_SHORT;
            bufferSize = width * height * byteCount * colorCount;

            int byteWidth = byteCount*width*colorCount;

            if (byteWidth % 8 == 0) alignment = 8;
            else if (byteWidth % 4 == 0) alignment = 4;
            else if (byteWidth % 2 == 0) alignment = 2;
            else alignment = 1;

            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, alignment);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    internalFormat,
                    width,
                    height,
                    0,
                    bufferFormat,
                    bufferType,
                    null);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4);


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
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4);

        }
    }


}
