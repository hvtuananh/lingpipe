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
 * A <code>PruneTrieReader</code> filters a contained trie
 * reader by removing all subtrees whose counts fall below
 * a specified minimum.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.3
 */
public class PruneTrieReader extends DeletingTrieReader {

    private final long mMinCount;

    /**
     * Construct a scaling trie reader wrapping the specified
     * reader which deletes all subtrees with counts below the
     * specified minimum.
     *
     * @param reader Contained reader.
     * @param minCount Minimum count to retain a subtree.
     * @throws IllegalArgumentException If the minimum count is negative.
     */
    public PruneTrieReader(TrieReader reader, long minCount) 
    throws IOException {

    super(reader);
    if (minCount < 0) {
        String msg = "Minimum count must be >= 0."
        + " Found minCount=" + minCount;
        throw new IllegalArgumentException(msg);
    }
    mMinCount = minCount;
    }

    @Override
    boolean bufferCount() throws IOException {
    mNextCount = nextCount();
    return mNextCount >= mMinCount;
    }


}
