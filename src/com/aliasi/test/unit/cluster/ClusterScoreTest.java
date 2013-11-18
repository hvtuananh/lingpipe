package com.aliasi.test.unit.cluster;

import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.cluster.ClusterScore;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.util.HashSet;
import java.util.Set;

public class ClusterScoreTest  {

    Set elts1 = set(new Object[] { "1" });
    Set elts2 = set(new Object[] { "2" });
    Set elts12 = set(new Object[] { "1", "2" });
    Set elts12345 = set(new Object[] { "1", "2", "3", "4", "5" });
    Set elts67 = set(new Object[] { "6", "7" });
    Set elts89ABC = set(new Object[] { "8", "9", "A", "B", "C" });
    Set elts6789ABC = set(new Object[] { "6", "7", "8", "9", "A", "B", "C" });
    Set elts1234589ABC = set(new Object[] { "1", "2", "3", "4", "5", "8", "9", "A", "B", "C" });

    Set partition1 = set(new Object[] { elts1 });
    Set partition2 = set(new Object[] { elts2 });
    Set partition12 = set(new Object[] { elts12 });
    Set partition1_2 = set(new Object[] { elts1, elts2 });
    Set partition12345_67_89ABC = set(new Object[] { elts12345, elts67, elts89ABC });
    Set partition12345_6789ABC  = set(new Object[] { elts12345, elts6789ABC });
    Set partition1234589ABC_67 = set(new Object[] { elts1234589ABC, elts67 });
    Set bogusPartition = set(new Object[] { elts1, elts12 });

    static Set set(Object[] xs) {
        HashSet set = new HashSet();
        for (int i = 0; i < xs.length; ++i)
            set.add(xs[i]);
        return set;
    }


