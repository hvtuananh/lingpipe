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

import java.io.Serializable;

/**
 * The <code>DotProductKernel</code> is the trivial kernel function
 * computed by taking the dot product of the input vectors.  The
 * dot-product kernel is, for instance, the kernel to use for the
 * ordinary (non-kernel) perceptron.  Typically, this kernel is used
 * as a baseline, as polynomial and radial basis kernels tend to
 * perform better in practice for classification and clustering
 * problems.
 *
 * @author  Bob Carpenter
 * @version 3.1
 * @since   LingPipe3.1
 */
public class DotProductKernel
    implements KernelFunction, Serializable {

    static final long serialVersionUID = -3943009270761485840L;

    /**
     * Construct a dot-product kernel.
     */
    public DotProductKernel() { /* empty constructor */
    }

    /**
     * Returns a string-based representation of this kernel.
     *
     * @return A string representing this kernel.
     */
    @Override
    public String toString() {
        return "DotProductKernel()";
    }


    /**
     * Returns the dot product of the input vectors.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return The dot product of the two vectors.
     * @throws IllegalArgumentException If the vectors are not of the
     * same dimensionality.
     */
    public double proximity(Vector v1, Vector v2) {
        return v1.dotProduct(v2);
    }

}
