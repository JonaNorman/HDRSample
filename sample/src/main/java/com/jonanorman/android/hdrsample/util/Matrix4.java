package com.jonanorman.android.hdrsample.util;

import android.opengl.Matrix;

import java.util.Arrays;
import java.util.Stack;

public class Matrix4 implements Cloneable {

    private static final int MATRIX_LENGTH = 16;
    private static final int ANDROID_MATRIX_LENGTH = 9;
    private static final int POINT_LENGTH = 4;
    private final float[] tempMulMatrix = new float[MATRIX_LENGTH];
    private final float[] tempMatrix = new float[MATRIX_LENGTH];
    private final float[] tempAndroidMatrix = new float[ANDROID_MATRIX_LENGTH];
    private final float[] tempPoint = new float[POINT_LENGTH];

    private final float[] currentMatrix = new float[MATRIX_LENGTH];
    private final Stack<float[]> matrixStack = new Stack<>();


    public Matrix4(float[] matrix) {
        set(matrix);
    }


    public Matrix4(Matrix4 matrix) {
        set(matrix.get());
    }

    public Matrix4(android.graphics.Matrix matrix) {
        set(matrix);
    }


    public Matrix4() {
        Matrix.setIdentityM(currentMatrix, 0);
    }


    public Matrix4 save() {
        matrixStack.push(Arrays.copyOf(currentMatrix, MATRIX_LENGTH));
        Matrix.setIdentityM(currentMatrix, 0);
        return this;
    }

    public Matrix4 restore() {
        if (matrixStack.empty())
            return this;
        System.arraycopy(matrixStack.pop(), 0, currentMatrix, 0, MATRIX_LENGTH);
        return this;
    }

    public boolean hasSave() {
        return !matrixStack.empty();
    }


    public Matrix4 lookAt(float eyeX, float eyeY, float eyeZ,
                          float centerX, float centerY, float centerZ, float upX, float upY,
                          float upZ) {
        Matrix.setLookAtM(tempMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        mul(tempMatrix);
        return this;
    }

    public Matrix4 frustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(tempMatrix, 0, left, right, bottom, top, near, far);
        mul(tempMatrix);
        return this;
    }

    public Matrix4 perspective(float fovy, float aspect, float zNear, float zFar) {
        Matrix.perspectiveM(tempMatrix, 0, fovy, aspect, zNear, zFar);
        mul(tempMatrix);
        return this;
    }

    public Matrix4 ortho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(tempMatrix, 0, left, right, bottom, top, near, far);
        mul(tempMatrix);
        return this;

    }

    public Matrix4 rotate(float angle, float x, float y, float z) {
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

    public Matrix4 rotateX(float angle) {
        return rotate(angle, 1, 0, 0);
    }

    public Matrix4 rotateY(float angle) {
        return rotate(angle, 0, 1, 0);
    }

    public Matrix4 rotateZ(float angle) {
        return rotate(angle, 0, 0, 1);
    }


    public Matrix4 translate(float x, float y, float z) {
        Matrix.setIdentityM(tempMatrix, 0);
        Matrix.translateM(tempMatrix, 0, x, y, z);
        mul(tempMatrix);
        return this;
    }


    public Matrix4 translateX(float x) {
        translate(x, 0, 0);
        return this;
    }

    public Matrix4 translateY(float y) {
        translate(0, y, 0);
        return this;
    }

    public Matrix4 translateZ(float z) {
        scale(0, 0, z);
        return this;
    }


    public Matrix4 scaleX(float scale) {
        return scale(scale, 1, 1);
    }

    public Matrix4 scaleY(float scale) {
        return scale(1, scale, 1);
    }

    public Matrix4 scaleZ(float scale) {
        return scale(1, 1, scale);
    }

    public Matrix4 scale(float x, float y, float z) {
        Matrix.setIdentityM(tempMatrix, 0);
        Matrix.scaleM(tempMatrix, 0, x, y, z);
        mul(tempMatrix);
        return this;
    }


    public Matrix4 scale(float x) {
        return scale(x, x, x);
    }

    public Matrix4 flipX() {
        scale(-1, 1, 1);
        return this;
    }


    public Matrix4 flipY() {
        scale(1, -1, 1);
        return this;
    }

    public Matrix4 flipZ() {
        scale(1, 1, -1);
        return this;
    }


    public Matrix4 reset() {
        Matrix.setIdentityM(currentMatrix, 0);
        return this;
    }

    public Matrix4 preMul(Matrix4 matrix4) {
        multiplyMM(currentMatrix, currentMatrix, matrix4.get());
        return this;
    }

    public Matrix4 preMul(float[] matrix) {
        multiplyMM(currentMatrix, this.currentMatrix, matrix);
        return this;
    }

    public Matrix4 mul(Matrix4 matrix) {
        multiplyMM(currentMatrix, matrix.get(), this.currentMatrix);
        return this;
    }


    public Matrix4 mul(float[] matrix) {
        multiplyMM(currentMatrix, matrix, this.currentMatrix);
        return this;
    }


    public float[] get() {
        return currentMatrix;
    }

    public Matrix4 set(float[] matrix) {
        System.arraycopy(matrix, 0, currentMatrix, 0, MATRIX_LENGTH);
        return this;
    }

    public Matrix4 set(Matrix4 matrix4) {
        System.arraycopy(matrix4.get(), 0, currentMatrix, 0, MATRIX_LENGTH);
        return this;
    }


    public Matrix4 set(android.graphics.Matrix matrix) {
        float[] values = tempAndroidMatrix;
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

    public void getInvert(Matrix4 matrix4) {
        Matrix.invertM(matrix4.get(), 0, currentMatrix, 0);
    }

    public void invert() {
        Matrix.invertM(tempMatrix, 0, currentMatrix, 0);
        System.arraycopy(tempMatrix, 0, currentMatrix, 0, MATRIX_LENGTH);
    }

    public void mapPoints(float[] point) {
        multiplyMV(point, currentMatrix, point);
    }

    public void mapPoints(float[] resultPoint, float[] point) {
        multiplyMV(resultPoint, currentMatrix, point);
    }

    @Override
    public Matrix4 clone() {
        Matrix4 matrix4 = new Matrix4(currentMatrix);
        return matrix4;
    }

    void multiplyMM(float[] result, float[] left, float[] right) {
        boolean useTemp = result == left || result == right;
        if (!useTemp) {
            Matrix.multiplyMM(result, 0, left, 0, right, 0);
            return;
        }
        Matrix.multiplyMM(tempMulMatrix, 0, left, 0, right, 0);
        System.arraycopy(tempMulMatrix, 0, result, 0, MATRIX_LENGTH);
    }

    void multiplyMV(float[] resultPoint, float[] matrix, float[] point) {
        boolean useTemp = resultPoint == point;
        if (!useTemp) {
            Matrix.multiplyMV(resultPoint, 0, matrix, 0, point, 0);
            return;
        }
        Matrix.multiplyMV(tempPoint, 0, matrix, 0, point, 0);
        System.arraycopy(tempPoint, 0, resultPoint, 0, POINT_LENGTH);
    }

}