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

package com.aliasi.features;

import com.aliasi.classify.Classified;
import com.aliasi.classify.Classification;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.stats.OnlineNormalEstimator;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@code ZScoreFeatureExtractor} converts features to their
 * z-scores, where means and deviations are determined by a
 * corpus supplied at compile time.

 * <p>Means and standard deviations are computed for each feature
 * in the training section of the corpus supplied to the
 * constructor. 
 *
 * <p>At run time, feature values are converted to z-scores, by:
 *
 * <blockquote><pre>
 * z(feat,val) = (val - mean(feat))/stdDev(feat)</pre></blockquote>
 *
 * where {@code feat} is the feature, {@code val} is the value
 * to be converted to a z-score, {@code mean(feat)} is the mean
 * (average) of the feature in the training corpus, and 
 * {@code stdDev(feat)} is the standard deviation of the feature
 * in the training course.
 *
 * <p>Z-score normalization ensures that the collection of each
 * feature's values has zero mean and unit standard deviation
 * over the training section of the training corpus.  This does
 * not guarantee zero means and unit standard deviation over
 * the test section of the corpus.  
 *
 * <h4>Constant (Zero Deviation) Features</h4>
 * 
 * <p>If a feature is unseen or has zero standard deviation in the
 * training corpus, it is removed from all output.  A feature only has
 * zero standard deviation if it has the same value every time it
 * occurs.  For instance, all features seen only once will have zero
 * variance.  Effectively, features which always have the same value
 * in the training set will be eliminated from future consideration.
 *
 * <h4>Sparseness</h4>
 *
 * Applying a z-score transform to features destroys sparseness.
 * Undefined features implicitly have value zero, but the z-score
 * of 0 is non-zero if the mean of the feature values is non-zero.
 * 
 * <h4>Serialization</h4> 
 *
 * <p>A length-norm feature extractor is serializable if its
 * base feature extractor is serializable.
 * 
 * @author Mike Ross
 * @author Bob Carpenter
 * @version 4.0.0
 * @since Lingpipe3.8
 * @param <E> The type of object whose features are extracted.
 */
