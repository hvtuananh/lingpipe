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

import com.aliasi.stats.MultivariateDistribution;

import com.aliasi.lm.LanguageModel;

import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An <code>LMClassifier</code> performs joint probability-based
 * classification of character sequences into non-overlapping
 * categories based on language models for each category and a
 * multivariate distribution over categories.  Thus the subclass of
 * <code>Classification</code> returned by the classify method is
 * {@link JointClassification}.  In addition to joint and conditional
 * probabilities of categories given the input, the score of the
 * returned joint classification is the character plus category
 * sample entropy rate.
 *
 * <P>A language-model classifier is constructed from a fixed, finite
 * set of categories which are assumed to have disjoint
 * (non-overlapping) sets of members.  The categories are represented
 * as simple strings.  Each category is assigned a language model.
 * Furthermore, a multivariate distribution over the set of categories
 * assigns marginal category probabilities.
 *
 * <P>Joint log probabilities are determined in the usual way:
 *
 * <blockquote><code>
 *    log<sub><sub>2</sub></sub> P(cs,cat)
 *    = log<sub><sub>2</sub></sub> P(cs|cat)
 *      + log<sub><sub>2</sub></sub> P(cat)
 * </code></blockquote>
 *
 * where <code>P(cs|cat)</code> is the probability of the character
 * sequence <code>cs</code> in the language model for category
 * <code>cat</code> and where <code>P(cat)</code> is the probability
 * assigned by the multivariate distribution over categories.  Scores
 * are defined to be adjusted sample cross-entropy rates:
 *
 * <blockquote><code>
 * score(cs,cat)
 * <br>&nbsp; = (log<sub><sub>2</sub></sub> P(cs,cat)) / (cs.length() + 2)
 * <br>&nbsp; = (log<sub><sub>2</sub></sub> P(cs|cat)
 *    + log<sub><sub>2</sub></sub> P(cat)) / (cs.length() + 2)
 * </code></blockquote>
 *
 * Note that the contribution of the category probability to the score
 * approaches zero as the sample size grows and the data overwhelms
 * the pre-data expectation.  Also note that each category has its
 * estimate divided by the same amount, so the probabilistic ordering
 * is preserved.  If the language models are process models, the
 * cross-entropy rate is just
 * <code>(log<sub><sub>2</sub></sub>P(cs|cat))/cs.length()</code>; for
 * process models, add one to the denominator to account for
 * figuratively generating the end-of-character-sequence symbol.
 *
 * <P>Note that maximizing joint probabilities is the same as
 * maximizing conditional probabilities because the character sequence
 * <code>cs</code> is constant:
 *
 * <blockquote><code>
 * ARGMAX<sub><sub>cat</sub></sub> P(cat|cs)
 * <br> =  ARGMAX<sub><sub>cat</sub></sub> P(cs,cat) / P(cs)
 * <br> =  ARGMAX<sub><sub>cat</sub></sub> P(cs,cat)
 * </code></blockquote>
 *
 * A computation of conditional estimates <code>P(cat|cs)</code> given
 * the joint estimates is defined in {@link JointClassification}.
 *
 * <P>To ensure consistent estimates, all of the language models
 * should either be process language models or sequence language
 * models over the same set of characters, depending on whether
 * probability normalization is over fixed length sequences or over
 * all strings.  On the other hand, the models themselves may be a
 * mixture of n-gram lengths and smoothing parameters, or even in the
 * case of sequence models, tokenized models and sequence character
 * models.
 *
 * <P>Boolean classifiers for membership can be constructed with this
 * class by means of a positive language model and a negative model.
 * A character sequence is considered an instance of the category if
 * they are more likely in the positive model than the negative model.
 * There are several strategies for constructing anti-models.  The
 * most common methodology is to build an anti-model from an unbiased
 * sample of negative cases, but this requires supervision for
 * negative cases and tends to bias toward the model with more
 * training data given the way language model cross-entropy tends to
 * go down with more training data in general.  Another approach is to
 * build a weaker model from the same training data as the positive
 * model, for instance by using lower order n-grams for the negative
 * model.  The simplest approach is to use a uniform negative model,
 * which amounts to a cross-entropy rejection threshold; this is the
 * basis of {@link BinaryLMClassifier}.
 *
 * <P>Language model classifiers may be trained using {@link
 * DynamicLMClassifier} using a trainable multivariate estimator and
 * dynamic language models.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.0
 * @param <L> the type of language model used to generate text from categories
 * @param <M> the multivariate distribution over categories
 */
