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
 * The <code>TrieReader</code> interface provides a means to read a
 * trie structure with counts.  
 *
 * <p>See {@link TrieWriter} for a description of how a trie is
 * encoded as a sequence of <code>long</code> values.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.3
 */
public interface TrieReader {


    /**
     * Returns the identifier of the symbol leading from the root of
     * the current tree to the daughter subtree for the symbol, or
     * <code>-1</code> if there are no more subtrees for the current
     * node.
     *
     * @return The symbol leading to the next subtree.
     * @throws IOException If there is an underlying I/O error.
     * @throws IllegalStateException If the next item to be read is
     * not a symbol.
     */
    public long readSymbol() throws IOException;

    /**
     * Returns the count of the next tree. 
     *
     * @return The count of the next tree.
     * @throws IOException If there is an underlying I/O error.
     * @throws IllegalStateException If the next item to be read is
     * not a count.
     */
    public long readCount() throws IOException;

}