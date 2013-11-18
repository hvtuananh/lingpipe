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

import com.aliasi.features.Features;

import com.aliasi.matrix.EuclideanDistance;
import com.aliasi.matrix.Vector;

import com.aliasi.symbol.MapSymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Compilable;
import com.aliasi.util.Distance;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.Proximity;
import com.aliasi.util.ScoredObject;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A <code>KnnClassifier</code> implements k-nearest-neighor
 * classification based on feature extraction and a vector proximity
 * or distance.  K-nearest-neighbor classification is a kind of
 * memory-based learning in which every training instance is stored
 * along with its category.  To classify an object, the k nearest
 * training examples to the object being classified are found.
 * Each of the k nearest neighbors votes for its training category.
 * The resulting classification scores are the result of voting.
 * It is possible to weight the votes by proximity.
 *
 * <p>K-nearest-neighbor classifiers are particularly effective for
 * highly irregular classification boundaries where linear classifiers
 * like the perceptron have a hard time discriminiting instances.  For
 * instance, it's possible to learn checkerboard patterns in 2D space
 * using k-nearest-neighbor classification.
 *
 * <h3>Construction</h3>
 *
 * <p>A k-nearest neighbor classifier is constructed using a feature
 * extractor, the number of neighbors k to consider, a vector
 * distance or proximity and a boolean indicator of whether to
 * weight results by proximity or treat the nearest neighbors
 * equally.
 *
 * <h3>Training</h3>
 *
 * <p>Training simply involves storing the feature vector for each
 * training instance along with its category.  The vectors are stored
 * as instances of {@link com.aliasi.matrix.SparseFloatVector} for
 * space efficiency.  They are constructed using a symbol table for
 * features based on the specified feature extractor for this
 * classifier.
 *
 * <h3>Appropriate Distance and Proximity Functions</h3>
 *
 * <p>Nearness is defined by the proximity or distance functions over
 * vectors supplied at construction time.  As objects move nearer to
 * one another, their distance decreases and their proximity
 * increases.  Distance measures are converted into proximities behind
 * the scenes by inversion, as it leaves all proximities positive and
 * finite:
 *
 * <pre>
 *     proximity(v1,v2) = 1 / (1 + distance(v1,v2))</pre>
 *
 * This will scale distance functions that return results between 0
 * and positive infinity to return proximities between 0 and 1.
 *
 * <p><b>Warning:</b>
 * Distance functions used for k-nearest-neighbors classification
 * should not return negative values; any zero or negative values
 * will be converted to <code>Double.POSITIVE_INFINITY</code>.
 *
 * <h3>Classification</h3>
 *
 * <p>Classification involves finding the k nearest neighbors to a
 * query point.  Both training instances and instances to be classified
 * are converted to feature mappings using the specified feature
 * extractor, and then encoded as sparse vectors using an implicitly
 * managed feature symbol table.
 *
 * <p>The first step in classification is simply collecting the
 * k nearest neighbors.  That is, the training examples that have
 * the greatest proximity to the example being classified.
 * Given the set of k training examples that are closest to the
 * test point, one of two strategies is used for determing scores.
 * In the simple, unweighted case, the score of a category is
 * simply the number of vectors in the k nearest neighbors with
 * that category.  In the weighted case, each vector in the
 * k nearest neighbors contributes its proximity, and the final score
 * is the sum of all proximities.
 *
 * <h3>Choosing the Number of Neighbors k</h3>
 *
 * <p>In most cases, it makes sense to try to optimize for the number
 * of neighbors k using cross-validation or held-out data.
 *
 * <p>In the weighted case, it sometimes makes sense to take the
 * maximum number of neighbors k to be very large, potentially even
 * <code>Integer.MAX_VALUE</code>.  This is because the examples are
 * weighted by proximity, and those far away may have vanishingly
 * small proximities.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * <p>There is no compilation required for k-nearest-neighbors
 * classification, so serialization and compilation produce the
 * same result.  The object read back in after serialization or
 * compilation should be identical to the one serialized or
 * compiled.
 *
 * <h3>Implementation Notes</h3>
 *
 * <p>This is a brute force implementation of k-nearest neighbors in
 * the sense that every training example is multiplied by every object
 * being classified.  Some k-nearest-neighbor implementations attempt
 * to efficiently index the training examples, using techniques such
 * as <a href="http://en.wikipedia.org/wiki/Kd-tree">KD trees</a>, so
 * that search for nearest neighbors can be more efficient.
 *
 * <h3>References</h3>
 *
 * <p>K-nearest-neighbor classification is widely used, and well
 * described in several texts:
 *
 * <ul>
 * <li>Hastie, T., R. Tibshirani, and J. H. Friedman.  2001.
 * <i>Elements of Statistical Learning</i>.  Springer-Verlag.</li>

 * <li>Witten, I. and E. Frank.  <i>Data Mining, 2nd Edition</i>.
 * Morgan Kaufmann.</li>
 *
 * <li>Wikipedia: <a href="http://en.wikipedia.org/wiki/Nearest_neighbor_(pattern_recognition)">K Nearest Neighbor Algorithm</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 4.1.0
 * @since   LingPipe3.1
 * @param <E> the type of objects being classified
 */