    @Test
    public void testStandard() {

	// TP=1 TN=0
	// FP=0 FN=0
	ClusterScore scorer = new ClusterScore(partition1,partition1);
	PrecisionRecallEvaluation prEval = scorer.equivalenceEvaluation();
	assertEquals(1.0,prEval.precision(),0.0001);
	assertEquals(1.0,prEval.recall(),0.0001);
	assertEquals(1.0,prEval.fMeasure(),0.0001);
	assertEquals(1.0,scorer.mucPrecision(),0.0001);
	assertEquals(1.0,scorer.mucRecall(),0.0001);
	assertEquals(1.0,scorer.mucF(),0.0001);
	assertEquals(1.0,scorer.b3ElementPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ElementRecall(),0.0001);
	assertEquals(1.0,scorer.b3ElementF(),0.0001);
	assertEquals(1.0,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ClusterRecall(),0.0001);
	assertEquals(1.0,scorer.b3ClusterF(),0.0001);

	//  TP=4 TN=0
	//  FP=0 FN=0
	scorer = new ClusterScore(partition12,partition12);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(1.0,prEval.precision(),0.0001);
	assertEquals(1.0,prEval.recall(),0.0001);
	assertEquals(1.0,prEval.fMeasure(),0.0001);
	assertEquals(1.0,scorer.mucPrecision(),0.0001);
	assertEquals(1.0,scorer.mucRecall(),0.0001);
	assertEquals(1.0,scorer.mucF(),0.0001);
	assertEquals(1.0,scorer.b3ElementPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ElementRecall(),0.0001);
	assertEquals(1.0,scorer.b3ElementF(),0.0001);
	assertEquals(1.0,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ClusterRecall(),0.0001);
	assertEquals(1.0,scorer.b3ClusterF(),0.0001);

	//  TP=2 TN=2
	//  FP=0 FN=0
	scorer = new ClusterScore(partition1_2,partition1_2);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(1.0,prEval.precision(),0.0001);
	assertEquals(1.0,prEval.recall(),0.0001);
	assertEquals(1.0,prEval.fMeasure(),0.0001);
	assertEquals(1.0,scorer.mucPrecision(),0.0001);
	assertEquals(1.0,scorer.mucRecall(),0.0001);
	assertEquals(1.0,scorer.mucF(),0.0001);
	assertEquals(1.0,scorer.b3ElementPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ElementRecall(),0.0001);
	assertEquals(1.0,scorer.b3ElementF(),0.0001);
	assertEquals(1.0,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ClusterRecall(),0.0001);
	assertEquals(1.0,scorer.b3ClusterF(),0.0001);


	//  TP=104 TN=40
	//  FP=0 FN=0
	scorer = new ClusterScore(partition1234589ABC_67,partition1234589ABC_67);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(1.0,prEval.precision(),0.0001);
	assertEquals(1.0,prEval.recall(),0.0001);
	assertEquals(1.0,prEval.fMeasure(),0.0001);
	assertEquals(1.0,scorer.mucPrecision(),0.0001);
	assertEquals(1.0,scorer.mucRecall(),0.0001);
	assertEquals(1.0,scorer.mucF(),0.0001);
	assertEquals(1.0,scorer.b3ElementPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ElementRecall(),0.0001);
	assertEquals(1.0,scorer.b3ElementF(),0.0001);
	assertEquals(1.0,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ClusterRecall(),0.0001);
	assertEquals(1.0,scorer.b3ClusterF(),0.0001);

	// TP=2 TN=0
	// FP=2 FN=0
	scorer = new ClusterScore(partition1_2,partition12);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(0.5,prEval.precision(),0.0001);
	assertEquals(1.0,prEval.recall(),0.0001);
	assertEquals(0.66666,prEval.fMeasure(),0.0001);
	assertEquals(0.0,scorer.mucPrecision(),0.0001);
	assertEquals(1.0,scorer.mucRecall(),0.0001);
	assertEquals(0.0,scorer.mucF(),0.0001);
	assertEquals(0.5,scorer.b3ElementPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ElementRecall(),0.0001);
	assertEquals(1.0/1.5,scorer.b3ElementF(),0.0001);
	assertEquals(0.5,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ClusterRecall(),0.0001);
	assertEquals(1.0/1.5,scorer.b3ClusterF(),0.0001);

	// TP=2 TN=0
	// FP=0 FN=2
	scorer = new ClusterScore(partition12,partition1_2);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(1.0,prEval.precision(),0.0001);
	assertEquals(0.5,prEval.recall(),0.0001);
	assertEquals(0.66666,prEval.fMeasure(),0.0001);
	assertEquals(1.0,scorer.mucPrecision(),0.0001);
	assertEquals(0.0,scorer.mucRecall(),0.0001);
	assertEquals(0.0,scorer.mucF(),0.0001);
	assertEquals(1.0,scorer.b3ElementPrecision(),0.0001);
	assertEquals(0.5,scorer.b3ElementRecall(),0.0001);
	assertEquals(1.0/1.5,scorer.b3ElementF(),0.0001);
	assertEquals(1.0,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(0.5,scorer.b3ClusterRecall(),0.0001);
	assertEquals(1.0/1.5,scorer.b3ClusterF(),0.0001);

	// TP=54 TN=70
	// FP=20 FN=0
	scorer = new ClusterScore(partition12345_67_89ABC,partition12345_6789ABC);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(0.729729729,prEval.precision(),0.0001);
	assertEquals(1.0,prEval.recall(),0.0001);
	assertEquals(0.9,scorer.mucPrecision(),0.0001);
	assertEquals(1.0,scorer.mucRecall(),0.0001);
	assertEquals(0.7619,scorer.b3ElementPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ElementRecall(),0.0001);
	assertEquals(0.7959,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ClusterRecall(),0.0001);
    
	// TP=54 TN=70
	// FP=0 FN=20
	scorer = new ClusterScore(partition12345_6789ABC,partition12345_67_89ABC);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(1.0,prEval.precision(),0.0001);
	assertEquals(0.729729729,prEval.recall(),0.0001);
	assertEquals(2.0 * 0.729729729 / (1.729729729),
		     prEval.fMeasure(),0.0001);
	assertEquals(20,prEval.falseNegative());
	assertEquals(1.0,scorer.mucPrecision(),0.0001);
	assertEquals(0.9,scorer.mucRecall(),0.0001);
	assertEquals(1.0,scorer.b3ElementPrecision(),0.0001);
	assertEquals(0.7619,scorer.b3ElementRecall(),0.0001);
	assertEquals(1.0,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(0.7959,scorer.b3ClusterRecall(),0.0001);

	// TP=54 TN=40
	// FP=50 FN=0
	scorer = new ClusterScore(partition12345_67_89ABC,partition1234589ABC_67);
	prEval = scorer.equivalenceEvaluation();
	assertEquals(0.519230769,prEval.precision(),0.0001);
	assertEquals(1.0,prEval.recall(),0.0001);
	assertEquals(0.9,scorer.mucPrecision(),0.0001);
	assertEquals(1.0,scorer.mucRecall(),0.0001);
	assertEquals(2 * .9 / 1.9,scorer.mucF(),0.0001);
	assertEquals(0.58333,scorer.b3ElementPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ElementRecall(),0.0001);
	assertEquals(0.75,scorer.b3ClusterPrecision(),0.0001);
	assertEquals(1.0,scorer.b3ClusterRecall(),0.0001);
    }



}
