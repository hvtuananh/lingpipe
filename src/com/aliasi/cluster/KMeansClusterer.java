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

package com.aliasi.cluster;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.Statistics;

import com.aliasi.symbol.MapSymbolTable;

// import com.aliasi.util.Arrays;
import com.aliasi.util.Distance;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.SmallSet;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * A <code>KMeansClusterer</code> provides an implementation of
 * k-means(++) clustering based on vectors constructed by feature
 * extractors.  An instance fixes a specific value of <code>K</code>, the
 * number of clusters returned.  Initialization may be either by
 * the traditional k-means random assignment, or by the k-means++
 * initialization strategy. 
 *
 * <p>This clustering class is defined so as to be able to cluster
 * arbitrary objects.  These objects are converted to (sparse) vectors
 * using a feature extractor specified during construction.
 *
 * <p><b>Feature Parsing to Cluster Objects</b>
 *
 * <p>The elements being clustered are first converted into feature
 * d-dimensional vectors using a feature extractor.  These feature
 * vectors are then evenly distributed among the clusters using random
 * assignment.  Feature extractors may normalize their input in any
 * number of ways.  Run time is dominated by the density of the object
 * vectors.
 *
 * <p><b>Centroids</b>
 * 
 * <p>In k-means, each cluster is modeled by the centroid of the
 * feature vectors assigned to it.  The centroid of a set of points is
 * just the mean of the points, computed by dimension:
 *
 * <blockquote><pre>
 * centroid({v[0],...,v[n-1]}) = (v[0] + ... + v[n-1]) / n</blockquote></pre>
 *
 * Thus centroids are thus located in the same vector space
 * as the objects, namely they are d-dimensional vectors.
 * The code represents centroids as dense vectors, and objects
 * as sparse vectors.
 *
 * <p><b>Euclidean Distance</b>
 * 
 * <p>Feature vectors are always compared to cluster centroids
 * using squared Euclidean distance, defined by:
 *
 * <blockquote><pre>
 * distance(x,y)<sup>2</sup> =(x - y) * (x - y)
 *                = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub> (x[i] - y[i])<sup>2</sup>)</pre></blockquote>
 *
 * The centroid of a set of points is the point that minimizes the sum
 * of square distances from the points in that set to the set.
 *
 * <p><b>K-means</b>
 * 
 * <p>The k-means algorithm then iteratively improves cluster
 * assignments.  Each epoch consists of two stages, reassignment
 * and recomputing the means.
 *
 * <p><i>Cluster Assignment:</i> &nbsp; In each epoch, we assign each object
 * to the cluster represented by the closest centroid.  Ties go to the
 * lowest indexed element.
 *
 * <p><i>Mean Recomputation:</i> &nbsp; At the end of each epoch, the
 * centroids are then recomputed as the means of points assigned to
 * the cluster.
 *
 * <p><i>Convergence:</i> &nbsp; If no objects change cluster during an
 * iteration, the algorithm has converged and the results will be returned.
 * We also consider the algorithm converged if the relative reduction
 * in error from epoch to epoch falls below a set threshold.  Also, the
 * algorithm will return if the maximum number of epochs have been reached.
 *
 *
 * <p><b>K-means as Minimization</b>
 *
 * <p>K-means clustering may be viewed as an iterative approach to the
 * minimization of the average sauare distance between items and their
 * cluster centers, which is:
 *
 * <blockquote><pre>
 * Err(cs) = <big><big><big>&Sigma;</big></big></big><sub>c in cs</sub> <big><big><big>&Sigma;</big></big></big><sub>x in c</sub> distance(x,centroid(x))<sup>2</sup></pre></blockquote>
 *
 * where <code>cs</code> is the set of clusters and <code>centroid(x)</code>
 * is the centroid (mean or average) of the cluster containing x.
 *
 * <p><b>Convergence Guarantees</b>
 *
 * <p>K-means clustering is guaranteed to converge to a local mimium
 * of error because both steps of K-means reduce error.  First,
 * assigning each object to its closest centroid minimizes error given
 * the centroids.  Second, recalculating centroids as the means of
 * elements assigned to them minimizes errors given the clustering.
 * Given that error is bounded at zero, and changes are discrete,
 * k-means must eventually converge.  While there are exponentially
 * many possible clusterings in theory, in practice k-means converges
 * very quickly.
 *
 *
 * <p><b>Local Minima and Multiple Runs</b>
 *
 * <p>Like the EM algorithm, k-means clustering is highly sensitive to
 * the initial assignment of elements.  In practice, it is often
 * helpful to apply k-means clustering repeatedly to the same input
 * data, returning the clustering with minimum error.
 *
 * <p>At the start of each iteration, the error of the previous
 * assignment is reported (at convergence, this will be the final
 * error).
 *
 * <p>Multiple runs may be used to provide bootstrap estimates
 * of the relatedness of any two elements.   Bootstrap estimates
 * work by subsampling the elements to cluster with replacement
 * and then running k-means on them.  This is repeated multiple
 * times, and the percentage of runs in which two elements fall
 * in the same cluster forms the bootstrap estimate of the likelihood
 * that they are in the same cluster.
 *
 *
 * <p><b>Degenerate Solutions</b>
 *
 * <p>In some cases, the iterative approach taken by k-means leads to
 * a solution in which not every cluster is populated.  This happens
 * during a step in which no feature vector is closest to a given
 * centroid.  This is most likely to happen in highly skewed data in
 * high dimensional spaces.  Sometimes, rerunning the clusterer with a
 * different initialization will find a solution with k clusters.
 *
 *
 * <p><b>Picking a good <code>K</code></b>
 *
 * <p>The number of clusters, k, may also be varied.  In this case,
 * new k-means clustering instances must be created, as each uses
 * a fixed number of clusters.
 *
 * <p>By varying <code>k</code>, the maximum number of clusters,
 * the within-cluster scatter may be compared across different choices
 * of <code>k</code>.  Typically, a value of <code>k</code> is chosen
 * at a knee of the within-cluster scatter scores.  There are automatic
 * ways of performing this selection, though they are heuristically
 * rather than theoretically motivated.
 *
 * <p>In practice, it is technically possible, though unlikely, for
 * clusters to wind up with no elements in them.  This implementation
 * will simply return fewer clusters than the maximum specified in
 * this case.
 *
 *
 * <p><b>K-Means++ Initialization</b>
 *
 * <p> K-means++ is a k-means algorithm that attempts to make a good
 * randomized choice of initial centers. This requires choosing a
 * diverse set of centers, but not overpopulating the initial set of
 * centers with outliers.  <p>K-means++ has reasonable expected
 * performance bounds in theory, and quite good performance in
 * practice.
 *
 * <p>Suppose we have K clusters and a set X of size at least K.
 * K-means++ chooses a single point c[k] in X as each initial
 * centroid, using the following strategy:
 *
 * <blockquote><pre>
 * 1.  Sample the first centroid c[1] randomly from X.
 *
 * 2.  For k = 2 to K
 *
 *        Sample the next centroid c[k] = x
 *        with probability proportional to D(x)<sup><sup>2</sup></sup>
 * </pre></blockquote>
 *
 * where D(x) is the minimum distance to an existing centroid:
 *
 * <blockquote><pre>
 * D(x) = min<sub><sub>k' &lt; k</sub></sub> d(x,c[k'])</pre></blockquote>
 *
 * After initialization, k-means++ proceeds just as traditional
 * k-means clustering.
 *
 * <p>The good expected behavior form k-means++ arises from choosing
 * the next center in such a way that it's far away from existing
 * centers.  Many nearby points in some sense pool their behavior,
 * because the chance that one will be picked is the sum of the chance
 * that each will be picked.  Outliers are, by definition, points x
 * with high values of D(x) but which are not near other points.
 *
 *
 * <p><b>Relation to Gaussian Mixtures and Expectation/Maximization</b>
 *
 * <p>The k-means clustering algorithm is implicitly based on a
 * multi-dimensional Gaussian with independent dimensions with shared
 * means (the centroids) and equal variances.  Estimates are carried
 * out by maximum likelihood; that is, no prior, or equivalently the
 * fully uninformative prior, is used.  Where k-means differs from
 * standard expectation/maximization (EM) is that k-means reweights
 * the expectations so that the closest centroid gets an expectation
 * of 1.0 and all other centroids get an expectation of 0.0.  This
 * approach has been called &quot;winner-take-all&quot; EM in the EM
 * literature.
 *
 *
 * <p><b>Implementation Notes</b>
 *
 * <p>The current implementation conserves some computations versus
 * the brute-force approach by (1) only computing vector products
 * in comparing two vectors, and (2) not recomputing distances if
 * we know the 
 *
 *
 * <p><b>References</b>
 *
 * <ul>
 * <li>
 * MacQueen, J. B. 1967.  Some Methods for classification and Analysis of Multivariate Observations.
 * <i> Proceedings of Fifth Berkeley Symposium on Mathematical Statistics and Probability</i>.
 * University of California Press.
 *
 * <li>
 * Andrew Moore's <a href="http://www.autonlab.org/tutorials/kmeans11.pdf">K-Means Tutorial</a> including most of the mathematics
 * </li>
 *
 * <li>
 * Matteo Matteucci's <a href="http://home.dei.polimi.it/matteucc/Clustering/tutorial_html/kmeans.html">K-Means Tutorial</a> including a