public class ZScoreFeatureExtractor<E> 
    extends FeatureExtractorFilter<E>
    implements Serializable {

    static final long serialVersionUID = -5628628145432035433L;

    final Map<String,MeanDev> mFeatureToMeanDev;
    
    ZScoreFeatureExtractor(FeatureExtractor<? super E> extractor,
                           Map<String,MeanDev> featureToMeanDev) {
        super(extractor);
        mFeatureToMeanDev = new LinkedHashMap<String,MeanDev>(featureToMeanDev);
    }

    /**
     * Construct a z-core feature extractor from the specified base
     * feature extractor and the training section of the supplied
     * corpus.
     *
     * @param extractor Base feature extractor.
     * @param corpus The corpus whose training section will be visited            
     * @throws IOException If there is an I/O error visting the corpus.
     */
    public ZScoreFeatureExtractor(Corpus<ObjectHandler<Classified<E>>> corpus,
                                  FeatureExtractor<? super E> extractor) 
        throws IOException {

        this(extractor,meanDevs(corpus,extractor));
    }

    /**
     * Return the feature map resulting from converting the feature
     * map produced by the underlying feature extractor to z-scores.
     * See the class documentation above for definition.
     *
     * @param in Input object.
     * @return Feature map for the input object.
     */
    public Map<String,? extends Number> features(E in) {
        Map<String,? extends Number> featureMap = super.features(in);
        Map<String,Double> result = new HashMap<String,Double>();
        for (Map.Entry<String,MeanDev> featMeanDev : mFeatureToMeanDev.entrySet()) {
            String feature = featMeanDev.getKey();
            MeanDev meanDev = featMeanDev.getValue();
            Number n = featureMap.get(feature);
            double val = meanDev.zScore(n == null 
                                        ? 0.0
                                        : featureMap.get(feature).doubleValue());
            result.put(feature,val);
        }
        return result;
    }

    /**
     * Return the z-score for the specified feature and value.
     * See the class documentation above for definitions.
     *
     * @param feature Feature name.
     * @param value Value of feature.
     * @return The z-score of the value for the specified feature.
     */
    public double zScore(String feature, double value) {
        MeanDev meanDev = mFeatureToMeanDev.get(feature);
        return meanDev == null
            ? null
            : meanDev.zScore(value);
    }
    

    /**
     * Returns the mean for the specified feature, or
     * {@code Double.NaN} if the feature is not known.
     *
     * @param feature Feature whose mean is returned.
     * @return Mean for the specified feature.
     */
    public double mean(String feature) {
        MeanDev meanDev = mFeatureToMeanDev.get(feature);
        return meanDev == null
            ? Double.NaN
            : meanDev.mMean;
    }

    /**
     * Returns the standard deviation for the specified feature, or
     * {@code Double.NaN} if the feature is not known.
     *
     * @param feature Feature whose standard deviation is returned.
     * @return Standard deviation for the specified feature.
     */
    public double standardDeviation(String feature) {
        MeanDev meanDev = mFeatureToMeanDev.get(feature);
        return meanDev == null
            ? Double.NaN
            : meanDev.mDev;
    }
    
    /**
     * Returns an unmodifiable view of the known features
     * for this z-score feature extractor.
     *
     * @return The set of known features for this extractor.
     */
    public Set<String> knownFeatures() {
        return Collections.unmodifiableSet(mFeatureToMeanDev.keySet());
    }
        

    /**
     * Returns a string representation of this z-score feature 
     * extractor, listing the mean and deviation for each
     * feature.  
     *
     * @return String representation of this extractor.
    */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,MeanDev> entry : mFeatureToMeanDev.entrySet()) {
            String feature = entry.getKey();
            MeanDev meanDev = entry.getValue();
            sb.append("|");
            sb.append(feature);
            sb.append("| ");
            sb.append(meanDev);
            sb.append('\n');
        }
        return sb.toString();
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static <F> Map<String,MeanDev> meanDevs(Corpus<ObjectHandler<Classified<F>>> corpus,
                                            final FeatureExtractor<? super F> extractor) 
        throws IOException {
        
        final Set<String> collectedFeatures = new HashSet<String>();
        corpus.visitTrain(new ObjectHandler<Classified<F>>() {
                              public void handle(Classified<F> classified) {
                                  collectedFeatures.addAll(extractor.features(classified.getObject()).keySet());
                              }
                          });
        
        final Map<String,OnlineNormalEstimator> featToEstimator 
            = new HashMap<String,OnlineNormalEstimator>();
        //For each entry ("in") of the corpus's training section...
        corpus.visitTrain(new ObjectHandler<Classified<F>>() {
                              public void handle(Classified<F> classified) { 
                                  F in = classified.getObject();
                                  //For each feature ("feat") of "in"...
                                  for (String feature : collectedFeatures) {
                                      Number value = extractor.features(in).get(feature);
                                      double v = value==null ? 0.0 : value.doubleValue();
                                      //Get or create an OnlineNormalEstimator for that feature
                                      OnlineNormalEstimator estimator = featToEstimator.get(feature);
                                      if (estimator == null) {
                                          estimator = new OnlineNormalEstimator();
                                          featToEstimator.put(feature,estimator);
                                      }
                                      //Send the feature's value to the estimator...
                                      estimator.handle(v);
                                  }
                              }
                          });
        Map<String,MeanDev> result = new HashMap<String,MeanDev>();
        for (Map.Entry<String,OnlineNormalEstimator> entry : featToEstimator.entrySet()) {
            String feat = entry.getKey();
            OnlineNormalEstimator estimator = entry.getValue();
            double mean = estimator.mean();
            double dev = estimator.standardDeviation();
            if (dev > 0.0)
                result.put(feat,new MeanDev(mean,dev));
        }
        return result;
    }

    static final class MeanDev {
        final double mMean;
        final double mDev;
        MeanDev(double mean, double dev) {
            mMean = mean;
            mDev = dev;
        }
        double zScore(double x) {
            return (x - mMean)/mDev;
        }
        public String toString() {
            return "mean=" + mMean + " dev=" + mDev;
        }
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 6365515337527915147L;
        private final ZScoreFeatureExtractor<F> mFilter;
        public Serializer() {
            this(null);
        }
        public Serializer(ZScoreFeatureExtractor<F> filter) {
            mFilter = filter;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mFilter.baseExtractor());
            out.writeInt(mFilter.mFeatureToMeanDev.size());
            for (Map.Entry<String,MeanDev> entry : mFilter.mFeatureToMeanDev.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeDouble(entry.getValue().mMean);
                out.writeDouble(entry.getValue().mDev);
            }
        }

        @Override
        public Object read(ObjectInput in) throws IOException,
                                                  ClassNotFoundException {
            // required for deserialization
            @SuppressWarnings("unchecked")
            FeatureExtractor<? super F> extractor = (FeatureExtractor<? super F>) in.readObject();
            int numFeats = in.readInt();
            Map<String,MeanDev> featureToMeanDev = new HashMap<String,MeanDev>((3 * numFeats)/2);
            for (int i = 0; i < numFeats; ++i) {
                String feature = in.readUTF();
                double mean = in.readDouble();
                double dev = in.readDouble();
                featureToMeanDev.put(feature,new MeanDev(mean,dev));
            }
            return new ZScoreFeatureExtractor<F>(extractor,featureToMeanDev);
        }
    }
}
