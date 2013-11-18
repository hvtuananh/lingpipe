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
package com.aliasi.stats;

/**
 * A <code>Model</code> represents a generic interface for
 * classes that estimate probabilities of objects.
 *
 * @author  Bob Carpenter
 * @version 3.5.1
 * @since   LingPipe3.5.1
 * @param <E> the type of objects being modeled
 */
public interface Model<E> {

    /**
     * Returns the probability of the specified object.
     *
     * @param e The object whose probability is returned.
     * @return The probability of the specified object.
     */
    public double prob(E e);

    /**
     * Returns the log (base 2) of the probability of
     * the specified object.
     *
     * @param e The object whose probability is returned.
     * @return The log (base 2) probability of the specified object.
     */
    public double log2Prob(E e);

}