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

import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;


/**
 * A {@code BigVectorClassifier} provides an efficient linear
 * classifier implementation for large numbers of categories.
 * Inputs are vector implementations and outputs are scored
 * classifications pruned to the top N.
 *
 *
 * <h3>Computation</h3>
 *
 * <p>This class reverses what's typically a category (row) dominant
 * approach to a feature (column) dominant representation, allowing
 * scaling to large number of categories when the columns are sparse.
 *
 * <p>The standard approach in linear classifiers is to multiply a
 * (possibly sparse) input vector by each category's vector
 * representation.  The vector representing a category maps features
 * to values, and may be sparse.
 *
 * <p>This class reverses the representation.  Rather than a map from
 * categories to features to values, it uses a map from features to
 * categories to values.  For a sparse input, it then iterates over
 * the categories for each feature and adds the results.  If the
 * maps from categories to values for features are very sparse, this
 * saves significant time over multiplying the input by each category's
 * vector representation.

 * <p>This class uses a custom heap to efficiently merge the features
 * for each category, and a bounded priority queue for collecting
 * n-best results.
 *
 * <h3>Input Representation</h3>
 *
 * The constructor takes an array of vectors, one for each dimension,
 * or feature of the linear classifier.  Each of these vectors is
 * sparse and has dimensions corresponding to categories with non-zero
 * values for the feature.  It thus corresponds to a term/document
 * matrix in search, with terms being features and documents being
 * categories.
 *
 * <h3>Training</h3>
 *
 * <p>There are no training methods provided as part of this class.
 * It is meant as a general utility for importing large category
 * linear classifiers.
 *
 *
 * <h3>Serialization</h3>
 *
 * Instances may be serialized.  When read back in they will
 * be members of this class.
 *
 * <h3>Thread Safety</h3>
 *
 * This class is read-write threadsafe, where the only write operation
 * sets the maximum number of results.  Thus any number of concurrent
 * classifications may be carried out with a single instance of this
 * class.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe3.9
 */
