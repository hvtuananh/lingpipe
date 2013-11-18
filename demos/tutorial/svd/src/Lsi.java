import com.aliasi.matrix.SvdMatrix;

import java.util.Arrays;

public class Lsi {

    static final double[][] TERM_DOCUMENT_MATRIX
	= new double[][] {
	    {1, 0, 0, 1, 0, 0, 0, 0, 0 },
	    {1, 0, 1, 0, 0, 0, 0, 0, 0 },
	    {1, 1, 0, 0, 0, 0, 0, 0, 0 },
	    {0, 1, 1, 0, 1, 0, 0, 0, 0 },
	    {0, 1, 1, 2, 0, 0, 0, 0, 0 },
	    {0, 1, 0, 0, 1, 0, 0, 0, 0 },
	    {0, 1, 0, 0, 1, 0, 0, 0, 0 },
	    {0, 0, 1, 1, 0, 0, 0, 0, 0 },
	    {0, 1, 0, 0, 0, 0, 0, 0, 1 },
	    {0, 0, 0, 0, 0, 1, 1, 1, 0 },
	    {0, 0, 0, 0, 0, 0, 1, 1, 1 },
	    {0, 0, 0, 0, 0, 0, 0, 1, 1 }
    };

    static final String[] TERMS 
	= new String[] {
	"human",
	"interface",
	"computer",
	"user",
	"system",
	"response",
	"time",
	"EPS",
	"survey",
	"trees",
	"graph",
	"minors"
    };

    static final String[] DOCS
	= new String[] {
	"Human machine interface for Lab ABC computer applications",
	"A survey of user opinion of computer system response time",
	"The EPS user interface management system",
	"System and human system engineering testing of EPS",
	"Relation of user-perceived response time to error measurement",
	"The generation of random, binary, unordered trees",
	"The intersection graph of paths in trees",
	"Graph minors IV: Widths of trees and well-quasi-ordering",
	"Graph minors: A survey"
    };

    static final int NUM_FACTORS = 2;


    public static void main(String[] args) throws Exception {
        double featureInit = 0.01;
        double initialLearningRate = 0.005;
        int annealingRate = 1000;
        double regularization = 0.00;
        double minImprovement = 0.0000;
        int minEpochs = 10;
        int maxEpochs = 50000;

        System.out.println("  Computing SVD");
        System.out.println("    maxFactors=" + NUM_FACTORS);
        System.out.println("    featureInit=" + featureInit);
        System.out.println("    initialLearningRate=" + initialLearningRate);
        System.out.println("    annealingRate=" + annealingRate);
        System.out.println("    regularization" + regularization);
        System.out.println("    minImprovement=" + minImprovement);
        System.out.println("    minEpochs=" + minEpochs);
        System.out.println("    maxEpochs=" + maxEpochs);

        SvdMatrix matrix
            = SvdMatrix.svd(TERM_DOCUMENT_MATRIX,
			    NUM_FACTORS,
			    featureInit,
			    initialLearningRate,
			    annealingRate,
			    regularization,
                            null,
			    minImprovement,
			    minEpochs,
			    maxEpochs);

	double[] scales = matrix.singularValues();
	double[][] termVectors = matrix.leftSingularVectors();
	double[][] docVectors = matrix.rightSingularVectors();

	System.out.println("\nSCALES");
	for (int k = 0; k < NUM_FACTORS; ++k)
	    System.out.printf("%d  %4.2f\n",k,scales[k]);
	

	System.out.println("\nTERM VECTORS");
	for (int i = 0; i < termVectors.length; ++i) {
	    System.out.print("(");
	    for (int k = 0; k < NUM_FACTORS; ++k) {
		if (k > 0) System.out.print(", ");
		System.out.printf("% 5.2f",termVectors[i][k]);
	    }
	    System.out.print(")  ");
	    System.out.println(TERMS[i]);
	}
	
	System.out.println("\nDOC VECTORS");
	for (int j = 0; j < docVectors.length; ++j) {
	    System.out.print("(");
	    for (int k = 0; k < NUM_FACTORS; ++k) {
		if (k > 0) System.out.print(", ");
		System.out.printf("% 5.2f",docVectors[j][k]);
	    }
	    System.out.print(")  ");
	    System.out.println(DOCS[j]);
	}


	for (int m = 0; m < args.length; ++m) {
	    search(scales,termVectors,docVectors,args[m]);
	}
    }

	


    static void search(double[] scales,
		       double[][] termVectors,
		       double[][] docVectors,
		       String arg) {
	String[] terms = arg.split(" |,"); // space or comma separated
	
	double[] queryVector = new double[NUM_FACTORS];
	Arrays.fill(queryVector,0.0);

	for (String term : terms)
	    addTermVector(term,termVectors,queryVector);

	
	System.out.println("\nQuery=" + Arrays.asList(terms));
	System.out.print("Query Vector=(");
	for (int k = 0; k < queryVector.length; ++k) {
	    if (k > 0) System.out.print(", ");
	    System.out.printf("% 5.2f",queryVector[k]);
	}
	System.out.println(" )");

	System.out.println("\nDOCUMENT SCORES VS. QUERY");
	for (int j = 0; j < docVectors.length; ++j) {
	    double score = dotProduct(queryVector,docVectors[j],scales);
	    // double score = cosine(queryVector,docVectors[j],scales);
	    System.out.printf("  %d: % 5.2f  %s\n",j,score,DOCS[j]);
	}

	System.out.println("\nTERM SCORES VS. QUERY");
	for (int i = 0; i < termVectors.length; ++i) {
	    double score = dotProduct(queryVector,termVectors[i],scales);
	    // double score = cosine(queryVector,termVectors[i],scales);
	    System.out.printf("  %d: % 5.2f  %s\n",i,score,TERMS[i]);
	}	
    }


    static void addTermVector(String term, double[][] termVectors, double[] queryVector) {
	for (int i = 0; i < TERMS.length; ++i) {
	    if (TERMS[i].equals(term)) {
		for (int j = 0; j < NUM_FACTORS; ++j) {
		    queryVector[j] += termVectors[i][j];
		}
		return;
	    }
	}
    }

    static double dotProduct(double[] xs, double[] ys, double[] scales) {
	double sum = 0.0;
	for (int k = 0; k < xs.length; ++k)
	    sum += xs[k] * ys[k] * scales[k];
	return sum;
    }

    static double cosine(double[] xs, double[] ys, double[] scales) {
	double product = 0.0;
	double xsLengthSquared = 0.0;
	double ysLengthSquared = 0.0;
	for (int k = 0; k < xs.length; ++k) {
	    double sqrtScale = Math.sqrt(scales[k]);
	    double scaledXs = sqrtScale * xs[k];
	    double scaledYs = sqrtScale * ys[k];
	    xsLengthSquared += scaledXs * scaledXs;
	    ysLengthSquared += scaledYs * scaledYs;
	    product += scaledXs * scaledYs;
	}
	return product / Math.sqrt(xsLengthSquared * ysLengthSquared);
    }
    

}