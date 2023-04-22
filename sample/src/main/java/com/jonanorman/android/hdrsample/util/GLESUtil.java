package com.jonanorman.android.hdrsample.util;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;


public class GLESUtil {

    private static final int FLOAT_SIZE = 4;
    private static final int SHORT_SIZE = 2;
    public static int createVertexShader(String shaderCode) {
        return compileShaderCode(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    public static int createFragmentShader(String shaderCode) {
        return compileShaderCode(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShaderCode(int type, String shaderCode) {
        int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId != 0) {
            GLES20.glShaderSource(shaderObjectId, shaderCode);
            GLES20.glCompileShader(shaderObjectId);
            int[] status = new int[1];
            GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, status, 0);
            if (status[0] == 0) {
                String error = GLES20.glGetShaderInfoLog(shaderObjectId);
                Log.e("ShaderLog", "compile shader fail " + error);
                GLES20.glDeleteShader(shaderObjectId);
                return -1;
            }
        }
        return shaderObjectId;
    }

    public static int createProgramId() {
        return GLES20.glCreateProgram();
    }

    public static int createProgramId(String vertCode, String fragCode) {
        int programId = GLESUtil.createProgramId();
        int vertexShaderId = GLESUtil.createVertexShader(vertCode);
        int fragmentShaderId = GLESUtil.createFragmentShader(fragCode);
        GLES20.glAttachShader(programId, vertexShaderId);
        GLES20.glAttachShader(programId, fragmentShaderId);
        GLES20.glLinkProgram(programId);
        deleteShaderId(vertexShaderId);
        deleteShaderId(fragmentShaderId);
        return programId;
    }

    public static void delProgramId(int programId) {
        if (programId <= 0) {
            return;
        }
        GLES20.glDeleteProgram(programId);
    }

    public static void deleteShaderId(int shaderId) {
        if (shaderId <= 0) {
            return;
        }
        GLES20.glDeleteShader(shaderId);
    }

    public static int createTextureId() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return texture[0];
    }

    public static int createTextureId(int width, int height) {
        int textureId = createTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    public static int createTextureId(Bitmap bitmap){
        int textureId = createTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    public static int createExternalTextureId() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return texture[0];
    }



    public static void delTextureId(int textureId) {
        if (textureId <= 0) {
            return;
        }
        int[] textures = new int[]{textureId};
        GLES20.glDeleteTextures(textures.length, textures, 0);
    }

    public static void bindTexture(int textureUnit, int textureId) {
        GLES30.glActiveTexture(textureUnit);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
    }

    public static void unbindTexture() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    public static void uploadTexture(int pixelWidth,
                                     int width,
                                     int height,
                                     int internalFormat,
                                     int bufferFormat,
                                     Buffer buffer) {
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ROW_LENGTH, pixelWidth);
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                internalFormat,
                width,
                height,
                0,
                bufferFormat,
                GLES30.GL_UNSIGNED_BYTE,
                buffer);
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ROW_LENGTH, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }



    public static int createBufferId() {
        int[] buffer = {0};
        GLES20.glGenBuffers(1, buffer, 0);
        return buffer[0];
    }

    public static void deleteBufferId(int bufferId) {
        if (bufferId <= 0) {
            return;
        }
        GLES20.glDeleteShader(bufferId);
    }


    public static void updateArrayBuffer(int bufferId, float[] buffer) {
        FloatBuffer floatBuffer = FloatBuffer.wrap(buffer);
        updateArrayBuffer(bufferId, floatBuffer);
    }

    public static void updateArrayBuffer(int bufferId, FloatBuffer buffer) {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buffer.remaining() * FLOAT_SIZE, buffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    public static void updateElementArrayBuffer(int bufferId, ShortBuffer buffer) {
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, bufferId);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffer.remaining() * SHORT_SIZE, buffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }


    public static int createFrameBufferId() {
        int[] buffer = {0};
        GLES20.glGenFramebuffers(1, buffer, 0);
        return buffer[0];
    }

    public static void deleteFrameBufferId(int bufferId) {
        if (bufferId <= 0) {
            return;
        }
        GLES20.glDeleteFramebuffers(1, new int[]{bufferId}, 0);
    }

    public static int createRenderBufferId() {
        int[] buffer = {0};
        GLES20.glGenRenderbuffers(1, buffer, 0);
        return buffer[0];
    }

    public static int createRenderBufferId(int width, int height) {
        int bufferId = createRenderBufferId();
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, bufferId);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        return bufferId;
    }


    public static void deleteRenderBufferId(int bufferId) {
        if (bufferId <= 0) {
            return;
        }
        GLES20.glDeleteRenderbuffers(1, new int[]{bufferId}, 0);
    }


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
