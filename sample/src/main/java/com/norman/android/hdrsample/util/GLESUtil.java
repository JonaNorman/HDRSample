package com.norman.android.hdrsample.util;

import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.glGetError;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLException;
import android.opengl.GLU;
import android.opengl.GLUtils;

import java.nio.FloatBuffer;


public class GLESUtil {

    private static final String TAG = "GLESUtil";

    private static final float[] POSITION_COORDINATES = {
            -1.0f, -1.0f,//left bottom
            1.0f, -1.0f,//right bottom
            -1.0f, 1.0f,//left top
            1.0f, 1.0f,//right top
    };

    private static final float[] TEXTURE_COORDINATES = {
            0.0f, 0.0f,//left bottom
            1.0f, 0.0f,//right bottom
            0.0f, 1.0f,//left top
            1.0f, 1.0f,//right  top
    };

    private static final float[] TEXTURE_COORDINATES_UPSIDE_DOWN = {
            0.0f, 1.0f,//left bottom
            1.0f, 1.0f,//right bottom
            0.0f, 0.0f,//left top
            1.0f, 0.0f,//right  top
    };


    public static final int FLAT_VERTEX_LENGTH = 2;


    /**
     * 创建平面的顶点坐标
     * @return
     */
    public static FloatBuffer createPositionFlatBuffer(){
         return BufferUtil.createDirectFloatBuffer(POSITION_COORDINATES);
    }
    /**
     * 创建纹理坐标
     * @return
     */
    public static FloatBuffer createTextureFlatBuffer(){
        return BufferUtil.createDirectFloatBuffer(TEXTURE_COORDINATES);
    }

    /**
     * 创建上下颠倒的纹理矩阵，因为纹理坐标是从下方开始的，颠倒以后就是正的
     * @return
     */
    public static FloatBuffer createTextureFlatBufferUpsideDown(){
        return BufferUtil.createDirectFloatBuffer(TEXTURE_COORDINATES_UPSIDE_DOWN);
    }

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
                LogUtil.e(TAG, "can not compile shader: " + error+"\n"+shaderCode);
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
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            LogUtil.e(TAG,"could not link program: "+GLES20.glGetProgramInfoLog(programId));
        }
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

    public static int createNearestTextureId() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return texture[0];
    }

    public static int createExternalTextureId() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return texture[0];
    }

    public static int create3DTextureId() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, texture[0]);
        GLES20.glTexParameterf(GLES30.GL_TEXTURE_3D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES30.GL_TEXTURE_3D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES30.GL_TEXTURE_3D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES30.GL_TEXTURE_3D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES30.GL_TEXTURE_3D,
                GLES30.GL_TEXTURE_WRAP_R, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, 0);
        return texture[0];
    }

    public static int createTextureId(int width, int height) {
        int textureId = createTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    public static int createTextureId(int width, int height,int bitDepth) {
        int textureId = createTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        if (bitDepth <= 8){
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        }else  if (bitDepth <= 16){
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_RGBA16F, width, height, 0, GLES20.GL_RGBA, GLES20.GL_FLOAT, null);
        }else {
            throw new IllegalArgumentException("not support bitDepth:"+bitDepth);
        }
        GLESUtil.checkGLError();
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


    public static void delTextureId(int textureId) {
        if (textureId <= 0) {
            return;
        }
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
    }


    public static int createFrameBufferId() {
        int[] buffer = new int[1];
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

    public static void checkGLError(){
        int errorCode;
        while ((errorCode = glGetError()) != GL_NO_ERROR) {
            String errorString = GLU.gluErrorString(errorCode);
            if (errorString == null) {
                errorString = "unknown error";
            }
            throw  new GLException(errorCode, "gl " + errorString + " 0x" + Integer.toHexString(errorCode));
        }
    }

}
