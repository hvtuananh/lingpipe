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

import com.aliasi.util.Proximity;

/**
 * A <code>KernelFunction</code> computes real-valued proximities
 * between vectors.  Note that proximity runs in the reverse direction
 * from distance: the more similar two vectors are,
 * the greater their proximity.
 *
 * <p>Implementations of the standard kernel functions used for
 * machine learning are provided in this package, including {@link
 * DotProductKernel}, {@link PolynomialKernel}, {@link
 * GaussianRadialBasisKernel}, and {@link HyperbolicTangentKernel}.
 * See those classes' documentation for definitions of the specific
 * kernel functions.
 *
 * <p>Typically kernel functions will be functions that could,
 * in theory, be represented by inner products of vectors
 * <code>f(v)</code>, where <code>f</code> maps an n-dimensional
 * input vector to an m-dimensional or even infinite-dimensional
 * vector <code>f(v)</code>.  The kernel function is then
 * defined as <code>kernel(v1,v2) = f(v1) * f(v2)</code>, where
 * <code>f(v)</code> is the embedding function and <code>*</code>
 * represents the dot-product.
 *
 * <p>The use of kernel functions is usually for the so-called
 * &quot;kernel trick&quot;, which allows classification or clustering
 * in high-dimensional spaces by embedding a lower-dimensional space
 * and then working with linear combinations of kernel function
 * results.
 *
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Kernel_trick">Wikipedia: Kernel Trick</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.1
 * @since   LingPipe3.1
 */
public interface KernelFunction extends Proximity<Vector> {

    /**
     * Return the result of applying the kernel function to the
     * specified pair of vectors.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return Kernel function applied to the vectors.
     */
    public double proximity(Vector v1, Vector v2);

}