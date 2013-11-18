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

package com.aliasi.lm;

import java.io.IOException;

/**
 * A <code>ScaleTrieReader</code> filters a contained trie reader by
 * scaling all counts by a given multiple, removing all subtrees with
 * zero root counts.  Counts are rounded after multiplication using
 * {@link java.lang.Math#round(double)}.  Thus pruning will only occur
 * if the scaling factor is less than <code>0.5</code>, because
 * <code>0.5</code> rounds to <code>1</code>.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.3
 */
public class ScaleTrieReader extends DeletingTrieReader {

    private final double mScale;

    /**
     * Construct a scaling trie reader wrapping the specified
     * reader and scaling counts with the specified value.
     *
     * @param reader Contained reader.
     * @param scale Scaling factor.
     * @throws IllegalArgumentException If the scale is not a
     * positive, non-infinite value.
     */
    public ScaleTrieReader(TrieReader reader, double scale) 
    throws IOException {
    
    super(reader);
    if (scale <= 0.0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
        String msg = "Scale must be positive and non-infinite."
        + " Found scale=" + scale;
        throw new IllegalArgumentException(msg);
    }
    mScale = scale;
    }


    @Override
    boolean bufferCount() throws IOException {
    mNextCount = Math.round(mScale * (double)nextCount());
    return mNextCount > 0L;
    }


}