very nice <a href="http://www.elet.polimi.it/upload/matteucc/Clustering/tutorial_html/AppletKM.html">interactive servlet demo</a>
 * </li>
 *
 * <li>
 * Hastie, T., R. Tibshirani and J.H. Friedman. 2001. <i>The
 * Elements of Statistical Learning</i>. Springer-Verlag.
 * </li>
 *
 * <li>
 * Wikipedia: <a href="http://en.wikipedia.org/wiki/K-means_algorithm">K-means Algorithm</a>
 * </li>
 *
 * <li>
 * Arthur, David and Sergei Vassilvitski (2007) <a href="http://www.stanford.edu/~darthur/kMeansPlusPlus.pdf">k-means++: The Advantages of Careful Seeding</a>. SODA 2007. 
 * </li>
 * </ul>
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.0
 * @param <E> the type of objects being clustered
 */
public class KMeansClusterer<E> implements Clusterer<E> {

    final FeatureExtractor<E> mFeatureExtractor;
    final int mMaxNumClusters;
    final int mMaxEpochs;
    final boolean mKMeansPlusPlus;
    final double mMinRelativeImprovement;

    /**
     * Construct a k-means clusterer with the specified feature
     * extractor, number of clusters and limit on number of epochs
     * to run optimization.  Initialization of each cluster is with
     * random shuffling of all elements into a cluster.
     *
     * <p>If the number of epochs is set to zero, the result
     * will be random balanced clusterings of the specified size.
     *
     * @param featureExtractor Feature extractor for this clusterer.
     * @param numClusters Number of clusters to return.
     * @param maxEpochs Maximum number of epochs during
     * optimization.
     * @throws IllegalArgumentException If the number of clusters is
     * less than 1, or if the maximum number of epochs is less
     * than 0.
     */
    KMeansClusterer(FeatureExtractor<E> featureExtractor,
                           int numClusters,
                           int maxEpochs) {
        this(featureExtractor,numClusters,maxEpochs,
             false,0.0);
    }

