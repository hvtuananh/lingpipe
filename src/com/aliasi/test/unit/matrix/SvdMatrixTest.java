package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.SvdMatrix;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


import java.util.Random;

public class SvdMatrixTest  {

    static Random RANDOM = new Random();

    static int M = 5;
    static int N = 10;

    @Test
    public void testFixed() {
        double[][] values = { { 5, 9, 2 },
                              { 3, -4, 5 },
                              { 2, 5, 1 },
                              { -8, 3, 3 } };
        int m = values.length;
        int n = values[0].length;
        int[][] columnIds = new int[m][n];
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < n; ++j)
                columnIds[i][j] =  j;

        assertConverge(m,n,columnIds,values,3,0.001);
    }

    @Test
    public void testFull() {
        int[][] columnIds = new int[M][N];
        for (int i = 0; i < M; ++i)
            for (int j = 0; j < N; ++j)
                columnIds[i][j] =  j;

        double[][] values = new double[M][N];
        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                values[i][j] = random(1.0,5.0);
            }
        }

        // assertConverge(M,N,columnIds,values,5,0.001);
    }

    static int M2 = 1000;
    static int N2 = 500;
    static int MAX_INCR2 = 100;

    @Test
    public void testPartial() {

        int[][] columnIds = new int[M2][];
        double[][] values = new double[M2][];
        for (int i = 0; i < columnIds.length; ++i) {
            int[] columnIdsForRowBuf = new int[N2];
            int pos = 0;
            int j = 0;
            while (true) {
                int incr = RANDOM.nextInt(MAX_INCR2);
                if (incr == 0 && j != 0) ++incr;
                j += incr;
                if (j >= N2) break;
                columnIdsForRowBuf[pos++] =  j;
            }
            columnIds[i] = new int[pos];
            for (int k = 0; k < pos; ++k)
                columnIds[i][k] = columnIdsForRowBuf[k];
            values[i] = new double[pos];
            for (int k = 0; k < pos; ++k)
                values[i][k] = random(1.0,5.0);
        }

        // assertConverge(M2,N2,columnIds,values,16,0.1);
    }

    void assertConverge(int numRows, int numCols,
                        int[][] columnIds,
                        double[][] values,
                        int maxOrder,
                        double tolerance) {
        double featureInit = .1;       // 0.1
        double initialLearningRate = 0.001;    // 0.001
        double annealingRate = 100000;    // POSITIVE_INFINITY
        double regularization = 0.00;    // 0.015;
        double minImprovement = 0.0000000; // 0.0001;
        int minEpochs = 1000;          // 160
        int maxEpochs = 1000000;         // 200

        SvdMatrix matrix
            = SvdMatrix.partialSvd(columnIds,
                                   values,
                                   maxOrder,
                                   featureInit,
                                   initialLearningRate,
                                   annealingRate,
                                   regularization,
                                   null, // reporter
                                   minImprovement,
                                   minEpochs,maxEpochs);

        // System.out.println("SVD RECONSTRUCTION");

        for (int i = 0; i < numRows; ++i)
            for (int j = 0; j < numCols; ++j)
                // System.out.println(i + "," + j + "=" + matrix.value(i,j))
                ;


        // System.out.println("\nSINGULAR VALUES");
        double[] singularValues = matrix.singularValues();
        // for (int i = 0; i < singularValues.length; ++i)
        // System.out.println(i + "=" + singularValues[i]);

        // System.out.println("\nLEFT SINGULAR VECTORS");
        // printMatrix(matrix.leftSingularVectors());

        // System.out.println("\nRIGHT SINGULAR VECTORS");
        // printMatrix(matrix.rightSingularVectors());

        assertTrue(singularValues[0] >= 0.0);
        for (int i = 1; i < singularValues.length; ++i) {
            assertTrue(singularValues[i] <= singularValues[i-1]);
        }

        double[][] leftSingularVectors = matrix.leftSingularVectors();
        assertOrthonormal(leftSingularVectors);

        double[][] rightSingularVectors = matrix.rightSingularVectors();
        assertOrthonormal(rightSingularVectors);

        for (int i = 0; i < columnIds.length; ++i) {
            for (int j = 0; j < columnIds[i].length; ++j) {
                int row = i;
                int column = columnIds[i][j];
                double val = values[i][j];
                double estimatedVal = matrix.value(row,column);
                assertEquals(val,estimatedVal,tolerance);
            }
        }

    }

    void assertOrthonormal(double[][] xs) {
        int numCols = xs[0].length;
        for (int j = 0; j < numCols; ++j) {
            assertUnitLengthColumn(xs,j);
            for (int k = j+1; k < numCols; ++k)
                assertOrthogonalColumns("col=" + j + " col2=" + k, xs,j,k);
        }
    }

    void assertUnitLengthColumn(double[][] xs, int j) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sum += xs[i][j] * xs[i][j];
        assertEquals("unit columns",1.0,sum,0.01);
    }

    void assertOrthogonalColumns(String msg, double[][] xs, int i, int j) {
        double sum = 0.0;
        for (int k = 0; k < xs.length; ++k)
            sum += xs[k][i] * xs[k][j];
        assertEquals("ortho columns " + msg, 0.0, sum, 0.01);
    }

    void assertUnitLength(double[] xs) {
        assertProduct(xs,xs,1.0);
    }


    void assertOrthogonal(String msg, double[] xs, double[] ys) {
        assertProduct(msg, xs,ys,0.0);
    }

    void assertProduct(double[] xs, double[] ys, double expected) {
        assertProduct("",xs,ys,expected);
    }

    void assertProduct(String msg, double[] xs, double[] ys, double expected) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sum += xs[i] * ys[i];
        assertEquals(msg,expected,sum,0.01);
    }

    double random(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    void printMatrix(double[][] xs) {
        for (int i = 0; i < xs.length; ++i) {
            for (int j = 0; j < xs[i].length; ++j) {
                if (j > 0) System.out.print(", ");
                printNumber(xs[i][j]);
            }
            System.out.println();
        }
    }

    void printNumber(double x) {
        System.out.printf("% 7.3f",x);
    }
}
