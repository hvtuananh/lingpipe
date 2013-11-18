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
 * The <code>TaxicabDistance</code> class implements standard taxicab,
 * or Manhattan distance between vectors.  The taxicab distance forms
 * a metric.  The taxicab distance is often called the
 * <code>L<sub>1</sub></code> distance, because it is 1-norm Minkowski
 * distance after the inventor of the general family of vector
 * distance metrics and related geometries.
 *
 * <p>The definition of the taxicab distance over vectors
 * <code>v1</code> and <code>v2</code> is:
 *
 * <blockquote><pre>
 * distance(v1,v2) = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub> abs(v1[i] - v2[i])</pre></blockquote>
 *
 * with <code>v1[i]</code> standing for the method call
 * <code>v1.value(i)</code> and <code>i</code> ranging over the
 * dimensions of the vectors, which must be the same.
 *
 * <p>An understandable explanation of the taxicab distance
 * and related distances may be found at:
 *
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Distance#Distance_in_Euclidean_space">Wikipedia: Distance in Euclidean Space</a></li>
 * <li><a href="http://en.wikipedia.org/wiki/Taxicab_geometry">Wikipedia: Taxicab Geometry</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe3.1
 */
public class TaxicabDistance
    implements Distance<Vector>,
               Serializable {

    static final long serialVersionUID = -8456511197031445244L;

    /**
     * The taxicab distance.  All instances of taxicab distance
     * perform the same function.  Because the distance function is
     * thread safe, this instance may be used wherever taxicab
     * distance is needed.
     */
    public static final TaxicabDistance DISTANCE
        = new TaxicabDistance();

    /**
     * Construct a new taxicab distance.
     */
    public TaxicabDistance() { /* empty constructor */
    }

    /**
     * Returns the taxicab distance between the specified pair
     * of vectors.  See the class definition above for details.
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
            double diff = Math.abs(v1.value(i) - v2.value(i));
            sum += diff;
        }
        return sum;
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
                    : vals2[index2++] );
            sum += Math.abs(diff);
        }
        for ( ; index1 < keys1.length; ++index1)
            sum += Math.abs(vals1[index1]);
        for ( ; index2 < keys2.length; ++index2)
            sum += Math.abs(vals2[index2]);
        return sum;
    }

}
