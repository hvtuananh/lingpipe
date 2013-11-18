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
 * A {@code ListCorpus} implements a corpus based on a list of
 * training and test cases.  Training and testing cases are added to
 * the corpus with {@link #addTrain(Object)} and {@link
 * #addTest(Object)} respectively.  The training and test data may
 * be randomized using {@link #permuteCorpus(Random)}.
 *
 * <h3>Serialization</h3>
 *
 * A list corpus is serializable if all of the training and test
 * objects are serializable.  The deserialized object will also
 * be a list corpus.
 *
 * <h3>Thread Safety</h3>
 *
 * A list corpus must be concurrent read/synchronous write synchronized.
 * The two add operations and permute operation must be synchronized
 * as writers.  The list views must be synchronized as must their
 * iterators.
 *
 * @author Bob Carpenter
 * @version 4.0.1
 * @since LingPipe3.9.2
 * @param <E> the type of objects handled in the corpus.
 */
public class ListCorpus<E> 
    extends Corpus<ObjectHandler<E>>
    implements Serializable {

    static final long serialVersionUID = 2705587926190761352L;

    private final List<E> mTrainCases;
    private final List<E> mTestCases;

    /**
     * Construct a list-based corpus with no entries.
     */
    public ListCorpus() {
        mTrainCases = new ArrayList<E>();
        mTestCases = new ArrayList<E>();
    }

    /**
     * Add the specified item to the list of test items.
     *
     * @param e Item to add as test case.
     */
    public void addTest(E e) {
        mTestCases.add(e);
    }

    /**
     * Add the specified item to the list of training items.
     *
     * @param e Item to add as training case.
     */
    public void addTrain(E e) {
        mTrainCases.add(e);
    }

    /**
     * Uses the specified random number generator to permute the
     * training and test cases in the corpus.  Using the same
     * randomizer will produce the same result.
     *
     * @param random Randomizer for corpus.
     */
    public void permuteCorpus(Random random) {
        Collections.shuffle(mTrainCases,random);
        Collections.shuffle(mTestCases,random);
    }

    /**
     * Returns an unmodifiable view of the list of test cases
     * underlying this corpus.  This view will change as test items
     * are added to the corpus and may change if the corpus is
     * permuted.
     *
     * @return The list of test cases for this corpus.
     */
    public List<E> testCases() {
        return Collections.unmodifiableList(mTestCases);
    }
    
    /**
     * Returns an unmodifiable view of the list of training cases
     * underlying this corpus.  This view will change as training
     * items are added to the corpus and may change if the corpus is
     * permuted.
     *
     * @return The list of training cases for this corpus.
     */
    public List<E> trainCases() {
        return Collections.unmodifiableList(mTrainCases);
    }


    public void visitTrain(ObjectHandler<E> handler) {
        for (E e : mTrainCases)
            handler.handle(e);
    }

    public void visitTest(ObjectHandler<E> handler) {
        for (E e : mTestCases)
            handler.handle(e);
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -5552459221218525839L;
        private final ListCorpus<F> mCorpus;
        public Serializer() {
            this(null);
        }
        public Serializer(ListCorpus<F> corpus) {
            mCorpus = corpus;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mCorpus.mTrainCases.size());
            for (F e : mCorpus.mTrainCases)
                out.writeObject(e);
            out.writeInt(mCorpus.mTestCases.size());
            for (F e : mCorpus.mTestCases)
                out.writeObject(e);
        }
        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {

            ListCorpus<F> corpus = new ListCorpus<F>();
            int numTrain = in.readInt();
            for (int i = 0; i < numTrain; ++i) {
                @SuppressWarnings("unchecked")
                F e = (F) in.readObject();
                corpus.addTrain(e);
            }
            int numTest = in.readInt();
            for (int i = 0; i < numTest; ++i) {
                @SuppressWarnings("unchecked")
                F e = (F) in.readObject();
                corpus.addTest(e);
            }
            return corpus;
        }
    }

}