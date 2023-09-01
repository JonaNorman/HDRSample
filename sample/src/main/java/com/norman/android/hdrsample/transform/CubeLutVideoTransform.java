package com.norman.android.hdrsample.transform;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.norman.android.hdrsample.player.GLVideoTransform;
import com.norman.android.hdrsample.player.color.ColorSpace;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.FloatBuffer;

/**
 * 直接使用3D纹理加载CubeLut数据，和@see <a href="https://github.com/google/ExoPlayer/blob/release-v2/library/effect/src/main/assets/shaders/fragment_shader_lut_es2.glsl#L23"/> ExoPlayer</a> 的LUT区别
 * 1. 不需要手动手动去插值处理更方便
 * 2. 不使用Bitmap直接使用ByteBuffer加载CUBE文件
 */
public class CubeLutVideoTransform extends GLVideoTransform {

    private static final int VERTEX_LENGTH = 2;

    private static final String VERTEX_SHADER = "#version 300 es\n" +
            "in vec4 position;\n" +
            "in vec4 inputTextureCoordinate;\n" +
            "out vec2 textureCoordinate;\n" +
            "void main() {\n" +
            "    gl_Position =position;\n" +
            "    textureCoordinate =(inputTextureCoordinate).xy;\n" +
            "}";


    private static final String FRAGMENT_SHADER = "#version 300 es\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "precision highp sampler3D;\n" +
            "in  vec2 textureCoordinate;\n" +
            "out vec4 outColor;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform sampler3D cubeLutTexture;\n" +
            "uniform float cubeLutSize;\n" +
            "\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    // 解决OpenGL时线性插值在边缘处的精度问题\n" +
            "    // https://zhuanlan.zhihu.com/p/302608139\n" +
            "    vec4 rawColor = texture(inputImageTexture, textureCoordinate);\n" +
            "    vec3 scale = vec3((cubeLutSize - 1.0) / cubeLutSize);\n" +
            "    vec3 offset = vec3(1.0 / (2.0 * cubeLutSize));\n" +
            "    outColor.rgb = texture(cubeLutTexture, scale * rawColor.rgb + offset).rgb;\n" +
            "    outColor.a = rawColor.a;\n" +
            "}";


    private int lutTextureId;
    private int lutSize;

    private boolean lutEnable;

    private CubeLutBuffer currentCube;

    private FloatBuffer textureCoordinateBuffer;
    private FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;

    private int cubeLutTextureUniform;

    private int cubeLutSizeUniform;


    private CubeLutBuffer cubeLutBuffer;


    public CubeLutVideoTransform() {
        positionCoordinateBuffer = GLESUtil.createPositionFlatBuffer();
        textureCoordinateBuffer = GLESUtil.createTextureFlatBuffer();
        setVertexShader(VERTEX_SHADER);
        setFrameShader(FRAGMENT_SHADER);
    }

    @Override
    protected void onProgramChange(int programId) {
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
        textureUnitUniform = GLES20.glGetUniformLocation(programId, "inputImageTexture");
        cubeLutTextureUniform = GLES20.glGetUniformLocation(programId, "cubeLutTexture");
        cubeLutSizeUniform = GLES20.glGetUniformLocation(programId, "cubeLutSize");
    }

    @Override
    protected boolean onTransformStart() {
        int colorSpace = getInputColorSpace();
        if (colorSpace == ColorSpace.VIDEO_SDR) {
            return false;
        }
        if (cubeLutBuffer != currentCube) {
            currentCube = cubeLutBuffer;
            GLESUtil.delTextureId(lutTextureId);
            lutTextureId = 0;
            lutSize = 0;
            lutEnable = false;
            if (currentCube != null) {
                lutTextureId = currentCube.createTextureId();
                lutSize = currentCube.size;
                lutEnable = true;
            }
        }
        return lutEnable;
    }

    @Override
    protected void onTransform() {
        setOutputColorSpace(ColorSpace.VIDEO_SDR);
        clearColor();
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();

        GLES20.glEnableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glVertexAttribPointer(positionCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, positionCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);

        // 原始图像纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getInputTextureId());
        GLES20.glUniform1i(textureUnitUniform, 0);

        // CubeLut纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, lutTextureId);
        GLES20.glUniform1i(cubeLutTextureUniform, 1);
        GLES20.glUniform1f(cubeLutSizeUniform, lutSize);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, 0);

    }


    public synchronized void setCubeLut(String asset) {
        if (asset == null) {
            cubeLutBuffer = null;
            return;
        }
        cubeLutBuffer = CubeLutBuffer.loadAsset(asset);
    }
}
