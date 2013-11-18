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
 * The <code>MinkowskiDistance</code> class implements Minkowski
 * distance of a fixed order between vectors.  or Manhattan distance
 * between vectors.  Minkowski distance of any order forms a metric.
 * The Minkowski distance of order <code>p</code> is often called
 * <code>L<sub>p</sub></code> or the <code>p-norm distance</code>.
 *
 * <p>Minkowski distance generalizes taxicab and Euclidean distance,
 * which are just the Minkowski distances of order 1 and 2
 * respectively.  For orders 1 and 2, the taxicab and Euclidean
 * distance classes {@link TaxicabDistance} and {@link
 * EuclideanDistance} are more efficient in that they do not require
 * exponentiation to be calculated.
 *
 * <p>The definition of Minkowski distance of order <code>p</code>
 * over vectors <code>v1</code> and <code>v2</code> is:
 *
 * <blockquote><pre>
 * distance(v1,v2,p) = (<big><big>&Sigma;</big></big><sub><sub>i</sub></sub> abs(v1[i] - v2[i])<sup><sup>p</sup></sup>)<sup><sup><big>(1/p)</big></sup></sup></pre></blockquote>
 *
 * with <code>v1[i]</code> standing for the method call
 * <code>v1.value(i)</code> and <code>i</code> ranging over the
 * dimensions of the vectors, which must be the same.
 *
 * <p>An understandable explanation of the Minkowski distances,
 * including the special cases of Taxicab (<code>L<sub>1</sub></code> norm)
 * and Euclidean (<code>L<sub>2</sub></code> norm) may be
 * found at:
 *
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Distance#Distance_in_Euclidean_space">Wikipedia: Distance in Euclidean Space</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe3.1
 */
public class MinkowskiDistance
    implements Distance<Vector>,
               Serializable {

    static final long serialVersionUID = -3492306373950488519L;

    int mOrder;

    /**
     * Construct a new Minkowski distance of the specified order.
     *
     * @param order Order of metric.
     * @throws IllegalArgumentException If the order is not 1 or greater.
     */
    public MinkowskiDistance(int order) {
        mOrder = order;
    }

    /**
     * Returns the order of this Minkowski distance.
     *
     * @return The order of this Minkowski distance.
     */
    public int order() {
        return mOrder;
    }

    /**
     * Returns the Minkowski distance between the specified pair
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
            double absDiff = Math.abs(v1.value(i) - v2.value(i));
            sum += java.lang.Math.pow(absDiff,mOrder);
        }
        return java.lang.Math.pow(sum,1.0/mOrder);
    }

    double sparseDistance(SparseFloatVector v1,
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
                = Math.abs((comp == 0)
                           ? (vals1[index1++] - vals2[index2++])
                           : (comp < 0) ? vals1[index1++] : vals2[index2++]);
            sum += java.lang.Math.pow(diff,mOrder);
        }
        for ( ; index1 < keys1.length; ++index1)
            sum += java.lang.Math.pow(Math.abs(vals1[index1]),mOrder);
        for ( ; index2 < keys2.length; ++index2)
            sum += java.lang.Math.pow(Math.abs(vals2[index2]),mOrder);
        return java.lang.Math.pow(sum,1.0/mOrder);
    }

}
