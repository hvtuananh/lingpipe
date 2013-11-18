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

import com.aliasi.matrix.KernelFunction;
import com.aliasi.matrix.Vector;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Arrays;
import com.aliasi.util.Compilable;
import com.aliasi.util.FeatureExtractor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * A <code>PerceptronClassifier</code> implements a binary classifier
 * based on an averaged kernel-based perceptron.  These
 * classifiers are large margin (discriminitive) linear classifiers in
 * a feature space expanded by a plug-and-play kernel implemeting
 * {@link KernelFunction}.
 *
 * <p>A perceptron classifier may be applied to any type of object.
 * An appropriately typed {@link FeatureExtractor} is used to map
 * these objects to feature vectors for use in the perceptron
 * classifier.
 *
 * <h3>Corpus Training</h3>
 *
 * <p>Unlike the language-model-based classifiers, for which training,
 * classification and compilation may be interleaved, averaged
 * perceptron-based classifiers require batch training.  This requires
 * the entire training corpus to be available in one shot.  In
 * particular, training will iterate over a fixed corpus multiple
 * times before producing a completely trained classifier.
 *
 * <p>The constructor will do the training using the supplied instance of
 * {@link Corpus}.  The constructor will store the entire corpus in
 * memory in the form of {@link com.aliasi.matrix.SparseFloatVector} and boolean
 * polarities for the accept/reject decision.  The corpus will only be
 * held locally in the constructor; it is available for garbage
 * collection, as are all intermediate results, as soon as the
 * constructor is done training.
 *
 * <h3>Kernel Function</h3>
 *
 * <p>The basic (non-kernel) perceptron is equivalent to using the
 * kernel function {@link com.aliasi.matrix.DotProductKernel}.  A good
 * choice for most classification tasks is the polynomial kernel,
 * implemented in {@link com.aliasi.matrix.PolynomialKernel}.
 * Usually, higher polynomial kernel degrees perform dramatically
 * better than dot products.  3 is a good general starting degree, as
 * sometimes performance degrades in higher kernel degrees.  In
 * some cases, the Gaussian radial basis kernel implemented in
 * {@link com.aliasi.matrix.GaussianRadialBasisKernel} works well.
 *
 * <p>If the kernel function is neither serializable nor compilable,
 * then the resulting perceptron classifier will not be serializable.
 *
 * <h3>Training Iterations</h3>
 *
 * <p>More training iterations are usually better for accuracy.  As
 * more basis vectors are added to the perceptron, more memory is
 * needed for the model and more time is needed for classification.
 * Typically, the amount of memory required at run time will
 * stabilize after a few training iterations.
 *
 * <h3>Memory Usage: Training and Compiled</h3>
 *
 * <p>The memory usage of a perceptron classifier may be very high.
 * The perceptron must store every feature vector in the input on
 * which the classifier being trained made a mistake in some
 * iteration.  During training, every feature vector in the input
 * corpus must be stored.  These are stored in memory as instances of
 * {@link com.aliasi.matrix.SparseFloatVector}.
 *
 * <p>If the data are linearly separable in the kernel space,
 * the training process will converge to the point where no additional
 * basis feature vectors are needed and the result will converge to
 * using the single final perceptron (which requires all the intermediate
 * to be stored given the demands of a non-linear kernel calculation).
 *
 * <h3>Serialization</h3>
 *
 * <p>After a perceptron classifier is constructed, it may be
 * serialized to an object output stream.  If the underlying
 * feature extractor is compilable, it's compiled, but if
 * it's not compilable, it's serialized.  To be serializable,
 * a perceptron classifier requires both its feature extractor
 * and kernel function to be {@link Serializable}.
 *
 * <p>The object read back in after serialization will
 * be an instance of <code>PerceptronClassifier</code>.
 *
 * <h3>About Averaged Kernel Perceptrons</h3>
 * <p><a
 * href="http://en.wikipedia.org/wiki/Perceptron">Perceptrons</a>
 * are a kind of large-margin linear classifier.  The
 * polynomial <a
 * href="http://en.wikipedia.org/wiki/Kernel_trick">kernel trick</a>
 * is used to embed the basic feature vector in a higher-degree vector space
 * in which the data are more separable.
 *
 * <p>An average of all of the perceptrons created during training is
 * used for the final prediction. The factored formulation of the
 * algorithm allows the perceptrons to be expressed as linearly
 * weighted training samples.
 *
 * <p>Although theoretical bounds are almost equivalent, in practice
 * averaged perceptrons slightly underperform <a
 * href="http://en.wikipedia.org/wiki/Support_vector_machine">support
 * vector machine</a> (SVM) learners over the same polynomial kernels.
 * The advantage of perceptrons is that they are much more efficient
 * in time and slightly more efficient in space in practice.
 *
 * <h3>Averaged Perceptron Model with Polynomial Kernel</h3>
 *
 * <p>The model used for runtime predictions by the averaged
 * perceptron is quite straightforward, consisting of a set of
 * weighted feature vectors (represented as parallel arrays of basis
 * vectors and weights) and a kernel degree:
 *
 * <blockquote><pre>
 * Vector[] basisVectors;
 * int[] weights;
 * int degree;</pre></blockquote>
 *
 * The basis vectors are all vectors derived from single training
 * examples by the specified feature extractor.  The weights may
 * be positive or negative and represent the cumulative voted
 * weight of the specified basis vector.
 *
 * <p>The kernel function computes a distance
 * <code>kernel(v1,v2)</code> between vectors <code>v1</code> and
 * <code>v2</code> in an enhanced feature space defined by the
 * particular kernel employed.
 *
 * <p>A new input to classify is first converted to a feature
 * vector by the feature extractor.  Classification is then
 * based on the sign of the following score:
 *
 * <blockquote><pre>
 * score(Vector v) = <big><big><big>&Sigma;</big></big></big><sub><sub>i</sub></sub> weights[i] * kernel(basisVectors[i],v)</pre></blockquote>
 *
 * An example is accepted if the score of its feature vector is
 * greater than zero and rejected otherwise.
 *
 * <h3>Estimating the Perceptron Model</h3>
 *
 * To estimate the perceptron model, we will assume that we have a
 * training corpus consisting of an array of vectors with boolean
 * polarities indicating whether they are positive (to accept) or
 * negative (to reject) examples.  We also assume we have a fixed
 * kernel function.  The training method iterates over the corpus a
 * specified number of times.
 *
 * <blockquote><pre>
 * Vector[] basisVectors;
 * int[] incrementalWeights;
 * boolean[] polarities;
 * int degree;
 * int index = -1;
 * for (# iterations)
 *     for (vector,polarity) in training corpus
 *         yHat = scoreIntermediate(vector);
 *         if (yHat &gt 0 &amp;&amp; polarity) || (yHat &lt; 0 &amp;&amp; !polarity)
 *             ++incrementalWeights[index];
 *         else
 *             ++index;
 *             basisVectors[index] = vector;
 *             polarities[index] = polarity;
 *             incrementalWeights[index] = 1;</pre></blockquote>
 *
 * <blockquote><pre>
 * scoreIntermediate(vector)
 *   = <big><big><big>&Sigma;</big></big></big><sub><sub>i &lt;= index</sub></sub> polarities[i] * kernel(basisVectors[i],vector)
 * </pre></blockquote>
 *
 * The final weight for a vector is the cumulative weight
 * computed as follows:
 *
 * <blockquote><pre>
 * cumulativeWeight(j) = <big><big><big>&Sigma;</big></big></big><sub><sub>k &gt;= j</sub></sub> incrementalWeights[k]
 * </pre></blockquote>
 *
 * The actual implementations of these methods involve
 * considerably more indirection and index chasing to avoid
 * copies and duplication in the final vectors.
 *
 * <h3>Historical Notes</h3>
 *
 * <p>The averaged kernel perceptron implemented here was introduced
 * in the following paper, which also provides error bounds
 * for learning and evaluations with polynomial kernels of various
 * degrees:
 *
 * <blockquote>
 * Freund, Yoav and Robert E. Schapire (1999)
 * Large margin classification using the perceptron algorithm.
 * <i>Machine Learning</i> <b>37</b>(3):277-296.
 * </blockquote>
 *
 * The basic perceptron model was introduced in:
 *
 * <blockquote>
 * Block, H.D. (1962) The perceptron: a model for brain functioning.
 * <i>Reviews of Modern Physics</i> <b>34</b>:123-135.
 * </blockquote>

 * <p>The kernel-based perceptron was introduced in:
 *
 * <blockquote>
 * Aizerman, M.A., E.M. Braverman, and L.I. Rozonoer.  1964.
 * Theoretical foundations of the potential function method in pattern
 * recognition learning.  <i>Automation and Remote Control</i>.
 * <b>25</b>:821-837.
 * </blockquote>
 *
 * The basis of the voting scheme is a deterministically averaged
 * version of the randomized approach of adapting online learners
 * to a batch setup described in the following paper:
 *
 * <blockquote>
 * Helmbold, D.P. and M.K. Warmuth.  (1995)
 * On weak learning.  <i>Journal of Computer and System Sciences</i>
 * <b>50</b>:551-573.
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe3.1
 * @param <E> the type of object being classified
 */
public class PerceptronClassifier<E>
    implements ScoredClassifier<E>,
               Serializable {

    static final long serialVersionUID = 8752291174601085455L;

    final FeatureExtractor<? super E> mFeatureExtractor;
    final MapSymbolTable mSymbolTable;
    final KernelFunction mKernelFunction;

    final Vector[] mBasisVectors;
    final int[] mBasisWeights;

    final String mAcceptCategory;
    final String mRejectCategory;


    PerceptronClassifier(FeatureExtractor<? super E> featureExtractor,
                         KernelFunction kernelFunction,
                         MapSymbolTable symbolTable,
                         Vector[] basisVectors,
                         int[] basisWeights,
                         String acceptCategory,
                         String rejectCategory) {

        mFeatureExtractor = featureExtractor;

        mKernelFunction = kernelFunction;
        mBasisVectors = basisVectors;
        mBasisWeights = basisWeights;

        mAcceptCategory = acceptCategory;
        mRejectCategory = rejectCategory;

        mSymbolTable = symbolTable;
    }

    /**
     * Construct a perceptron classifier from the specified feature extractor,
     * corpus with designated accept category, polynomial kernel degree and
     * number of training iterations, and output accept and reject categories.
     *
     * @param corpus Corpus to use for training.
     * @param featureExtractor Feature extractor for objects.
     * @param corpusAcceptCategory Category in training data to treat as positive.
     * @param kernelFunction Kernel function for expanding vector basis.
     * @param numIterations Number of iterations to carry out during training.
     * @param outputAcceptCategory Category with which to label accepted instances.
     * @param outputRejectCategory Category with which to label rejected instances.
     */
    public PerceptronClassifier(Corpus<ObjectHandler<Classified<E>>> corpus,
                                FeatureExtractor<? super E> featureExtractor,
                                KernelFunction kernelFunction,
                                String corpusAcceptCategory,
                                int numIterations,
                                String outputAcceptCategory,
                                String outputRejectCategory)
        throws IOException {

        mFeatureExtractor = featureExtractor;
        mKernelFunction = kernelFunction;
        mAcceptCategory = outputAcceptCategory;
        mRejectCategory = outputRejectCategory;

        mSymbolTable = new MapSymbolTable();

        // collect training vectors and categories
        CorpusCollector collector = new CorpusCollector();
        corpus.visitCorpus(collector);
        Vector[] featureVectors = collector.featureVectors();
        boolean[] polarities = collector.polarities();
        corpus = null; // don't need it any more

        // initialize perceptrons
        int currentPerceptronIndex = -1;  // no initial zero perceptron
        int[] weights = new int[INITIAL_BASIS_SIZE];
        int[] basisIndexes = new int[INITIAL_BASIS_SIZE];

        for (int iteration = 0; iteration < numIterations; ++iteration) {
            // System.out.println("\n\nIteration=" + iteration);
            for (int i = 0; i < featureVectors.length; ++i) {
                double yHat = prediction(featureVectors[i],
                                         featureVectors,
                                         polarities,
                                         weights,
                                         basisIndexes,
                                         currentPerceptronIndex);
                boolean accept = yHat > 0.0;
                //System.out.println("      yHat=" + yHat
                // + " accept=" + accept
                // + " for vect=" + featureVectors[i]);
                if (accept == polarities[i]) {
                    // System.out.println("       correct");
                    if (currentPerceptronIndex >= 0) // avoid incrementing zero
                        ++weights[currentPerceptronIndex];
                } else {
                    // System.out.println("       incorrect");
                    ++currentPerceptronIndex;
                    if (currentPerceptronIndex >= weights.length) {
                        weights = Arrays.reallocate(weights);
                        basisIndexes = Arrays.reallocate(basisIndexes);
                    }
                    basisIndexes[currentPerceptronIndex] = i;
                    weights[currentPerceptronIndex] = 1;
                }
            }
        }

        // renumber indexes to pack only necessary basis vectors
        Map<Integer,Integer> renumbering = new HashMap<Integer,Integer>();
        int next = 0;
        for (int i = 0; i <= currentPerceptronIndex; ++i)
            if (!renumbering.containsKey(basisIndexes[i]))
                renumbering.put(basisIndexes[i],next++);

        // compute basis vectors and cumulative weight for avg
        mBasisVectors = new Vector[renumbering.size()];
        mBasisWeights = new int[renumbering.size()];
        int weightSum = 0;
        for (int i = currentPerceptronIndex+1; --i >= 0; ) {
            int oldIndex = basisIndexes[i];
            int newIndex = renumbering.get(oldIndex);
            mBasisVectors[newIndex] = featureVectors[oldIndex];
            weightSum += weights[i];
            if (polarities[i])
                mBasisWeights[newIndex] += weightSum;
            else
                mBasisWeights[newIndex] -= weightSum;
        }
    }

    /**
     * Returns the kernel function for this perceptron.
     *
     * @return The kernel function for this perceptron.
     */
    public KernelFunction kernelFunction() {
        return mKernelFunction;
    }


    /**
     * Returns the feature extractor for this perceptron.
     *
     * @return The feature extractor for this perceptron.
     */
    public FeatureExtractor<? super E> featureExtractor() {
        return mFeatureExtractor;
    }

    /**
     * Returns a string-based representation of this perceptron.
     * This may be long, as it outputs every basis vector and weight.
     *
     * @return A string-based representation of this perceptron.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Averaged Perceptron");
        sb.append("  Kernel Function=" + mKernelFunction + "\n");
        for (int i = 0; i < mBasisVectors.length; ++i)
            sb.append("  idx=" + i + " "
                      + "vec=" + mBasisVectors[i]
                      + " wgt=" + mBasisWeights[i]
                      + "\n");
        return sb.toString();
    }


    /**
     * Return the scored classification for the specified input.  The
     * input is first converted to a feature vector using the feature
     * extractor, then scored against the perceptron.  The resulting
     * score for the accept category is the perceptron score, and
     * the resulting score for the reject category is the negative
     * perceptron score.
     *
     * @param in The element to be classified.
     * @return The scored classification for the specified element.
     */
    public ScoredClassification classify(E in) {
        Map<String,? extends Number> featureVector = mFeatureExtractor.features(in);
        Vector inputVector = Features.toVector(featureVector,mSymbolTable,Integer.MAX_VALUE,false);
        double sum = 0.0;
        for (int i = mBasisVectors.length; --i >= 0; )
            sum += mBasisWeights[i] * mKernelFunction.proximity(mBasisVectors[i],
                                                               inputVector);
        return sum > 0
            ? new ScoredClassification(new String[] { mAcceptCategory,
                                                      mRejectCategory },
                                       new double[] { sum, -sum })
            : new ScoredClassification(new String[] { mRejectCategory,
                                                      mAcceptCategory },
                                       new double[] { -sum, sum });
    }

    double prediction(Vector inputVector,
                      Vector[] featureVectors,
                      boolean[] polarities,
                      int[] ignoreMyWeights,
                      int[] basisIndexes,
                      int currentPerceptronIndex) {
        double sum = 0.0;
        // int weightSum = 0;
        int weightSum = 1;
        for (int i = currentPerceptronIndex; i >= 0; --i) {
            // weightSum += weights[i];
            int index = basisIndexes[i];
            double kernel = mKernelFunction.proximity(inputVector,featureVectors[index]);
            double total = (polarities[i] ? weightSum : -weightSum) * kernel;
            sum += total;
        }
        return sum;
    }

    static double power(double base, int exponent) {
        switch (exponent) {
        case 0:
            return 1.0;
        case 1:
            return base;
        case 2:
            return base * base;
        case 3:
            return base * base * base;
        case 4:
            return base * base * base * base;
        default:
            return java.lang.Math.pow(base,exponent);
        }
    }

    private Object writeReplace() {
        return new Externalizer<E>(this);
    }


    class CorpusCollector
        implements ObjectHandler<Classified<E>> {
        
        final List<Vector> mInputFeatureVectorList
            = new ArrayList<Vector>();
        final List<Boolean> mInputAcceptList
            = new ArrayList<Boolean>();

        public void handle(Classified<E> classified) {
            E object = classified.getObject();
            Classification c = classified.getClassification();
            Map<String,? extends Number> featureMap = mFeatureExtractor.features(object);
            mInputFeatureVectorList.add(Features.toVectorAddSymbols(featureMap,mSymbolTable,Integer.MAX_VALUE,false));
            mInputAcceptList.add(mAcceptCategory.equals(c.bestCategory())
                                 ? Boolean.TRUE
                                 : Boolean.FALSE);
        }
        Vector[] featureVectors() {
            return mInputFeatureVectorList.toArray(EMPTY_SPARSE_FLOAT_VECTOR_ARRAY);
        }
        boolean[] polarities() {
            boolean[] categories = new boolean[mInputAcceptList.size()];
            for (int i = 0; i < categories.length; ++i)
                categories[i] = mInputAcceptList.get(i).booleanValue();
            return categories;
        }
    }

    static final Vector[] EMPTY_SPARSE_FLOAT_VECTOR_ARRAY
        = new Vector[0];


    static class Externalizer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -1901362811305741506L;
        final PerceptronClassifier<F> mClassifier;
        public Externalizer() {
            this(null);
        }
        public Externalizer(PerceptronClassifier<F> classifier) {
            mClassifier = classifier;
        }
        @Override
        @SuppressWarnings("deprecation")
        public Object read(ObjectInput in) throws ClassNotFoundException, IOException {

            // required for read object
            @SuppressWarnings("unchecked")
            FeatureExtractor<? super F> featureExtractor
                = (FeatureExtractor<? super F>) in.readObject();

            KernelFunction kernelFunction
                = (KernelFunction) in.readObject();

            MapSymbolTable symbolTable = (MapSymbolTable) in.readObject();

            int basisLen = in.readInt();
            Vector[] basisVectors = new Vector[basisLen];
            for (int i = 0; i < basisLen; ++i)
                basisVectors[i] = (Vector) in.readObject();

            int[] basisWeights = new int[basisLen];
            for (int i = 0; i < basisLen; ++i)
                basisWeights[i] = in.readInt();

            String acceptCategory = in.readUTF();
            String rejectCategory = in.readUTF();

            return new PerceptronClassifier<F>(featureExtractor,
                                               kernelFunction,
                                               symbolTable,
                                               basisVectors,
                                               basisWeights,
                                               acceptCategory,
                                               rejectCategory);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            AbstractExternalizable.compileOrSerialize(mClassifier.mFeatureExtractor,out);
            AbstractExternalizable.compileOrSerialize(mClassifier.mKernelFunction,out);

            // symbol table
            out.writeObject(mClassifier.mSymbolTable);


            // basis length
            out.writeInt(mClassifier.mBasisVectors.length);

            // basis vectors
            for (int i = 0; i < mClassifier.mBasisVectors.length; ++i)
                out.writeObject(mClassifier.mBasisVectors[i]);

            // basis weights
            for (int i = 0; i < mClassifier.mBasisWeights.length; ++i)
                out.writeInt(mClassifier.mBasisWeights[i]);

            // accept, reject cats
            out.writeUTF(mClassifier.mAcceptCategory);
            out.writeUTF(mClassifier.mRejectCategory);

         }
    }


    static final int INITIAL_BASIS_SIZE = 32*1024;  // 32K * 8B = 240KB initially

}
