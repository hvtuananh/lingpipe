package com.aliasi.matrix;

abstract class MatrixBackedVector extends AbstractVector {

    protected final Matrix mMatrix;
    protected final int mIndex;

    MatrixBackedVector(Matrix m, int index) {
        mMatrix = m;
        mIndex = index;
    }

    static class Row extends MatrixBackedVector implements Vector {
        Row(Matrix m, int index) {
            super(m,index);
        }
        @Override
        public int numDimensions() {
            return mMatrix.numColumns();
        }
        @Override
        public void setValue(int column, double value) {
            mMatrix.setValue(mIndex,column,value);
        }
        @Override
        public double value(int column) {
            return mMatrix.value(mIndex,column);
        }
        @Override
        public Vector add(Vector v) {
            return Matrices.add(this,v);
        }
    }

    static class Column extends MatrixBackedVector implements Vector {
        Column(Matrix m, int index) {
            super(m,index);
        }
        @Override
        public int numDimensions() {
            return mMatrix.numRows();
        }
        @Override
        public void setValue(int row, double value) {
            mMatrix.setValue(row,mIndex,value);
        }
        @Override
        public double value(int row) {
            return mMatrix.value(row,mIndex);
        }
        @Override
        public Vector add(Vector v) {
            return Matrices.add(this,v);
        }
    }

}
