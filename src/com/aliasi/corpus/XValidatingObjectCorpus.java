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

package com.aliasi.corpus;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;



/**
 * An {@code XValidatingObjectCorpus} holds a list of items
 * which it uses to provide training and testing items using
 * cross-validation.  
 *
 * <h4>Handler Implementation</h4>
 *
 * The method {@link #handle(Object)} is used to add items to the
 * corpus.  The items will be stored in the order in which they
 * are received (though they may be permuted later).
 *
 * <p>When used as a handler, this class simply collects the items and
 * stores them in a list.  This allows an instance of this class to be
 * used like any other object handler.
 *
 * <h4>Cross Validation</h4>
 *
 * Cross-validation divides a corpus up into roughly equal sized
 * parts, called folds, assigning one of the parts as the test section and
 * the other parts as training sections.  A typical number of
 * folds is 10, with 90% of the data being used for training and
 * 10% for testing.  The number of folds is set in the constructor.
 *
 * <p>Initially, the fold will be set to 0, but the fold may be reset
 * later using {@link #setFold(int)}.  Iterating between 0 and the
 * number of folds minus 1 will work through all folds.  The method
 * {@link #size()} returns the size of the corpus and {@link #fold()}
 * is the current fold.
 *
 * <p>For cases where {@code numFolds()} is greater than zero,
 * the start and end of a fold are defined by:
 *
 * <blockquote><pre>
 * start(fold) = (int) (size() * fold() / (double) numFolds())
 * 
 * end(fold) = start(fold+1)</blockquote></pre>
 *
 * If {@code numFolds()} is 0, the start and end for the fold
 * are 0, so that visiting the training part of the corpus visits
 * the entire corpus.  
 *
 *
 * <h3>Permuting the Corpus</h3>
 * 
 * <p>The randomization method {@link #permuteCorpus(Random)} randomizes
 * the list of items.  This can be useful for removing local
 * dependencies.  See the section on thread safety below for more
 * information on the interaction of permutation and thread safety.
 *
 * <h4>Use Without Cross Validation</h4>
 *
 * No matter how the folds are set, using {@link
 * #visitCorpus(Handler)} will run the specified handler over all of
 * the data collected in this corpus.  
 *
 * <p>If the number of folds is set to 1, then {@link
 * #visitTest(Handler)} visits the entire corpus.
 *
 * <p>If the number of folds is set to 0, {@link #visitTrain(Handler)}
 * visits the entire corpus.
 * 
 * <h4>Thead Safety</h4>
 *
 * <p>This class must be used with external read/write
 * synchronization.  The write operations include the constructor,
 * set-fold, set number of folds, permute corpus, and handle methods.
 * The read operations include the visit num instances and fold
 * reporting methods.
 *
 * <p>Specifically, if the corpus is not being written to, folds may
 * be visited concurrently.
 *
 * <h3>Thread Safety</h3>
 *
 * A cross-validating object corpus must be concurrent read/sigle write
 * synchronized, with {@code handle()}, {@code setFold()}, {@code setNumFolds()},
 * and {@code permuteCorpus()} being the writers.
 *
 * <h3>Multi-Threaded Cross-Validation</h3>
 *
 * After the items in a cross-validating corpus are added and
 * optionally permuted, it is possible to carry out multi-threaded
 * cross-validation with views of the corpus.  The method {@link
 * #itemView()} returns a view of a corpus with an immutable item list.
 * But it allows the number of folds and fold to be set.  In
 * particular, as long as the underlying corpus is not modified,
 * a view for each fold may be created and run concurrently.
 *
 * <p>If a common evaluator is used, access to it must be synchronized
 * to set the appropriate model and run the evaluation.  If a separate
 * evaluation is used per thread, there is no need for synchronization.
 *
 * 
 * <h3>Serialization</h3>
 *
 * An {@code XValidatingObjectCorpus} may be serialized.  The 
 * corpus read back in will have the same items in the same
 * permutatino, with the same number of folds and the same fold
 * set as the corpus at the point it was serialized.
 * 
 * @author Bob Carpenter
 * @version 3.9.2
 * @since LingPipe3.9
 * @param <E> the type of objects handled.
 */
