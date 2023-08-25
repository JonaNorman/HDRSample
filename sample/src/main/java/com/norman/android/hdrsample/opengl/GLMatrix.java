package com.norman.android.hdrsample.opengl;

import android.opengl.Matrix;

import androidx.annotation.NonNull;

/**
 *OpenGL Matrix的封装，set开头的方法会把数据替换调，其他方法是可以连续调用的
 */
public class GLMatrix implements Cloneable {

    private static final int MATRIX_LENGTH = 16;//矩阵长度4*4 =16
    private static final int POINT_LENGTH = 4;//
    private final float[] tempMatrix = new float[MATRIX_LENGTH];
    private final float[] tempPoint = new float[POINT_LENGTH];

    private final float[] currentMatrix = new float[MATRIX_LENGTH];


    public GLMatrix(float[] matrix) {
        set(matrix);
    }


    public GLMatrix(GLMatrix matrix) {
        set(matrix.get());
    }

    public GLMatrix(android.graphics.Matrix matrix) {
        set(matrix);
    }


    public GLMatrix() {
        Matrix.setIdentityM(currentMatrix, 0);
    }



    public GLMatrix setLookAt(float eyeX, float eyeY, float eyeZ,
                              float centerX, float centerY, float centerZ, float upX, float upY,
                              float upZ) {
        Matrix.setLookAtM(currentMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        return this;
    }

    public GLMatrix setLookAt(float[] eye,
                              float[] center,
                              float[] up) {
        Matrix.setLookAtM(currentMatrix, 0, eye[0], eye[1], eye[2], center[0], center[1], center[2], up[0], up[1], up[2]);
        return this;
    }

    public GLMatrix setFrustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(currentMatrix, 0, left, right, bottom, top, near, far);
        return this;
    }

    public GLMatrix setPerspective(float fovy, float aspect, float zNear, float zFar) {
        Matrix.perspectiveM(currentMatrix, 0, fovy, aspect, zNear, zFar);
        return this;
    }

