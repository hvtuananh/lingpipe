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
 * A <code>PolynomialKernel</code> provides a dot product over a fixed
 * degree polynomial basis expansion of a vector.
 *
 * <p>The polynomial kernel of degree <code>d</code> over vectors
 * <code>v1</code> and <code>v2</code> is defined in terms of
 * underlying vector dot products:
 *
 * <blockquote><pre>
 * kernel(v1,v2) = (1 + v1 * v2)<sup><sup>d</sup></sup></pre></blockquote>
 *
 * where <code>v1 * v2</code> is shorthand for the method call
 * <code>v1.dotProduct(v2)</code>.
 *
 * <h3>Serialization</h3>
 *
 * <p>A polynomial kernel may be serialized.
 *
 * <h3>Background Reading</h3>
 *
 * <p>A thorough discussion of kernel functions and kernel-based
 * classifiers may be found in:
 *
 * <ul>
 * <li>Trevor Hastie, Robert Tibshirani, and Jerome Friedman. 2001.
 * <i>The Elements of Statistical Learning</i>. Springer-Verlag.
 * </li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe3.1
 */
public class PolynomialKernel
    implements KernelFunction, Serializable {

    static final long serialVersionUID = 2807317510032521328L;

    private final int mDegree;

    /**
     * Construct a polynomial kernel function of the specified degree.
     *
     * @param degree Degree of the polynomial kernel.
     */
    public PolynomialKernel(int degree) {
        mDegree = degree;
    }

    /**
     * Returns the result of applying the polynomial kernel of
     * this class's degree to the specified vectors.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return Polynomial kernel function applied to the two vectors.
     * @throws IllegalArgumentException If the vectors are not of the
     * same dimensionality.
     */
    public double proximity(Vector v1, Vector v2) {
        return power(1.0 + v1.dotProduct(v2));
    }

    double power(double base) {
        switch (mDegree) {
        case 0: return 1.0;
        case 1: return base;
        case 2: return base * base;
        case 3: return base * base * base;
        case 4: return base * base * base * base;
        default: return java.lang.Math.pow(base,mDegree);
        }
    }

    /**
     * Returns a string-based representation of this kernel
     * function, including the kernel type and degree.
     *
     * @return A string representing this kernel.
     */
    @Override
    public String toString() {
        return "PolynomialKernel(" + mDegree + ")";
    }

    Object writeReplace() {
        return new Externalizer(mDegree);
    }

    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = 4795059467534365487L;
        final int mDegree;
        public Externalizer() {
            this(-1);
        }
        public Externalizer(int degree) {
            mDegree = degree;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mDegree);
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            int degree = in.readInt();
            return new PolynomialKernel(degree);
        }
    }

}
