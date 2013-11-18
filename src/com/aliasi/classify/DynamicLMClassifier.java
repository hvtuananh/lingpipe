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

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.TokenizedLM;

import com.aliasi.stats.MultivariateDistribution;
import com.aliasi.stats.MultivariateEstimator;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

/**
 * A <code>DynamicLMClassifier</code> is a language model classifier
 * that accepts training events of categorized character sequences.
 * Training is based on a multivariate estimator for the category
 * distribution and dynamic language models for the per-category
 * character sequence estimators.  These models also form the basis of
 * the superclass's implementation of classification.
 *
 * <P>Because this class implements training and classification, it
 * may be used in tag-a-little, learn-a-little supervised learning
 * without retraining epochs.  This makes it ideal for active
 * learning applications, for instance.
 *
 * <P>At any point after adding training events, the classfier may be
 * compiled to an object output.  The classifier read back in will be
 * a non-dynamic instance of {@link LMClassifier}.  It will be based
 * on the compiled version of the multivariate estimator and the
 * compiled version of the dynamic language models for the categories.
 *
 * <P>Instances of this class allow concurrent read operations but
 * require writes to run exclusively.  Reads in this context are
 * either calculating estimates or compiling; writes are training.
 * Extensions to LingPipe's classes may impose tighter restrictions.
 * For instance, a subclass of <code>MultivariateEstimator</code>
 * might be used that does not allow concurrent estimates; in that
 * case, its restrictions are passed on to this classifier.  The same
 * goes for the language models and in the case of token language
 * models, the tokenizer factories.
 *
 * <h3>Compilation</h3>
 *
 * When compiling a dynamic LM classifier, the type of the object read
 * back in will be {@code
 * LMClassifier<LanguageModel,MultivariateDistribution>}.  The actual
 * language model will be the compiled version of the language model
 * in the classifier that was compiled, which varies by the type of
 * dynamic language model created.  For instance, the dynamic LM
 * classifiers produced by the factory methods {@code createNGramBoundary()},
 * {@code createNGramProcess()} and {@code createTokenized()} deserialize
 * with language models that are instances of
 * {@code LanguageModel.Sequence}, {@code LanguageModel.Process} and
 * {@code LanguageModel.Tokenized} respectively.
 * 
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe2.0
 * @param <L> the type of dynamic language model for this classifier
 */
