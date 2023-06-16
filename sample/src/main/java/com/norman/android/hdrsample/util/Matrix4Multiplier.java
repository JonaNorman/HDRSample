package com.norman.android.hdrsample.util;

import java.util.ArrayList;
import java.util.List;

public class Matrix4Multiplier {

    private List<Matrix4> matrix4List = new ArrayList<>();


    public void add(Matrix4 matrix) {
        matrix4List.add(matrix);
    }

    public void remove(Matrix4 matrix) {
        matrix4List.remove(matrix);
    }

    public int indexOf(Matrix4 matrix) {
        return matrix4List.indexOf(matrix);
    }

    public int size() {
        return matrix4List.size();
    }

    public void clear() {
        matrix4List.clear();
    }

    public void add(int index, Matrix4 matrix) {
        matrix4List.add(index, matrix);
    }

    public void set(int index, Matrix4 matrix) {
        matrix4List.set(index, matrix);
    }

    public void getFinal(Matrix4 finalMatrix) {
        finalMatrix.reset();
        for (Matrix4 matrix : matrix4List) {
            finalMatrix.mul(matrix);
        }
    }
}