public class LMClassifier<L extends LanguageModel, M extends MultivariateDistribution>
    implements JointClassifier<CharSequence> {

    final L[] mLanguageModels;
    final M mCategoryDistribution;
    final HashMap<String,L> mCategoryToModel;
    final String[] mCategories;

    /**
     * Construct a joint classifier for character sequences
     * classifying over a specified set of categories, with a
     * multivariate distribution over those categories and a language
     * model per category.  The language models are supplied in a
     * parallel array to the categories.
     *
     * <P>The category distribution is the marginal over categories,
     * and each language model provides a conditional estimate given
     * its category.  Categorization is as described in the class
     * documentation.
     *
     * @param categories Array of categories for classification.
     * @param languageModels A parallel array of language models for
     * the categories.
     * @param categoryDistribution The marginal distribution over the
     * categories for classification.
     * @throws IllegalArgumentException If there are not at least two
     * categories, or if the category and language model arrays are not
     * the same length, or if there are duplicate categories.
     */
    public LMClassifier(String[] categories,
                        L[] languageModels,
                        M categoryDistribution) {
        Set<String> categorySet = new HashSet<String>();
        for (String cat : categories) {
            if (!categorySet.add(cat)) {
                String msg = "Duplicate category=" + cat;
                throw new IllegalArgumentException(msg);
            }
        }
        if (categories.length < 2) {
            String msg = "Require at least two categories."
                + " Found categories.length=" + categories.length;
            throw new IllegalArgumentException(msg);
        }
        if (categories.length != categoryDistribution.numDimensions()) {
            String msg = "Require same number of categories as dimensions."
                + " Found categories.length="
                + categories.length
                + " Found categoryDistribution.numDimensions()="
                + categoryDistribution.numDimensions();
            throw new IllegalArgumentException(msg);
        }

        mCategories = categories;
        if (categories.length != languageModels.length) {
            String msg = "Categories and language models must be same length."
                + " Found categories length=" + categories.length
                + " Found language models length=" + languageModels.length;
            throw new IllegalArgumentException(msg);
        }
        mLanguageModels = languageModels;
        mCategoryDistribution = categoryDistribution;
        mCategoryToModel = new HashMap<String,L>();
        for (int i = 0; i < categories.length; ++i)
            mCategoryToModel.put(categories[i],languageModels[i]);
    }

    /**
     * Returns the array of categories for this classifier.
     *
     * <P>This method copies the array and thus changes to
     * it do not affect the categories for this classifier.
     *
     * @return The array of categories for this classifier.
     */
    public String[] categories() {
        return mCategories.clone();
    }

    /**
     * Returns the language model for the specified category.  The
     * model for a specified category is used to provide estimates of
     * <code>P(cSeq|category)</code>, the conditional probability of a
     * character sequence given the specified category as described in
     * the class documentation above.
     *
     * <P>Changes to the returned model affect this classifier's
     * behavior.
     *
     * @param category The specified category.
     * @return The language model for the specified category.
     * @throws IllegalArgumentException If the category is not known.
     */
    public L languageModel(String category) {
        for (int i = 0; i < mCategories.length; ++i)
            if (category.equals(mCategories[i]))
                return mLanguageModels[i];
        String msg = "Category not known.  Category=" + category;
        throw new IllegalArgumentException(msg);
    }

    /**
     * Returns a multivariate distribution over categories for this
     * classifier.  This is method returns <code>P(category)</code>,
     * the marginal distribution over categories used during
     * classification as described in the class documentation.
     *
     * <P>Changes to the returned distribution affect this classifier's
     * behavior.
     *
     * @return The distribution over categories.
     */
    public M categoryDistribution() {
        return mCategoryDistribution;
    }

    /**
     * Returns the joint classification of the specified character sequence.
     *
     * @param cSeq Character sequence being classified.
     * @return Joint classification of the specified character
     * sequence.
     * @throws IllegalArgumentException If the specified object is not
     * a character sequence.
     */
    public JointClassification classify(CharSequence cSeq) {
        if (!(cSeq instanceof CharSequence)) {
            String msg = "LM Classification requires CharSequence input."
                + " Found class=" + (cSeq == null ? null : cSeq.getClass());
            throw new IllegalArgumentException(msg);
        }
        return classifyJoint(Strings.toCharArray(cSeq),0,cSeq.length());
    }

    /**
     * A convenience method returning a joint classification over a
     * character array slice.  Note that
     * <code>estimateJoint(cs,start,end)</code> returns the same result
     * as <code>esimtateJoint(new String(cs,start,end-start))</code>.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end One plus the index of the last character in the slice.
     * @throws IllegalArgumentException If the start index is less than zero
     * or greater than the end index or if the end index is not within bounds.
     */
    public JointClassification classifyJoint(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);

        // need to deal with array of generic typed objects
        @SuppressWarnings({"unchecked","rawtypes"})
        ScoredObject<String>[] estimates
            = new ScoredObject[categories().length];

        for (int i = 0; i < categories().length; ++i) {
            String category = categories()[i];
            LanguageModel model = mLanguageModels[i];
            double charsGivenCatLogProb
                = model.log2Estimate(new String(cs,start,end-start));
            double catLogProb
                = mCategoryDistribution.log2Probability(category);
            double charsCatJointLogProb = charsGivenCatLogProb + catLogProb;
            estimates[i]
                = new ScoredObject<String>(category,
                                           charsCatJointLogProb);
        }
        return toJointClassification(estimates,
                                     end-start+2); // divide by length + 1
    }

    static JointClassification toJointClassification(ScoredObject<String>[] estimates,
                                                     double length) {
        Arrays.sort(estimates,ScoredObject.reverseComparator());
        String[] categories = new String[estimates.length];
        double[] jointEstimates = new double[estimates.length];
        double[] scores = new double[estimates.length];
        for (int i = 0; i < estimates.length; ++i) {
            categories[i] = estimates[i].getObject();
            jointEstimates[i] = estimates[i].score();
            scores[i] = jointEstimates[i] / length;
        }
        return new JointClassification(categories,scores,jointEstimates);
    }

}

