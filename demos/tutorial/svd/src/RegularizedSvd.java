import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.SvdMatrix;

public class RegularizedSvd {

    static final double[][] MATRIX
        = new double[][] {
	{ 1, 2, 3, -4 },
	{ 1, 4, 9, -16 },
	{ 1, 8, 27, -64 },
	{ -1, -16, -81, 256 }
    };
    
    static final double[][] MATRIX_Z
        = new double[][] {
	{ -0.10, -0.09, -0.07, -0.17 },
	{ -0.10, -0.06,  0.01, -0.34 },
	{ -0.10, -0.00,  0.26, -1.01 },
	{ -0.13, -0.34, -1.25,  3.47 }
    };

    static double sRegularization;
    static int sMaxEpochs;

    public static void main(String[] args) {
        System.out.println("Regularized SVD.");

        sRegularization = Double.valueOf(args[0]);
        sMaxEpochs = Integer.valueOf(args[1]);

        test(MATRIX);
    }

    static void test(double[][] matrix) {
        System.out.print("\n\nTEST MATRIX");
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[i].length; ++j) {
                if (j == 0) System.out.println();
                else System.out.print(", ");
                System.out.printf("% 4.1f",matrix[i][j]);
            }
        }
        System.out.println();

        System.out.println("SVD PARAMS");
        System.out.println("Regularization=" + sRegularization);

        double featureInit = 0.01;
        double initialLearningRate = 0.0005;
        int annealingRate = 10000;
        double regularization = sRegularization;
        double minImprovement = 0.0000;
        int minEpochs = 10;
        int maxEpochs = sMaxEpochs;
        int numFactors = 4;

        System.out.println("  Computing SVD");
        System.out.println("    maxFactors=" + numFactors);
        System.out.println("    featureInit=" + featureInit);
        System.out.println("    initialLearningRate=" + initialLearningRate);
        System.out.println("    annealingRate=" + annealingRate);
        System.out.println("    regularization=" + regularization);
        System.out.println("    minImprovement=" + minImprovement);
        System.out.println("    minEpochs=" + minEpochs);
        System.out.println("    maxEpochs=" + maxEpochs);

	java.io.PrintWriter writer
	    = new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));
        Reporter reporter
            = Reporters.writer(writer).setLevel(LogLevel.DEBUG);

        SvdMatrix svdMatrix
            = SvdMatrix.svd(matrix,
                            numFactors,
                            featureInit,
                            initialLearningRate,
                            annealingRate,
                            regularization,
                            reporter,
                            minImprovement,
                            minEpochs,
                            maxEpochs);

        double[] singularValues = svdMatrix.singularValues();
        System.out.println("\n\nSingular Values");
        for (int k = 0; k < singularValues.length; ++k)
            System.out.printf(" k=%d  value=%10.2f\n",k,singularValues[k]);
        
        System.out.print("\nReconstructed Matrix");
        for (int i = 0; i < svdMatrix.numRows(); ++i) {
            for (int j = 0; j < svdMatrix.numColumns(); ++j) {
                if (j == 0) System.out.println();
                else System.out.print(", ");
                System.out.printf("% 7.2f",svdMatrix.value(i,j));
            }
        }
    }

}