public class KnnClassifier<E>
    implements ScoredClassifier<E>,
               ObjectHandler<Classified<E>>,
               Compilable,
               Serializable {

    static final long serialVersionUID = 5692985587478284405L;

    final FeatureExtractor<? super E> mFeatureExtractor;
    final int mK;
    final Proximity<Vector> mProximity;
    final boolean mWeightByProximity;
    final List<Integer> mTrainingCategories;
    final List<Vector> mTrainingVectors;
    final MapSymbolTable mFeatureSymbolTable;
    final MapSymbolTable mCategorySymbolTable;

    /**
     * Construct a k-nearest-neighbor classifier based on the
     * specified feature extractor and using the specified number of
     * neighbors.  The distance measure will be taken to be {@link
     * EuclideanDistance}.  The nearest neighbors will not be
     * weighted by proximity, and thus all have equal votes.
     *
     * @param featureExtractor Feature extractor for training and
     * classification instances.
     * @param k Maximum number of neighbors to use during
     * classification.
     */
    public KnnClassifier(FeatureExtractor<? super E> featureExtractor,
                         int k) {
        this(featureExtractor,k,EuclideanDistance.DISTANCE);
    }


    /**
     * Construct a k-nearest-neighbor classifier based on the
     * specified feature extractor, specified maximum number of
     * neighbors, and specified distance function.  The nearest
     * neighbors will not be weighted by proximity, and thus all have
     * equal votes.
     *
     * @param featureExtractor Feature extractor for training and
     * classification instances.
     * @param k Maximum number of neighbors to use during
     * classification.
     * @param distance Distance function to use to compare examples.
     */
    public KnnClassifier(FeatureExtractor<? super E> featureExtractor,
                         int k,
                         Distance<Vector> distance) {
        this(featureExtractor,k,new ProximityWrapper(distance),false);
    }


    /**
     * Construct a k-nearest-neighbor classifier based on the
     * specified feature extractor, specified maximum number of
     * neighbors, specified proximity function, and boolean flag
     * indicating whether or not to weight nearest neighbors by proximity
     * during classification.
     *
     * @param extractor Feature extractor for training and
     * classification instances.
     * @param k Maximum number of neighbors to use during
     * classification.
     * @param proximity Proximity function to compare examples.
     * @param weightByProximity Flag indicating whether to weight
     * neighbors by proximity during classification.
     */
    public KnnClassifier(FeatureExtractor<? super E> extractor,
                         int k,
                         Proximity<Vector> proximity,
                         boolean weightByProximity) {
        this(extractor,k,proximity,weightByProximity,
             new ArrayList<Integer>(),new ArrayList<Vector>(),
             new MapSymbolTable(), new MapSymbolTable());
    }

    KnnClassifier(FeatureExtractor<? super E> featureExtractor,
                  int k,
                  Proximity<Vector> proximity,
                  boolean weightByProximity,
                  List<Integer> trainingCategories,
                  List<Vector> trainingVectors,
                  MapSymbolTable featureSymbolTable,
                  MapSymbolTable categorySymbolTable) {
        mFeatureExtractor = featureExtractor;
        mK = k;
        mProximity = proximity;
        mWeightByProximity = weightByProximity;
        mTrainingCategories = trainingCategories;
        mTrainingVectors = trainingVectors;
        mFeatureSymbolTable = featureSymbolTable;
        mCategorySymbolTable = categorySymbolTable;
    }

    /**
     * Returns the feature extractor for ths KNN classifier.
     *
     * @return The feature extractor.
     */
    public FeatureExtractor<? super E> featureExtractor() {
        return mFeatureExtractor;
    }

    /**
     * Returns the proximity measure for this KNN classifier.
     *
     * @return The proximity.
     */
    public Proximity<Vector> proximity() {
        return mProximity;
    }

    /**
     * Returns a copy of he current list of categories for
     * this classifier.
     *
     * @return The categories for this classifier.
     */
    public List<String> categories() {
        List<String> catList = new ArrayList<String>();
        for (Integer i : mTrainingCategories)
            catList.add(mCategorySymbolTable.idToSymbol(i));
        return catList;
    }

    /**
     * Returns {@code true} if resposes are weighted by proximity.
     *
     * @return A flag indicating if this classifier is weighted
     * by proximity.
     */
    public boolean weightByProximity() {
        return mWeightByProximity;
    }

    /**
     * Returns the number K of neighbors used by this K-nearest
     * neighbors classifier.
     *
     * @return The K for this KNN classifier.
     */
    public int k() {
        return mK;
    }

    /**
     * Handle the specified classified training instance.  The
     * training instance is converted to a feature vector using the
     * feature extractor, and then stored as a sparse vector relative
     * to a feature symbol table.
     *
     * @param trainingInstance Object being classified during training.
     * @param classification Classification for specified object.
     */
    void handle(E trainingInstance, Classification classification) {
        String category = classification.bestCategory();
        Map<String,? extends Number> featureMap
            = mFeatureExtractor.features(trainingInstance);
        Vector vector
            = Features
            .toVectorAddSymbols(featureMap,
                                mFeatureSymbolTable,
                                Integer.MAX_VALUE-1,
                                false);

        mTrainingCategories.add(mCategorySymbolTable.getOrAddSymbolInteger(category));
        mTrainingVectors.add(vector);
    }

    /**
     * Handle the specified classified object as a training instance.
     * The training instance is converted to a feature vector using
     * the feature extractor, and then stored as a sparse vector
     * relative to a feature symbol table.
     *
     * @param classifiedObject Classified Object to use for training.
     */
    public void handle(Classified<E> classifiedObject) {
        handle(classifiedObject.getObject(),
               classifiedObject.getClassification());
    }

    /**
     * Return the k-nearest-neighbor classification result for the
     * specified input object.  The resulting classification will have
     * all of the categories defined, though those with no support in
     * the nearest neighbors will have scores of zero.
     *
     * <p>If this classifier does not weight by proximity, the
     * resulting score for a category will be the number of nearest
     * neighbors of the specified category.  That is, it will be a
     * straight vote.
     *
     * <p>If the classifier does weight by proximity, the resulting
     * score for a category will be the sum of the proximity scores
     * for the nearest neighbors of a given category.  Instances with
     * no near neighbors will be scored zero (<code>0</code>).  Thus
     * proximities should be configured to return positive values.
     *
     * @param in Object to classify.
     * @return Scored classification for the specified object.
     */
    public ScoredClassification classify(E in) {
        Map<String,? extends Number> featureMap
            = mFeatureExtractor.features(in);
        Vector inputVector
            = Features
            .toVector(featureMap,
                      mFeatureSymbolTable,
                      Integer.MAX_VALUE-1,
                      false);

        BoundedPriorityQueue<ScoredObject<Integer>> queue
            = new BoundedPriorityQueue<ScoredObject<Integer>>(ScoredObject.comparator(),
                                                              mK);
        for (int i = 0; i < mTrainingCategories.size(); ++i) {
            Integer catId = mTrainingCategories.get(i);
            Vector trainingVector = mTrainingVectors.get(i);
            double score = mProximity.proximity(inputVector,trainingVector);
            queue.offer(new ScoredObject<Integer>(catId,score));
        }

        int numCats = mCategorySymbolTable.numSymbols();
        double[] scores = new double[numCats];

        for (ScoredObject<Integer> catScore : queue) {
            int key = catScore.getObject().intValue();
            double score = catScore.score();
            scores[key] += mWeightByProximity ? score : 1.0;
        }

        // necessary for array
        List<ScoredObject<String>> catScores
            = new ArrayList<ScoredObject<String>>(numCats);
        for (int i = 0; i < numCats; ++i)
            catScores.add(new ScoredObject<String>(mCategorySymbolTable.idToSymbol(i),
                                                   scores[i]));
        return ScoredClassification.create(catScores);
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    /**
     * Compiles this k-nearest-neighbor classifier to the specified
     * object output stream.
     *
     * <p>This is only a convenience method.  It provides exactly
     * the same function as standard serialization.
     *
     * @param out Output stream to which this classifier is written.
     * @throws IOException If there is an underlying I/O exception
     * during compilation.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(writeReplace());
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 4951969636521202268L;
        final KnnClassifier<F> mClassifier;
        public Serializer() {
            this(null);
        }
        public Serializer(KnnClassifier<F> classifier) {
            mClassifier = classifier;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            AbstractExternalizable.serializeOrCompile(mClassifier.mFeatureExtractor,out);
            out.writeInt(mClassifier.mK);
            AbstractExternalizable.serializeOrCompile(mClassifier.mProximity,out);
            out.writeBoolean(mClassifier.mWeightByProximity);
            int numInstances = mClassifier.mTrainingCategories.size();
            out.writeInt(numInstances);

            for (int i = 0; i < numInstances; ++i)
                out.writeInt(mClassifier.mTrainingCategories.get(i).intValue());

            for (int i = 0; i < numInstances; ++i)
                AbstractExternalizable.serializeOrCompile(mClassifier.mTrainingVectors.get(i),
                                                          out);
            AbstractExternalizable.serializeOrCompile(mClassifier.mFeatureSymbolTable,out);
            AbstractExternalizable.serializeOrCompile(mClassifier.mCategorySymbolTable,out);
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            // required because of serialization
            @SuppressWarnings("unchecked")
            FeatureExtractor<? super F> featureExtractor
                = (FeatureExtractor<? super F>) in.readObject();
            int k = in.readInt();

            // required because of serialization
            @SuppressWarnings("unchecked")
            Proximity<Vector> proximity
                = (Proximity<Vector>) in.readObject();

            boolean weightByProximity = in.readBoolean();
            int numInstances = in.readInt();
            List<Integer> categoryList = new ArrayList<Integer>(numInstances);
            for (int i = 0; i < numInstances; ++i)
                categoryList.add(Integer.valueOf(in.readInt()));
            List<Vector> vectorList
                = new ArrayList<Vector>(numInstances);
            for (int i = 0; i < numInstances; ++i)
                vectorList.add((Vector) in.readObject());
            MapSymbolTable featureSymbolTable
                = (MapSymbolTable) in.readObject();
            MapSymbolTable categorySymbolTable
                = (MapSymbolTable) in.readObject();

            return new KnnClassifier<F>(featureExtractor,
                                        k,
                                        proximity,
                                        weightByProximity,
                                        categoryList,
                                        vectorList,
                                        featureSymbolTable,
                                        categorySymbolTable);
        }
    }

    static class ProximityWrapper
        implements Proximity<Vector>, Serializable {

        static final long serialVersionUID = -1410999733708772109L;

        Distance<Vector> mDistance;

        public ProximityWrapper() { /* empty on purpose */ }

        public ProximityWrapper(Distance<Vector> distance) {
            mDistance = distance;
        }
        public double proximity(Vector v1, Vector v2) {
            double d = mDistance.distance(v1,v2);
            return (d < 0) ? Double.MAX_VALUE : (1.0/(1.0 + d));
        }
    }

    static class TrainingInstance {
        final String mCategory;
        final Vector mVector;
        TrainingInstance(String category, Vector vector) {
            mCategory = category;
            mVector = vector;
        }
    }

}
