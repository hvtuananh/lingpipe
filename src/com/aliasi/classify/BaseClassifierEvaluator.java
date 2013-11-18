/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */
package com.aliasi.classify;

import com.aliasi.corpus.ObjectHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A <code>BaseClassifierEvaluator</code> provides an evaluation harness
 * for first-best classifiers.  An evaluator is constructed from a classifier and
 * a complete list of the categories returned by the classifier.  
 *
 * <p>Test cases are then added using the {@link #handle(Classified)}
 * which accepts a string-based category and object to classify.  The
 * evaluator will run the classifier over the input object and collect
 * results over multiple cases. 
 *
 * <p>There are subtypes of this classifier that evaluate richer
 * classifiers.
 *
 * <P>An exhaustive set of evaluation metrics for first-best
 * classification results is accessbile as a confusion matrix through
 * the {@link #confusionMatrix()} method.  Confusion matrices provide
 * dozens of statistics on classification which can be computed from
 * first-best results; see {@link ConfusionMatrix} for more
 * information.
 *
 * <h4>Thread Safety</h4>
 *
 * <P>This class requires concurrent read and synchronous write
 * synchronization.  Reads are any of the statistics gathering methods
 * and write is just adding new test cases.
 *
 * <h4>Storing Cases</h4>
 *
 * This class always stores the classification results and true
 * category of an cases.  There is a flag in the constructor that
 * additionally allows the inputs for cases to be stored as part of an
 * evaluation.  If the flag is set to {@code true}, all input cases
 * are stored.  This enables the output of true positives, false
 * positives, false negatives, and true negatives through the methods
 * of the same names.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 * @param <E> The type of objects being classified by the evaluated classifier.
 */
public class BaseClassifierEvaluator<E> 
    implements ObjectHandler<Classified<E>> {

    private final ConfusionMatrix mConfusionMatrix;
    private int mNumCases = 0;
    final String[] mCategories;
    final Set<String> mCategorySet;
    final boolean mStoreInputs;
    BaseClassifier<E> mClassifier;
    final List<Classification> mClassifications = new ArrayList<Classification>();
    final List<E> mCases = new ArrayList<E>();
    final List<String> mReferenceCategories = new ArrayList<String>();



    /**
     * Construct a classifier evaluator for the specified classifier
     * that records results for the specified set of categories,
     * storing cases or not based on the specified flag.
     *
     * <P>If the classifier evaluator is only going to be populated
     * using the {@link #addClassification(String,Classification,Object)}
     * method, then the classifier may be null.
     *
     * @param classifier Classifier to evaluate.
     * @param categories Categories of the classifier.
     * @param storeInputs Flag indicating whether input cases should be
     * stored.
     */
    public BaseClassifierEvaluator(BaseClassifier<E> classifier,
                                   String[] categories,
                                   boolean storeInputs) {
        mClassifier = classifier;
        mStoreInputs = storeInputs;
        mCategories = categories;
        mCategorySet = new HashSet<String>();
        Collections.addAll(mCategorySet,categories);
        mConfusionMatrix = new ConfusionMatrix(categories);
    }

    /**
     * Returns the number of categories for the classifier being
     * evaluated.
     *
     * @return Number of categories being evaluated.
     */
    public int numCategories() {
        return mCategories.length;
    }

    /**
     * Returns a copy of the the categories for which this evaluator
     * stores results.
     *
     * @return The categories for which this evaluator stores
     * results.
     */
    public String[] categories() {
        return mCategories.clone();
    }

    /**
     * Returns the classifier for this evaluator.
     *
     * @return The classifier for this evaluator.
     */
    public BaseClassifier<E> classifier() {
        return mClassifier;
    }

    /**
     * Set the classfier for this evaluator to the specified value.
     * This method allows the results of evaluating several different
     * classifiers to be aggregated into a single evaluation.  The
     * primary use case is cross-validation, where a single evaluator
     * may be used to evaluate classifiers trained on different folds.
     *
     * <p>This method will throw an exception if called from an
     * evaluator with a more specific runtime type.
     *
     * @param classifier New classifier for this evaluator.
     * @throws IllegalArgumentException If called from a class with
     * a runtime type other than {@code BaseClassifierEvaluator}.
     */
    public void setClassifier(BaseClassifier<E> classifier) {
        setClassifier(classifier,BaseClassifierEvaluator.class);
    }

    /**
     * Returns the list of true positive cases along with their
     * classifications for items of the specified category.
     *
     * <p>The cases will be returned in decreasing order of
     * conditional probability if applicable, decreasing order of
     * score otherwise, and if not scored, in the order in which they
     * were processed.
     *
     * <p>A true positive case for the specified category has
     * reference category equal to the specified category and
     * first-best classification result equal to the specified
     * category.
     *
     * @param category Category whose cases are returned.
     * @return True positives for specified category.
     * @throws UnsupportedOperationException If this class does not
     * store its cases.
     */
    public List<Classified<E>> truePositives(String category) {
        return caseTypes(category,true,true);
    }

    /**
     * Returns the list of false positive cases along with their
     * classifications for items of the specified category.
     *
     * <p>A false positive case for the specified category has
     * reference category unequal to the specified category and
     * first-best classification result equal to the specified
     * category.
     *
     * @param category Category whose cases are returned.
     * @return False positives for specified category.
     * @throws UnsupportedOperationException If this class does not
     * store its cases.
     */
    public List<Classified<E>> falsePositives(String category) {
        return caseTypes(category,false,true);
    }

    /**
     * Returns the list of false negative cases along with their
     * classifications for items of the specified category.
     *
     * <p>A false negative case for the specified category has
     * reference category equal to the specified category and
     * first-best classification result unequal to the specified
     * category.
     *
     * @param category Category whose cases are returned.
     * @return False negatives for specified category.
     * @throws UnsupportedOperationException If this class does not
     * store its cases.
     */
    public List<Classified<E>> falseNegatives(String category) {
        return caseTypes(category,true,false);
    }

    /**
     * Returns the list of true negative cases along with their
     * classifications for items of the specified category.
     *
     * <p>A true negative case for the specified category has
     * reference category unequal to the specified category and
     * first-best classification result unequal to the specified
     * category.
     *
     * @param category Category whose cases are returned.
     * @return True positives for specified category.
     * @throws UnsupportedOperationException If this class does not
     * store its cases.
     */
    public List<Classified<E>> trueNegatives(String category) {
        return caseTypes(category,false,false);
    }

    /**
     * Add the specified classified object to this evaluator.
     *
     * @param classified Classified object to add to evaluation.
     */
    public void handle(Classified<E> classified) {
        // CUT AND PASTE INTO SUBCLASS
        E input = classified.getObject();
        Classification refClassification = classified.getClassification();
        String refCategory = refClassification.bestCategory();
        validateCategory(refCategory);
        Classification classification = mClassifier.classify(input);
        addClassification(refCategory,classification,input);
    }

    /**
     * Adds the specified classification as a response for the specified
     * reference category.  If this evaluator stores cases, the input
     * will be stored.
     *
     * @param referenceCategory Reference category for case.
     * @param classification Response classification for case.
     * @param input Input for the specified classification.
     */
    public void addClassification(String referenceCategory,
                                  Classification classification,
                                  E input) {
        addClassificationOld(referenceCategory,classification);
        if (mStoreInputs)
            mCases.add(input);
    }

    /**
     * Returns the number of test cases which have been provided
     * to this evaluator.
     *
     * @return The number of test cases which have been provided
     * to this evaluator.
     */
    public int numCases() {
        return mNumCases;
    }

    /**
     * Returns the confusion matrix of first-best classification
     * result statistics for this evaluator.  See {@link
     * ConfusionMatrix} for details of the numerous available
     * evaluation metrics provided by confusion matrices.
     *
     * @return The confusion matrix for the test cases evaluated so far.
     */
    public ConfusionMatrix confusionMatrix() {
        return mConfusionMatrix;
    }

    /**
     * Returns the first-best one-versus-all precision-recall
     * evaluation of the classification of the specified reference
     * category versus all other categories.  This method may be
     * called for any evaluation.
     *
     * @param refCategory Reference category.
     * @return The first-best one-versus-all precision-recall
     * evaluatuion.
     * @throws IllegalArgumentException If the specified category
     * is unknown.
     */
    public PrecisionRecallEvaluation oneVersusAll(String refCategory) {
        validateCategory(refCategory);
        PrecisionRecallEvaluation prEval = new PrecisionRecallEvaluation();
        int numCases = mReferenceCategories.size();
        for (int i = 0; i < numCases; ++i) {
            Object caseRefCategory = mReferenceCategories.get(i);
            Classification response = mClassifications.get(i);
            Object caseResponseCategory = response.bestCategory();
            boolean inRef = caseRefCategory.equals(refCategory);
            boolean inResp = caseResponseCategory.equals(refCategory);
            prEval.addCase(inRef,inResp);
        }
        return prEval;
    }


    /**
     * Return a string-based representation of the results of the
     * evaluation.
     *
     * @return String-based representation of the results.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        baseToString(sb);

        sb.append("\nONE VERSUS ALL EVALUATIONS BY CATEGORY\n");
        String[] cats = categories();
        for (int i = 0; i < cats.length; ++i) {
            sb.append("\n\nCATEGORY[" + i + "]=" + cats[i] + " VERSUS ALL\n");
            oneVsAllToString(sb,cats[i],i);
        }       
        return sb.toString();
    }

    void baseToString(StringBuilder sb) {
        sb.append("BASE CLASSIFIER EVALUATION\n");
        mConfusionMatrix.toStringGlobal(sb);
    }

    void oneVsAllToString(StringBuilder sb, String category, int i) {
        sb.append("\nFirst-Best Precision/Recall Evaluation\n");
        sb.append(oneVersusAll(category));
        sb.append('\n');
    }


    void setClassifier(BaseClassifier<E> classifier, Class<?> clazz) {
        if (!this.getClass().equals(clazz)) {
            String msg = "Require appropriate classifier type."
                + " Evaluator class=" + this.getClass()
                + " Found classifier.class=" + classifier.getClass();
            throw new IllegalArgumentException(msg);
        }
        mClassifier = classifier;
    }

    private List<Classified<E>> caseTypes(String category, boolean refMatch, boolean respMatch) {
        if (!mStoreInputs) {
            String msg = "Class must store items to return true positives."
                + " Use appropriate constructor flag to store.";
            throw new UnsupportedOperationException(msg);
        }
        List<Classified<E>> result = new ArrayList<Classified<E>>();
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            String refCat = mReferenceCategories.get(i);
            Classification c = mClassifications.get(i);
            String respCat = c.bestCategory();
            if (category.equals(refCat) != refMatch) continue;
            if (category.equals(respCat) != respMatch) continue;
            Classified<E> classified = new Classified<E>(mCases.get(i),c);
            result.add(classified);
        }
        return result;
    }

    private void addClassificationOld(String referenceCategory,
                                      Classification classification) {

        mConfusionMatrix.increment(referenceCategory,
                                   classification.bestCategory());
        mReferenceCategories.add(referenceCategory);
        mClassifications.add(classification);
        ++mNumCases;
    }

    void validateCategory(String category) {
        if (mCategorySet.contains(category))
            return;
        String msg = "Unknown category=" + category;
        throw new IllegalArgumentException(msg);
    }


}
