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

import com.norman.android.hdrsample.exception.GLShaderCompileException;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;


/**
 * OpenGL的方法工具类
 */
public class GLESUtil {

    private static final String TAG = "GLESUtil";

    //顶点坐标
    private static final float[] POSITION_COORDINATES = {
            -1.0f, -1.0f,//left bottom
            1.0f, -1.0f,//right bottom
            -1.0f, 1.0f,//left top
            1.0f, 1.0f,//right top
    };

    // 纹理坐标，注意纹理坐标本身是从下往上的也就是说相对图像文件本身是颠倒的
    private static final float[] TEXTURE_COORDINATES = {
            0.0f, 0.0f,//left bottom
            1.0f, 0.0f,//right bottom
            0.0f, 1.0f,//left top
            1.0f, 1.0f,//right  top
    };

    //上下颠倒纹理坐标
    private static final float[] TEXTURE_COORDINATES_UPSIDE_DOWN = {
            0.0f, 1.0f,//left bottom
            1.0f, 1.0f,//right bottom
            0.0f, 0.0f,//left top
            1.0f, 0.0f,//right  top
    };


    /**
     * 平面顶点坐标的位数
     */
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

    /**
     * 创建VertexShader
     * @param shaderCode
     * @return
     */

    public static int createVertexShader(String shaderCode) {
        return compileShaderCode(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 创建FragmentShader
     * @param shaderCode
     * @return
     */
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
                LogUtil.w(TAG,  shaderCode);
                throw new GLShaderCompileException(error);
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
        // 把vertexShader和fragmentShader关联在program上
        GLES20.glAttachShader(programId, vertexShaderId);
        GLES20.glAttachShader(programId, fragmentShaderId);
        GLES20.glLinkProgram(programId);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            LogUtil.e(TAG,"could not link program: \n"+GLES20.glGetProgramInfoLog(programId));
        }
        //虽然删除了ShaderId但是program还是可以运行的
        deleteShaderId(vertexShaderId);
        deleteShaderId(fragmentShaderId);
        return programId;
    }

    public static void delProgramId(int programId) {
        if (programId <= 0) {// programId从1开始
            return;
        }
        GLES20.glDeleteProgram(programId);
    }

    public static void deleteShaderId(int shaderId) {
        if (shaderId <= 0) {//shaderId从1开始
            return;
        }
        GLES20.glDeleteShader(shaderId);
    }

    /**
     * 创建2D纹理，插值方式是线性
     * @return
     */
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

    /**
     * 创建2D纹理，插值方式是Nearest
     * @return
     */

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

    /**
     * 创建OES格式的2D纹理，和SurfaceTexture搭配使用
     * @return
     */
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

    /**
     * 创建3D纹理
     * @return
     */
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

    /**
     * 创建纹理
     * @param width 宽
     * @param height 高
     * @return
     */
    public static int createTextureId(int width, int height) {
        int textureId = createTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //直接传null这样的做法是错误，根据https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glTexImage2D.xml文档描述
        //传null后纹理的颜色是未知，直接绘制在frameBuffer颜色是乱七八糟的或者花屏，测试出现过前面一个activity的画面数据(大部分情况下部分出现，因为没有直接用，而是用frameBuffer重绘后再去绘制)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    /**
     * 创建2D纹理，颜色格式是RGBA，位深支持16位，可以用来创建高精度的纹理在HDR的场景下使用
     * @param width 宽
     * @param height 高
     * @param bitDepth 位深  8表示8位RGBA，10表示RGBA1010102(注意alpha位数是2位，视频不需要alpha可以直接用，如果是其他情况还是要用16)， 16表示16位RGBA
     * @return
     */

    public static int createTextureId(int width, int height,int bitDepth) {
        int textureId = createTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        if (bitDepth == 8){
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4);//rgba4个通道
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                    GLES20.GL_RGBA,//纹理内部格式
                    width, height, 0,
                    GLES20.GL_RGBA,// buffer的格式
                    GLES20.GL_UNSIGNED_BYTE,//数据每个通道都是一个无符号字节数也就是8位
                    byteBuffer);
        }else  if (bitDepth ==10){
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4);// RGBA1010102，（10+10+10+2)/8 =4
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                    GLES30.GL_RGB10_A2,//纹理内部格式
                    width, height, 0,
                    GLES20.GL_RGBA,// buffer的格式
                    GLES30.GL_UNSIGNED_INT_2_10_10_10_REV,//数据通道RGBA1010102也就是10位
                    byteBuffer);
        }else  if (bitDepth == 16){//16位数据
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 8);//每个通道2个字节，4个通道也就是2*4=8
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                    GLES30.GL_RGBA16F, width, height, 0,
                    GLES20.GL_RGBA,
                    GLES30.GL_HALF_FLOAT,//这个地方传GL_HALF_FLOAT和GL_FLOAT都可以，16F用GL_HALF_FLOAT够用了
                    byteBuffer);
        }else {
            throw new IllegalArgumentException("not support bitDepth:"+bitDepth);
        }
        GLESUtil.checkGLError();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    /**
     * 根据bitmap创建纹理
     * @param bitmap
     * @return
     */
    public static int createTextureId(Bitmap bitmap){
        int textureId = createTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }


    public static void delTextureId(int textureId) {
        if (textureId <= 0) {//textureId从1开始
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
        if (bufferId <= 0) {//创建的frameBufferId从1开始，0表示默认FrameBuffer关联着WindowSurface，在上面操作就能画在WindowSurface上
            return;
        }
        GLES20.glDeleteFramebuffers(1, new int[]{bufferId}, 0);
    }

    /**
     * 关联frameBuffer和texture，画在frameBuffer的内容就会跑到texture上
     * @param frameBufferId
     * @param textureId
     */
    public static void attachTexture(int frameBufferId,int textureId){
        if (frameBufferId<=0 || textureId<=0)return;
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
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

    /**
     * 检查OPENGL执行异常，注意glGetError特殊性，检查出来的异常是当前执行的最近一次错误，如果发现有问题对不上，要在报错前面的所有OpenGL方法都加上检查
     */

    public static void checkGLError(){
        int errorCode;
        while ((errorCode = glGetError()) != GL_NO_ERROR) {//glGetError不断获取会清空这一次错误，直到变成GL_NO_ERROR，所以有异常就直接抛出
            String errorString = GLU.gluErrorString(errorCode);
            if (errorString == null) {
                errorString = "unknown error";
            }
            throw  new GLException(errorCode, "gl " + errorString + " 0x" + Integer.toHexString(errorCode));
        }
    }

}
