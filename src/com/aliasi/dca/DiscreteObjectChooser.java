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

package com.aliasi.dca;

import com.aliasi.features.Features;

import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.Vector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.List;
import java.util.Map;

/**
 * A {@code DiscreteObjectChooser} provides an implementation of
 * discrete choice analysis (DCA) over arbitrary objects using a
 * feature extractor.
 *
 * <p>The feature extractor is used to map objects to feature
 * maps, and a feature symbol table is used to convert these
 * feature maps into vectors which may be fed into the contained
 * discrete chooser.
 *
 * <h3>Estimation</h3>
 * 
 * Estimation is carried out the same way as for basic choosers, with
 * the intermediate step of feature extraction over the basic
 * corpus of object choices.
 *
 * <h3>Serialization</h3>
 *
 * A discrete object chooser may be serialized if its feature
 * extractor and feature symbol table are serializable.  
 *
 * <p>Discrete object choosers created by estimation are
 * serializable.
 * 
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 * @param E Type of objects being chosen.
 */
public class DiscreteObjectChooser<E>
    implements Serializable {

    static final long serialVersionUID = 3178131664571660923L;

    private final FeatureExtractor<E> mFeatureExtractor;
    private final SymbolTable mFeatureSymbolTable;
    private final DiscreteChooser mChooser;

    /**
     * Construct a discrete chooser based on the specified feature
     * extractor, feature symbol table, and base chooser.
     */
    public DiscreteObjectChooser(FeatureExtractor<E> featureExtractor,
                                 SymbolTable featureSymbolTable,
                                 DiscreteChooser chooser) {
        mFeatureExtractor = featureExtractor;
        mFeatureSymbolTable = featureSymbolTable;
        mChooser = chooser;
    }

    /**
     * Return the feature extractor for this chooser.
     *
     * @return This chooser's feature extractor.
     */
    public FeatureExtractor<E> featureExtractor() {
        return mFeatureExtractor;
    }

    /**
     * Returns an unmodifiable view of the feature symbol
     * table underlying this chooser.
     *
     * @return The feature symbol table for this chooser.
     */
    public SymbolTable featureSymbolTable() {
        return MapSymbolTable.unmodifiableView(mFeatureSymbolTable);
    }

    /**
     * Returns the discrete chooser on which this object chooser is
     * based.
     */
    public DiscreteChooser chooser() {
        return mChooser;
    }

    /**
     *
     * <p>Because intercepts are ignored, they are not added to
     * feature maps and should not be treated as noninformative in the
     * prior.
     */
    public static <F> DiscreteObjectChooser<F> 
        estimate(FeatureExtractor<F> featureExtractor,
                 List<List<F>> alternativeObjectss,
                 int[] choices,
                 int minFeatureCount,
                 RegressionPrior prior,
                 int priorBlockSize,
                 AnnealingSchedule annealingSchedule,
                 double minImprovement,
                 int minEpochs,
                 int maxEpochs,
                 Reporter reporter) {
        
        if (reporter == null)
            reporter = Reporters.silent();
        ObjectToCounterMap<String> featureCounter
            = new ObjectToCounterMap<String>();
        for (List<F> alternativeObjects : alternativeObjectss) {
            for (F alternativeObject : alternativeObjects) {
                Map<String,? extends Number> featureMap
                    = featureExtractor.features(alternativeObject);
                for (String feature : featureMap.keySet())
                    featureCounter.increment(feature);
            }
        }
        featureCounter.prune(minFeatureCount);
        MapSymbolTable featureSymbolTable = new MapSymbolTable();
        for (String feature : featureCounter.keySet())
            featureSymbolTable.getOrAddSymbol(feature);
        int numDimensions = featureSymbolTable.numSymbols();

        Vector[][] alternativess = new Vector[alternativeObjectss.size()][];
        for (int i = 0; i < alternativess.length; ++i) {
            List<F> alternativeObjects = alternativeObjectss.get(i);
            alternativess[i] = new Vector[alternativeObjects.size()];
            for (int k = 0; k < alternativess[i].length; ++k) {
                Map<String,? extends Number> featureMap
                    = featureExtractor.features(alternativeObjects.get(k));
                alternativess[i][k] 
                    = Features.toVectorAddSymbols(featureMap,featureSymbolTable,numDimensions,
                                                  ADD_INTERCEPT_FALSE);
            }
        }
        DiscreteChooser chooser
            = DiscreteChooser.estimate(alternativess,
                                       choices,
                                       prior,
                                       priorBlockSize,
                                       annealingSchedule,
                                       minImprovement,
                                       minEpochs,
                                       maxEpochs,
                                       reporter);
        return new DiscreteObjectChooser<F>(featureExtractor,
                                            featureSymbolTable,
                                            chooser);
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static final boolean ADD_INTERCEPT_FALSE = false;

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 4420046415835317661L;
        final DiscreteObjectChooser<F> mObjectChooser;
        public Serializer() {
            this(null);
        }
        public Serializer(DiscreteObjectChooser<F> objectChooser) {
            mObjectChooser = objectChooser;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mObjectChooser.mFeatureExtractor);
            out.writeObject(mObjectChooser.mFeatureSymbolTable);
            out.writeObject(mObjectChooser.mChooser);
        }
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked") // required for deser
            FeatureExtractor<F> featureExtractor
                = (FeatureExtractor<F>) in.readObject();
            @SuppressWarnings("unchecked") // required for deser
            SymbolTable featureSymbolTable
                = (SymbolTable) in.readObject();
            @SuppressWarnings("unchecked") // required for deser
            DiscreteChooser chooser
                = (DiscreteChooser) in.readObject();
            return new DiscreteObjectChooser<F>(featureExtractor,featureSymbolTable,chooser);
        }
    }


}