public class DynamicLMClassifier<L extends LanguageModel.Dynamic>
    extends LMClassifier<L,MultivariateEstimator>
    implements ObjectHandler<Classified<CharSequence>>,
               Compilable {


    /**
     * Construct a dynamic language model classifier over the
     * specified categories with specified language
     * models per category and an overall category estimator.
     *
     * <P>The multivariate estimator over categories is initialized
     * with one count for each category.  Technically, initializing
     * counts involves a uniform Dirichlet prior with
     * <code>&alpha;=1</code>, which is often called Laplace
     * smoothing.
     *
     * @param categories Categories used for classification.
     * @param languageModels Dynamic language models for categories.
     * @throws IllegalArgumentException If there are not at least two
     * categories, or if the length of the category and language model
     * arrays is not the same, or if there are duplicate categories.
     */
    public DynamicLMClassifier(String[] categories,
                               L[] languageModels) {
        super(categories,
              languageModels,
              createCategoryEstimator(categories));
    }


    /**
     * Provide a training instance for the specified category
     * consisting of the sequence of characters in the specified
     * character slice.  A call to this method increments the count of
     * the category in the maximum likelihood estimator and also
     * trains the language model for the specified category.  Thus the
     * balance of categories reflected in calls to this method for
     * training should reflect the balance of categories in the test
     * set.
     *
     * <P>No modeling of the begin or end of the sequence is carried
     * out.  If such a behavior is desired, it should be reflected in
     * the training instances supplied to this method.
     *
     * <P>The component models for this classifier may be accessed and
     * trained independently using {@link #categoryDistribution()} and
     * {@link #languageModel(String)}.
     *
     * @param category Category of this training sequence.
     * @param cs Characters used for training.
     * @param start Index of first character to use for training.
     * @param end Index of one past the last character to use for
     * training.
     * @throws IllegalArgumentException If the category is not known.
     */
    void train(String category, char[] cs, int start, int end) {
        train(category,new String(cs,start,end-start));
    }

    /**
     * Provide a training instance for the specified category
     * consisting of the specified sample character sequence.
     * Training behavior is as described in {@link
     * #train(String,char[],int,int)}.
     *
     * @param category Category of this training sequence.
     * @param sampleCSeq Category sequence for training.
     * @throws IllegalArgumentException If the category is not known.
     */
    void train(String category, CharSequence sampleCSeq) {
        train(category,sampleCSeq,1);
    }



    /**
     * Provide a training instance for the specified category
     * consisting of the specified sample character sequence with the
     * specified count.  Training behavior is as described in {@link
     * #train(String,char[],int,int)}.
     *
     * <p>Counts of zero are ignored, whereas counts less than
     * zero raise an exception.
     *
     * @param category Category of this training sequence.
     * @param sampleCSeq Category sequence for training.
     * @param count Number of training instances.
     * @throws IllegalArgumentException If the category is not known
     * or if the count is negative.
     */
    public void train(String category, CharSequence sampleCSeq, int count) {
        if (count < 0) {
            String msg = "Counts must be non-negative."
                + " Found count=" + count;
            throw new IllegalArgumentException(msg);
        }
        if (count == 0) return;
        languageModel(category).train(sampleCSeq,count);
        categoryDistribution().train(category,count);
    }


    /**
     * Provides a training instance for the specified character
     * sequence using the best category from the specified
     * classification.  Only the first-best category from the
     * classification is used.  The object is cast to {@link CharSequence},
     * and the result passed along with the first-best category
     * to {@link #train(String,CharSequence)}.
     *
     * @param charSequence Character sequence for training.
     * @param classification Classification to use for training.
     * @throws ClassCastException If the specified object does not
     * implement <code>CharSequence</code>.
     */
    void handle(CharSequence charSequence, Classification classification) {
        train(classification.bestCategory(),charSequence);
    }

    /**
     * Provides a training instance for the specified character
     * sequence using the best category from the specified
     * classification.  Only the first-best category from the
     * classification is used.  
     *
     * @param classified Classified character sequence to treat as
     * training data.
     */
    public void handle(Classified<CharSequence> classified) {
        handle(classified.getObject(), classified.getClassification());
    }

    /**
     * Returns the maximum likelihood estimator for categories in this
     * classifier.  Changes to the returned model will be reflected in
     * this classifier; thus it may be used to train the category
     * estimator without affecting the language models for any
     * category.
     *
     * @return The maximum likelihood estimator for categories in this
     * classifier.
     */
    MultivariateEstimator categoryEstimator() {
        return mCategoryDistribution;
    }

    /**
     * Returns the language model for the specified category.  Changes
     * to the returned model will be reflected in this classifier; thus
     * it may be used to train a language model without affecting
     * the category estimates.
     *
     * @return The language model for the specified category.
     * @throws IllegalArgumentException If the category is not known.
     */
    L lmForCategory(String category) {
        L result = mCategoryToModel.get(category);
        if (result == null) {
            String msg = "Unknown category=" + category;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    /**
     * Writes a compiled version of this classifier to the specified
     * object output.  The object returned will be an instance
     * of {@link LMClassifier}.
     *
     * @param objOut Object output to which this classifier is
     * written.
     * @throws IOException If there is an I/O exception writing to
     * the output stream.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer<L>(this));
    }

    /**
     * Resets the specified category to the specified language model.
     * This also resets the count in the multivariate estimator of
     * categories to zero.
     *
     * @param category Category to reset.
     * @param lm New dynamic language model for category.
     * @param newCount New count for category.
     * @throws IllegalArgumentException If the category is not known.
     */
    public void resetCategory(String category,
                              L lm,
                              int newCount) {
        if (newCount < 0) {
            String msg = "Count must be non-negative."
                + " Found new count=" + newCount;
            throw new IllegalArgumentException(msg);
        }
        categoryDistribution().resetCount(category); // resets to zero
        categoryDistribution().train(category,newCount);
        L currentLM = languageModel(category);
        for (int i = 0; i < mLanguageModels.length; ++i) {
            if (currentLM == mLanguageModels[i]) {
                mLanguageModels[i] = lm;
                break;
            }
        }
        mCategoryToModel.put(category,lm);
    }


    /**
     * Construct a dynamic classifier over the specified categories,
     * using process character n-gram models of the specified order.
     *
     * <P>See the documentation for the constructor {@link
     * #DynamicLMClassifier(String[], LanguageModel.Dynamic[])} for
     * information on the category multivariate estimate for priors.
     *
     * @param categories Categories used for classification.
     * @param maxCharNGram Maximum length of character sequence
     * counted in model.
     * @throws IllegalArgumentException If there are not at least two
     * categories or if there are duplicate categories.
     */
    public static DynamicLMClassifier<NGramProcessLM>
        createNGramProcess(String[] categories,
                           int maxCharNGram) {

        NGramProcessLM[] lms = new NGramProcessLM[categories.length];
        for (int i = 0; i < lms.length; ++i)
            lms[i] = new NGramProcessLM(maxCharNGram);

        return new DynamicLMClassifier<NGramProcessLM>(categories,lms);
    }

    /**
     * Construct a dynamic classifier over the specified cateogries,
     * using boundary character n-gram models of the specified order.
     *
     * <P>See the documentation for the constructor {@link
     * #DynamicLMClassifier(String[], LanguageModel.Dynamic[])} for
     * information on the category multivariate estimate for priors.
     *
     * @param categories Categories used for classification.
     * @param maxCharNGram Maximum length of character sequence
     * counted in model.
     * @throws IllegalArgumentException If there are not at least two
     * categories or if there are duplicate categories.
     */
    public static DynamicLMClassifier<NGramBoundaryLM>
        createNGramBoundary(String[] categories,
                              int maxCharNGram) {

        NGramBoundaryLM[] lms = new NGramBoundaryLM[categories.length];
        for (int i = 0; i < lms.length; ++i)
            lms[i] = new NGramBoundaryLM(maxCharNGram);

        return new DynamicLMClassifier<NGramBoundaryLM>(categories,lms);
    }


    /**
     * Construct a dynamic language model classifier over the
     * specified categories using token n-gram language models of the
     * specified order and the specified tokenizer factory for
     * tokenization.
     *
     * <P>The multivariate estimator over categories is initialized
     * with one count for each category.
     *
     * <P>The unknown token and whitespace models are uniform sequence
     * models.
     *
     * @param categories Categories used for classification.
     * @param maxTokenNGram Maximum length of token n-grams used.
     * @param tokenizerFactory Tokenizer factory for tokenization.
     * @throws IllegalArgumentException If there are not at least two
     * categories or if there are duplicate categories.
     */
    public static DynamicLMClassifier<TokenizedLM>
        createTokenized(String[] categories,
                        TokenizerFactory tokenizerFactory,
                        int maxTokenNGram) {
        TokenizedLM[] lms = new TokenizedLM[categories.length];
        for (int i = 0; i < lms.length; ++i)
            lms[i] = new TokenizedLM(tokenizerFactory,maxTokenNGram);
        return new DynamicLMClassifier<TokenizedLM>(categories,lms);
    }

    // used in init and by other classes to create a smoothed estimator
    static MultivariateEstimator createCategoryEstimator(String[] categories) {
        MultivariateEstimator estimator = new MultivariateEstimator();
        for (int i = 0; i < categories.length; ++i)
            estimator.train(categories[i],1);
        return estimator;
    }


    private static class Externalizer<LL extends LanguageModel.Dynamic>
        extends AbstractExternalizable {

        static final long serialVersionUID = -5411956637253735953L;
        final DynamicLMClassifier<LL> mClassifier;
        public Externalizer() {
            mClassifier = null;
        }
        public Externalizer(DynamicLMClassifier<LL> classifier) {
            mClassifier = classifier;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeObject(mClassifier.categories());
            mClassifier.categoryDistribution().compileTo(objOut);
            int numCategories = mClassifier.mCategories.length;
            for (int i = 0; i < numCategories; ++i)
                ((LanguageModel.Dynamic) mClassifier.mLanguageModels[i]).compileTo(objOut);
        }
        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {

            String[] categories
                = (String[]) objIn.readObject();
            MultivariateDistribution categoryEstimator
                = (MultivariateDistribution) objIn.readObject();
            LanguageModel[] models = new LanguageModel[categories.length];
            for (int i = 0; i < models.length; ++i)
                models[i] = (LanguageModel) objIn.readObject();
            return new LMClassifier<LanguageModel,MultivariateDistribution>(categories,models,categoryEstimator);
        }
    }


}


