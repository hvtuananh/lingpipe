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

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

// import com.aliasi.util.Math;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Counter;
import com.aliasi.util.Exceptions;
import com.aliasi.util.Factory;
import com.aliasi.util.Iterators;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;


import com.aliasi.stats.Statistics;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A {@code TradNaiveBayesClassifier} implements a traditional
 * token-based approach to naive Bayes text classification.  It wraps
 * a tokenization factory to convert character sequences into
 * sequences of tokens.  This implementation supports several
 * enhancements to simple naive Bayes: priors, length normalization,
 * and semi-supervised training with EM.
 *
 * <p>It is the token counts (aka &quot;bag of words&quot;) sequence
 * that is actually being classified, not the raw character sequence
 * input.  So any character sequences that produce the same bags of
 * tokens are considered equal.
 *
 * <p>Naive Bayes is trainable online, meaning that it can be given
 * training instances one at a time, and at any point can be used as a
 * classifier.  Training cases consist of a character sequence and
 * classification, as dictated by the interface {@code
 * ObjectHandler<Classified<CharSequence>>}.
 *
 * <p>Given a character sequence, a naive Bayes classifier returns
 * joint probability estimates of categories and tokens; this is
 * reflected in its implementing the {@code
 * Classifier<CharSequence,JointClassification>} interface.  Note that
 * this is the joint probability of the token counts, so sums of
 * probabilities over all input character sequences will exceed 1.0.
 * Typically, only the conditional probability estimates are used in
 * practice.
 *
 * <p>If there is length normalization, the joint probabilities will
 * not sum to 1.0 over all inputs and outputs.  The conditional
 * probabilities will always sum to 1.0.
 *
 *
 * <h3>Classification</h3>
 *
 * A token-based naive Bayes classifier computes joint token count and
 * category probabilities by factoring the joint into the marginal
 * probability of a category times the conditinoal probability of the
 * tokens given the category.
 *
 * <blockquote><pre>
 * p(tokens,cat) = p(tokens|cat) * p(cat)</pre></blockquote>
 *
 * Conditional probabilities are derived by applying Bayes's rule to
 * invert the probability calculation:
 *
 * <blockquote><pre>
 * p(cat|tokens) = p(tokens,cat) / p(tokens)
 *              = p(tokens|cat) * p(cat) / p(tokens)</pre></blockquote>
 *
 * The tokens are assumed to be independent (this is the
 * &quot;naive&quot; step):
 *
 * <blockquote><pre>
 * p(tokens|cat) = p(tokens[0]|cat) * ... * p(tokens[tokens.length-1]|cat)
 *              = <big><big>&Pi;</big></big><sub>i &lt; tokens.length</sub> p(tokens[i]|cat)</pre></blockquote>
 *
 * Finally, an explicit marginalization allows us to compute the
 * marginal distribution of tokens:
 *
 * <blockquote><pre>
 * p(tokens) = <big><big>&Sigma;</big></big><sub>cat'</sub> p(tokens,cat')
 *          = <big><big>&Sigma;</big></big><sub>cat'</sub> p(tokens|cat') * p(cat')</pre></blockquote>
 *
 *
 * <h3>Estimation with Priors</h3>
 *
 * We now have defined the conditional probability {@code
 * p(cat|tokens)} in terms of two distributions, the conditional
 * probability of a token given a category {@code p(token|cat)}, and the
 * marginal probability of a category {@code p(cat)} (sometimes called
 * the category's prior probability, though this shouldn't be confused
 * with the usual Bayesian prior on model parameters).
 *
 * <p>Traditional naive Bayes uses a maximum a posterior (MAP)
 * estimate of the multinomial distributions: {@code p(cat)} over the
 * set of categories, and for each category {@code cat}, the
 * multinomial distribution {@code p(token|cat)} over the set of tokens.
 * Traditional naive Bayes employs the Dirichlet conjugate prior for
 * multinomials, which is straightforward to compute by adding a fixed
 * "prior count" to each count in the training data.  This lends the
 * traditional name "additive smoothing".
 *
 * <p>Two sets of counts are sufficient for estimating a traditional
 * naive Bayes classifier.  The first is {@code tokenCount(w,c)}, the
 * number of times token {@code w} appeared as a token in a training
 * case for category {@code c}.  The second is {@code caseCount(c)},
 * which is the number of training cases for category {@code c}.
 *
 * <p>We assume prior counts <code>&alpha;</code> for the case counts
 * and <code>&beta;</code> for the token counts.  These values are supplied
 * in the constructor for this class.
 *
 * The estimates for category and token probabilities <code>p'</code>
 * are most easily understood as proportions:
 *
 * <blockquote><pre>
 * p'(w|c) &prop; tokenCount(w,c) + &beta;
 *
 *   p'(c) &prop; caseCount(c) + &alpha;</pre></blockquote>
 *
 * The probability estimates <code>p'</code> are obtained through the
 * usual normalization:
 *
 * <blockquote><pre>
 * p'(w|c) = ( tokenCount(w,c) + &beta; ) / <big><big>&Sigma;</big></big><sub>w</sub> ( tokenCount(w,c) + &beta; )
 *
 *   p'(c) = ( caseCount(c) + &alpha; ) / <big><big>&Sigma;</big></big><sub>c</sub> ( caseCount(c) + &alpha; )</pre></blockquote>
 *
 *
 * <h3>Maximum Likelihood Estimates</h3>
 *
 * <p>Although not traditionally used for naive Bayes, maximum
 * likelihood estimates arise from setting the prior counts equal to
 * zero (<code>&alpha; = &beta; = 0</code>).  The prior counts drop
 * out of the equations to yield the maximum likelihood estimates
 * <code>p<sup>*</sup></code>:
 *
 * <blockquote><pre>
 * p<sup>*</sup>(w|c) = tokenCount(w,c) / <big><big>&Sigma;</big></big><sub>w</sub> tokenCount(w,c)
 *
 *   p<sup>*</sup>(c) = caseCount(c) / <big><big>&Sigma;</big></big><sub>c</sub> caseCount(c)</pre></blockquote>
 *
 * <h3>Weighted and Conditional Training</h3>
 *
 * <p>Unlike traditional naive Bayes implementations, this class
 * allows weighted training, including training directly from a
 * conditional classification.  When training using a conditional
 * classification, each category is weighted according to its
 * conditional probability.
 *
 * <p>Weights may be negative, allowing
 * counts to be decremented (e.g. for Gibbs sampling).
 *
 *
 * <h3>Length Normalization</h3>
 *
 * <p>Because the (almost always faulty) independence of tokens
 * assumptions underlying the naive Bayes classifier, the conditional
 * probability estimates tend toward either 0.0 or 1.0 as the input
 * grows longer.  In practice, it sometimes help to length normalize
 * the documents.  That is, consider each document to be a given
 * number of tokens long, <code>lengthNorm</code>.
 *
 * <p>Length normalization can be computed directly on the linear
 * scale:
 *
 * <blockquote><pre>
 * p<sup>n</sup>(tokens|cat) = p(tokens|cat)<sup><sup>(lengthNorm/tokens.length)</sup></sup>
 * </pre></blockquote>
 *
 * but is more easily understood on the log scale, where we multiply
 * the length norm by the log probability normalized per token:
 *
 * <blockquote><pre>
 * log<sub>2</sub> p<sup>n</sup>(tokens|cat) = lengthNorm * log<sub>2</sub> p(tokens|c) / tokens.length
 * </pre></blockquote>
 *
 * The length normalization parameter is supplied in the
 * constructor, with a {@code Double.NaN} value indicating
 * that no length normalization should be done.
 *
 * <p>Length normalization will be applied during training, too.
 * Length normalization may be changed using the set method.
 * For instance, this allows training to skip length normalization
 * and classification to use length normalization.
 *
 *
 * <h3>Semi-Supervised Training with Expectation Maximization (EM)</h3>
 *
 * Naive bayes is a common model to use in conjunction with the
 * general semi-supervised or unsupervised training strategy known as
 * expectation maximization (EM).  The basic idea behind EM is
 * is that it starts with a classifier, then applies it to
 * unseen data, looks at the weighted output predictions, then
 * uses the output predictions as training data.
 *
 * <p>EM is controlled by epoch.  Each epoch consists of an
 * expectation (E) step, followed by a maximization (M) step.
 * The expectation step computes expectations which are then
 * fed in as training weights to the maximization step.
 *
 * <p>The version of EM implemented in this class allows a mixture of
 * supervised and unsupervised data.
 *
 * <p>The supervised training data is
 * in the form of a corpus of classifications, implementing
 * Corpus<ClassificationHandler<CharSequence,Classification>>}.
 *
 * <p>Unsupervised data is in the form of a corpus of texts, implementing
 * {@code Corpus<TextHandler>}.
 *
 * <p>The method also requires a factory with which to produce a new
 * classifier in each epoch, namely an implementation of {@code
 * Factory<TradNaiveBayesClassifier>}.  And it also takes an initial
 * classifier, which may be different than the classifiers generated
 * by the factory.
 *
 * <p>EM works by iteratively training better and better classifiers
 * using the previous classifier to label unlabeled data to use
 * for training.
 *
 * <blockquote><pre>
 * set lastClassifier to initialClassifier
 * for (epoch = 0; epoch < maxEpochs; ++epoch) {
 *      create classifier using factory
 *      train classifier on supervised items
 *      for (x in unsupervised items) {
 *          compute p(c|x) with lastClassifier
 *          for (c in category)
 *              train classifier on c weighted by p(c|x)
 *      }
 *      evaluate corpus and model probability under classifier
 *      set lastClassifier to classifier
 *      break if converged
 * }
 * return lastClassifier</pre></blockquote>
 *
 * <p>Note that in each round, the new classifier is trained on
 * the supervised items.
 *
 * <p>In general, we have found that EM training works best if the
 * initial classifier does more smoothing than the classifiers
 * returned by the factory.
 *
 * <p>Annealing, of a sort, may be built in by having the factory
 * return a sequence of classifiers with ever longer length
 * normalizations and/or lower prior counts, both of which attenuate
 * the posterior predictions of a naive Bayes classifier.  With a
 * short length normalization, classifications are driven closer to
 * uniform; with longer length normalizations they are more peaky.
 *
 *
 * <h3>Unsupervised Learning and EM Soft Clustering</h3>
 *
 * <p>It is possible to train a classifier in a completely
 * unsupervised fashion by having the initial classifier assign
 * categories at random.  Only the number of categories must be fixed.
 * The algorithm is exactly the same, and the result after
 * convergence or the maximum number of epochs is a classifier.
 *
 * <p>Now take the trained classifier and run it over the texts in the
 * unsupervised text corpus.  This will assign probabilities of the
 * text belonging to each of the categories.  This is known as a soft
 * clustering, and the algorithm overall is known as EM clustering.
 * If we assign each item to its most likely category, the result
 * is then a hard clustering.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * <p>A naive Bayes classifier may be serialized.  The object read
 * back in will behave just as the naive Bayes classifier that was
 * serialized.  The tokenizer factory must be serializable in order
 * to serialize the classifier.  
 *
 * <p>A naive Bayes classifier may be compiled.  In order to be
 * compiled, the tokenizer factory must be either serializable or
 * compilable.  The object read back in will implement {@code
 * ConditionalClassifier<CharSequence>} if the compiled classifier is
 * binary (i.e., has exactly two categories) and {@code
 * JointClassifier<CharSequence>} if the compiled classifier has more
 * than two categories.  The ability to compute joint probabilities in
 * the binary case is lost due to an optimization in the compiler, so
 * the resulting class only implements conditional classifier.
 *
 * <p>A compiled classifier may not be trained.  
 *
 * <h3>Comparison to {@code NaiveBayesClassifier}</h3>
 *
 * The naive Bayes classifier implemented in {@link
 * NaiveBayesClassifier} differs from this version in smoothing the
 * token estimates with character language model estimates.
 *
 *
 * <h3>Thread Safety</h3>
 *
 * A {@code TradNaiveBayesClassifier} must be synchronized externally
 * using read/write synchronization (e.g. with {@link
 * java.util.concurrent.locks.ReadWriteLock}.  The write methods
 * include {@link #handle(Classified)}, {@link
 * #train(CharSequence,Classification,double)}, {@link
 * #trainConditional(CharSequence,ConditionalClassification,double,double)},
 * and {@link #setLengthNorm(double)}.  All other methods are read
 * methods.
 *
 * <p>A compiled classifier is completely thread safe.
 *
 * @author  Bob Carpenter
 * @version 4.1.0
 * @since   Lingpipe3.8
 */
public class TradNaiveBayesClassifier
    implements JointClassifier<CharSequence>,
               ObjectHandler<Classified<CharSequence>>,
               Serializable,
               Compilable {

    static final long serialVersionUID = -300327951207213311L;

    private final Set<String> mCategorySet;
    private final String[] mCategories;
    private final TokenizerFactory mTokenizerFactory;
    private final double mCategoryPrior;
    private final double mTokenInCategoryPrior;

    private Map<String,double[]> mTokenToCountsMap; // wordCount(w,c)
    private double[] mTotalCountsPerCategory;        // SUM_w wordCount(w,c); indexed by c

    private double[] mCaseCounts;   // caseCount(c)
    private double mTotalCaseCount; // SUM_c caseCount(c)

    private double mLengthNorm;


    /**
     * Return a string representation of this classifier.
     *
     * @return String representation of this classifier.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("categories=" + Arrays.asList(mCategories) + "\n");
        sb.append("category Prior=" + mCategoryPrior + "\n");
        sb.append("token in category prior=" + mTokenInCategoryPrior + "\n");
        sb.append("total case count=" + mTotalCaseCount + "\n");
        for (int i = 0; i < mCategories.length; ++i) {
            sb.append("category count(" + mCategories[i] + ")=" + mCaseCounts[i] + "\n");
        }
        for (String token : mTokenToCountsMap.keySet()) {
            sb.append("token=" + token + "\n");
            double[] counts = mTokenToCountsMap.get(token);
            for (int i = 0; i < mCategories.length; ++i) {
                sb.append("  tokenCount(" + mCategories[i] + "," + token + ")=" + counts[i] + "\n");
            }
        }
        return sb.toString();
    }


    private TradNaiveBayesClassifier(String[] categories,
                                     TokenizerFactory tokenizerFactory,
                                     double categoryPrior,
                                     double tokenInCategoryPrior,
                                     Map<String,double[]> tokenToCountsMap,
                                     double[] totalCountsPerCategory,
                                     double[] caseCounts,
                                     double totalCaseCount,
                                     double lengthNorm) {
        mCategories = categories;
        mCategorySet = new HashSet<String>(Arrays.asList(categories));
        mTokenizerFactory = tokenizerFactory;
        mCategoryPrior = categoryPrior;
        mTokenInCategoryPrior = tokenInCategoryPrior;
        mTokenToCountsMap = tokenToCountsMap;
        mTotalCountsPerCategory = totalCountsPerCategory;
        mCaseCounts = caseCounts;
        mTotalCaseCount = totalCaseCount;
        mLengthNorm = lengthNorm;
    }

    /**
     * Constructs a naive Bayes classifier over the specified
     * categories, using the specified tokenizer factory.  The
     * category and token-in-category priors will be set to reasonable
     * default value of 0.5, and there is no length normlization (length
     * normalization set to {@code Double.NaN}).
     *
     * <p>See the class documentation above for more information.
     *
     * @param categorySet Categories for classification.
     * @param tokenizerFactory Factory to convert char sequences to
     * tokens.
     * @throws IllegalArgumentException If there are fewer than two
     * categories.
     */
    public TradNaiveBayesClassifier(Set<String> categorySet,
                                    TokenizerFactory tokenizerFactory) {
        this(categorySet,tokenizerFactory,0.5,0.5,Double.NaN);
    }

    /**
     * Constructs a naive Bayes classifier over the specified
     * categories, using the specified tokenizer factory, priors and
     * length normalization.  See the class documentation for an
     * explanation of the parameter's affect on classification.
     *
     * @param categorySet Categories for classification.
     * @param tokenizerFactory Factory to convert char sequences to
     * tokens.
     * @param categoryPrior Prior count for categories.
     * @param tokenInCategoryPrior Prior count for tokens per category.
     * @param lengthNorm A positive, finite length norm, or {@code
     * Double.NaN} if no length normalization is to be done.
     * @throws IllegalArgumentException If either prior is negative or
     * not finite, if there are fewer than two categories, or if the
     * length normalization constant is negative, zero, or infinite.
     */
    public TradNaiveBayesClassifier(Set<String> categorySet,
                                    TokenizerFactory tokenizerFactory,
                                    double categoryPrior,
                                    double tokenInCategoryPrior,
                                    double lengthNorm) {

        if (categorySet.size() < 2) {
            String msg = "Require at least two categorySet."
                + " Found categorySet.size()=" + categorySet.size();
            throw new IllegalArgumentException(msg);
        }
        Exceptions.finiteNonNegative("categoryPrior",categoryPrior);
        Exceptions.finiteNonNegative("tokenInCategoryPrior",


                                                     tokenInCategoryPrior);

        setLengthNorm(lengthNorm);

        mTotalCaseCount = 0L;

        mCategorySet = new HashSet<String>(categorySet);

        mCategories = mCategorySet.<String>toArray(Strings.EMPTY_STRING_ARRAY);
        Arrays.sort(mCategories);

        mTokenizerFactory = tokenizerFactory;
        mCategoryPrior = categoryPrior;
        mTokenInCategoryPrior = tokenInCategoryPrior;

        mTokenToCountsMap = new HashMap<String,double[]>();
        mTotalCountsPerCategory = new double[mCategories.length];
        mCaseCounts = new double[mCategories.length];
    }

    /**
     * Returns a set of categories for this classifier.
     *
     * @return The set of categories for this classifier.
     */
    public Set<String> categorySet() {
        return Collections.unmodifiableSet(mCategorySet);
    }

    /**
     * Set the length normalization factor to the specified value.
     * See the class documentation for
     *
     * @param lengthNorm Length normalization or {@code Double.NaN} to turn
     * off normalization.
     * @throws IllegalArgumentException If the length norm is
     * infinite, zero, or negative.
     */
    public void setLengthNorm(double lengthNorm) {
        if (lengthNorm <= 0.0 || Double.isInfinite(lengthNorm)) {
            String msg = "Length norm must be finite and positive, or Double.NaN."
                + " Found lengthNorm=" + lengthNorm;
            throw new IllegalArgumentException(msg);
        }
        mLengthNorm = lengthNorm;
    }

    /**
     * Return the classification of the specified character sequence.
     *
     * @param in Character sequence being classified.
     * @return The classifcation of the char sequence.
     */
    public JointClassification classify(CharSequence in) {
        double[] logps = new double[mCategories.length];
        char[] cs = Strings.toCharArray(in);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        int tokenCount = 0;
        for (String token : tokenizer) {
            double[] tokenCounts = mTokenToCountsMap.get(token);
            ++tokenCount;
            if (tokenCounts == null)
                continue;
            for (int i = 0; i < mCategories.length; ++i)
                logps[i] += com.aliasi.util.Math.log2(probTokenByIndexArray(i,tokenCounts));
        }
        if ((!Double.isNaN(mLengthNorm)) && (tokenCount > 0)) {
            for (int i = 0; i < logps.length; ++i)
                logps[i] *= mLengthNorm/tokenCount;
        }
        for (int i = 0; i < logps.length; ++i)
            logps[i] += com.aliasi.util.Math.log2(probCatByIndex(i));

        return JointClassification.create(mCategories,logps);
    }

    /**
     * Returns the length normalization factor for this
     * classifier.  See the class documentation above for
     * details.
     *
     * @return The length normalization for this classifier.
     */
    public double lengthNorm() {
        return mLengthNorm;
    }

    /**
     * Returns {@code true} if the token has been seen in
     * training data.
     *
     * @param token Token to test.
     * @return {@code true} if the token has been seen in
     * training data.
     */
    public boolean isKnownToken(String token) {
        return mTokenToCountsMap.containsKey(token);
    }

    /**
     * Returns an unmodifiable view of the set of tokens.
     * The set is not modifiable, but will change to reflect
     * any tokens added during training.
     *
     * @return The set of known tokens.
     */
    public Set<String> knownTokenSet() {
        return Collections.unmodifiableSet(mTokenToCountsMap.keySet());
    }

    /**
     * Returns the probability of the specified token
     * in the specified category.  See the class documentation
     * above for definitions.
     *
     * @throws IllegalArgumentException If the category is not known
     * or the token is not known.
     */
    public double probToken(String token, String cat) {
        int catIndex = getIndex(cat);
        double[] tokenCounts = mTokenToCountsMap.get(token);
        if (tokenCounts == null) {
            String msg = "Requires known token."
                + " Found token=" + token;
            throw new IllegalArgumentException(msg);
        }
        return probTokenByIndexArray(catIndex,tokenCounts);
    }

    /**
     * Compile this classifier to the specified object output.
     *
     * @param out Object output to which this classifier is compiled.
     * @throws IOException If there is an underlying I/O error
     * during the write.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Compiler(this));
    }



    /**
     * Returns the probability estimate for the specified
     * category.
     *
     * @param cat Category whose probability is returned.
     * @return Probability for category.
     * @throws IllegalArgumentException If the category is not known.
     */
    public double probCat(String cat) {
        int catIndex = getIndex(cat);
        return probCatByIndex(catIndex);
    }


    /**
     * Trains the classifier with the specified classified character
     * sequence.  Only the first-best result is used from the
     * classification; to train on conditional outputs, see {@link
     * #trainConditional(CharSequence,ConditionalClassification,double,double)}.
     *
     * @param classifiedObject Classified character sequence.
     */
    public void handle(Classified<CharSequence> classifiedObject) {
        handle(classifiedObject.getObject(), classifiedObject.getClassification());
    }


    /**
     * Trains the classifier with the specified case consisting of a
     * character sequence and first-best classification.  Only the
     * first-best result is used from the classification; to train on
     * conditional outputs, see {@link
     * #trainConditional(CharSequence,ConditionalClassification,double,double)}.
     *
     * @param cSeq Character sequence being classified.
     * @param classification Classification of character sequence.
     */
    void handle(CharSequence cSeq, Classification classification) {
        train(cSeq,classification,1.0);
    }

    /**
     * Trains this classifier using tokens extracted from the
     * specified character sequence, using category count multipliers
     * derived by multiplying the specified count multiplier by the
     * conditional probablity of a category in the specified
     * classification.  A category is not trained for the sequence
     * if its conditional probability times the count multiplier
     * is less than the minimum count.

     *
     * @param cSeq Character sequence being trained.
     * @param classification Conditional classification to train.
     * @param countMultiplier Count multiplier of training instance.
     * @param minCount Minimum count for which a category is trained for this character
     * sequence.
     * @throws IllegalArgumentException If the countMultiplier is not finite and
     * non-negative, or if the min count is below zero or not a number.
     */
    public void trainConditional(CharSequence cSeq,
                                 ConditionalClassification classification,
                                 double countMultiplier,
                                 double minCount) {
        if (countMultiplier < 0.0
            || Double.isNaN(countMultiplier)
            || Double.isInfinite(countMultiplier)) {
            String msg = "Count multipliers must be finite and non-negative."
                + " Found countMultiplier=" + countMultiplier;
            throw new IllegalArgumentException(msg);
        }
        if (minCount < 0.0 || Double.isNaN(minCount) || Double.isInfinite(minCount)) {
            String msg = "Minimum count must be finite non-negative."
                + " Found minCount=" + minCount;
            throw new IllegalArgumentException(msg);
        }
        int numCats = 0;
        while (numCats < classification.size()
               && classification.conditionalProbability(numCats) * countMultiplier >= minCount)
            ++numCats;

        ObjectToCounterMap<String> tokenCountMap = tokenCountMap(cSeq);
        double lengthMultiplier = lengthMultiplier(tokenCountMap);

        // cache results per cat
        double[] lengthNormCatMultipliers = new double[numCats];
        int[] catIndexes = new int[numCats];
        for (int j = 0; j < numCats; ++j) {
            catIndexes[j] = getIndex(classification.category(j));
            double count = countMultiplier * classification.conditionalProbability(j);
            mTotalCaseCount += count;
            mCaseCounts[catIndexes[j]] += count;
            lengthNormCatMultipliers[j] = lengthMultiplier * count;
        }
        for (Map.Entry<String,Counter> entry : tokenCountMap.entrySet()) {
            String token = entry.getKey();
            double tokenCount = entry.getValue().doubleValue();
            double[] tokenCounts = mTokenToCountsMap.get(token);
            if (tokenCounts == null) {
                tokenCounts = new double[mCategories.length];
                mTokenToCountsMap.put(token,tokenCounts);
            }
            for (int j = 0;  j < numCats; ++j) {
                double addend = tokenCount * lengthNormCatMultipliers[j];
                tokenCounts[catIndexes[j]] += addend;
                mTotalCountsPerCategory[catIndexes[j]] += addend;
            }
        }
    }

    /**
     * Trains the classifier with the specified case consisting of
     * a character sequence and conditional classification with the
     * specified count.
     *
     * <p>If the count value is negative, counts are subtracted rather
     * than added.  If any of the counts fall below zero, an illegal
     * argument exception will be thrown and the classifier will be
     * reverted to the counts in place before the method was called.
     * Cleanup after errors requires the tokenizer factory to return
     * the same tokenizer given the same string, but no check is made
     * that it does.
     *
     * @param cSeq Character sequence on which to train.
     * @param classification Classification to train with character
     * sequence.
     * @param count How many instances the classification will count
     * as for training purposes.
     * @throws IllegalArgumentException If the count is negative and
     * increments cause accumulated counts to fall below zero.
     */
    public void train(CharSequence cSeq, Classification classification, double count) {
        if (count == 0.0) return;
        String cat = classification.bestCategory();
        int catIndex = getIndex(cat);

        // throw if underflow
        if (mCaseCounts[catIndex] < -count) {
            String msg = "Decrement caused negative token count."
                    + "Revert to previous state."
                    + " cSeq=" + cSeq
                + " classification=" + cat
                    + " count=" + count;
            throw new IllegalArgumentException(msg);
        }

        mCaseCounts[catIndex] += count;
        mTotalCaseCount += count;

        ObjectToCounterMap<String> tokenCountMap = tokenCountMap(cSeq);
        double lengthMultiplier = lengthMultiplier(tokenCountMap);

        double lengthNormCount = lengthMultiplier * count;

        char[] cs = Strings.toCharArray(cSeq);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        int pos = 0;
        for (String token : tokenizer) {
            double[] tokenCounts = mTokenToCountsMap.get(token);

            // cleanup underflow and throw
            if (lengthNormCount < 0 && ((tokenCounts == null)
                              || (tokenCounts[catIndex] < -lengthNormCount))) {

                // first two are unnormed
                mCaseCounts[catIndex] -= count;
                mTotalCaseCount -= count;

                Tokenizer tokenizer2 = mTokenizerFactory.tokenizer(cs,0,cs.length);
                int fixPos = 0;
                for (String token2 : tokenizer2) {
                    if (fixPos >= pos) break;
                    ++fixPos;
                    double[] tokenCounts2 = mTokenToCountsMap.get(token2);
                    tokenCounts2[catIndex] -= lengthNormCount;
                    mTotalCountsPerCategory[catIndex] -= lengthNormCount;
                }
                String msg = "Decrement caused negative token count."
                    + "Revert to previous state."
                    + " cSeq=" + cSeq
                    + " classification=" + cat
                    + " count=" + count;
                throw new IllegalArgumentException(msg);
            }

            ++pos;
            if (tokenCounts == null) {
                tokenCounts = new double[mCategories.length];
                mTokenToCountsMap.put(token,tokenCounts);
            }
            tokenCounts[catIndex] += lengthNormCount;;
            mTotalCountsPerCategory[catIndex] += lengthNormCount;
        }
    }

    /**
     * Returns the log (base 2) marginal probability of the specified
     * input.  This value is calculated by:
     *
     * <blockquote><code>
     * p(x) = <big><big>&Sigma;</big></big><sub><sub>c in cats</sub></sub> p(c,x)
     * </code></blockquote>
     *
     * Note that this value is normalized by the number of tokens
     * in the input, so that
     *
     * <blockquote><code>
     * <big><big>&Sigma;</big></big><sub><sub>length(x) = n</sub></sub> p(x) = 1.0
     * </code></blockquote>
     *
     * @param input Input character sequence.
     * @return The log probability of the input under this joint
     * model.
     */
    public double log2CaseProb(CharSequence input) {
        JointClassification c = classify(input);
        double maxJointLog2P = Double.NEGATIVE_INFINITY;
        for (int rank = 0; rank < c.size(); ++rank) {
            double jointLog2P = c.jointLog2Probability(rank);
            if (jointLog2P > maxJointLog2P)
                maxJointLog2P = jointLog2P;
        }
        double sum = 0.0;
        for (int rank = 0; rank < c.size(); ++rank)
            sum += Math.pow(2.0,c.jointLog2Probability(rank) - maxJointLog2P);
        return maxJointLog2P + com.aliasi.util.Math.log2(sum);
    }

    /**
     * Returns the log (base 2) of the probability density of this
     * model in the Dirichlet prior specified by this classifier.
     * Note that the result is a log density is not technically a
     * probability, and may return values that are positive.
     *
     * <p>The result is the sum of the log density of the multinomial
     * over categories and the log density of the per-category
     * multinomials over tokens.
     *
     * <p>For a definition of the probability function for each
     * category's multinomial and the overall category multinomial,
     * see {@link Statistics#dirichletLog2Prob(double,double[])}.
     *
     * @return The log model density value.
     */
    public double log2ModelProb() {
        double[] catProbs = new double[mCategories.length];
        for (int i = 0; i < mCategories.length; ++i) {
            catProbs[i] = probCatByIndex(i);
        }
        double sum = Statistics.dirichletLog2Prob(mCategoryPrior,catProbs);

        double[] wordProbs = new double[mTokenToCountsMap.size()];
        for (int catIndex = 0; catIndex < mCategories.length; ++catIndex) {
            int j = 0;
            for (double[] counts : mTokenToCountsMap.values()) {
                double totalCountForCat = mTotalCountsPerCategory[catIndex];
                wordProbs[j++] = (counts[catIndex] + mTokenInCategoryPrior)/(totalCountForCat + mCaseCounts.length * mTokenInCategoryPrior);
            }
            sum += Statistics.dirichletLog2Prob(mTokenInCategoryPrior,wordProbs);
        }
        return sum;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new TradNaiveBayesClassifier.Serializer(this);
    }

    private double probTokenByIndexArray(int catIndex, double[] tokenCounts) {
        double tokenCatCount = tokenCounts[catIndex];
        double totalCatCount = mTotalCountsPerCategory[catIndex];
        return (tokenCatCount + mTokenInCategoryPrior)
            / (totalCatCount + mTokenToCountsMap.size() * mTokenInCategoryPrior);
    }

    private double probCatByIndex(int catIndex) {
        double caseCountCat = mCaseCounts[catIndex];
        return (caseCountCat + mCategoryPrior)
            / (mTotalCaseCount + mCategories.length * mCategoryPrior);
    }

    private ObjectToCounterMap<String> tokenCountMap(CharSequence cSeq) {
        ObjectToCounterMap<String> tokenCountMap = new ObjectToCounterMap<String>();
        char[] cs = Strings.toCharArray(cSeq);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        for (String token : tokenizer)
            tokenCountMap.increment(token);
        return tokenCountMap;
    }



    private double lengthMultiplier(ObjectToCounterMap<String> tokenCountMap) {
        if (Double.isNaN(mLengthNorm)) return 1.0;
        int length = 0;
        for (Counter counter : tokenCountMap.values())
            length += counter.intValue();
        return length != 0.0
            ? mLengthNorm / length
            : 1.0;
    }

    private int getIndex(String cat) {
        int catIndex = java.util.Arrays.binarySearch(mCategories,cat);
        if (catIndex < 0) {
            String msg = "Unknown category.  Require category in category set."
                + " Found category=" + cat
                + " category set=" + mCategorySet;
            throw new IllegalArgumentException(msg);
        }
        return catIndex;
    }

    /**
     * Apply the expectation maximization (EM) algorithm to train a traditional
     * naive Bayes classifier using the specified labeled and unabled data,
     * initial classifier and factory for creating subsequent factories.
     *
     * <p>This method lets the client take control over assessing convergence,
     * so there are no convergence-related arguments.
     *
     * @param initialClassifier Initial classifier to bootstrap.
     * @param classifierFactory Factory for creating subsequent classifiers.
     * @param labeledData Labeled data for supervised trianing.
     * @param unlabeledData Unlabeled data for unsupervised training.
     * @param minTokenCount Min count for a word to not be pruned.
     * @return An iterator over classifiers that returns each epoch's
     * classifier.
     */
    public static Iterator<TradNaiveBayesClassifier>
        emIterator(TradNaiveBayesClassifier initialClassifier,
                   Factory<TradNaiveBayesClassifier> classifierFactory,
                   Corpus<ObjectHandler<Classified<CharSequence>>>  labeledData,
                   Corpus<ObjectHandler<CharSequence>> unlabeledData,
                   double minTokenCount) throws IOException {
        
        return new EmIterator(initialClassifier,classifierFactory,
                              labeledData,unlabeledData,minTokenCount);

    }


    /**
     * Apply the expectation maximization (EM) algorithm to train a traditional
     * naive Bayes classifier using the specified labeled and unabled data,
     * initial classifier and factory for creating subsequent factories,
     * maximum number of epochs, minimum improvement per epoch, and reporter
     * to which progress reports are sent.
     *
     * @param initialClassifier Initial classifier to bootstrap.
     * @param classifierFactory Factory for creating subsequent classifiers.
     * @param labeledData Labeled data for supervised trianing.
     * @param unlabeledData Unlabeled data for unsupervised training.
     * @param minTokenCount Min count for a word to not be pruned.
     * @param maxEpochs Maximum number of epochs to run training.
     * @param minImprovement Minimum relative improvement per epoch.
     * @param reporter Reporter to which intermediate results are reported,
     * or {@code null} for no reporting.
     * @return The trained classifier.
     */
    public static TradNaiveBayesClassifier
        emTrain(TradNaiveBayesClassifier initialClassifier,
                Factory<TradNaiveBayesClassifier> classifierFactory,
                Corpus<ObjectHandler<Classified<CharSequence>>> labeledData,
                Corpus<ObjectHandler<CharSequence>> unlabeledData,
                double minTokenCount,
                int maxEpochs,
                double minImprovement,
                Reporter reporter) throws IOException {

        if (reporter == null)
            reporter = Reporters.silent();

        long startTime = System.currentTimeMillis();

        double lastLogProb = Double.NEGATIVE_INFINITY;
        Iterator<TradNaiveBayesClassifier> it
            = emIterator(initialClassifier,classifierFactory,labeledData,unlabeledData,minTokenCount);
        TradNaiveBayesClassifier classifier = null;
        for (int epoch = 0; it.hasNext() && epoch < maxEpochs; ++epoch) {
            classifier = it.next();
            double modelLogProb = classifier.log2ModelProb();
            double dataLogProb = dataProb(classifier,labeledData,unlabeledData);
            double logProb = modelLogProb + dataLogProb;
            double relativeDiff = relativeDiff(lastLogProb,logProb);
            if (reporter.isDebugEnabled()) {
                Formatter formatter = new Formatter();
                formatter.format("epoch=%4d   dataLogProb=%15.2f   modelLogProb=%15.2f   logProb=%15.2f   diff=%15.12f",
                                 epoch, dataLogProb, modelLogProb, logProb, relativeDiff);
                String msg = formatter.toString();
                reporter.debug(msg);
            }
            if (!Double.isNaN(lastLogProb) && relativeDiff < minImprovement) {
                reporter.info("Converged");
                return classifier;
            } else {
                lastLogProb = logProb;
            }
        }
        return classifier;
    }

    static double dataProb(TradNaiveBayesClassifier classifier,
                           Corpus<ObjectHandler<Classified<CharSequence>>> labeledData,
                           Corpus<ObjectHandler<CharSequence>> unlabeledData) throws IOException {

        CaseProbAccumulator accum = new CaseProbAccumulator(classifier);
        labeledData.visitTrain(accum.supHandler());
        unlabeledData.visitTrain(accum);
        return accum.mCaseProb;
    }

    static double relativeDiff(double x, double y) {
        return 2.0 * Math.abs(x-y) / (Math.abs(x) + Math.abs(y));
    }


    static class CaseProbAccumulator 
        implements ObjectHandler<CharSequence> {


        double mCaseProb = 0.0;
        final TradNaiveBayesClassifier mClassifier;
        CaseProbAccumulator(TradNaiveBayesClassifier classifier) {
            mClassifier = classifier;
        }
        public void handle(CharSequence cSeq) {
            mCaseProb += mClassifier.log2CaseProb(cSeq);
        }
        public ObjectHandler<Classified<CharSequence>> supHandler() {
            final ObjectHandler<CharSequence> cSeqHandler = this;
            return new ObjectHandler<Classified<CharSequence>>() {
                public void handle(Classified<CharSequence> classified) {
                    cSeqHandler.handle(classified.getObject());
                }
            };
        }
    }


    static class EmIterator extends Iterators.Buffered<TradNaiveBayesClassifier> {
        private final Factory<TradNaiveBayesClassifier> mClassifierFactory;
        private final Corpus<ObjectHandler<Classified<CharSequence>>> mLabeledData;
        private final Corpus<ObjectHandler<CharSequence>> mUnlabeledData;
        private final double mMinTokenCount;
        private JointClassifier<CharSequence> mLastClassifier;
        EmIterator(TradNaiveBayesClassifier initialClassifier,
                   Factory<TradNaiveBayesClassifier> classifierFactory,
                   Corpus<ObjectHandler<Classified<CharSequence>>> labeledData,
                   Corpus<ObjectHandler<CharSequence>> unlabeledData,
                   double minTokenCount) {
            mClassifierFactory = classifierFactory;
            mLabeledData = labeledData;
            mUnlabeledData = unlabeledData;
            mMinTokenCount = minTokenCount;
            trainSup(labeledData,initialClassifier);
            compile(initialClassifier);
        }
        @Override
        public TradNaiveBayesClassifier bufferNext() {
            TradNaiveBayesClassifier classifier = mClassifierFactory.create();
            trainSup(mLabeledData,classifier);
            trainUnsup(mUnlabeledData,classifier);
            compile(classifier);
            return classifier;
        }
        void trainSup(Corpus<ObjectHandler<Classified<CharSequence>>> labeledData,
                      TradNaiveBayesClassifier classifier) {
            try {
                labeledData.visitTrain(classifier);
            } catch (IOException e) {
                throw new IllegalStateException("Error during labeled training",e);
            }
        }
        void trainUnsup(final Corpus<ObjectHandler<CharSequence>> unlabeledData,
                        final TradNaiveBayesClassifier classifier) {
            try {
                unlabeledData.visitTrain(new ObjectHandler<CharSequence>() {
                                             public void handle(CharSequence cSeq) {
                                                 ConditionalClassification c = mLastClassifier.classify(cSeq);
                                                 classifier.trainConditional(cSeq,c,1.0,mMinTokenCount);
                                             }
                                         });
            } catch (IOException e) {
                throw new IllegalStateException("Error during unlabeled training",e);
            }
        }
        void compile(TradNaiveBayesClassifier classifier) {
            try {
                @SuppressWarnings("unchecked") // know this is OK, assignment required to to scope
                JointClassifier<CharSequence> lastClassifier
                    = (JointClassifier<CharSequence>)
                    AbstractExternalizable.compile(classifier);
                mLastClassifier = lastClassifier;
            } catch (IOException e) {
                mLastClassifier = null;
                throw new IllegalStateException("Error during compilation.",e);
            } catch (ClassNotFoundException e) {
                mLastClassifier = null;
                throw new IllegalStateException("Error during compilation.",e);
            }
        }
    }




    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = -4786039228920809976L;
        private final TradNaiveBayesClassifier mClassifier;
        public Serializer(TradNaiveBayesClassifier classifier) {
            mClassifier = classifier;
        }
        public Serializer() {
            this(null);
        }
        @Override
        public Object read(ObjectInput in) throws ClassNotFoundException, IOException {
            int numCats = in.readInt();
            String[] categories = new String[numCats];
            for (int i = 0; i < numCats; ++i)
                categories[i] = in.readUTF();
            TokenizerFactory tokenizerFactory = (TokenizerFactory) in.readObject();
            double catPrior = in.readDouble();
            double tokenInCatPrior = in.readDouble();

            int tokenToCountsMapSize = in.readInt();
            Map<String,double[]> tokenToCountsMap
                = new HashMap<String,double[]>((tokenToCountsMapSize*3)/2);
            for (int k = 0; k < tokenToCountsMapSize; ++k) {
                String key = in.readUTF();
                double[] vals = new double[categories.length];
                for (int i = 0; i < categories.length; ++i)
                    vals[i] = in.readDouble();
                tokenToCountsMap.put(key,vals);
            }
            double[] totalCountsPerCategory = new double[categories.length];
            for (int i = 0; i < categories.length; ++i)
                totalCountsPerCategory[i] = in.readDouble();
            double[] caseCounts = new double[categories.length];
            for (int i = 0; i < categories.length; ++i)
                caseCounts[i] = in.readDouble();
            double totalCaseCount = in.readDouble();
            double lengthNorm = in.readDouble();
            return new TradNaiveBayesClassifier(categories,
                                                tokenizerFactory,
                                                catPrior,
                                                tokenInCatPrior,
                                                tokenToCountsMap,
                                                totalCountsPerCategory,
                                                caseCounts,
                                                totalCaseCount,
                                                lengthNorm);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mClassifier.mCategories.length);
            for (String category : mClassifier.mCategories)
                objOut.writeUTF(category);
            // may throw exception here if tokenizer factory not serializable
            objOut.writeObject(mClassifier.mTokenizerFactory);
            objOut.writeDouble(mClassifier.mCategoryPrior);
            objOut.writeDouble(mClassifier.mTokenInCategoryPrior);
            objOut.writeInt(mClassifier.mTokenToCountsMap.size());
            for (Map.Entry<String,double[]> entry : mClassifier.mTokenToCountsMap.entrySet()) {
                objOut.writeUTF(entry.getKey());
                double[] vals = entry.getValue();
                for (int i = 0; i < mClassifier.mCategories.length; ++i)
                    objOut.writeDouble(vals[i]);
            }
            for (int i = 0; i < mClassifier.mCategories.length; ++i)
                objOut.writeDouble(mClassifier.mTotalCountsPerCategory[i]);
            for (int i = 0; i < mClassifier.mCategories.length; ++i)
                objOut.writeDouble(mClassifier.mCaseCounts[i]);
            objOut.writeDouble(mClassifier.mTotalCaseCount);
            objOut.writeDouble(mClassifier.mLengthNorm);
        }
    }

    static class Compiler extends AbstractExternalizable {
        static final long serialVersionUID = 5689464666886334529L;
        private final TradNaiveBayesClassifier mClassifier;
        public Compiler() {
            this(null);
        }
        public Compiler(TradNaiveBayesClassifier classifier) {
            mClassifier = classifier;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mClassifier.mCategories.length);
            for (int i = 0; i < mClassifier.mCategories.length; ++i)
                objOut.writeUTF(mClassifier.mCategories[i]);
            AbstractExternalizable.compileOrSerialize(mClassifier.mTokenizerFactory,objOut);
            objOut.writeInt(mClassifier.mTokenToCountsMap.size());
            for (Map.Entry<String,double[]> entry : mClassifier.mTokenToCountsMap.entrySet()) {
                objOut.writeUTF(entry.getKey());
                double[] tokenCounts = entry.getValue();
                for (int i = 0; i < mClassifier.mCategories.length; ++i) {
                    double log2Prob = com.aliasi.util.Math.log2(mClassifier.probTokenByIndexArray(i,tokenCounts));
                    if (log2Prob > 0.0) {
                        String msg = "key=" + entry.getKey() +
                            " i=" + i
                            + " log2Prob=" + log2Prob
                            + " prob=" + mClassifier.probTokenByIndexArray(i,tokenCounts)
                            + " token counts[" + i + "]=" + tokenCounts[i]
                            + " totalCatCount=" + mClassifier.mTotalCountsPerCategory[i]
                            + " mTokenToCountsMap.size()=" + mClassifier.mTokenToCountsMap.size();
                        throw new IllegalArgumentException(msg);
                    }
                    objOut.writeDouble(log2Prob);
                }
            }
            for (int i = 0; i < mClassifier.mCategories.length; ++i)
                objOut.writeDouble(com.aliasi.util.Math.log2(mClassifier.probCatByIndex(i)));
            objOut.writeDouble(mClassifier.mLengthNorm);
        }
        @Override
        public Object read(ObjectInput in) throws ClassNotFoundException, IOException {
            int numCategories = in.readInt();
            String[] categories = new String[numCategories];
            for (int i = 0; i < numCategories; ++i)
                categories[i] = in.readUTF();
            TokenizerFactory tokenizerFactory = (TokenizerFactory) in.readObject();
            int size = in.readInt();
            Map<String,double[]> tokenToLog2ProbsInCats
                = new HashMap<String,double[]>((size * 3)/2);
            for (int k = 0; k < size; ++k) {
                String token = in.readUTF();
                double[] log2ProbsInCats = new double[numCategories];
                for (int i = 0; i < numCategories; ++i)
                    log2ProbsInCats[i] = in.readDouble();
                tokenToLog2ProbsInCats.put(token,log2ProbsInCats);
            }
            double[] log2CatProbs = new double[numCategories];
            for (int i = 0; i < numCategories; ++i)
                log2CatProbs[i] = in.readDouble();

            double lengthNorm = in.readDouble();

            return (categories.length == 2)
                ? new CompiledBinaryTradNaiveBayesClassifier(categories,
                                                             tokenizerFactory,
                                                             tokenToLog2ProbsInCats,
                                                             log2CatProbs,
                                                             lengthNorm)
                : new CompiledTradNaiveBayesClassifier(categories,
                                                       tokenizerFactory,
                                                       tokenToLog2ProbsInCats,
                                                       log2CatProbs,
                                                       lengthNorm);
        }
    }

    private static class CompiledBinaryTradNaiveBayesClassifier
        implements ConditionalClassifier<CharSequence> {
        private final TokenizerFactory mTokenizerFactory;
        private final Map<String,Double> mTokenToLog2ProbDiff;
        private final double mLog2CatProbDiff;
        private final double mLengthNorm;
        private final String[] mCats01;
        private final String[] mCats10;

        CompiledBinaryTradNaiveBayesClassifier(String[] categories,
                                               TokenizerFactory tokenizerFactory,
                                               Map<String,double[]> tokenToLog2ProbsInCats,
                                               double[] log2CatProbs,
                                               double lengthNorm) {
            mTokenizerFactory = tokenizerFactory;
            mTokenToLog2ProbDiff = new HashMap<String,Double>();
            for (Map.Entry<String,double[]> entry : tokenToLog2ProbsInCats.entrySet()) {
                String token = entry.getKey();
                double[] log2Probs = entry.getValue();
                double log2ProbDiff = (log2Probs[0] - log2Probs[1]) / com.aliasi.util.Math.LOG2_E;
                mTokenToLog2ProbDiff.put(token,log2ProbDiff);
            }
            mLog2CatProbDiff = (log2CatProbs[0] - log2CatProbs[1]) / com.aliasi.util.Math.LOG2_E;
            mLengthNorm = lengthNorm;
            mCats01 = new String[] { categories[0], categories[1] };
            mCats10 = new String[] { categories[1], categories[0] };
        }

        public ConditionalClassification classify(CharSequence in) {
            double logDiff = 0.0;
            char[] cs = Strings.toCharArray(in);
            Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
            int tokenCount = 0;
            for (String token : tokenizer) {
                Double tokLogDiff = mTokenToLog2ProbDiff.get(token);
                ++tokenCount;
                if (tokLogDiff == null) continue;
                logDiff += tokLogDiff;
            }
            if ((!Double.isNaN(mLengthNorm)) && (tokenCount > 0))
                logDiff *= mLengthNorm / tokenCount;
            
            logDiff += mLog2CatProbDiff;

            double expProd = Math.exp(logDiff);
            double p0 = expProd / (1.0 + expProd);
            double p1 = 1.0 - p0;
            return (p0 > p1)
                ? new ConditionalClassification(mCats01, new double[] { p0, p1 })
                : new ConditionalClassification(mCats10, new double[] { p1, p0 });
        }
    }

    private static class CompiledTradNaiveBayesClassifier
        implements JointClassifier<CharSequence>  {

        private final TokenizerFactory mTokenizerFactory;
        private final String[] mCategories;
        private final Map<String,double[]> mTokenToLog2ProbsInCats;
        private final double[] mLog2CatProbs;
        private final double mLengthNorm;

        CompiledTradNaiveBayesClassifier(String[] categories,
                                         TokenizerFactory tokenizerFactory,
                                         Map<String,double[]> tokenToLog2ProbsInCats,
                                         double[] log2CatProbs,
                                         double lengthNorm) {
            mCategories = categories;
            mTokenizerFactory = tokenizerFactory;
            mTokenToLog2ProbsInCats = tokenToLog2ProbsInCats;
            mLog2CatProbs = log2CatProbs;
            mLengthNorm = lengthNorm;
        }

        public JointClassification classify(CharSequence in) {
            double[] logps = new double[mCategories.length];
            char[] cs = Strings.toCharArray(in);
            Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
            int tokenCount = 0;
            for (String token : tokenizer) {
                double[] tokenLog2Probs = mTokenToLog2ProbsInCats.get(token);
                ++tokenCount;
                if (tokenLog2Probs == null) continue;
                for (int i = 0; i < logps.length; ++i) {
                    logps[i] += tokenLog2Probs[i];
                }
            }
            if ((!Double.isNaN(mLengthNorm)) && (tokenCount > 0)) {
                for (int i = 0; i < logps.length; ++i) {
                    logps[i] *= mLengthNorm / tokenCount;
                }
            }
            for (int i = 0; i < logps.length; ++i) {
                logps[i] += mLog2CatProbs[i];
            }
            return JointClassification.create(mCategories,logps);
        }

    }


}



