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

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A <code>GaussianRadialBasisKernel</code> provides a kernel based on
 * a Gaussian radial basis function with a fixed variance parameter.
 * As a kernel function, it unfolds into an infinite-dimension Hilbert
 * space.
 *
 * <p>The radial basis kernel function of radius <code>r</code> is
 * defined between vectors <code>v1</code> and <code>v2</code> as
 * follows:
 *
 * <blockquote><pre>
 * rbf(v1,v2) = exp(- r * distance(v1,v2)<sup><sup>2</sup></sup>)
 * </pre></blockquote>
 *
 * where <code>distance(v1,v2)</code> is the Euclidean distance,
 * as defined in the class documentation for {@link EuclideanDistance}.
 * In this formulation, the radius <code>r</code> is related to
 * the variance <code>&sigma;<sup><sup>2</sup></sup></code> by:
 *
 * <blockquote><pre>
 * r = 1/(2 * &sigma;<sup><sup>2</sup></sup>)</pre></blockquote>
 *
 * <p>For more information on the Gaussian radial basis kernel
 * and applications, see:
 *
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Radial_basis_function">Wikipedia: Radial Basis Function</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe3.1
 */
public class GaussianRadialBasisKernel
    implements KernelFunction, Serializable {

    static final long serialVersionUID = -1670587197184485884L;

    private final double mNegativeRadius;

    /**
     * Construct a Gaussian radial basis kernel with the specified
     * radius of influence.
     *
     * @param radius The radius of influence for the kernel.
     */
    public GaussianRadialBasisKernel(double radius) {
        if (radius <= 0.0
            || Double.isInfinite(radius)
            || Double.isNaN(radius)) {

            String msg = "Radius must be positive and finite."
                + " Found radius=" + radius;
            throw new IllegalArgumentException(msg);
        }
        mNegativeRadius = -radius;
    }

    GaussianRadialBasisKernel(double negativeRadius, boolean ignore) {
        mNegativeRadius = negativeRadius;
    }

    /**
     * Returns the result of applying this Guassian radial basis
     * kernel to the specified vectors.  See the class documentation
     * above for a full definition.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return Kernel function applied to the two vectors.
     * @throws IllegalArgumentException If the vectors are not of the
     * same dimensionality.
     */
    public double proximity(Vector v1, Vector v2) {
        double dist = EuclideanDistance.DISTANCE.distance(v1,v2);
        return Math.exp(mNegativeRadius * (dist * dist));
    }

    /**
     * Returns a string-based representation of this kernel
     * function, including the kernel type and radius.
     *
     * @return A string representing this kernel.
     */
    @Override
    public String toString() {
        return "GaussianRadialBasedKernel(" + (-mNegativeRadius) + ")";
    }

    Object writeReplace() {
        return new Externalizer(mNegativeRadius);
    }

    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -5223595743791099605L;
        final double mNegativeRadius;
        public Externalizer() {
            this(1.0);
        }
        public Externalizer(double negativeRadius) {
            mNegativeRadius = negativeRadius;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeDouble(mNegativeRadius);
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            double negativeRadius = in.readDouble();
            return new GaussianRadialBasisKernel(negativeRadius,true);
        }
    }

}
