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

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.UniformBoundaryLM;
import com.aliasi.lm.UniformProcessLM;

/**
 * A <code>BinaryLMClassifier</code> is a boolean dynamic language
 * model classifier for use when there are two categories, but
 * training data is only available for one of the categories.
 * 
 * <p>A binary LM classifier is based on a single language model and
 * cross-entropy threshold.  It defines two categories, accept and
 * reject, with acceptance determined by measuring sample
 * cross-entropy rate in a language model against a threshold.  As a
 * language model classifier, the multivariate category estimator is
 * uniform, the accepting language model is dynamic, and the rejecting
 * language model is constant.
 *
 * <P>As an instance of language model classifier, this class provides
 * scores that are adjusted per-character average log probabilities,
 * which are roughly negative sample cross-entropy rates (see {@link
 * LMClassifier}).  The accepting language model behaves in the usual
 * way.  The rejecting language model provides a constant
 * per-character log estimate.  The uniform rejecting model is defined
 * to be a boundary uniform lanuage model if the specified model is a
 * sequence language model and a process uniform language model
 * otherwise.
 *
 * <P>Training events may be supplied in the same way as for the
 * superclass {@link DynamicLMClassifier}, with two caveats.  First,
 * the multivariate category model remains uniform and thus does not
 * contribute to classification.  Second, training events for the
 * rejection category are ignored.  Thus only the language model for
 * the accepting category is trained.  The broader interface is
 * implemented without exceptions in order to allow binary classifiers
 * to be plugged in for ones with explicit rejection models.
 *
 * <P>Instances of this class are compilable as instances of their
 * superclass. The resulting object read back in will be an instance
 * of {@link LMClassifier}, not of this class, but its classification
 * behavior will be identical.  
 *
 * <P>Resetting category language models is not allowed for binary
 * language model classifiers, because they only contain one model and
 * all else is constant. 
 *
 * <P>Binary langauge model classifiers are concurrent-read and
 * single-write thread safe.  The only write operation is training the
 * accepting category.  Classification and compilation are reads.  If
 * the language model underlying this classifier is not thread safe,
 * then reads may not be called concurrently.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.0
 */
public class BinaryLMClassifier extends DynamicLMClassifier<LanguageModel.Dynamic> {

    private final String mAcceptCategory;
    private final String mRejectCategory;

    /**
     * Construct a binary character sequence classifier that accepts
     * or rejects inputs based on their cross-entropy being above or
     * below a fixed cross-entropy threshold.  If an input is accepted
     * the best category will be {@link #DEFAULT_ACCEPT_CATEGORY},
     * otherwise it will be {@link #DEFAULT_REJECT_CATEGORY}.  The
     * labels of the categories can be reversed in order to build a
     * rejector or changed altogether with the four-argument
     * constructor.  See the class documentation for more information
     * on training, classification and compilation.
     * 
     * @param acceptingLM The language model that determines
     * whether an input is accepted or rejected.
     * @param crossEntropyThreshold Maximum cross-entropy against a
     * model to accept the input.
     */ 
    public BinaryLMClassifier(LanguageModel.Dynamic acceptingLM,
                              double crossEntropyThreshold) {
        this(acceptingLM,crossEntropyThreshold,
             DEFAULT_ACCEPT_CATEGORY,
             DEFAULT_REJECT_CATEGORY);
    }

    /**
     * Construct a binary character sequence classifier that accepts
     * or rejects inputs based on their cross-entropy being above or
     * below a fixed cross-entropy threshold.  If an input is accepted
     * the best category will be the specified accept category,
     * otherwise it will be the specified reject category.  See the
     * class documentation for more information on training,
     * classification and compilation.
     * 
     * @param acceptingLM The language model that determines
     * whether an input is accepted or rejected.
     * @param crossEntropyThreshold Maximum cross-entropy against a
     * model to accept the input.
     * @param acceptCategory Category label for matching input.
     * @param rejectCategory Category label for rejecting input.
     */ 
    public BinaryLMClassifier(LanguageModel.Dynamic acceptingLM,
                              double crossEntropyThreshold,
                              String acceptCategory,
                              String rejectCategory) {
        super(new String[] { rejectCategory,
                             acceptCategory },
              new LanguageModel.Dynamic[] { 
                  createRejectLM(crossEntropyThreshold,
                                 acceptingLM),
                  acceptingLM });
        mAcceptCategory = acceptCategory;
        mRejectCategory = rejectCategory;
        // set up to uniform distribution 
        categoryDistribution().train(acceptCategory,1);
        categoryDistribution().train(rejectCategory,1);
    }

