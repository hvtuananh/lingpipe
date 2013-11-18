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

package com.aliasi.matrix;

import com.aliasi.util.Distance;

import java.io.Serializable;

/**
 * The <code>EuclideanDistance</code> class implements standard
 * Euclidean distance between vectors.  Euclidean distance forms a
 * metric.  Euclidean distance is often called the
 * <code>L<sub>2</sub></code> distance, because it is 2-norm Minkowski
 * distance.
 *
 * <p>The definition of Euclidean distance over vectors
 * <code>v1</code> and <code>v2</code> is:
 *
 * <blockquote><pre>
 * distance(v1,v2) = sqrt(<big><big>&Sigma;</big></big><sub><sub>i</sub></sub> (v1[i] - v2[i])<sup><sup>2</sup></sup>  )
 * </pre></blockquote>
 *
 * with <code>v1[i]</code> standing for the method call
 * <code>v1.value(i)</code> and <code>i</code> ranging over the
 * dimensions of the vectors, which must be the same.
 *
 * <p>Note that the Euclidean distance is equivalent to the
 * Minkowski distance metric of order 2.  See the class
 * documentation for {@link MinkowskiDistance} for more information.
 *
 * <p>An understandable explanation of Euclidean and related
 * distances may be found at:
 *
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Distance#Distance_in_Euclidean_space">Wikipedia: Distance in Euclidean Space</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.1
 * @since   LingPipe3.1
 */
public class EuclideanDistance
    implements Distance<Vector>,
               Serializable {

    static final long serialVersionUID = -7331942504500606550L;

    /**
     * The Euclidean distance.  All instances of Euclidean distance
     * perform the same function.  Because the distance function is
     * thread safe, this instance may be used wherever Euclidean
     * distance is needed.
     */
    public static final EuclideanDistance DISTANCE
        = new EuclideanDistance();

    /**
     * Construct a new Euclidean distance.
     */
    public EuclideanDistance() { /* empty constructor */
    }

    /**
     * Returns the Euclidean distance between the specified pair
     * of vectors.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return The distance between the vectors.
     * @throws IllegalArgumentException If the vectors are not of the
     * same dimensionality.
     */
    public double distance(Vector v1, Vector v2) {
        if (v1.numDimensions() != v2.numDimensions()) {
            String msg = "Vectors must have same dimensions."
                + " v1.numDimensions()=" + v1.numDimensions()
                + " v2.numDimensions()=" + v2.numDimensions();
            throw new IllegalArgumentException(msg);
        }
        if (v1 instanceof SparseFloatVector && v2 instanceof SparseFloatVector)
            return sparseDistance((SparseFloatVector)v1,
                                  (SparseFloatVector)v2);
        double sum = 0.0;
        for (int i = v1.numDimensions(); --i >= 0; ) {
            double diff = v1.value(i) - v2.value(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    static double sparseDistance(SparseFloatVector v1,
                                 SparseFloatVector v2) {
        double sum = 0.0;
        int index1 = 0;
        int index2 = 0;
        int[] keys1 = v1.mKeys;
        int[] keys2 = v2.mKeys;
        float[] vals1 = v1.mValues;
        float[] vals2 = v2.mValues;
        while (index1 < keys1.length && index2 < keys2.length) {
            int comp = keys1[index1] - keys2[index2];
            double diff
                = (comp == 0)
                ? (vals1[index1++] - vals2[index2++])
                : ( (comp < 0)
                    ? vals1[index1++]
                    : vals2[index2++]);
            sum += diff * diff;
        }
        for ( ; index1 < keys1.length; ++index1)
            sum += vals1[index1] * vals1[index1];
        for ( ; index2 < keys2.length; ++index2)
            sum += vals2[index2] * vals2[index2];
        return Math.sqrt(sum);
    }

}