package com.norman.android.hdrsample.opengl;

import java.util.ArrayList;
import java.util.List;

public class GLMatrixCalculator {

    private List<GLMatrix> matrix4List = new ArrayList<>();


    public void add(GLMatrix matrix) {
        matrix4List.add(matrix);
    }

    public void remove(GLMatrix matrix) {
        matrix4List.remove(matrix);
    }

    public int indexOf(GLMatrix matrix) {
        return matrix4List.indexOf(matrix);
    }

    public int size() {
        return matrix4List.size();
    }

    public void clear() {
        matrix4List.clear();
    }

    public void add(int index, GLMatrix matrix) {
        matrix4List.add(index, matrix);
    }

    public void set(int index, GLMatrix matrix) {
        matrix4List.set(index, matrix);
    }

    public void getMulMatrix(GLMatrix finalMatrix) {
        finalMatrix.reset();
        for (GLMatrix matrix : matrix4List) {
            finalMatrix.mul(matrix);
        }
    }
}
