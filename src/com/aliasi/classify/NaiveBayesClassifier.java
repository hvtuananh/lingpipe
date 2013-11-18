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

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;

/**
 * A <code>NaiveBayesClassifier</code> provides a trainable naive Bayes
 * text classifier, with tokens as features.  A classifier is
 * constructed from a set of categories and a tokenizer factory.  The
 * token estimator is a unigram token language model with a uniform
 * whitespace model and an optional n-gram character language model
 * for smoothing unknown tokens.
 *
 * <P>Naive Bayes applied to tokenized text results in a so-called
 * &quot;bag of words&quot; model where the tokens (words) are assumed
 * to be independent of one another:
 *
 * <blockquote><code>
 * P(tokens|cat) 
 * = <big><big>&Pi;</big></big><sub><sub>i&lt;tokens.length</sub></sub>
 *       P(tokens[i]|cat)
 * </code></blockquote>
 *
 * This class implements this assumption by plugging unigram token
 * language models into a dynamic language model classifier.  The
 * unigram token language model makes the naive Bayes assumption by
 * virtue of having no tokens of context.
 *
 * <P>The unigram model smooths maximum likelihood token estimates
 * with a character-level model.  Unfolding the general definition of
 * that class to the unigram case yields the model:
 *
 * <blockquote><code>
 *  P(token|cat) 
 * <br> = P<sub><sub>tokenLM(cat)</sub></sub>(token) 
 * <br> = &lambda; * count(token,cat) / totalCount(cat)
 * <br> &nbsp; + (1 - &lambda;) * P<sub>charLM(cat)</sub>(Word)
 * </code></blockquote>
 *
 * where <code>tokenLM(cat)</code> is the token language model defined
 * for the specified category and <code>charLM(cat)</code> is the
 * character level language model it uses for smoothing.  The unigram
 * token model is based on counts <code>count(token,cat)</code> of a
 * token in the category and an overall count
 * <code>totalCount(cat)</code> of tokens in the category.  The
 * interpolation factor <code>&lambda;</code> is computed as per the
 * Witten-Bell model C with hyperparameter one:
 *
 * <blockquote><code>
 *  &lambda = totalCount(cat) / (totalCount(cat) + numTokens(cat))
 * </code></blockquote>
 *
 * Roughly, the probability mass smoothed from the token model is
 * equal to the number of first-sightings of tokens in the training
 * data.
 *
 * <P>If this character smoothing model is uniform, there are two
 * extremes that need to be balanced, especially in cases where there
 * is not very much training data per category.  If it is in
 * initialized with the true number of characters, it will return a
 * proper uniform character estimate.  In practice, this will probably
 * underestimate unknown tokens and thus categories in which they are
 * unknown will pay a high penalty.  If the token smoothing model is
 * initalized with zero as the max number of characters, the token
 * backoff will always be zero and thus not contribute to the
 * classification scores.  This will overestimate unknown tokens for
 * classification, with probabilities summing to more than one.  In
 * practice, it will probably not penalize unknown words in categories
 * enough.  If the cost is greater than zero, it will be linear in the
 * length of the unknown token.
 * 
 * <P>Another way to smooth unknown tokens is to provide each model at
 * least one instance of each token known to every other model, so
 * there are no tokens known to one model and not another.  But this
 * adds an additional smoothing bias to the maximum likelihood
 * character estimates which may or may not be helpful.
 *
 * <P>The unigram model is constructed with a whitespace model that
 * returns a constant zero estimate, {@link UniformBoundaryLM#ZERO_LM},
 * and thus contributes no probability mass to estimates.
 * 
 * <P>As with the other language model classifiers, the conditional
 * category probability ratios are determined with a category
 * distribution and inversion:
 *
 * <blockquote><code>
 * ARGMAX<sub><sub>cat</sub></sub> P(cat|tokens)
 * <br>= ARGMAX<sub><sub>cat</sub></sub> P(cat,tokens) / P(tokens)
 * <br>= ARGMAX<sub><sub>cat</sub></sub> P(cat,tokens)
 * <br>= ARGMAX<sub><sub>cat</sub></sub> P(tokens|cat) * P(cat)
 * </code></blockquote>
 *
 * The category probability model <code>P(cat)</code> is taken
 * to be a multivariate estimator with an initial count of one
 * for each category.
 *
 * <P>For this class, the tokens are produced by a tokenizer factory.
 * This tokenizer factory may normalize tokens to stems, to lower
 * case, remove stop words, etc.  An extreme example would be to trim
 * the bag to a small set of salient words, as picked out by TF/IDF with
 * categories as documents.
 *
 * <h3>Compilation</h3>
 *
 * <P>Instances of this class may be compiled and read back into
 * memory in the same way as other instances of {@link
 * DynamicLMClassifier} using the {@code compileTo()} method or
 * utiltiies in the class {@code
 * com.aliasi.util.AbstractExternalizable}.
 *
 * <p>Deserializing After compilation, Deserialized instances of naive
 * Bayes classifiers should be cast to the interface {@code
 * JointClassifier<CharSequence>}, though they may also be cast to
 * {@code LMClassifier<CompiledTokenizedLM,MultivariateEstimator>}; the only
 * advantage to the latter cast is that you can still retrieve the
 * multivariate estimator over categories as well as the underlying
 * language model for each category.  These will be compiled instances.
 * 
 * <h3>Thread Safety</h3>
 *
 * Like almost all of LingPipe's statistical models, naive Bayes
 * classifiers are thread safe under read/write synchronization.
 * That is, any number of classification jobs may be performed
 * concurrently, but any parameter setting or training must be
 * done exclusively.
 * 
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class NaiveBayesClassifier 
    extends DynamicLMClassifier<TokenizedLM> {


    /**
     * Construct a naive Bayes classifier with the specified
     * categories and tokenizer factory.  
     *
     * <P>The character backoff models are assumed to be uniform
     * and there is no limit on the number of observed characters
     * other than {@link Character#MAX_VALUE}.
     *
     * @param categories Categories into which to classify text.
     * @param tokenizerFactory Text tokenizer.
     * @throws IllegalArgumentException If there are not at least two
     * categories.
     */
    public NaiveBayesClassifier(String[] categories,
                                TokenizerFactory tokenizerFactory) {
        this(categories,tokenizerFactory,0);
    }         

    /**
     * Construct a naive Bayes classifier with the specified
     * categories, tokenizer factory and level of character n-gram for
     * smoothing token estimates.  If the character n-gram is less
     * than one, a uniform model will be used.  
     *
     * <P>There is no limit on the number of observed characters
     * other than {@link Character#MAX_VALUE}.
     *
     * @param categories Categories into which to classify text.
     * @param tokenizerFactory Text tokenizer.
     * @param charSmoothingNGram Order of character n-gram used to
     * smooth token estimates.
     * @throws IllegalArgumentException If there are not at least two
     * categories.
     */
    public NaiveBayesClassifier(String[] categories,
                                TokenizerFactory tokenizerFactory,
                                int charSmoothingNGram) {
        this(categories,tokenizerFactory,
             charSmoothingNGram,Character.MAX_VALUE-1);
    }


    /**
     * Construct a naive Bayes classifier with the specified
     * categories, tokenizer factory and level of character n-gram for
     * smoothing token estimates, along with a specification of the
     * total number of characters in test and training instances.  If
     * the character n-gram is less than one, a uniform model will be
     * used.  
     * 
     * <P>As noted in the class documentation above, setting the
     * max observed characters parameter to one effectively eliminates
     * estimates of the string of an unknown token.
     *
     * @param categories Categories into which to classify text.
     * @param tokenizerFactory Text tokenizer.
     * @param charSmoothingNGram Order of character n-gram used to
     * smooth token estimates.
     * @param maxObservedChars The maximum number of characters found
     * in the text of training and test sets.
     * @throws IllegalArgumentException If there are not at least two
     * categories or if the number of observed characters is less than 1
     * or more than the total number of characters.
     */
    public NaiveBayesClassifier(String[] categories,
                                TokenizerFactory tokenizerFactory,
                                int charSmoothingNGram,
                                int maxObservedChars) {
        super(categories,
              naiveBayesLMs(categories.length,
                            tokenizerFactory,
                            charSmoothingNGram,
                            maxObservedChars));
    }

    // construct the LMs for categories
    private static TokenizedLM[] 
        naiveBayesLMs(int length, TokenizerFactory tokenizerFactory,
                      int charSmoothingNGram, int maxObservedChars) {
    
        TokenizedLM[] lms = new TokenizedLM[length];
        for (int i = 0; i < lms.length; ++i) {
            LanguageModel.Sequence charLM;
            if (charSmoothingNGram < 1)
                charLM = new UniformBoundaryLM(maxObservedChars);
            else
                charLM = new NGramBoundaryLM(charSmoothingNGram,
                                             maxObservedChars);
            lms[i]
                = new TokenizedLM(tokenizerFactory,
                                  1,
                                  charLM,
                                  UniformBoundaryLM.ZERO_LM,
                                  1);
        }
        return lms;
    }
}