    /**
     * Returns the category assigned to matching/accepted cases.
     *
     * @return The acceptance category.
     */
    public String acceptCategory() {
        return mAcceptCategory;
    }

    /**
     * Returns the category assigned to non-matching/rejected cases.
     *
     * @return The rejection category.
     */
    public String rejectCategory() {
        return mRejectCategory;
    }

    /**
     * If the specified category is the accept catgory, train the
     * underlying language model.  If the category is the reject
     * category, only the category distribution is trained.  Either way, the multivariate
     * category estimate is not updated.
     *
     * @throws IllegalArgumentException If the category is unknown.
     */
    @Override
    void train(String category, char[] cs, int start, int end) {
        super.train(category,cs,start,end);
    }

    
    /**
     * If the specified category is the accept catgory, train the
     * underlying language model.  If the category is the reject
     * category, ignore the call.  Either way, the multivariate
     * category estimate is not updated.
     *
     * @param category Category of this training sample.
     * @param cSeq Char sequence for this training sample.
     * @throws IllegalArgumentException If the category is unknown.
     */
    @Override
    void train(String category, CharSequence cSeq) {
        languageModel(mAcceptCategory).train(cSeq);
    }

    /**
     * Train this classifier using the character sequence from the
     * specified classified object if the best category of the
     * classification is the accept category for this binary
     * classifier.  If the category is neither the accept or
     * reject category, this method throws an illegal argument
     * exception.
     *
     * @param classified Classified character sequence.
     * @throws IllegalArgumentException If the best category in the
     * classification of the classified object is neither the accept
     * nor the reject category for this binary classifier.
     */
    @Override
    public void handle(Classified<CharSequence> classified) {
        CharSequence cSeq  = classified.getObject();
        Classification classification = classified.getClassification();
        String bestCategory = classification.bestCategory();
        if (mRejectCategory.equals(bestCategory))
            return; // silently ignore reject data
        if (!mAcceptCategory.equals(bestCategory)) {
            String msg = "Require accept or reject category."
                + " Accept category=" + mAcceptCategory
                + " Reject category=" + mRejectCategory
                + " Found classified best category=" + bestCategory;
            throw new IllegalArgumentException(msg);
        }
        languageModel(mAcceptCategory).train(cSeq);
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     *
     * @param category Ignored.
     * @param lm Ignored.
     * @param newCount Ignored.
     * @throws UnsupportedOperationException Always.
     */
    @Override
    public void resetCategory(String category,
                              LanguageModel.Dynamic lm,
                              int newCount) {
        String msg = "Resets not allowed for Binary LM classifier.";
        throw new UnsupportedOperationException(msg);
    }

    static LanguageModel.Dynamic 
        createRejectLM(double crossEntropyThreshold,
                       LanguageModel acceptingLM) {

        if (acceptingLM instanceof LanguageModel.Sequence) 
            return new UniformBoundaryLM(crossEntropyThreshold);
        else 
            return new UniformProcessLM(crossEntropyThreshold);
    }
    
    /**
     * The default value of the category for accepting
     * input, &quot;true&quot;.
     */
    public static final String DEFAULT_ACCEPT_CATEGORY
        = Boolean.TRUE.toString();

    /**
     * The default value of the category for rejecting input,
     * &quot;false&quot;.
     */
    public static final String DEFAULT_REJECT_CATEGORY
        = Boolean.FALSE.toString();

    

}
