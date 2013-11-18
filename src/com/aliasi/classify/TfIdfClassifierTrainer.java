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

import com.aliasi.symbol.MapSymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <code>TfIdfClassifierTrainer</code> provides a framework for
 * training discriminative classifiers based on term-frequency (TF)
 * and inverse document frequency (IDF) weighting of features.
 *
 * <h3>Construction</h3>
 *
 * <p>A <code>TfIdfClassifierTrainer</code> is constructed from a
 * feature extractor of a specified type.  If the instance is to
 * be compiled, the feature extractor must be either serializable
 * or compilable.

, producing an instance
 * that may be trained through
 *
 * <h3>Training</h3>
 *
 * <p>Categories may be added dynamically.  The initial classifier
 * will be empty and not defined for any categories.
 *
 * <p>A TF/IDF classifier trainer is trained through the {@link
 * ObjectHandler} interface with a classified object.  Specifically,
 * the method <code>handle(Classified<E>)</code> is called, the
 * generic object being the training instance and the classification
 * being a simple first-best classification.
 *
 * <p>For multiple training examples of the same category,
 * their feature vectors are added together to produce
 * the raw category vectors.
 *
 * <h3>Classification</h3>
 *
 * <p>The compiled models perform scored classification.  That is,
 * they implement the method <code>classify(E)</code> to return a
 * <code>ScoredClassification</code>.  The scores assigned to the
 * different categories are normalized dot products after term
 * frequency and inverse document frequency weighting.
 *
 * <p>Suppose training supplied <code>n</code> training
 * categories <code>cat[0], ..., cat[n-1]</code>, with
 * associated raw feature vectors <code>v[0], ..., v[n-1]</code>.
 * The dimensions of these vectors are the features, so that
 * if <code>f</code> is a feature, <code>v[i][f]</code> is
 * the raw score for the feature <code>f</code> in
 * category <code>cat[i]</code>.

 * <p>First, the inverse document frequency weighting of
 * each term is defined:
 *
 * <pre>
 *     idf(f) = ln (df(f) / n)</pre>
 *
 * where <code>df(f)</code> is the document frequency of
 * feature <code>f</code>, defined to be the number of
 * distinct categories in which feature <code>f</code> is
 * defined.  This has the effect of upweighting the scores of
 * features that occur in few categories and downweighting
 * the scores of features that occur in many categories
 *
 * <p>Term frequency normalization dampens the term
 * frequencies using square roots:
 *
 * <pre>
 *     tf(x) = sqrt(x)</pre>

 * This produces a linear relation in pairwise growth rather than the
 * usual quadratic one derived from a simple cross-product.
 *
 * <p>The weighted feature vectors are as follows:
 *
 * <pre>
 *     v'[i][f] = tf(v[i][f]) * idf(f)</pre>
 *
 * <p>Given an instance to classify, first the feature
 * extractor is used to produce a raw feature vector
 * <code>x</code>.  This is then normalized in the same
 * way as the document vectors <code>v[i]</code>, namely:
 *
 * <pre>
 *     x'[f] = tf(x[f]) * idf(f)</pre>
 *
 * The resulting query vector <code>x'</code> is then compared
 * against each normalized document vector <code>v'[i]</code>
 * using vector cosine, which defines its classification score:
 *
 * <pre>
 *     score(v'[i],x')
 *     = cos(v'[i],x')
 *     = v'[i] * x' / ( length(v'[i]) * length(x') )</pre>
 *
 * where <code>v'[i] * x'</code> is the vector dot product:
 *
 * <pre>
 *     <big><big>&Sigma;</big></big><sub><sub>f</sub></sub> v'[i][f] * x'[f]</pre>
 *
 * and where the length of a vector is defined to be
 * the square root of its dot product with itself:
 *
 * <pre>
 *     length(y) = sqrt(y * y)</pre>
 *
 * <p>Cosine scores will vary between <code>-1</code> and
 * <code>1</code>.  The cosine is only <code>1</code> between two
 * vectors if they point in the same direction; that is, one is a
 * positive scalar product of the other.  The cosine is only
 * <code>-1</code> between two vectors if they point in opposite
 * direction; that is, one is a negative scalar product of the other.
 * The cosine is <code>0</code> for two vectors that are orthogonal,
 * that is, at right angles to each other.  If all the values
 * in all of the category vectors and the query vector are
 * positive, cosine will run between <code>0</code> and <code>1</code>.
 *
 * <p><i>Warning:</i> Because of floating-point arithmetic rounding,
 * these results about signs and bounds are not strictly guaranteed to
 * hold; instances may return cosines slightly below <code>-1</code>
 * or above <code>1</code>, or not return exactly <code>0</code> for
 * orthogonal vectors.
 *
 * <h3>Serialization</h3>
 *
 * <p>A TF/IDF classifier trainer may be serialized at any point.
 * The object read back in will be an instance of the same
 * class with the same parametric type for the objects being
 * classified.  During serialization, the feature extractor
 * will be serialized if it's serializable, or compiled if
 * it's compilable but not serializable.  If the feature extractor
 * is neither serializable nor compilable, serialization will
 * throw an error.
 *
 * <h3>Compilation</h3>
 *
 * <p>At any point, a TF/IDF classifier may be compiled to an object
 * output stream.  The object read back in will be an instance of
 * <code>ScoredClassifier&lt;E&gt;</code>.  During compilation, the
 * feature extractor will be compiled if it's compilable, or
 * serialized if it's serializable but not compilable.  If the feature
 * extractor is neither compilable nor serializable, compilation will
 * throw an error.
 *
 * <h3>Reverse Indexing</h3>
 *
 * <p>The TF/IDF classifier indexes instances by means of
 * their feature values.
 *
 * @author  Bob Carpenter
 * @version 4.1.0
 * @since   LingPipe3.1
 * @param <E> the type of object being classified
 */