    /**
     * Construct a k-means clusterer with the specified feature
     * extractor, number of clusters, minimum improvement per cluster,
     * using either traditional or k-means++ initialization.
     *
     * <p>If the kMeansPlusPlus flag is set to {@code true}, the
     * k-means++ initialization strategy is used.  If it is set to
     * false, initialization of each cluster will be handled by a
     * random shuffling of all elements into a cluster.
     *
     * <p>If the number of epochs is set to zero, the result will be a
     * random balanced clusterings of the specified size.
     *
     * @param featureExtractor Feature extractor for this clusterer.
     * @param numClusters Number of clusters to return.
     * @param maxEpochs Maximum number of epochs during
     * @param kMeansPlusPlus Set to {@code true} to use k-means++
     * initialization.  optimization.
     * @param minImprovement Minimum relative improvement in squared
     * distance scatter to keep going to the next epoch.
     * @throws IllegalArgumentException If the number of clusters is
     * less than 1, if the maximum number of epochs is less than 0, or
     * if the minimum improvement is not a finite, non-negative number.
     */
    public KMeansClusterer(FeatureExtractor<E> featureExtractor,
                           int numClusters,
                           int maxEpochs,
                           boolean kMeansPlusPlus,
                           double minImprovement) {
        if (numClusters < 1) {
            String msg = "Number of clusters must be positive."
                + " Found numClusters=" + numClusters;
            throw new IllegalArgumentException(msg);
        }
        if (maxEpochs < 0) {
            String msg = "Number of epochs must be non-negative."
                + " Found maxEpochs=" + maxEpochs;
            throw new IllegalArgumentException(msg);
        }
        if (minImprovement < 0.0 || Double.isNaN(minImprovement)) {
            String msg = "Mimium improvement must be non-negative."
                + " Found minImprovement=" + minImprovement;
            throw new IllegalArgumentException(msg);
        }

        mFeatureExtractor = featureExtractor;
        mMaxNumClusters = numClusters;
        mMaxEpochs = maxEpochs;
        mKMeansPlusPlus = kMeansPlusPlus;
        mMinRelativeImprovement = minImprovement;
    }