    public GLMatrix setOrtho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(currentMatrix, 0, left, right, bottom, top, near, far);
        return this;

    }

    public GLMatrix rotate(float angle, float x, float y, float z) {
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }
        Matrix.setRotateM(tempMatrix, 0, angle, x, y, z);
        mul(tempMatrix);
        return this;
    }

    public GLMatrix rotateX(float angle) {
        return rotate(angle, 1, 0, 0);
    }

    public GLMatrix rotateY(float angle) {
        return rotate(angle, 0, 1, 0);
    }

    public GLMatrix rotateZ(float angle) {
        return rotate(angle, 0, 0, 1);
    }


    public GLMatrix translate(float x, float y, float z) {
        Matrix.setIdentityM(tempMatrix, 0);
        Matrix.translateM(tempMatrix, 0, x, y, z);
        mul(tempMatrix);
        return this;
    }

    public GLMatrix preTranslate(float x, float y, float z) {
        Matrix.setIdentityM(tempMatrix, 0);
        Matrix.translateM(tempMatrix, 0, x, y, z);
        preMul(tempMatrix);
        return this;
    }


    public GLMatrix translateX(float x) {
        translate(x, 0, 0);
        return this;
    }

    public GLMatrix translateY(float y) {
        translate(0, y, 0);
        return this;
    }

    public GLMatrix translateZ(float z) {
        scale(0, 0, z);
        return this;
    }


    public GLMatrix scaleX(float scale) {
        return scale(scale, 1, 1);
    }

    public GLMatrix scaleY(float scale) {
        return scale(1, scale, 1);
    }

    public GLMatrix scaleZ(float scale) {
        return scale(1, 1, scale);
    }

    public GLMatrix scale(float x, float y, float z) {
        Matrix.setIdentityM(tempMatrix, 0);
        Matrix.scaleM(tempMatrix, 0, x, y, z);
        mul(tempMatrix);
        return this;
    }


    public GLMatrix scale(float x) {
        return scale(x, x, x);
    }

    public GLMatrix flipX() {
        scale(-1, 1, 1);
        return this;
    }


    public GLMatrix flipY() {
        scale(1, -1, 1);
        return this;
    }

    public GLMatrix flipZ() {
        scale(1, 1, -1);
        return this;
    }


    public GLMatrix reset() {
        Matrix.setIdentityM(currentMatrix, 0);
        return this;
    }

    /**
     * 预乘
     * @param matrix4
     * @return
     */

    public GLMatrix preMul(GLMatrix matrix4) {
        multiplyMM(currentMatrix, currentMatrix, matrix4.get());
        return this;
    }

    public GLMatrix preMul(float[] matrix) {
        multiplyMM(currentMatrix, this.currentMatrix, matrix);
        return this;
    }

    public GLMatrix mul(GLMatrix matrix) {
        multiplyMM(currentMatrix, matrix.get(), this.currentMatrix);
        return this;
    }


    public GLMatrix mul(float[] matrix) {
        multiplyMM(currentMatrix, matrix, this.currentMatrix);
        return this;
    }


    public float[] get() {
        return currentMatrix;
    }

    public GLMatrix get(float[] matrix) {
        copyMM(currentMatrix,matrix);
        return this;
    }


    public GLMatrix set(float[] matrix) {
        copyMM(matrix,currentMatrix);
        return this;
    }

    public GLMatrix setTranspose(float[] matrix) {
        copyMM(matrix,currentMatrix);
        transpose();
        return this;
    }

    public GLMatrix set(GLMatrix matrix4) {
        set(matrix4.get());
        return this;
    }


    public GLMatrix set(android.graphics.Matrix matrix) {
        float[] values = tempMatrix;
        matrix.getValues(values);
        currentMatrix[0] = values[0 * 3 + 0];
        currentMatrix[1] = values[1 * 3 + 0];
        currentMatrix[2] = 0;
        currentMatrix[3] = values[2 * 3 + 0];

        currentMatrix[4] = values[0 * 3 + 1];
        currentMatrix[5] = values[1 * 3 + 1];
        currentMatrix[6] = 0;
        currentMatrix[7] = values[2 * 3 + 1];

        currentMatrix[8] = 0;
        currentMatrix[9] = 0;
        currentMatrix[10] = 1;
        currentMatrix[11] = 0;

        currentMatrix[12] = values[0 * 3 + 2];
        currentMatrix[13] = values[1 * 3 + 2];
        currentMatrix[14] = 0;
        currentMatrix[15] = values[2 * 3 + 2];
        return this;
    }

    /**
     * 获取逆矩阵
     * @param matrix4
     */

    public void getInvert(GLMatrix matrix4) {
        Matrix.invertM(matrix4.get(), 0, currentMatrix, 0);
    }

    /**
     * 逆转矩阵
     */
    public void invert() {
        Matrix.invertM(tempMatrix, 0, currentMatrix, 0);
        copyMM(tempMatrix,currentMatrix);
    }



    public void transpose(){
        Matrix.transposeM(tempMatrix, 0, currentMatrix, 0);
        copyMM(tempMatrix,currentMatrix);
    }

    public void mapPoints(float[] point) {
        multiplyMV(point, currentMatrix, point);
    }

    public void mapPoints(float[] resultPoint, float[] point) {
        multiplyMV(resultPoint, currentMatrix, point);
    }

    @NonNull
    @Override
    public GLMatrix clone() {
        return new GLMatrix(currentMatrix);
    }

    // Matrix.multiplyMM原生方法result和left、right同一个引用，通过新建一个矩阵做中转解决
    void multiplyMM(float[] result, float[] left, float[] right) {
        boolean useTemp = result == left || result == right;
        if (!useTemp) {
            Matrix.multiplyMM(result, 0, left, 0, right, 0);
            return;
        }
        Matrix.multiplyMM(tempMatrix, 0, left, 0, right, 0);
        copyMM(tempMatrix,result);
    }

    // Matrix.multiplyMV原生方法resultPoint和point同一个引用，通过新建一个数组做中转解决

    void multiplyMV(float[] resultPoint, float[] matrix, float[] point) {
        boolean useTemp = resultPoint == point;
        if (!useTemp) {
            Matrix.multiplyMV(resultPoint, 0, matrix, 0, point, 0);
            return;
        }
        Matrix.multiplyMV(tempPoint, 0, matrix, 0, point, 0);
        System.arraycopy(tempPoint, 0, resultPoint, 0, POINT_LENGTH);
    }

    void copyMM(float[] src,float[] dst){
        System.arraycopy(src, 0, dst, 0, MATRIX_LENGTH);
    }

}