public class BigVectorClassifier
    implements ScoredClassifier<Vector>,
               Serializable {

    static final long serialVersionUID = 5149230080619243511L;

    private final int[] mTermIndexes;
    private final int[] mDocumentIds;
    private final float[] mScores;
    private final String[] mCategories;

    private int mMaxResults;

    /**
     * Construct a big vector classifier with the specified term
     * vectors, maximum number of results, and categories equal to the
     * string representations of the category identifiers.
     *
     * <p>See {@link #BigVectorClassifier(Vector[],String[],int)} for
     * more information.
     *
     * @param termVectors Term vectors for classifier.
     * @param maxResults Maximum number of top results returned.
     */
    public BigVectorClassifier(Vector[] termVectors,
                               int maxResults) {
        this(termVectors,categoriesFor(termVectors),maxResults);
    }

    /**
     * Construct a big vector classifier with the specified term
     * vectors, categories, and maximum number of results.  The term
     * vectors have category identifiers as
     *
     * @param termVectors Term vectors for classifier.
     * @param categories Category names indexed by number.
     * @param maxResults Maximum number of top results returned.
     */
    public BigVectorClassifier(Vector[] termVectors,
                               String[] categories,
                               int maxResults) {
        mCategories = categories;
        // does not automatically prune zero values
        mTermIndexes = new int[termVectors.length];
        int size = termVectors.length; // 0 padding
        for (Vector termVector : termVectors)
            size += termVector.nonZeroDimensions().length;
        mDocumentIds = new int[size];
        mScores = new float[size];
        int pos = 0;
        for (int i = 0; i < termVectors.length; ++i) {
            mTermIndexes[i] = pos;
            Vector termVector = termVectors[i];
            int[] nzDims = termVector.nonZeroDimensions();
            for (int k = 0; k < nzDims.length; ++k) {
                int j = nzDims[k];
                mDocumentIds[pos] = j;
                mScores[pos] = (float) termVector.value(j);
                ++pos;
            }
            mDocumentIds[pos] = -1;
            ++pos;
        }
        setMaxResults(maxResults);

        /*
        System.out.println("termIndexes");
        for (int i = 0; i < mTermIndexes.length; ++i)
            System.out.println("  " + i + " " + mTermIndexes[i]);
        System.out.println("documentIds");
        for (int i = 0; i < mDocumentIds.length; ++i)
            System.out.println("  " + i + " " + mDocumentIds[i]);
        System.out.println("scores");
        for (int i = 0; i < mScores.length; ++i)
            System.out.println("  " + i + " " + mScores[i]);
        */

    }

    BigVectorClassifier(int[] termIndexes,
                        int[] documentIds,
                        float[] scores,
                        String[] categories,
                        int maxResults) {
        mTermIndexes = termIndexes;
        mDocumentIds = documentIds;
        mScores = scores;
        setMaxResults(maxResults);
        mCategories = categories;
    }

    static String[] categoriesFor(Vector[] termVectors) {
        int max = 0;
        for (Vector termVector : termVectors) {
            int[] nzDims = termVector.nonZeroDimensions();
            for (int k = 0; k < nzDims.length; ++k)
                max = Math.max(max,nzDims[k]);
        }
        String[] categories = new String[max];
        for (int i = 0; i < categories.length; ++i)
            categories[i] = Integer.toString(i);
        return categories;
    }

    /**
     * Return the maximum number of top results returned
     * by this classifier.
     *
     * @return Maximum number of results from classification.
     */
    public int maxResults() {
        return mMaxResults;
    }

    /**
     * Sets the maximum number of results returned by this
     * classifier.
     *
     * <p>This method is a write method which should be read-write
     * synchronized with calls to {@link #classify(Vector)}.
     *
     * @param maxResults Maximum number of top results returned
     * by this classifier.
     */
    public void setMaxResults(int maxResults) {
        if (maxResults < 1) {
            String msg = "Max results must be positive."
                + " Found maxResults=" + maxResults;
            throw new IllegalArgumentException(msg);
        }
        mMaxResults = maxResults;
    }

    /**
     * Return a scored classification consisting of the top results
     * for the specified vector input.
     *
     * <p>The maximum size of the returned scored classification is
     * given by {@link #maxResults()} and set with {@link
     * #setMaxResults(int)}.
     *
     * @param x Vector to classify.
     * @return Classification of the vector.
     *
     */
    public ScoredClassification classify(Vector x) {
        int[] nzDims = x.nonZeroDimensions();
        int heapSize = 0; // number dims in range of terms
        for (int k = 0; k < nzDims.length; ++k)
            if (nzDims[k] < mTermIndexes.length)
                ++heapSize;
        int[] current = new int[heapSize];
        float[] vals = new float[heapSize];
        int j = 0;
        for (int k = 0; k < heapSize; ++k) {
            if (nzDims[k] >= mTermIndexes.length)
                continue;
            current[j] = mTermIndexes[nzDims[k]];
            vals[j] = (float) x.value(nzDims[k]);
            ++j;
        }
        for (int k = (heapSize+1)/2; --k >= 0; )
            heapify(k,heapSize,current,vals,mDocumentIds);

        BoundedPriorityQueue<ScoredDoc> queue
            = new BoundedPriorityQueue<ScoredDoc>(ScoredObject.comparator(),
                                                  mMaxResults);
        int[] documentIds = mDocumentIds;
        while (heapSize > 0) {
            // printHeap(heapSize,current,vals,documentIds);
            int doc = documentIds[current[0]];
            // System.out.println("doc=" + doc);
            double score = 0.0;
            while (heapSize > 0 && documentIds[current[0]] == doc) {
                score += vals[0] * mScores[current[0]];
                ++current[0];
                if (documentIds[current[0]] == -1) {
                    --heapSize;
                    if (heapSize > 0) {
                        current[0] = current[heapSize];
                        vals[0] = vals[heapSize];
                    }
                }
                heapify(0,heapSize,current,vals,documentIds);
            }
            queue.offer(new ScoredDoc(doc,score));
        }
        String[] categories = new String[queue.size()];
        double[] scores = new double[queue.size()];
        int pos = 0;
        for (ScoredDoc sd : queue) {
            categories[pos] = Integer.toString(sd.docId());
            scores[pos] = sd.score();
            ++pos;
        }
        return new ScoredClassification(categories,scores);

    }

    Object writeReplace() {
        return new Serializer(this);
    }


    static void heapify(int i, int heapSize,
                        int[] current, float[] vals,
                        int[] documentIds) {
        while (true) {
            int left = 2 * (i+1) - 1;
            if (left >= heapSize)
                return;
            if (documentIds[current[i]] > documentIds[current[left]]) {
                swap(left,i,current);
                swap(left,i,vals);
                i = left;
                continue;
            }
            int right = left+1;
            if (right >= heapSize)
                return;
            if (documentIds[current[i]] > documentIds[current[right]]) {
                swap(right,i,current);
                swap(right,i,vals);
                i = right;
                continue;
            }
            return;
        }
    }

    static void printHeap(int heapSize,
                          int[] current, float[] vals,
                          int[] documentIds) {
        System.out.println("\nHeapSize=" + heapSize);
        for (int i = 0; i < heapSize; ++i)
            System.out.println("i=" + i + " curent=" + current[i] + " vals=" + vals[i]
                               + " docId=" + documentIds[current[i]]);
    }

    static void swap(int i, int j, int[] xs) {
        int tempXsI = xs[i];
        xs[i] = xs[j];
        xs[j] = tempXsI;
    }


    static void swap(int i, int j, float[] xs) {
        float tempXsI = xs[i];
        xs[i] = xs[j];
        xs[j] = tempXsI;
    }


    static class ScoredDoc implements Scored {
        private final int mDocId;
        private final double mScore;
        public ScoredDoc(int docId, double score) {
            mDocId = docId;
            mScore = score;
        }
        public int docId() {
            return mDocId;
        }
        public double score() {
            return mScore;
        }
        public String toString() {
            return mDocId + ":" + mScore;
        }
    }



    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 3954262240692411543L;
        private final BigVectorClassifier mClassifier;
        public Serializer() {
            this(null);
        }
        public Serializer(BigVectorClassifier classifier) {
            mClassifier = classifier;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            writeInts(mClassifier.mTermIndexes,objOut);
            writeInts(mClassifier.mDocumentIds,objOut);
            writeFloats(mClassifier.mScores,objOut);
            writeUTFs(mClassifier.mCategories,objOut);
            objOut.writeInt(mClassifier.mMaxResults);
        }
        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {
            int[] termIndexes = readInts(objIn);
            int[] documentIds = readInts(objIn);
            float[] scores = readFloats(objIn);
            String[] categories = readUTFs(objIn);
            int maxResults = objIn.readInt();
            return new BigVectorClassifier(termIndexes,
                                           documentIds,
                                           scores,
                                           categories,
                                           maxResults);
        }
    }





}