    /**
     * Returns the feature extractor for this clusterer.
     *
     * @return The feature extractor for this clusterer.
     */
    public FeatureExtractor<E> featureExtractor() {
        return mFeatureExtractor;
    }


    /**
     * Returns the maximum number of clusters this clusterer will
     * return.  This is the &quot;<code>k</code>&quot; in
     * &quot;k-means&quot;.
     *
     * <p>Clustering fewer elements will result in fewer clusters.
     *
     * @return The number of clusters this clusterer will return.
     */
    public int numClusters() {
        return mMaxNumClusters;
    }

    /**
     * Returns the maximum number of epochs for this clusterer.
     *
     * @return The maximum number of epochs.
     */
    public int maxEpochs() {
        return mMaxEpochs;
    }

    /**
     * Return a k-means clustering of the specified set of elements
     * using a freshly generated random number generator without
     * intermediate reporting.  The feature extractor, maximum number of
     * epochs, number of clusters, and minimum relative improvement,
     * and whether to use k-means++ initialization are defined in the
     * class.
     *
     * <p>This is just a utility method implementing the
     * {@link Clusterer} interface.  A call to 
     * {@code cluster(elementSet)} produces the same result
     * as {@code cluster(elementSet,new Random(),null)}.
     * 
     * <p>See the class documentation above for more information.
     *
     * @param elementSet Set of elements to cluster.
     * @return Clustering of the specified elements.
     */
    public Set<Set<E>> cluster(Set<? extends E> elementSet) {
        return cluster(elementSet,new Random(),null);
    }




    /**
     * Return the k-means clustering for the specified set of
     * elements, using the specified random number generator, sending
     * progress reports to the specified reporter.  The feature
     * extractor, maximum number of epochs, number of clusters, and
     * minimum relative improvement, and whether to use k-means++
     * initialization are defined in the class.
     *
     * <p>The reason this is a separate method is that typical
     * implementations of {@link Random} are not thread safe,
     * and rarely should reports from different clustering runs
     * be interleaved to a reporter.
     *
     * <p>Using a fixed random number generator (e.g. by using the
     * same seed for {@link Random}) will result in the same
     * resulting clustering, which can be useful for replicating
     * tests.
     *
     * <p>See the class documentation above for more information.
     *
     * @param elementSet Set of elements to cluster
     * @param random Random number generator
     * @param reporter Reporter to which progress reports are sent,
     * or {@code null} if no reporting is required.
     */
    public Set<Set<E>> cluster(Set<? extends E> elementSet,
                               Random random,
                               Reporter reporter) {
        if (reporter == null) 
            reporter = Reporters.silent();

        final int numElements = elementSet.size();
        final int numClusters = mMaxNumClusters;
        reporter.report(LogLevel.INFO, "#Elements=" + numElements);
        reporter.report(LogLevel.INFO, "#Clusters=" + numClusters);

        if (numElements <= numClusters) {
            reporter.report(LogLevel.INFO,
                            "Returning trivial clustering due to #elements < #clusters");
            return trivialClustering(elementSet);
        }

        @SuppressWarnings("unchecked")
        final E[] elements = (E[]) elementSet.toArray(new Object[0]);

        reporter.report(LogLevel.DEBUG,"Converting inputs to sparse vectors");
        final int[][] featuress = new int[numElements][];
        final double[][] valss = new double[numElements][];
        final double[] eltSqLengths = new double[numElements];
        MapSymbolTable symTab
            = toVectors(elements,featuress,valss,eltSqLengths);
        int numDims = symTab.numSymbols();
        reporter.report(LogLevel.INFO,"#Dimensions=" + numDims);

        final double[][] centroidss = new double[numClusters][numDims];
        final int[] closestCenters = new int[numElements];
        final double[] sqDistToCenters = new double[numElements];

        if (true) {
            reporter.report(LogLevel.INFO,"K-Means++ Initialization");
            kmeansPlusPlusInit(featuress,valss,eltSqLengths,
                               closestCenters,
                               centroidss,
                               random);
        } else {
            reporter.report(LogLevel.INFO,"K-Means Random Initialization");
            randomInit(featuress,valss,
                       closestCenters,
                       centroidss,
                       random);
        }
        
        
        return kMeansEpochs(elements,eltSqLengths,
                            centroidss,
                            featuress,valss,
                            sqDistToCenters,closestCenters,
                            mMaxEpochs,reporter);
    }