public class XValidatingObjectCorpus<E> 
    extends Corpus<ObjectHandler<E>> 
    implements ObjectHandler<E>, 
               Serializable {

    static final long serialVersionUID = -4855182679645668642L;

    private final List<E> mItemList;
    private int mNumFolds;
    private int mFold;

    /**
     * Construct a cross-validating corpus with the specified
     * number of folds.  The initial fold is set to 0.
     *
     * <p>See the class documentation above for information on
     * how the number of folds is used.
     *
     * @param numFolds Number of folds in the corpus.
     * @throws IllegalArgumentException If the number of folds is
     * negative.
     */
    public XValidatingObjectCorpus(int numFolds) {
        this(new ArrayList<E>(), numFolds, 0);
    }

    XValidatingObjectCorpus(List<E> itemList,
                           int numFolds,
                           int fold) {
        mItemList = itemList;
        setNumFolds(numFolds);
        mFold = fold;
    }

    /**
     * Returns a cross-validating corpus whose items are an immutable
     * view of the items in this corpus, but whose number of folds
     * or fold may be changed.  The mani purpose of this method is
     * to allow thread-safe cross-validationg.  See the class documentation
     * for examples.
     *
     * <p>Attempts to modify the items or their order using {@code
     * handle()} or {@code permuteCorpus()} will raise an {@code
     * UnsupportedOperationException} (note that permuting a
     * zero-length or length one list does not modify it, so permuting
     * an unmodifiable length one list does not raise an unsupported
     * opration exception.
     * 
     * @return View of this cross-validating corpus with a list of items
     * defined as an immutable view of the items in this corpus.
     */
    public XValidatingObjectCorpus<E> itemView() {
        return new XValidatingObjectCorpus<E>(Collections.unmodifiableList(mItemList),
                                              mNumFolds,
                                              mFold);
    }


    /**
     * Return the number of folds for this cross-validating corpus.
     *
     * @return Current number of folds.
     */
    public int numFolds() {
        return mNumFolds;
    }

    /**
     * Sets the number of folds to the specified value.
     *
     * <p>See the class documentation above for information on
     * how the number of folds is used.
     *
     * @param numFolds Number of folds.
     * @throws IllegalArgumentException If the number of folds is
     * negative.
     */
    public void setNumFolds(int numFolds) {
        if (numFolds < 0) {
            String msg = "Number of folds must be non-negative."
                + " Found numFolds=" + numFolds;
            throw new IllegalArgumentException(msg);
        }
        mNumFolds = numFolds;
    }

    /**
     * Returns the current fold.
     *
     * @return The current fold.
     */
    public int fold() {
        return mFold;
    }

    /**
     * Randomly permutes the corpus using the specified randomizer.
     *
     * @param random Randomizer to use for permutation.
     */
    public void permuteCorpus(Random random) {
        Collections.shuffle(mItemList,random);
    }

    /**
     * Set the current fold to the specified value.
     *
     * <p><i>Warning:</i>  If the number of folds is set to zero, this
     * method will throw an exception.
     *
     * @throws IllegalArgumentException If the fold is not greater than
     * or equal to 0 and less than the number of folds.
     */
    public void setFold(int fold) {
        if (mNumFolds == 0) {
            String msg = "Cannot set folds when numFolds() is 0.";
            throw new IllegalArgumentException(msg);
        }
        if (fold < 0 || fold >= mNumFolds) {
            String msg = "Fold must be non-negative and less than numFolds."
                + " Found numFolds=" + mNumFolds
                + " fold=" + fold;
            throw new IllegalArgumentException(msg);
        }
        mFold = fold;
    }

    /**
     * Return the number of items in this corpus.
     *
     * @return Number of items.
     */
    public int size() {
        return mItemList.size();
    }


    /**
     * Add the specified item to the end of the corpus.
     *
     * @param e Item to add to corpus.
     */
    public void handle(E e) {
        mItemList.add(e);
    }


    /**
     * Send all of the training items to the specified
     * handler.  See the class documentation above for
     * a specification of which items are visited based on
     * the value of the number of folds and the current fold.
     *
     * @param handler Handler receiving training items.
     */
    @Override
    public void visitTrain(ObjectHandler<E> handler) {
        handle(handler,0,startTestFold());
        handle(handler,endTestFold(),size());
    }

    /**
     * Send all of the test items to the specified
     * handler.  See the class documentation above for
     * a specification of which items are visited based on
     * the value of the number of folds and the current fold.
     *
     * @param handler Handler receiving training items.
     */
    @Override
    public void visitTest(ObjectHandler<E> handler) {
        handle(handler,startTestFold(),endTestFold());
    }
    
    @Override
    public void visitCorpus(ObjectHandler<E> handler) {
        for (E e : mItemList)
            handler.handle(e);
    }

    @Override
    public void visitCorpus(ObjectHandler<E> trainHandler,
                            ObjectHandler<E> testHandler) {
        visitTrain(trainHandler);
        visitTest(testHandler);
    }
    

    /**
     * Visit the test portion of the specified fold with the
     * specified handler.
     *
     * <p>This method ignores the value of the current fold.
     *
     * @param handler Handler for objects in corpus.
     * @param fold Fold whose test portion is visited.
     */
    public void visitTest(ObjectHandler<E> handler, int fold) {
        handle(handler, startTestFold(fold), endTestFold(fold));
    }

    /**
     * Visit the training portion of the specified fold with the
     * specified handler.
     *
     * <p>This method ignores the value of the current fold.
     *
     * @param handler Handler for objects in corpus.
     * @param fold Fold whose training portion is visited.
     */
    public void visitTrain(ObjectHandler<E> handler, int fold) {
        handle(handler,0,startTestFold(fold));
        handle(handler,endTestFold(fold),size());
    }
    

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    private void handle(ObjectHandler<E> handler, int start, int end) {
        for (int i = start; i < end; ++i)
            handler.handle(mItemList.get(i));
    }

    private int startTestFold() {
        return startTestFold(mFold);
    }

    private int startTestFold(int fold) {
        if (mNumFolds == 0) return 0;
        return (int) (size() * (fold / (double) mNumFolds));
    }

    private int endTestFold() {
        return endTestFold(mFold);
    }

    private int endTestFold(int fold) {
        if (mNumFolds == 0) return 0;
        if (fold == (mNumFolds - 1))
            return size(); // make sure cover the last example
        return (int) (size() * ((fold + 1.0)/ mNumFolds));
    }


    private static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 5857544240166060800L;
        final XValidatingObjectCorpus<F> mCorpus;
        public Serializer() {
            this(null);
        }
        public Serializer(XValidatingObjectCorpus<F> corpus) {
            mCorpus = corpus;
        }
        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeInt(mCorpus.numFolds());
            out.writeInt(mCorpus.fold());
            out.writeInt(mCorpus.size());
            for (F f : mCorpus.mItemList)
                out.writeObject(f);
        }
        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {

            int numFolds = in.readInt();
            int fold = in.readInt();
            int size = in.readInt();

            XValidatingObjectCorpus<Object> corpus = new XValidatingObjectCorpus<Object>(numFolds);
            corpus.setFold(fold);

            for (int i = 0; i < size; ++i) {
                Object o = in.readObject();
                corpus.handle(o);
            }
            return corpus;
        }
        
    }


}