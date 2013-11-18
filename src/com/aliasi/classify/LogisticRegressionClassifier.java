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

import com.aliasi.corpus.Corpus;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.features.Features;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.LogisticRegression;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <code>LogisticRegressionClassifier</code> provides conditional
 * probability classifications of input objects using an underlying
 * logistic regression model and feature extractor.  Logistic
 * regression is a discrimitive classifier which operates over
 * arbitrary feature vectors extracted from items.  See {@link
 * LogisticRegression} for a full definition of logistic regression
 * and its implementation.
 *
 * <h4>Training</h4>
 *
 * <p>Logistic regression classifiers may be trained from a data
 * corpus using the method {@link
 * #train(Corpus,FeatureExtractor,int,boolean,RegressionPrior,AnnealingSchedule,double,int,int,Reporter)},
 * the last six arguments of which are shared with the logistic
 * regression training method {@link
 * LogisticRegression#estimate(Vector[],int[],RegressionPrior,AnnealingSchedule,Reporter,double,int,int)}.
 * The first three arguments are required to adapt logistic regression
 * to general classification, and consist of a feature extractor, a
 * corpus to train over, and a boolean flag indicating whether or not
 * to add an intercept feature to every input vector.
 *
 * <p>This class merely acts as an adapter to implement the {@link
 * ConditionalClassifier} interface based on the {@link LogisticRegression} class
 * in the statistics package.  The basis of the adaptation is a
 * general feature extractor, which is an instance of {@link
 * FeatureExtractor}.  A feature extractor converts an arbitrary input
 * object (whose type is specified generically in this class) to a
 * mapping from features (represented as strings) to values
 * (represented as instances of {@link Number}).  The class then uses
 * a symbol table for features to convert the maps from feature names
 * to numbers into sparse vectors, where the dimensions are the
 * identifiers for the features in the symbol table.  By convention,
 * if the intercept feature flag is set, it will set dimension 0 of
 * all inputs to 1.0.
 *
 * <h4>Serialization and Compilation</h4>
 *
 * <p>This class implements both {@link Serializable} and {@link
 * Compilable}, but both do the same thing and simply write the
 * content of the model to the object output.  The model read back in
 * will be an instance of {@link LogisticRegressionClassifier} with
 * the same components as the model that was serialized or compiled.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe3.5
 * @param <E> the type of object being classified
 */
public class LogisticRegressionClassifier<E>
    implements ConditionalClassifier<E>,
               Compilable, 
               Serializable {

    static final long serialVersionUID = -400005337034204553L;

    private final LogisticRegression mModel;
    private final FeatureExtractor<? super E> mFeatureExtractor;
    private final boolean mAddInterceptFeature;
    private final SymbolTable mFeatureSymbolTable;
    private final String[] mCategorySymbols;

    /**
     * Construct a logistic regression classifier using the specified
     * model, feature extractor, intercept flag, symbol table for features
     * and categories.

     * <p>The typical way to construct a logistic regression classifier
     * is through the training method
     * {@link #train(FeatureExtractor,Corpus,int,boolean,RegressionPrior,AnnealingSchedule,Reporter,double,int,int)},
     *
     * @param model Logistic regression model.
     * @param featureExtractor Feature extractor to convert input
     * objects to feature maps.
     * @param addInterceptFeature Flag set to <code>true</code> if the intercept
     * feature at dimension 0 should always be set to 1.0.
     * @param featureSymbolTable Symbol table for converting features to vector
     * dimensions.
     * @param categorySymbols List of outputs aligned with the model's categories.
     * @throws IllegalArgumentException If the number of outcomes in the model is
     * not the same as the length of the category symbols array, or if the
     * category symbols are not all unique.
     */
    LogisticRegressionClassifier(LogisticRegression model,
                                 FeatureExtractor<? super E> featureExtractor,
                                 boolean addInterceptFeature,
                                 SymbolTable featureSymbolTable,
                                 String[] categorySymbols) {
        if (model.numOutcomes() != categorySymbols.length) {
            String msg = "Number of model outcomes must match category symbols length."
                + " Found model.numOutcomes()=" + model.numOutcomes()
                + " categorySymbols.length=" + categorySymbols.length;
            throw new IllegalArgumentException(msg);
        }
        Set<String> categorySymbolSet = new HashSet<String>();
        for (int i = 0; i < categorySymbols.length; ++i) {
            if (!categorySymbolSet.add(categorySymbols[i])) {
                String msg = "Categories must be unique."
                    + " Found duplicate category categorySymbols[" + i + "]=" + categorySymbols[i];
                throw new IllegalArgumentException(msg);
            }
        }
        mModel = model;
        mFeatureExtractor = featureExtractor;
        mAddInterceptFeature = addInterceptFeature;
        mFeatureSymbolTable = featureSymbolTable;
        mCategorySymbols = categorySymbols;
    }

    /**
     * Returns an unmodifiable view of the symbol table used for
     * features in this classifier.
     *
     * @return The feature symbol table for this classifier.
     */
    public SymbolTable featureSymbolTable() {
        return MapSymbolTable.unmodifiableView(mFeatureSymbolTable);
    }

    /**
     * Returns a copy of the category symbols used by this classifier
     * in the same order as used by the underlying logistic regression
     * model.  Classifications that this class returns will use only
     * these symbols.
     *
     * @return The category symbols for this classifier.
     */
    public List<String> categorySymbols() {
        return Arrays.<String>asList(mCategorySymbols);
    }

    /**
     * Returns the logistic regression model underlying this
     * classifier.
     *
     * @return A copy of the model underlying this classifier.
     */
    public LogisticRegression model() {
        return mModel;
    }

    /**
     * Returns {@code true} if this classifier automatically adds
     * an intercept feature to each feature vector.
     *
     * @return Whether this classifier adds intercepts to feature
     * vectors.
     */
    public boolean addInterceptFeature() {
        return mAddInterceptFeature;
    }

    /**
     * Returns an immutable view of the feature extractor for this
     * classifier.
     *
     * <p><i>Warning:</i> If the feature extractor has side-effects
     * (as, for example, the caching feature extractor does), these
     * will be preserved by the returned result, which merely wraps
     * the contained feature extractor in an anonymous inner feature
     * extractor.
     *
     * @return The feature extractor for this classifier.
     */
    public FeatureExtractor<E> featureExtractor() {
        return new FeatureExtractor<E>() {
            public Map<String,? extends Number> features(E in) {
                return mFeatureExtractor.features(in);
            }
        };
    }

    /**
     * Returns the classification of the specified vector using the
     * logistic regression model underlying this classifier.  This
     * bypasses the conversion of an object to a feature map, and
     * the subsequent conversion of a feature map to a vector.
     *
     * @param v Vector to classify.
     * @return Conditional classification of the vector.
     */
    public ConditionalClassification classifyVector(Vector v) {
        double[] conditionalProbs = mModel.classify(v);
        @SuppressWarnings({"unchecked","rawtypes"})
        ScoredObject<String>[] sos
            = (ScoredObject<String>[]) new ScoredObject[conditionalProbs.length];
        for (int i = 0; i < conditionalProbs.length; ++i)
            sos[i] = new ScoredObject<String>(mCategorySymbols[i],
                                              conditionalProbs[i]);
        Arrays.sort(sos, ScoredObject.reverseComparator());
        String[] categories = new String[conditionalProbs.length];
        for (int i = 0; i < conditionalProbs.length; ++i) {
            categories[i] = sos[i].getObject().toString();
            conditionalProbs[i] = sos[i].score();
        }
        return new ConditionalClassification(categories, conditionalProbs);
    }

    /**
     * Return the conditional classification of a feature map using
     * this classifier.  This method bypasses the feature extraction
     * step of converting an object to a feature map, which is carried
     * out by the method {@link #classify(Object)} using the feature
     * symbol table {@link #featureSymbolTable()} and the flag {@link
     * #addInterceptFeature()}.
     *
     * @param featureMap the feature vector to classify.
     * @return The conditional classification of the feature vector.
     */
    public ConditionalClassification classifyFeatures(Map<String, ? extends Number> featureMap) {
        Vector v = Features.toVector(featureMap,
                                     mFeatureSymbolTable,
                                     mFeatureSymbolTable.numSymbols(),
                                     mAddInterceptFeature);
        return classifyVector(v);
    }

    /**
     * Return the conditional classification of the specified object
     * using logistic regression classification.  All categories will
     * have conditional probabilities in results.
     *
     * @param in Input object to classify.
     * @return The conditional classification of the object.
     */
    public ConditionalClassification classify(E in) {
        return classifyFeatures(mFeatureExtractor.features(in));
    }


    /**
     * Compile this classifier to the specified object output.  This
     * method is only for storage convenience; the classifier read
     * back in from the serialized object will be equivalent to this
     * one (but not in the <code>Object.equals()</code> sense).
     *
     * <p>Serializing this class produces exactly the same output.
     *
     * @param objOut Object output to which this classifier is
     * written.
     * @throws IOException If there is an underlying I/O error
     * writing the model to the stream.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer<E>(this));
    }

    private int categoryToId(String category) {
        for (int i = 0; i < mCategorySymbols.length; ++i)
            if (mCategorySymbols[i].equals(category))
                return i;
        return -1;
    }

    /**
     * Returns a mapping from features to their parameter values for
     * the specified category.  If the category is the last category,
     * which implicitly has zero values for all parameters, the map returned
     * by this method will also have zero values for all features.
     *
     * @param category Classification category.
     * @return The map from features to their parameter values for the
     * specified category.
     * @throws IllegalArgumentException If the category is unknown.
     */
    public ObjectToDoubleMap<String> featureValues(String category) {
        int categoryId = categoryToId(category);
        if (categoryId < 0) {
            String msg = "Unknown category=" + category;
            throw new IllegalArgumentException(msg);
        }
        ObjectToDoubleMap<String> result = new ObjectToDoubleMap<String>();
        if (categoryId == mCategorySymbols.length-1)
            return result;
        int numSymbols = mFeatureSymbolTable.numSymbols();
        Vector[] weightVectors = mModel.weightVectors();
        Vector weightVector = weightVectors[categoryId];
        for (int i = 0; i < numSymbols; ++i) {
            String symbol = mFeatureSymbolTable.idToSymbol(i);
            result.set(symbol,weightVector.value(i));
        }
        return result;
    }

    /**
     * Returns a string-based representation of this classifier,
     * listing the parameter vectors for each category.
     *
     * @return A string-based representation of this classifier.
     */
    @Override
    public String toString() {
        CharArrayWriter writer = new CharArrayWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        List<String> categorySymbols = categorySymbols();
        printWriter.println("NUMBER OF CATEGORIES=" + categorySymbols.size());
        printWriter.println("NUMBER OF FEATURES=" + mFeatureSymbolTable.numSymbols());
        for (int i = 0; i < categorySymbols.size()-1; ++i) {
            String category = categorySymbols.get(i);
            printWriter.println("\n  CATEGORY=" + category);
            ObjectToDoubleMap<String> parameterVector = featureValues(category);
            for (String feature : parameterVector.keysOrderedByValueList())
                printWriter.printf("%20s %15.6f\n",feature,parameterVector.get(feature));
        }
        printWriter.write('\n');
        return writer.toString();
    }


    private Object writeReplace() {
        return new Externalizer<E>(this);
    }


    /**
     * The name of the feature used for intercepts, {@code
     * *&^INTERCEPT%$^&**}.
     */
    public static final String INTERCEPT_FEATURE_NAME = "*&^INTERCEPT%$^&**";

    /**
     * Returns a trained logistic regression classifier given the specified
     * feature extractor, training corpus, model priors and search parameters.
     *
     * <p>Only the training section of the specified corpus is used for
     * training.
     *
     * <p>See the class documentation above and the class
     * documentation for {@link LogisticRegression} for more
     * information on the parameters.
     *
     * <p>The block size defaults to the corpus training
     * size divided by 50.
     *
     * @param corpus Corpus of training data.
     * @param featureExtractor Converter from objects to feature maps.
     * @param minFeatureCount Minimum count for features in corpus to
     * keep feature as part of model.
     * @param addInterceptFeature A flag set to <code>true</code> if
     * an intercept feature should be added to each input vector.
     * @param prior The prior for regularization of the regression.
     * @param annealingSchedule Class to compute learning rate for each epoch.
     * @param minImprovement Minimum relative improvement in error during
     * an epoch to stop search.
     * @param minEpochs Minimum number of search epochs.
     * @param maxEpochs Maximum number of epochs.
     * @param reporter Reporter to which progress reports are written,
     * or {@code null} for no reporting.
     * @throws IOException If there is an underlying I/O exception
     * reading the data from the corpus.
     * @param <F> the type of object to be classified
     */
    public static <F> LogisticRegressionClassifier<F>
        train(Corpus<ObjectHandler<Classified<F>>> corpus,
              FeatureExtractor<? super F> featureExtractor,
              int minFeatureCount,
              boolean addInterceptFeature,
              RegressionPrior prior,
              AnnealingSchedule annealingSchedule,
              double minImprovement,
              int minEpochs,
              int maxEpochs,
              Reporter reporter) throws IOException {

        int blockSize = -1;
        return train(corpus,
                     featureExtractor,
                     minFeatureCount,
                     addInterceptFeature,
                     prior,
                     
                     blockSize,
                     null, // hot start
                     annealingSchedule,
                     minImprovement,
                     5, // rolling average size
                     minEpochs,
                     maxEpochs,
                     null, // handler
                     reporter);
    }

    /**
     * Returns a trained logistic regression classifier given the specified
     * feature extractor, training corpus, model priors and search parameters.
     *
     * <p>Only the training section of the specified corpus is used for
     * training.
     *
     * <p>See the class documentation above and the class
     * documentation for {@link LogisticRegression} for more
     * information on the parameters.
     *
     * @param corpus Corpus of training data.
     * @param featureExtractor Converter from objects to feature maps.
     * @param minFeatureCount Minimum count for features in corpus to
     * keep feature as part of model.
     * @param addInterceptFeature A flag set to <code>true</code> if
     * an intercept feature should be added to each input vector.
     * @param prior The prior for regularization of the regression.
     * @param blockSize Number of examples whose probabilities are computed
     * before applying a gradient update.
     * @param hotStart Logistic regression classifier to use as initial
     * coefficient values for training.
     * @param annealingSchedule Class to compute learning rate for each epoch.
     * @param minImprovement Minimum relative improvement in error during
     * an epoch to stop search.
     * @param rollingAverageSize Number of epochs over which to
     * average objective improvement for monitoring convergence.
     * @param minEpochs Minimum number of search epochs.
     * @param maxEpochs Maximum number of epochs.
     * @param classifierHandler Handler for classifiers produced at each
     * epoch.
     * @param reporter Reporter to which progress reports are written,
     * or {@code null} for no reporting.
     * @throws IOException If there is an underlying I/O exception
     * reading the data from the corpus.
     * @param <F> the type of object to be classified
     */
    public static <F> LogisticRegressionClassifier<F>
        train(Corpus<ObjectHandler<Classified<F>>> corpus,
              FeatureExtractor<? super F> featureExtractor,
              int minFeatureCount,
              boolean addInterceptFeature,
              RegressionPrior prior,
              int blockSize,
              LogisticRegressionClassifier<F> hotStart,
              AnnealingSchedule annealingSchedule,
              double minImprovement,
              int rollingAverageSize,
              int minEpochs,
              int maxEpochs,
              ObjectHandler<LogisticRegressionClassifier<F>> classifierHandler,
              Reporter reporter) throws IOException {

        MapSymbolTable featureSymbolTable = new MapSymbolTable();
        MapSymbolTable categorySymbolTable = new MapSymbolTable();

        if (reporter == null)
            reporter = Reporters.silent();

        if (addInterceptFeature)
            featureSymbolTable.getOrAddSymbol(INTERCEPT_FEATURE_NAME);
        
        reporter.info("Feature Extractor class=" + featureExtractor.getClass());
        reporter.info("min feature count=" + minFeatureCount);

        reporter.info("Extracting Training Data");
        reporter.debug("  Counting features");
        ObjectToCounterMap<String> featureCounter = new ObjectToCounterMap<String>();
        corpus.visitTrain(new FeatureCounter<F>(featureExtractor,featureCounter));

        reporter.debug("  Pruning features");
        featureCounter.prune(minFeatureCount);
        for (String feature : featureCounter.keySet())
            featureSymbolTable.getOrAddSymbol(feature);

        reporter.debug("  Extracting vectors");
        DataExtractor<F> dataExtractor
            = new DataExtractor<F>(featureExtractor,
                                   featureSymbolTable,
                                   categorySymbolTable,
                                   addInterceptFeature,
                                   featureSymbolTable.numSymbols());
        corpus.visitTrain(dataExtractor);
        Vector[] inputs = dataExtractor.inputs();
        int[] categories = dataExtractor.categories();

        int numInputDimensions = inputs[0].numDimensions();

        String[] categorySymbols = new String[categorySymbolTable.numSymbols()];
        for (int i = 0; i < categorySymbols.length; ++i)
            categorySymbols[i] = categorySymbolTable.idToSymbol(i);


        LogisticRegression lrHotStart = null;
        if (hotStart != null) {
            reporter.debug("hot starting");
            // take vals from hot start for coeffs
            // that exist in new setup
            Set<String> hotStartCategorySet 
                = new HashSet<String>(hotStart.categorySymbols());
            Vector[] weightVectors = new Vector[categorySymbols.length-1];
            for (int k = 0; k < weightVectors.length; ++k) {
                weightVectors[k] = new DenseVector(numInputDimensions);
            }
            for (int k = 0; k < weightVectors.length - 1; ++k) {
                String category = categorySymbols[k];
                if (!hotStartCategorySet.contains(category))
                    continue;
                ObjectToDoubleMap<String> featureVector
                    = hotStart.featureValues(category);
                for (int i = 0; i < numInputDimensions; ++i) {
                    String feature = featureSymbolTable.idToSymbol(i);
                    double value = featureVector.getValue(feature);
                    weightVectors[k].setValue(i,value);
                }
            }
            lrHotStart = new LogisticRegression(weightVectors);
        }
        reporter.info(hotStart != null ? "Hot start" : "Cold start");

        ObjectHandler<LogisticRegression> regressionHandler
            = classifierHandler == null
            ? null
            : new RegressionHandlerAdapter<F>(classifierHandler,
                                              featureExtractor,
                                              addInterceptFeature,
                                              featureSymbolTable,
                                              categorySymbols);
        reporter.info((regressionHandler != null)
                      ? ("Regssion callback handler class=" + regressionHandler.getClass())
                      : "Regression callback handler=" + null);
        
        // may want to trap ArithmeticExceptions from estimate() here
        if (blockSize == -1)
            blockSize = Math.max(1,categories.length/50);

        LogisticRegression model
            = LogisticRegression.estimate(inputs,
                                          categories,
                                          prior,
                                          blockSize,
                                          lrHotStart,
                                          annealingSchedule,
                                          minImprovement,
                                          rollingAverageSize,
                                          minEpochs,
                                          maxEpochs,
                                          regressionHandler,
                                          reporter);

        return new LogisticRegressionClassifier<F>(model,
                                                   featureExtractor,
                                                   addInterceptFeature,
                                                   featureSymbolTable,
                                                   categorySymbols);


    }

    static class RegressionHandlerAdapter<F> implements ObjectHandler<LogisticRegression> {
        private final ObjectHandler<LogisticRegressionClassifier<F>> mClassifierHandler;
        private final FeatureExtractor<? super F> mFeatureExtractor;
        private final boolean mAddInterceptFeature;
        private final SymbolTable mFeatureSymbolTable;
        private final String[] mCategorySymbols;
        public RegressionHandlerAdapter(ObjectHandler<LogisticRegressionClassifier<F>> handler,
                                        FeatureExtractor<? super F> featureExtractor,
                                        boolean addInterceptFeature,
                                        SymbolTable featureSymbolTable,
                                        String[] categorySymbols) {
            mClassifierHandler = handler;
            mFeatureExtractor = featureExtractor;
            mAddInterceptFeature = addInterceptFeature;
            mFeatureSymbolTable = featureSymbolTable;
            mCategorySymbols = categorySymbols;
        }
        public void handle(LogisticRegression regressionModel) {
            mClassifierHandler.handle(new LogisticRegressionClassifier<F>(regressionModel,
                                                                          mFeatureExtractor,
                                                                          mAddInterceptFeature,
                                                                          mFeatureSymbolTable,
                                                                          mCategorySymbols));
        }
    }


    static class FeatureCounter<H> implements ObjectHandler<Classified<H>> {
        private final FeatureExtractor<? super H> mFeatureExtractor;
        private final ObjectToCounterMap<String> mFeatureCounter;
        FeatureCounter(FeatureExtractor<? super H> featureExtractor,
                       ObjectToCounterMap<String> featureCounter) {
            mFeatureExtractor = featureExtractor;
            mFeatureCounter = featureCounter;
        }
        public void handle(Classified<H> classified) { 
            H h = classified.getObject();
            Map<String,? extends Number> featureMap = mFeatureExtractor.features(h);
            for (String feature : featureMap.keySet()) {
                mFeatureCounter.increment(feature);
            }
        }
    }

    static class Externalizer<G> extends AbstractExternalizable {
        static final long serialVersionUID = -2003123148721825458L;
        final LogisticRegressionClassifier<G> mClassifier;
        public Externalizer() {
            this(null);
        }
        public Externalizer(LogisticRegressionClassifier<G> classifier) {
            mClassifier = classifier;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeObject(mClassifier.mModel);
            objOut.writeObject(mClassifier.mFeatureExtractor);
            objOut.writeBoolean(mClassifier.mAddInterceptFeature);
            objOut.writeObject(mClassifier.mFeatureSymbolTable);
            objOut.writeInt(mClassifier.mCategorySymbols.length);
            for (int i = 0; i < mClassifier.mCategorySymbols.length; ++i)
                objOut.writeUTF(mClassifier.mCategorySymbols[i]);
        }
        @SuppressWarnings("deprecation")
        @Override
        public Object read(ObjectInput objIn) throws IOException, ClassNotFoundException {
            LogisticRegression model = (LogisticRegression) objIn.readObject();

            // required for read object
            @SuppressWarnings("unchecked")
            FeatureExtractor<? super G> featureExtractor
                = (FeatureExtractor<? super G>) objIn.readObject();
            boolean addInterceptFeature = objIn.readBoolean();
            SymbolTable featureSymbolTable = (SymbolTable) objIn.readObject();
            int numSymbols = objIn.readInt();
            String[] categorySymbols = new String[numSymbols];
            for (int i = 0; i < categorySymbols.length; ++i)
                categorySymbols[i] = objIn.readUTF();
            return new LogisticRegressionClassifier<G>(model,
                                                       featureExtractor,
                                                       addInterceptFeature,
                                                       featureSymbolTable,
                                                       categorySymbols);
        }
    }


    static class DataExtractor<F> implements ObjectHandler<Classified<F>> {
        final FeatureExtractor<? super F> mFeatureExtractor;
        final SymbolTable mFeatureSymbolTable;
        final SymbolTable mCategorySymbolTable;
        final boolean mAddInterceptFeature;
        final int mNumSymbols;

        final List<Vector> mInputVectorList = new ArrayList<Vector>();
        final List<Integer> mOutputCategoryList = new ArrayList<Integer>();

        // if has intercept, already added
        DataExtractor(FeatureExtractor<? super F> featureExtractor,
                      SymbolTable featureSymbolTable,
                      SymbolTable categorySymbolTable,
                      boolean addInterceptFeature,
                      int numSymbols) {
            mFeatureExtractor = featureExtractor;
            mFeatureSymbolTable = featureSymbolTable;
            mCategorySymbolTable = categorySymbolTable;
            mAddInterceptFeature = addInterceptFeature;
            mNumSymbols = numSymbols;
        }
        public void handle(Classified<F> classified) { 
            F input = classified.getObject();
            Classification output = classified.getClassification();
            String outputCategoryName = output.bestCategory();
            Integer outputCategoryId = mCategorySymbolTable.getOrAddSymbol(outputCategoryName);
            Map<String,? extends Number> featureMap = mFeatureExtractor.features(input);
            Vector vector
                = Features
                .toVector(featureMap,
                          mFeatureSymbolTable,
                          mNumSymbols,
                          mAddInterceptFeature);
            mInputVectorList.add(vector);
            mOutputCategoryList.add(outputCategoryId);
        }
        int[] categories() {
            int[] inputs = new int[mOutputCategoryList.size()];
            for (int i = 0; i < inputs.length; ++i)
                inputs[i] = mOutputCategoryList.get(i).intValue();
            return inputs;
        }
        Vector[] inputs() {
            return mInputVectorList.<Vector>toArray(EMPTY_VECTOR_ARRAY);
        }

    }


    static final Vector[] EMPTY_VECTOR_ARRAY
        = new Vector[0];

}