    /**
     * Returns the minimum reduction in relative error required to
     * continue to the next epoch during clustering.
     *
     * @return The minimum improvement per epoch.
     */
    public double minRelativeImprovement() {
        return mMinRelativeImprovement;
    }

    /**
     * Recluster the specified initial clustering, adding in the
     * unclustered elements, reporting progress to the specified
     * reporter.  The number of clusters is set by the size of the
     * initial clustering as measured by the number of non-empty
     * clusters it contains.  This clusterer's maximum number of epochs and minimum
     * improvement will be used.
     *
     * @param initialClustering Initialization of clustering.
     * @param unclusteredElements Elements that have not been clustered.
     * @param reporter Reporter to which to send progress reports,
     * or {@code null} for no reporting.
     * @throws IllegalArgumentException If there are empty clusters
     * in the clustering or if an element belongs to more than
     * one cluster.
     * @return The reclustering.
     */
    public Set<Set<E>> recluster(Set<Set<E>> initialClustering,
                                 Set<E> unclusteredElements,
                                 Reporter reporter) {
        return recluster(initialClustering,unclusteredElements,
                         mMaxEpochs,reporter);
    }


    /**
     * Recluster the specified clustering using up to the specified
     * number of k-means epochs with no reporting.
     *
     * <p>This  method allows   users  to specify  their own
     * initial  clusterings,  which  are  then  reallocated using  the
     * standard k-means algorithm.
     *
     * <p>The number of clusters produced will be the size of the
     * initial clustering, which may not match the number of clusters
     * defined in the constructor.
     *
     * @param clustering Clustering to recluster.
     * @param maxEpochs Maximum number of reclustering epochs.
     * @return New clustering of input elements.
     * @throws IllegalArgumentException If there are empty clusters
     * in the clustering or if an element belongs to more than
     * one cluster.
     */
    Set<Set<E>> recluster(Set<Set<E>> clustering,
                                 int maxEpochs) {
        return recluster(clustering, SmallSet.<E>create(), 
                         maxEpochs, null);
    }


