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


import com.aliasi.stats.MultivariateEstimator;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Counter;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * A <code>BernoulliClassifier</code> provides a feature-based
 * classifier where feature values are reduced to booleans based on a
 * specified threshold.  Training events are supplied in the usual
 * way through the {@link #handle(Classified)} method.
 *
 * <p>Given a feature threshold of <code>t</code>, any feature with
 * value strictly greater than the threshold <code>t</code> for a
 * given input is activated, and all other features are not activated
 * for that input.
 *
 * <p>The likelihood of a feature in a category is estimated with the
 * training sample counts using add-one smoothing (also known as
 * Laplace smoothing, or a uniform Dirichlet prior).  There is also
 * a term for the category distribution.  Suppose <code>F</code> is
 * the complete set of features seen during training.  Further suppose
 * that <code>count(cat)</code> is the number of training samples
 * for category <code>cat</code>, and <code>count(cat,feat)</code>
 * is the number of training instaces of the specified category that
 * had the specified feature activated.  Thus the contribution of
 * each feature is computed by:
 *
 * <pre>
 * p(+feat|cat) = (count(cat,feat) + 1) / (count(cat)+2)
 * p(-feat|cat) = 1.0 - p(cat,feat)</pre>
 *
 * <p>Assuming the total number of training instances is <code>totalCount</code>,
 * we use a simple maximum-likelihood estimate for the category probability:
 *
 * <pre>
 * p(cat) = count(cat) / totalCount</pre>
 *
 * With these two definitions, we define the joint probability estimate for
 * a category <code>cat</code> given activated features
 * <code>{f[0],...,f[n-1]}</code> and unactivated features
 * <code>{g[0],...,g[m-1]}</code> is:
 *
 * <pre>
 * p(cat,{f[0],...f[n-1]})
 *   = p(cat)
 *   * <big><big>&Pi;</big></big><sub><sub>i &lt; n</sub></sub> p(f[i]|cat)
 *   * <big><big>&Pi;</big></big><sub><sub>j &lt; m</sub></sub> p(-g[j]|cat)</pre>
 *
 * <p>The {@link JointClassification} class requires log (base 2) estimates,
 * and is responsible for converting these to conditional estimates.
 * The scores in this case are just the log2 joint estimates.
 *
 * <p>The dynamic form of the estimator may be used for classification,
 * but it is not very efficient.  It loops over every feature for every
 * category.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * <p>The serialized version of a Bernoulli classifier will
 * deserialize as an equivalent instance of
 * <code>BernoulliClassifier</code>.  In order to serialize a
 * Bernoulli classifier, the feature extractor must be serializable.
 * Otherwise an exception will be raised during serialization.
 *
 * <p>Compilation is not yet implemented.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe3.1
 * @param <E> the type of object classified
 */
public class BernoulliClassifier<E>
    implements JointClassifier<E>,
               ObjectHandler<Classified<E>>,
               Serializable {

    static final long serialVersionUID = -7761909693358968780L;

    private final MultivariateEstimator mCategoryDistribution;
    private final FeatureExtractor<E> mFeatureExtractor;
    private final double mActivationThreshold;
    private final Set<String> mFeatureSet;
    private final Map<String,ObjectToCounterMap<String>> mFeatureDistributionMap;

    /**
     * Construct a Bernoulli classifier with the specified feature
     * extractor and the default feature activation threshold of 0.0.
     *
     * @param featureExtractor Feature extractor for classification.
     */
    public BernoulliClassifier(FeatureExtractor<E> featureExtractor) {
        this(featureExtractor,0.0);
    }

    /**
     * Construct a Bernoulli classifier with the specified feature
     * extractor and specified feature activation threshold.
     *
     * @param featureExtractor Feature extractor for classification.
     * @param featureActivationThreshold The threshold for feature
     * activation (see the class documentation).
     */
    public BernoulliClassifier(FeatureExtractor<E> featureExtractor,
                               double featureActivationThreshold) {
        this(new MultivariateEstimator(),
             featureExtractor,
             featureActivationThreshold,
             new HashSet<String>(),
             new HashMap<String,ObjectToCounterMap<String>>());
    }

    BernoulliClassifier(MultivariateEstimator catDistro,
                        FeatureExtractor<E> featureExtractor,
                        double activationThreshold,
                        Set<String> featureSet,
                        Map<String,ObjectToCounterMap<String>> featureDistributionMap) {
        mCategoryDistribution = catDistro;
        mFeatureExtractor = featureExtractor;
        mActivationThreshold = activationThreshold;
        mFeatureSet = featureSet;
        mFeatureDistributionMap = featureDistributionMap;
    }

    /**
     * Returns the feature activation threshold.
     *
     * @return The feature activation threshold for this classifier.
     */
    public double featureActivationThreshold() {
        return mActivationThreshold;
    }

    /**
     * Return the feature extractor for this classifier.
     *
     * @return The feature extractor for this classifier.
     */
    public FeatureExtractor<E> featureExtractor() {
        return mFeatureExtractor;
    }

    /**
     * Returns a copy of the list the categories for this classifier.
     *
     * @return The categories for this classifier. 
     */
    public String[] categories() {
        String[] categories = new String[mCategoryDistribution.numDimensions()];
        for (int i = 0; i < mCategoryDistribution.numDimensions(); ++i)
            categories[i] = mCategoryDistribution.label(i);
        return categories;
    }

    /**
     * Handle the specified training classified object.
     *
     * @param classified Classified object to add to handle
     * as training data.
     */
    public void handle(Classified<E> classified) {
        handle(classified.getObject(),
               classified.getClassification());
    }


    /**
     * Handle the specified training event, consisting of an input
     * and its first-best classification.
     *
     * @param input Object whose classification result is being
     * trained.
     * @param classification Classification result for object.
     */
    void handle(E input, Classification classification) {
        String category = classification.bestCategory();
        mCategoryDistribution.train(category,1L);
        ObjectToCounterMap<String> categoryCounter
            = mFeatureDistributionMap.get(category);
        if (categoryCounter == null) {
            categoryCounter = new ObjectToCounterMap<String>();
            mFeatureDistributionMap.put(category,categoryCounter);
        }

        for (String feature : activeFeatureSet(input)) {
            categoryCounter.increment(feature);
            mFeatureSet.add(feature);
        }
    }

    /**
     * Classify the specified input using this Bernoulli classifier.
     * See the class documentation above for mathematical details.
     *
     * @param input Input to classify.
     * @return Classification of the specified input.
     */
    public JointClassification classify(E input) {
        Set<String> activeFeatureSet = activeFeatureSet(input);
        Set<String> inactiveFeatureSet = new HashSet<String>(mFeatureSet);
        inactiveFeatureSet.removeAll(activeFeatureSet);

        String[] activeFeatures
            = activeFeatureSet.<String>toArray(Strings.EMPTY_STRING_ARRAY);
        String[] inactiveFeatures
            = inactiveFeatureSet.<String>toArray(Strings.EMPTY_STRING_ARRAY);

        ObjectToDoubleMap<String> categoryToLog2P
            = new ObjectToDoubleMap<String>();
        int numCategories = mCategoryDistribution.numDimensions();
        for (long i = 0; i < numCategories; ++i) {
            String category = mCategoryDistribution.label(i);
            double log2P = com.aliasi.util.Math.log2(mCategoryDistribution.probability(i));

            double categoryCount
                = mCategoryDistribution.getCount(i);

            ObjectToCounterMap<String> categoryFeatureCounts
                = mFeatureDistributionMap.get(category);

            for (String activeFeature : activeFeatures) {
                double featureCount = categoryFeatureCounts.getCount(activeFeature);
                if (featureCount == 0.0) continue; // ignore unknown features
                log2P += com.aliasi.util.Math.log2((featureCount+1.0) / (categoryCount+2.0));
            }

            for (String inactiveFeature : inactiveFeatures) {
                double notFeatureCount
                    = categoryCount
                    - categoryFeatureCounts.getCount(inactiveFeature);
                log2P += com.aliasi.util.Math.log2((notFeatureCount + 1.0) / (categoryCount + 2.0));
            }
            categoryToLog2P.set(category,log2P);
        }
        String[] categories = new String[numCategories];
        double[] log2Ps = new double[numCategories];
        List<ScoredObject<String>> scoredObjectList
            = categoryToLog2P.scoredObjectsOrderedByValueList();
        for (int i = 0; i < numCategories; ++i) {
            ScoredObject<String> so = scoredObjectList.get(i);
            categories[i] = so.getObject();
            log2Ps[i] = so.score();
        }
        return new JointClassification(categories,log2Ps);
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    private Set<String> activeFeatureSet(E input) {
        Set<String> activeFeatureSet = new HashSet<String>();
        Map<String,? extends Number> featureMap
            = mFeatureExtractor.features(input);
        for (Map.Entry<String,? extends Number> entry : featureMap.entrySet()) {
            String feature = entry.getKey();
            Number val = entry.getValue();
            if (val.doubleValue() > mActivationThreshold)
                activeFeatureSet.add(feature);
        }
        return activeFeatureSet;
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 4803666611627400222L;
        final BernoulliClassifier<F> mClassifier;
        public Serializer(BernoulliClassifier<F> classifier) {
            mClassifier = classifier;
        }
        public Serializer() {
            this(null);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeObject(mClassifier.mCategoryDistribution);
            objOut.writeObject(mClassifier.mFeatureExtractor);
            objOut.writeDouble(mClassifier.mActivationThreshold);
            objOut.writeInt(mClassifier.mFeatureSet.size());
            for (String feature : mClassifier.mFeatureSet)
                objOut.writeUTF(feature);
            objOut.writeInt(mClassifier.mFeatureDistributionMap.size());
            for (Map.Entry<String,ObjectToCounterMap<String>> entry : mClassifier.mFeatureDistributionMap.entrySet()) {
                objOut.writeUTF(entry.getKey());
                ObjectToCounterMap<String> map = entry.getValue();
                objOut.writeInt(map.size());
                for (Map.Entry<String,Counter> entry2 : map.entrySet()) {
                    objOut.writeUTF(entry2.getKey());
                    objOut.writeInt(entry2.getValue().intValue());
                }
            }
        }
        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {

            MultivariateEstimator estimator 
                = (MultivariateEstimator) objIn.readObject();
            @SuppressWarnings("unchecked")
            FeatureExtractor<F> featureExtractor
                = (FeatureExtractor<F>) objIn.readObject();
            double activationThreshold = objIn.readDouble();
            int featureSetSize = objIn.readInt();
            Set<String> featureSet = new HashSet<String>(2 * featureSetSize);
            for (int i = 0; i < featureSetSize; ++i)
                featureSet.add(objIn.readUTF());
            int featureDistributionMapSize = objIn.readInt();
            Map<String,ObjectToCounterMap<String>> featureDistributionMap
                = new HashMap<String,ObjectToCounterMap<String>>(2*featureDistributionMapSize);
            for (int i = 0; i < featureDistributionMapSize; ++i) {
                String key = objIn.readUTF();
                int mapSize = objIn.readInt();
                ObjectToCounterMap<String> otc = new ObjectToCounterMap<String>();
                featureDistributionMap.put(key,otc);
                for (int j = 0; j < mapSize; ++j) {
                    String key2 = objIn.readUTF();
                    int count = objIn.readInt();
                    otc.set(key2,count);
                }
            }
            return new BernoulliClassifier<F>(estimator,
                                              featureExtractor,
                                              activationThreshold,
                                              featureSet,
                                              featureDistributionMap);
        }
    }


}
