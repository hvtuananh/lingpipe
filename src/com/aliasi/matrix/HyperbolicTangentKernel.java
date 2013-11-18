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
 * A <code>HyperbolicTangentKernel</code> provides a kernel based on
 * the hyperbolic tangent of a dot product with fixed linear scaling.
 * Hyperbolic tangent kernels are popular as neural network activation
 * functions.
 *
 * <p>The hyperbolic tangent kernel function of with parameters
 * <code>k0</code> and <code>k1</code> is defined between two
 * vectors <code>v1</code> and <code>v2</code> of the same
 * dimensionality by:
 *
 * <blockquote><pre>
 * kernel(v1,v2) = tanh(k1 * v1 * v2 + k0)</pre></blockquote>
 *
 * where <code>v1 * v2</code> is the usual dot product and
 * the constant <code>k1</code> is simply a scalar multiplier.
 *
 * <h3>References</h3>
 *
 * <ul>
 * <li>Wikipedia; <a href="http://en.wikipedia.org/wiki/Sigmoid_function">Sigmoid Function</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe3.1
 */
public class HyperbolicTangentKernel
    implements KernelFunction, Serializable {

    static final long serialVersionUID = -4623910478151947840L;

    private final double mK0;
    private final double mK1;

    /**
     * Construct a linearly offset hyperbolic tangent kernel
     * with the specified slope and intercept parameters.
     *
     * @param k0 Intercept parameter.
     * @param k1 Slope parameter.
     * @throws IllegalArgumentException If either of the parameters
     * are not finite numbers, or if the k1 parameter is zero.
     */
    public HyperbolicTangentKernel(double k0, double k1) {
        if (Double.isInfinite(k0) || Double.isNaN(k0)) {
            String msg = "k0 must be a finite number."
                + " Found k0=" + k0;
            throw new IllegalArgumentException(msg);
        }
        if (Double.isInfinite(k1) || Double.isNaN(k1) || k1 == 0.0) {
            String msg = "k1 must be a finite, non-zero number."
                + " Found k1=" + k1;
            throw new IllegalArgumentException(msg);
        }
        mK0 = k0;
        mK1 = k1;
    }

    /**
     * Returns the result of applying the hyperbolic tangent kernel
     * function to to the specified vectors.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return Kernel function applied to the two vectors.
     * @throws IllegalArgumentException If the vectors are not of the
     * same dimensionality.
     */
    public double proximity(Vector v1, Vector v2) {
        return Math.tanh(mK1 * v1.dotProduct(v2) + mK0);
    }

    /**
     * Returns a string-based representation of this kernel
     * function, including the offset and slope parameters.
     *
     * @return A string representing this kernel.
     */
    @Override
    public String toString() {
        return "HyperbolicTangentKernel("
            + mK0 + ", " + mK1 + ")";
    }


    Object writeReplace() {
        return new Externalizer(mK0,mK1);
    }

    static class Externalizer extends AbstractExternalizable {

        static final long serialVersionUID = 5756879441704225246L;

        final double mK0;
        final double mK1;
        public Externalizer() {
            this(0.0,0.0);
        }
        public Externalizer(double k0, double k1) {
            mK0 = k0;
            mK1 = k1;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeDouble(mK0);
            out.writeDouble(mK1);
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            double k0 = in.readDouble();
            double k1 = in.readDouble();
            return new HyperbolicTangentKernel(k0,k1);
        }
    }

}