public class TfIdfClassifierTrainer<E>
    implements ObjectHandler<Classified<E>>,
               Compilable, 
               Serializable {

    static final long serialVersionUID = -2793388723202924633L;

    final FeatureExtractor<? super E> mFeatureExtractor;
    final Map<Integer,ObjectToDoubleMap<Integer>> mFeatureToCategoryCount;
    final MapSymbolTable mFeatureSymbolTable;
    final MapSymbolTable mCategorySymbolTable;

    /**
     * Construct a TF/IDF classifier trainer based on the specified
     * feature extractor.  This feature extractor must be either
     * serializable or compilable if the resulting trainer is to be
     * compilable.
     *
     * @param featureExtractor Feature extractor for examples.
     */
    public TfIdfClassifierTrainer(FeatureExtractor<? super E> featureExtractor) {
        this(featureExtractor,
             new HashMap<Integer,ObjectToDoubleMap<Integer>>(),
             new MapSymbolTable(),
             new MapSymbolTable());
    }


    TfIdfClassifierTrainer(FeatureExtractor<? super E> featureExtractor,
                           Map<Integer,ObjectToDoubleMap<Integer>> featureToCategoryCount,
                           MapSymbolTable featureSymbolTable,
                           MapSymbolTable categorySymbolTable) {
        mFeatureExtractor = featureExtractor;
        mFeatureToCategoryCount = featureToCategoryCount;
        mFeatureSymbolTable = featureSymbolTable;
        mCategorySymbolTable = categorySymbolTable;
    }

    /**
     * Returns the inverse document frequency of the specified
     * feature.  The value -1 is returned if the feature is unknown.
     *
     * @param feature Feature whose IDF is returned.
     * @return Inverse document frequency of specified feature.
     */
    public double idf(String feature) {
        Integer featureId = mFeatureSymbolTable.symbolToIDInteger(feature);
        if (featureId == null)
            return 0.0;
        ObjectToDoubleMap<Integer> otd 
            = mFeatureToCategoryCount.get(featureId);
        int df = otd.size();
        int numDocs = mCategorySymbolTable.numSymbols(); 
        return idf(df,numDocs);
    }

    /**
     * Return the TF/IDF score for the specified feature and
     * category. The value 0 is returned if the feature is uknown or if
     * it has zero count in the specified category.
     *
     * @param feature Feature for inverse document feature.
     * @param category Category for term frequency of feature in category.
     * @return TF/IDF score for the specified feature and category.
     */
    public double tfIdf(String feature, String category) {
        Integer featureId = mFeatureSymbolTable.symbolToIDInteger(feature);
        if (featureId == null)
            return 0.0;
        ObjectToDoubleMap<Integer> otd 
            = mFeatureToCategoryCount.get(featureId);
        Integer categoryId = mCategorySymbolTable.symbolToIDInteger(category);
        if (categoryId == null)
            return 0.0;
        double count = otd.getValue(categoryId);
        if (count == 0.0)
            return 0.0;
        int df = otd.size();
        int numDocs = mCategorySymbolTable.numSymbols(); 
        double idf = idf(df,numDocs);
        double tf = tf(count);
        return tf * idf;
    }

    /**
     * Rerturn the term frequency for the specified feature
     * in the specified category.   The value -1 is returned if the
     * feature is unknown.  
     *
     * <p>Note: This is not the raw count of the feature in the
     * category, but the square root of the raw count of the feature
     * in the category, as described in the class documentation above.
     *
     * @param feature Feature whose term frequency is required.
     * @param category Category of documents over which to count.
     * @return Term frequency of the feature in the specified category.
     */
    public double tf(String feature, String category) {
        Integer featureId = mFeatureSymbolTable.symbolToIDInteger(feature);
        if (featureId == null)
            return 0.0;
        ObjectToDoubleMap<Integer> otd 
            = mFeatureToCategoryCount.get(featureId);
        Integer categoryId = mCategorySymbolTable.symbolToIDInteger(category);
        if (categoryId == null)
            return 0;
        double count = otd.getValue(categoryId);
        return tf(count);
    }

    /**
     * Return the feature extractor for this classifier.
     *
     * @return The feature extractor for this classifier.
     */
    public FeatureExtractor<? super E> featureExtractor() {
        return mFeatureExtractor;
    }

    /**
     * Return an unmodifiable view of the underlying set of categories seen
     * in the training data.
     *
     * @return The set of categories for this trainer.
     */
    public Set<String> categories() {
        return mCategorySymbolTable.symbolSet();
    }

    /**
     * Train the classifier on the specified object with the specified
     * classification.
     *
     * @param input Classified object.
     * @param classification Classification of the the object.
     */
    void handle(E input, Classification classification) {
        String category = classification.bestCategory();
        int categoryId = mCategorySymbolTable.getOrAddSymbol(category);

        Map<String,? extends Number> featureVector
            = mFeatureExtractor.features(input);
        for (Map.Entry<String,? extends Number> entry
                 : featureVector.entrySet()) {
            String feature = entry.getKey();
            double value = entry.getValue().doubleValue();
            int featureId = mFeatureSymbolTable.getOrAddSymbol(feature);
            ObjectToDoubleMap<Integer> categoryCounts
                = mFeatureToCategoryCount.get(featureId);
            if (categoryCounts == null) {
                categoryCounts = new ObjectToDoubleMap<Integer>();
                mFeatureToCategoryCount.put(featureId,categoryCounts);
            }
            categoryCounts.increment(categoryId,value);
        }
    }


    /**
     * Handle the specified classified object as training data.
     *
     * @param classified Classified object for training.
     */
    public void handle(Classified<E> classified) {
        handle(classified.getObject(), classified.getClassification());
    }

    /**
     * Compile this trainer to the specified object output.
     *
     * @param out Stream to which a compiled classifier is written.
     * @throws UnsupportedOperationException If the underlying feature
     * extractor is neither compilable nor serializable.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Externalizer<E>(this));
    }

    // called via reflection during serialization
    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static double idf(double docFrequency, double numDocs) {
        return java.lang.Math.log(numDocs/docFrequency);
    }

    static double tf(double count) {
        return Math.sqrt(count);
    }


    static class Externalizer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 5578122239615646843L;
        final TfIdfClassifierTrainer<F> mTrainer;
        public Externalizer() {
            this(null);
        }
        public Externalizer(TfIdfClassifierTrainer<F> trainer) {
            mTrainer = trainer;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            // Feature Extractor
            AbstractExternalizable
                .compileOrSerialize(mTrainer.mFeatureExtractor,out);

            // Feature Symbol Table
            int numFeatures = mTrainer.mFeatureSymbolTable.numSymbols();
            out.writeObject(mTrainer.mFeatureSymbolTable);

            int numCats = mTrainer.mCategorySymbolTable.numSymbols();
            double numCatsD = (double) numCats;
            // num cats
            out.writeInt(numCats);
            // string categories [* num cats]
            for (int i = 0; i < numCats; ++i)
                out.writeUTF(mTrainer.mCategorySymbolTable.idToSymbol(i));

            // idfs [* num features]
            for (int i = 0; i < mTrainer.mFeatureSymbolTable.numSymbols(); ++i) {
                int docFrequency = mTrainer.mFeatureToCategoryCount.get(i).size();
                float idf = (float) idf(docFrequency,numCatsD);
                out.writeFloat(idf);
            }

            // feature offset [* numFeatures]
            int nextFeatureOffset = 0;
            for (int i = 0; i < numFeatures; ++i) {
                out.writeInt(nextFeatureOffset);
                int featureSize = mTrainer.mFeatureToCategoryCount.get(i).size();
                nextFeatureOffset += featureSize;
            }

            // catId/tfIdf array sizes
            out.writeInt(nextFeatureOffset);

            double[] catLengths = new double[numCats];
            for (Map.Entry<Integer,ObjectToDoubleMap<Integer>> entry :
                     mTrainer.mFeatureToCategoryCount.entrySet()) {
                ObjectToDoubleMap<Integer> categoryCounts = entry.getValue();
                double idf = idf(categoryCounts.size(),numCatsD);
                for (Map.Entry<Integer,Double> categoryCount : categoryCounts.entrySet()) {
                    int catId = categoryCount.getKey().intValue();
                    double count = categoryCount.getValue().doubleValue();
                    double tfIdf = tf(count) * idf;
                    catLengths[catId] += tfIdf * tfIdf;
                }
            }
            for (int i = 0; i < catLengths.length; ++i)
                catLengths[i] = Math.sqrt(catLengths[i]);

            // catId, normedTfIdf [* array size]
            for (int featureId = 0; featureId < numFeatures; ++featureId) {
                ObjectToDoubleMap<Integer> categoryCounts
                    = mTrainer.mFeatureToCategoryCount.get(featureId);
                double idf = idf(categoryCounts.size(),numCatsD);
                for (Map.Entry<Integer,Double> categoryCount : categoryCounts.entrySet()) {
                    int catId = categoryCount.getKey().intValue();
                    double count = categoryCount.getValue().doubleValue();
                    float tfIdf = (float) ((tf(count) * idf) / catLengths[catId]);
                    out.writeInt(catId);
                    out.writeFloat(tfIdf);
                }
            }
        }

        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {


            @SuppressWarnings("unchecked")
            FeatureExtractor<? super F> featureExtractor
                = (FeatureExtractor<? super F>) objIn.readObject();

            MapSymbolTable featureSymbolTable
                = (MapSymbolTable) objIn.readObject();
            int numFeatures = featureSymbolTable.numSymbols();

            int numCategories = objIn.readInt();
            String[] categories = new String[numCategories];
            for (int i = 0; i < numCategories; ++i)
                categories[i] = objIn.readUTF();

            float[] featureIdfs = new float[featureSymbolTable.numSymbols()];
            for (int i = 0; i < featureIdfs.length; ++i)
                featureIdfs[i] = objIn.readFloat();

            int[] featureOffsets = new int[numFeatures + 1];
            for (int i = 0; i < numFeatures; ++i)
                featureOffsets[i] = objIn.readInt();

            int catIdTfIdfArraySize = objIn.readInt();
            featureOffsets[featureOffsets.length-1] = catIdTfIdfArraySize;
            int[] catIds = new int[catIdTfIdfArraySize];
            float[] normedTfIdfs = new float[catIdTfIdfArraySize];
            for (int i = 0; i < catIdTfIdfArraySize; ++i) {
                catIds[i] = objIn.readInt();
                normedTfIdfs[i] = objIn.readFloat();
            }

            return new TfIdfClassifier<F>(featureExtractor,
                                          featureSymbolTable,
                                          categories,
                                          featureIdfs,
                                          featureOffsets,
                                          catIds,
                                          normedTfIdfs);
        }
    }


    static class TfIdfClassifier<G>
        implements ScoredClassifier<G> {

        final FeatureExtractor<? super G> mFeatureExtractor;
        final MapSymbolTable mFeatureSymbolTable;

        final String[] mCategories;

        // parallel (mFeatureIdfs, mFeatureIndexes)
        final float[] mFeatureIdfs;
        final int[] mFeatureOffsets;

        // parallel (mCategoryIds, mTfIdfs)
        final int[] mCategoryIds;
        final float[] mTfIdfs;

        TfIdfClassifier(FeatureExtractor<? super G> featureExtractor,
                        MapSymbolTable featureSymbolTable,
                        String[] categories,
                        float[] featureIdfs, int[] featureOffsets,
                        int[] categoryIds, float[] tfIdfs) {
            mFeatureExtractor = featureExtractor;
            mFeatureSymbolTable = featureSymbolTable;
            mCategories = categories;
            mFeatureIdfs = featureIdfs;
            mFeatureOffsets = featureOffsets;
            mCategoryIds = categoryIds;
            mTfIdfs = tfIdfs;

        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("TfIdfClassifierTrainer.TfIdfClassifier\n");
            sb.append("Feature Symbol Table\n  ");
            sb.append(mFeatureSymbolTable.toString());
            sb.append("\n");
            sb.append("Categories\n");
            for (int i = 0; i < mCategories.length; ++i)
                sb.append("  " + i + "=" + mCategories[i] + "\n");
            sb.append("Index  Feature IDF  offset\n");
            for (int i = 0; i < mFeatureIdfs.length; ++i) {
                sb.append("  " + i
                          + "  " + mFeatureSymbolTable.idToSymbol(i)
                          + "   " + mFeatureIdfs[i]
                          + "   " + mFeatureOffsets[i]
                          + "\n");
            }
            sb.append("Index  CategoryID  TF-IDF\n");
            for (int i = 0; i < mCategoryIds.length; ++i) {
                sb.append("  " + i + "   " + mCategoryIds[i] + "    " + mTfIdfs[i] + "\n");
            }
            return sb.toString();
        }

        public ScoredClassification classify(G in) {
            Map<String,? extends Number> featureVector
                = mFeatureExtractor.features(in);

            double[] scores = new double[mCategories.length];
            double inputLengthSquared = 0.0;
            for (Map.Entry<String,? extends Number> featureValue
                     : featureVector.entrySet()) {
                String feature = featureValue.getKey();
                int featureId = mFeatureSymbolTable.symbolToID(feature);
                if (featureId == -1) continue;
                double inputTf = tf(featureValue.getValue().doubleValue());
                double inputIdf = mFeatureIdfs[featureId];
                double inputTfIdf = inputTf * inputIdf;
                inputLengthSquared += inputTfIdf * inputTfIdf;
                for (int offset = mFeatureOffsets[featureId];
                     offset < mFeatureOffsets[featureId+1];
                     ++offset) {
                    int categoryId = mCategoryIds[offset];
                    double docNormedTfIdf = mTfIdfs[offset];
                    scores[categoryId] += docNormedTfIdf * inputTfIdf;
                }

            }
            double inputLength = Math.sqrt(inputLengthSquared);

            List<ScoredObject<String>> catScores
                = new ArrayList<ScoredObject<String>>(mCategories.length);
            for (int i = 0; i < scores.length; ++i) {
                double score = scores[i] / inputLength; // cosine norm for input
                catScores.add(new ScoredObject<String>(mCategories[i],score));
            }
            return ScoredClassification.create(catScores);
        }

    }


    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -4757808688956812832L;
        final TfIdfClassifierTrainer<F> mTrainer;
        public Serializer() {
            this(null);
        }
        public Serializer(TfIdfClassifierTrainer<F> trainer) {
            mTrainer = trainer;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            AbstractExternalizable
                .serializeOrCompile(mTrainer.mFeatureExtractor,out);
            out.writeObject(mTrainer.mFeatureToCategoryCount);
            out.writeObject(mTrainer.mFeatureSymbolTable);
            out.writeObject(mTrainer.mCategorySymbolTable);
        }
        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {

            @SuppressWarnings("unchecked")
            FeatureExtractor<? super F> featureExtractor
                = (FeatureExtractor<? super F>) objIn.readObject();

            // required for readObject
            @SuppressWarnings("unchecked")
            Map<Integer,ObjectToDoubleMap<Integer>> featureToCategoryCount
                = (Map<Integer,ObjectToDoubleMap<Integer>>)
                objIn.readObject();

            MapSymbolTable featureSymbolTable
                = (MapSymbolTable) objIn.readObject();

            MapSymbolTable categorySymbolTable
                = (MapSymbolTable) objIn.readObject();

            return new TfIdfClassifierTrainer<F>(featureExtractor,
                                                 featureToCategoryCount,
                                                 featureSymbolTable,
                                                 categorySymbolTable);
        }
    }



}