    private Set<Set<E>> recluster(Set<Set<E>> clustering,
                                  Set<E> unclusteredElements,
                                  int maxEpochs,
                                  Reporter reporter) {
        if (reporter == null)
            reporter = Reporters.silent();

        reporter.report(LogLevel.INFO, "Reclustering");
        
        int numClusters = clustering.size();
        reporter.report(LogLevel.INFO,
                        "# Clusters=" + numClusters);
        Set<E> elementSet = new HashSet<E>();
        for (Set<E> cluster : clustering) {
            for (E e : cluster) {
                if (!elementSet.add(e)) {
                    String msg = "An element must not be in two clusters."
                        + " Found an element in two clusters."
                        + " Element=" + e;
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        int numClusteredElements = elementSet.size();
        for (E e : unclusteredElements) {
            if (!elementSet.add(e)) {
                String msg = "An element may not be in a cluster and unclustered."
                    + " Found unclustered element in a cluster."
                    + " Element=" + e;
                throw new IllegalArgumentException(msg);
            }
        }
        int numElements = elementSet.size();
        reporter.report(LogLevel.INFO,
                        "# Clustered Elements=" + numClusteredElements);
        reporter.report(LogLevel.INFO,
                        "# Unclustered Elements=" + unclusteredElements.size());
        reporter.report(LogLevel.INFO,
                        "# Elements Total=" + numElements);
        
        @SuppressWarnings("unchecked")
        E[] elements = (E[]) new Object[numElements];
        int i = 0;
        for (Set<E> cluster : clustering)
            for (E e : cluster)
                elements[i++] = e;
        for (E e : unclusteredElements)
            elements[i++] = e;

        reporter.report(LogLevel.DEBUG, "Converting to vectors");
        // cut and paste from main clustering
        final int[][] featuress = new int[numElements][];
        final double[][] valss = new double[numElements][];
        final double[] eltSqLengths = new double[numElements];
        MapSymbolTable symTab
            = toVectors(elements,featuress,valss,eltSqLengths);
        int numDims = symTab.numSymbols();
        reporter.report(LogLevel.INFO,"#Dimensions=" + numDims);

        double[][] centroidss = new double[numClusters][numDims];
        int[] closestCenters = new int[numElements];
        i = 0;
        int k = 0;
        for (Set<E> cluster : clustering) {
            double[] centroidK = centroidss[k];
            for (E e : cluster) {
                closestCenters[i] = k;
                increment(centroidK,featuress[i],valss[i]);
                ++i;
            }
            ++k;
        }
        double[] sqDistToCenters = new double[numElements];
        Arrays.fill(sqDistToCenters,Double.POSITIVE_INFINITY);
        // reassign everyone
        for (k = 0; k < numClusters; ++k) {
            double[] centroidK = centroidss[k];
            double centroidSqLength = selfProduct(centroidss[k]);
            for (i = 0; i < numElements; ++i) {
                double sqDistToCenter
                    = centroidSqLength
                    + eltSqLengths[i]
                    - 2.0 * product(centroidK,featuress[i],valss[i]);
                if (sqDistToCenter < sqDistToCenters[i]) {
                    sqDistToCenters[i] = sqDistToCenter;
                    closestCenters[i] = k;
                }
            }
        }
        for (double[] centroid : centroidss)
            Arrays.fill(centroid,0.0);
        setCentroids(centroidss,featuress,valss,closestCenters);

        return kMeansEpochs(elements,eltSqLengths,
                            centroidss,
                            featuress,valss,
                            sqDistToCenters,closestCenters,
                            maxEpochs,reporter);
    }


    private Set<Set<E>> kMeansEpochs(E[] elements,
                                     double[] eltSqLengths,
                                     double[][] centroidss,
                                     int[][] featuress,
                                     double[][] valss,
                                     double[] sqDistToCenters,
                                     int[] closestCenters,
                                     int maxEpochs,
                                     Reporter reporter) {

        int numClusters = centroidss.length;
        int numDims = centroidss[0].length;  
        int numElements = elements.length;

        final double[] centroidSqLengths = centroidSqLengths(centroidss);
        boolean[] lastCentroidChanges = createBooleanArray(numClusters,true);
        final int[] changedClusters = new int[numClusters];
        final int[] counts = new int[numClusters];

        double lastError = Double.POSITIVE_INFINITY;
        for (int epoch = 0; epoch < maxEpochs; ++epoch) {
            reporter.report(LogLevel.DEBUG,"Epoch=" + epoch);
            boolean atLeastOneClusterChanged = false;
            int numChangedClusters
                = setChangedClusters(changedClusters,lastCentroidChanges);
            reporter.report(LogLevel.DEBUG,"    #changed clusters=" + numChangedClusters);
            final boolean[] centroidChanges = createBooleanArray(numClusters,false);

            // *** multi-thread all of this loop; optimistic updates sets to true ***

            for (int i = 0; i < numElements; ++i) {
                final int[] featuresI = featuress[i];
                final double[] valsI = valss[i];
                final double eltSqLengthI = eltSqLengths[i];
                double closestSqDistToCenter
                    = lastCentroidChanges[closestCenters[i]]
                    ? Double.POSITIVE_INFINITY
                    : sqDistToCenters[i];
                int bestCenter = -1; // set if beat unchanged ctr, or if ctr changed
                for (int kk = 0; kk < numChangedClusters; ++kk) {
                    // cut and paste (below)
                    int k = changedClusters[kk];
                    double sqDistToCenter
                        = centroidSqLengths[k]
                        + eltSqLengthI
                        - 2.0 * product(centroidss[k],featuresI,valsI);
                    if (sqDistToCenter < closestSqDistToCenter) {
                        closestSqDistToCenter = sqDistToCenter;
                        bestCenter = k;
                    }
                }
                // leave unchanged if can't beat old (or infty if old's expired)
                if (bestCenter == -1) continue;
                // have to be worse than previous best, or skip unchanged clusts
                if (closestSqDistToCenter > sqDistToCenters[i]) {
                    for (int kk = numChangedClusters; kk < numClusters; ++kk) {
                        // cut and paste (above)
                        int k = changedClusters[kk];
                        double sqDistToCenter
                            = centroidSqLengths[k]
                            + eltSqLengthI
                            - 2.0 * product(centroidss[k],featuresI,valsI);
                        if (sqDistToCenter < closestSqDistToCenter) {
                            closestSqDistToCenter = sqDistToCenter;
                            bestCenter = k;
                        }
                    }
                }
                // could change even if center id doesn't
                sqDistToCenters[i] = closestSqDistToCenter;
                if (bestCenter == closestCenters[i]) continue;
                atLeastOneClusterChanged = true;
                centroidChanges[bestCenter] = true; // to
                centroidChanges[closestCenters[i]] = true; // from
                closestCenters[i] = bestCenter;
            }
            double error = sum(sqDistToCenters)/numElements;
            reporter.report(LogLevel.DEBUG,
                            "    avg dist to center=" + error);
            if (!atLeastOneClusterChanged) {
                reporter.report(LogLevel.INFO,"Converged by no elements changing cluster.");
                break;
            }
            double relImprovement = relativeImprovement(lastError,error);
            if (relImprovement < mMinRelativeImprovement) {
                reporter.report(LogLevel.INFO,
                                "Converged by relative improvement < threshold");
                break;
            }

            Arrays.fill(counts,0);
            int numChangedElts = 0;
            for (int k = 0; k < numClusters; ++k)
                if (centroidChanges[k])
                    Arrays.fill(centroidss[k],0.0);
            for (int i = 0; i < numElements; ++i) {
                int closestCenterI = closestCenters[i];
                if (centroidChanges[closestCenterI]) {
                    increment(centroidss[closestCenterI],
                              featuress[i],valss[i]);
                    ++counts[closestCenterI];
                    ++numChangedElts;
                }
            }
            reporter.report(LogLevel.DEBUG, "    #changed elts=" + numChangedElts);

            for (int k = 0; k < numClusters; ++k) {
                if (counts[k] > 0) {
                    final double[] centroidK = centroidss[k];
                    double countD = (double) counts[k];
                    double sqLength = 0.0;
                    for (int d = 0; d < numDims; ++d) {
                        centroidK[d] /= countD;
                        sqLength += centroidK[d] * centroidK[d];
                    }
                    centroidSqLengths[k] = sqLength;
                }
            }

            lastCentroidChanges = centroidChanges;
            if (epoch == (maxEpochs-1)) {
                reporter.report(LogLevel.INFO, 
                                "Reached max epochs. Breaking without convergence.");
            }
        }


        reporter.report(LogLevel.DEBUG,"Constructing Result");

        List<ObjectToDoubleMap<E>> scoreMapList = new ArrayList<ObjectToDoubleMap<E>>(numClusters);
        double[] totalScores = new double[numClusters];
        for (int k = 0; k < numClusters; ++k)
            scoreMapList.add(new ObjectToDoubleMap<E>());
        for (int i = 0; i < numElements; ++i) {
            scoreMapList.get(closestCenters[i]).set(elements[i],
                                                    sqDistToCenters[i] == 0.0
                                                    ? -Double.MIN_VALUE
                                                    : -sqDistToCenters[i]);
            totalScores[closestCenters[i]] -= sqDistToCenters[i];
        }
       
        ObjectToDoubleMap<Set<E>> clusterScores
            = new ObjectToDoubleMap<Set<E>>();
        for (int k = 0; k < numClusters; ++k) {
            ObjectToDoubleMap<E> clusterDistances = scoreMapList.get(k);
            if (clusterDistances.isEmpty())
                continue;
            Set<E> cluster
                = new LinkedHashSet<E>(clusterDistances.keysOrderedByValueList());
            clusterScores.set(cluster,
                              totalScores[k] == 0.0
                              ? -Double.MIN_VALUE
                              : totalScores[k]/cluster.size());
        }
        Set<Set<E>> result = new LinkedHashSet<Set<E>>(clusterScores.keysOrderedByValueList());
       
        return result;
    }

    static double relativeImprovement(double x, double y) {
        return Math.abs(2.0 * (x - y)  /(Math.abs(x) + Math.abs(y)));
    }

    static int setChangedClusters(int[] clusterIndexes, boolean[] changed) {
        int numChanged = 0;
        int numNotChanged = clusterIndexes.length - 1;
        // really fancy: numNotChanged = changed.length-i-numChanged-1
        for (int i = 0; i < changed.length; ++i)
            clusterIndexes[changed[i] ? numChanged++ : numNotChanged--] = i;
        return numChanged;
    }

    static boolean[] createBooleanArray(int length, boolean fillValue) {
        boolean[] result = new boolean[length];
        if (fillValue)
            Arrays.fill(result,true);
        return result;
    }

    private MapSymbolTable toVectors(E[] elements,
                                     int[][] featuress, double[][] valss,
                                     double[] eltSqLengths) {
        MapSymbolTable symTab = new MapSymbolTable();
        for (int i = 0; i < elements.length; ++i) {
            E e = elements[i];
            Map<String,? extends Number> featureMap
                = mFeatureExtractor.features(e);
            featuress[i] = new int[featureMap.size()];
            valss[i] = new double[featureMap.size()];
            int j = 0;
            for (Map.Entry<String,? extends Number> entry
                     : featureMap.entrySet()) {
                featuress[i][j] = symTab.getOrAddSymbol(entry.getKey());
                valss[i][j] = entry.getValue().doubleValue();
                ++j;
            }
            eltSqLengths[i] = selfProduct(valss[i]);
        }
        return symTab;
    }

    private Set<Set<E>> trivialClustering(Set<? extends E> elementSet) {
        Set<Set<E>> clustering
            = new HashSet<Set<E>>((3 * elementSet.size()) / 2);
        for (E elt : elementSet) {
            Set<E> cluster = SmallSet.<E>create(elt);
            clustering.add(cluster);
        }
        return clustering;
    }


    private void randomInit(int[][] featuress,
                            double[][] valss,
                            int[] closestCenters,
                            double[][] centroidss,
                            Random random) {
        int numClusters = centroidss.length;
        int numElements = featuress.length;
        int[] permutation = Statistics.permutation(numElements,random);
        int[] count = new int[numClusters];
        for (int i = 0; i < numElements; ++i) 
            closestCenters[i] = i % numClusters; 
        setCentroids(centroidss,featuress,valss,closestCenters);

    }

    private void kmeansPlusPlusInit(int[][] featuress,
                                    double[][] valss,
                                    double[] eltSqLengths,
                                    int[] closestCenters,
                                    double[][] centroidss, 
                                    Random random) {
        int numClusters = centroidss.length;
        int numElements = featuress.length;
        double[] sqDistToCenters = new double[numElements];
        Arrays.fill(sqDistToCenters,Double.POSITIVE_INFINITY);
        for (int k = 0; k < numClusters; ++k) {
            final double[] centroidK = centroidss[k];
            int centroidIndex
                = (k == 0)
                ? random.nextInt(numElements)
                : sampleNextCenter(sqDistToCenters,random);
            setCentroid(centroidK,
                        featuress[centroidIndex],valss[centroidIndex]);
            double centroidSqLength = selfProduct(valss[centroidIndex]);
            for (int i = 0; i < numElements; ++i) {
                double sqDistToCenter
                    = centroidSqLength
                    + eltSqLengths[i]
                    - 2.0 * product(centroidK,featuress[i],valss[i]);
                if (sqDistToCenter < sqDistToCenters[i]) {
                    sqDistToCenters[i] = sqDistToCenter;
                    closestCenters[i] = k;
                }
            }
        }
        for (double[] centroid : centroidss)
            Arrays.fill(centroid,0.0); // reset after previous use
        setCentroids(centroidss,featuress,valss,closestCenters);
    }


    private void setCentroids(double[][] centroidss, 
                              int[][] featuress, double[][] valss, 
                              int[] closestCenters) {
        int numClusters = centroidss.length;
        int numElements = featuress.length;
        final int[] count = new int[numClusters];
        for (int i = 0; i < numElements; ++i) {
            increment(centroidss[closestCenters[i]],
                      featuress[i],valss[i]);
            ++count[closestCenters[i]];
        }
        for (int k = 0; k < numClusters; ++k) {
            double countK = count[k];
            final double[] centroid = centroidss[k];
            for (int d = 0; d < centroid.length; ++d) {
                centroid[d] = centroid[d] / countK;
            }
        }

    }

    private static int sampleNextCenter(double[] probRatios, Random random) {
        double samplePoint = random.nextDouble() * sum(probRatios);
        double total = 0.0;
        for (int i = 0; i < probRatios.length; ++i) {
            total += probRatios[i];
            if (total >= samplePoint)
                return i;
        }
        return probRatios.length-1; // arith overrun
    }

    private static double[] centroidSqLengths(double[][] centroidss) {
        double[] result = new double[centroidss.length];
        for (int i = 0; i < result.length; ++i)
            result[i] = selfProduct(centroidss[i]);
        return result;
    }

    private static double selfProduct(double[] xs) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sum += xs[i] * xs[i];
        return sum;
    }

    private static double sum(double[] xs) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sum += xs[i];
        return sum;
    }

    // x = (indexes,values); c=centroid; returns x*c
    private static double product(double[] centroid,
                          int[] features,
                          double[] values) {
        double sum = 0.0;
        for (int i = 0; i < features.length; ++i)
            sum += values[i] * centroid[features[i]];
        return sum;
    }

    private static void setCentroid(double[] centroid,
                            int[] indexes,
                            double[] values) {
        for (int i = 0; i < indexes.length; ++i)
            centroid[indexes[i]] = values[i];
    }

    private static void increment(double[] centroid,
                                  int[] indexes,
                                  double[] values) {
        for (int i = 0; i < indexes.length; ++i)
            centroid[indexes[i]] += values[i];
    }

}




