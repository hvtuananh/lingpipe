import com.aliasi.matrix.Vector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.LogisticRegression;
import com.aliasi.stats.RegressionPrior;

public class RegularizationDemo {

    public static void main(String[] args) {
	for (double variance = 0.001; variance <= 1000; variance *= 2.0) {
	    System.out.println("\n\nVARIANCE=" + variance);
	    evaluate(RegressionPrior.laplace(variance,true));
	    evaluate(RegressionPrior.gaussian(variance,true));
	    evaluate(RegressionPrior.cauchy(variance,true));
	}
    }

    static void evaluate(RegressionPrior prior) {
	System.out.println("\nPrior=" + prior);
	LogisticRegression regression
	    = LogisticRegression.estimate(WalletProblem.INPUTS,
					  WalletProblem.OUTPUTS,
					  prior,
                                          AnnealingSchedule.inverse(.05,100),
                                          null,
					  0.0000001,
					  10,
					  5000);
	Vector[] betas = regression.weightVectors();
	for (int i = 0; i < betas.length; ++i) {
	    System.out.print(i + ") ");
	    for (int k = 0; k < betas[i].numDimensions(); ++k)
		System.out.printf("%5.2f, ",betas[i].value(k));
	    System.out.println();
	}
    }

}